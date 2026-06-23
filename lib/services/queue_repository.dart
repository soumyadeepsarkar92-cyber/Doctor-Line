import 'package:supabase_flutter/supabase_flutter.dart';

/// Models representing active queue states for a specific doctor on a specific date
class QueueModel {
  final String id;
  final String doctorId;
  final String date;
  final int currentCallingNumber;
  final String? currentCallingAppointmentId;
  final String status; // 'active', 'paused', 'completed'
  final DateTime updatedAt;

  QueueModel({
    required this.id,
    required this.doctorId,
    required this.date,
    required this.currentCallingNumber,
    this.currentCallingAppointmentId,
    required this.status,
    required this.updatedAt,
  });

  factory QueueModel.fromJson(Map<String, dynamic> json) {
    return QueueModel(
      id: json['id'] as String,
      doctorId: json['doctor_id'] as String,
      date: json['date'] as String,
      currentCallingNumber: (json['current_calling_number'] as num? ?? 0).toInt(),
      currentCallingAppointmentId: json['current_calling_appointment_id'] as String?,
      status: json['status'] as String? ?? 'active',
      updatedAt: DateTime.parse(json['updated_at'] as String),
    );
  }
}

/// Repository responsible for executing queue lifecycle and state operations
class QueueRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetch or initialize the queue state for a doctor on a specific date
  Future<QueueModel> fetchQueueState(String doctorId, String date) async {
    try {
      final response = await _client
          .from('queues')
          .select()
          .eq('doctor_id', doctorId)
          .eq('date', date)
          .maybeSingle();

      if (response != null) {
        return QueueModel.fromJson(response);
      }

      // Initialize a new queue state in the ledger if it doesn't exist
      final newQueue = await _client.from('queues').insert({
        'doctor_id': doctorId,
        'date': date,
        'current_calling_number': 0,
        'current_calling_appointment_id': null,
        'status': 'active',
        'updated_at': DateTime.now().toIso8601String(),
      }).select().single();

      return QueueModel.fromJson(newQueue);
    } catch (e) {
      print('Error fetching/creating queue state: $e');
      rethrow;
    }
  }

  /// Pharmacy calls the next patient in line
  Future<QueueModel?> callNextPatient({
    required String doctorId,
    required String date,
    required String pharmacyId,
  }) async {
    try {
      // 1. Fetch current queue state
      final queue = await fetchQueueState(doctorId, date);

      // 2. Query the next pending appointment sorted by queue_number
      final List<dynamic> nextAppointmentRes = await _client
          .from('appointments')
          .select()
          .eq('doctor_id', doctorId)
          .eq('date', date)
          .eq('status', 'pending')
          .order('queue_number', ascending: true)
          .limit(1);

      if (nextAppointmentRes.isEmpty) {
        print('No more pending patients in queue.');
        return null;
      }

      final nextAppointment = nextAppointmentRes.first;
      final String appointmentId = nextAppointment['id'] as String;
      final int queueNumber = (nextAppointment['queue_number'] as num).toInt();

      // 3. Update the appointment status to 'called'
      await _client
          .from('appointments')
          .update({'status': 'called', 'updated_at': DateTime.now().toIso8601String()})
          .eq('id', appointmentId);

      // 4. Update the core queue state
      final updatedQueue = await _client
          .from('queues')
          .update({
            'current_calling_number': queueNumber,
            'current_calling_appointment_id': appointmentId,
            'updated_at': DateTime.now().toIso8601String(),
          })
          .eq('id', queue.id)
          .select()
          .single();

      // 5. Notify the patient
      final String patientId = nextAppointment['patient_id'] as String;
      await _client.from('notifications').insert({
        'user_id': patientId,
        'title': 'Your turn has arrived!',
        'message': 'Please proceed to the doctor room. Your queue number: $queueNumber',
        'is_read': false,
      });

      // 6. Audit Logging
      await _client.from('audit_logs').insert({
        'user_id': pharmacyId,
        'role': 'pharmacy',
        'action': 'Called next patient. Queue No: $queueNumber, Appointment ID: $appointmentId',
        'table_name': 'queues',
        'record_id': queue.id,
      });

      return QueueModel.fromJson(updatedQueue);
    } catch (e) {
      print('Error calling next patient: $e');
      rethrow;
    }
  }

  /// Pharmacy marks patient's appointment as completed
  Future<void> markPatientCompleted({
    required String appointmentId,
    required String doctorId,
    required String date,
    required String pharmacyId,
  }) async {
    try {
      await _client
          .from('appointments')
          .update({'status': 'completed', 'updated_at': DateTime.now().toIso8601String()})
          .eq('id', appointmentId);

      // Reset calling status in queues if it was the currently called patient
      final queue = await fetchQueueState(doctorId, date);
      if (queue.currentCallingAppointmentId == appointmentId) {
        await _client.from('queues').update({
          'current_calling_appointment_id': null,
          'updated_at': DateTime.now().toIso8601String(),
        }).eq('id', queue.id);
      }

      // Dispatch audit log
      await _client.from('audit_logs').insert({
        'user_id': pharmacyId,
        'role': 'pharmacy',
        'action': 'Marked patient completed for appointment ID: $appointmentId',
        'table_name': 'appointments',
        'record_id': appointmentId,
      });
    } catch (e) {
      print('Error marking patient completed: $e');
      rethrow;
    }
  }

  /// Pharmacy skips the current patient in queue
  Future<void> skipPatient({
    required String appointmentId,
    required String doctorId,
    required String date,
    required String pharmacyId,
  }) async {
    try {
      await _client
          .from('appointments')
          .update({'status': 'skipped', 'updated_at': DateTime.now().toIso8601String()})
          .eq('id', appointmentId);

      // Reset calling status in queues if it was the currently called patient
      final queue = await fetchQueueState(doctorId, date);
      if (queue.currentCallingAppointmentId == appointmentId) {
        await _client.from('queues').update({
          'current_calling_appointment_id': null,
          'updated_at': DateTime.now().toIso8601String(),
        }).eq('id', queue.id);
      }

      // Notify patient
      final List<dynamic> apptData = await _client
          .from('appointments')
          .select('patient_id, queue_number')
          .eq('id', appointmentId);
      
      if (apptData.isNotEmpty) {
        final String patientId = apptData.first['patient_id'] as String;
        final int qNum = (apptData.first['queue_number'] as num).toInt();
        await _client.from('notifications').insert({
          'user_id': patientId,
          'title': 'Queue Turn Skipped',
          'message': 'You were skipped. Please contact the reception desk to be re-queued.',
          'is_read': false,
        });
      }

      // Dispatch audit log
      await _client.from('audit_logs').insert({
        'user_id': pharmacyId,
        'role': 'pharmacy',
        'action': 'Skipped patient for appointment ID: $appointmentId',
        'table_name': 'appointments',
        'record_id': appointmentId,
      });
    } catch (e) {
      print('Error skipping patient: $e');
      rethrow;
    }
  }

  /// Get remaining patients count in pending status
  Future<int> getRemainingPatientsCount(String doctorId, String date) async {
    try {
      final List<dynamic> pending = await _client
          .from('appointments')
          .select('id')
          .eq('doctor_id', doctorId)
          .eq('date', date)
          .eq('status', 'pending');

      return pending.length;
    } catch (e) {
      print('Error getting remaining count: $e');
      return 0;
    }
  }
}
