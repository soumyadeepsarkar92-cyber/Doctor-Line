import 'package:supabase_flutter/supabase_flutter.dart';

/// Models representing individual appointment entities
class AppointmentModel {
  final String id;
  final String patientId;
  final String doctorId;
  final String date;
  final String slot;
  final int queueNumber;
  final String status; // 'pending', 'called', 'completed', 'skipped', 'cancelled'
  final String? cancellationReason;
  final DateTime createdAt;
  final String? doctorName;
  final String? pharmacyName;

  AppointmentModel({
    required this.id,
    required this.patientId,
    required this.doctorId,
    required this.date,
    required this.slot,
    required this.queueNumber,
    required this.status,
    this.cancellationReason,
    required this.createdAt,
    this.doctorName,
    this.pharmacyName,
  });

  factory AppointmentModel.fromJson(Map<String, dynamic> json) {
    return AppointmentModel(
      id: json['id'] as String,
      patientId: json['patient_id'] as String,
      doctorId: json['doctor_id'] as String,
      date: json['date'] as String,
      slot: json['slot'] as String,
      queueNumber: (json['queue_number'] as num? ?? 0).toInt(),
      status: json['status'] as String? ?? 'pending',
      cancellationReason: json['cancellation_reason'] as String?,
      createdAt: DateTime.parse(json['created_at'] as String),
      doctorName: json['doctors']?['name'] as String?,
      pharmacyName: json['doctors']?['pharmacies']?['name'] as String?,
    );
  }
}

/// Repository responsible for executing CRUD and query operations for Appointments.
class AppointmentRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetch appointments for a specific pharmacy (through its doctors)
  Future<List<AppointmentModel>> fetchAppointmentsForPharmacy(String pharmacyId) async {
    try {
      final List<dynamic> response = await _client
          .from('appointments')
          .select('*, doctors!inner(*, pharmacies!inner(*))')
          .eq('doctors.pharmacy_id', pharmacyId)
          .order('date', ascending: true)
          .order('queue_number', ascending: true);

      return response.map((json) => AppointmentModel.fromJson(json)).toList();
    } catch (e) {
      print('Error fetching pharmacy appointments: $e');
      rethrow;
    }
  }

  /// Fetch appointments for a specific patient
  Future<List<AppointmentModel>> fetchAppointmentsForPatient(String patientId) async {
    try {
      final List<dynamic> response = await _client
          .from('appointments')
          .select('*, doctors(*, pharmacies(*))')
          .eq('patient_id', patientId)
          .order('date', ascending: false);

      return response.map((json) => AppointmentModel.fromJson(json)).toList();
    } catch (e) {
      print('Error fetching patient appointments: $e');
      rethrow;
    }
  }

  /// Book a new appointment
  Future<AppointmentModel> bookAppointment({
    required String patientId,
    required String doctorId,
    required String date,
    required String slot,
  }) async {
    try {
      // 1. Compute the queue number automatically
      final countResponse = await _client
          .from('appointments')
          .select('id')
          .eq('doctor_id', doctorId)
          .eq('date', date);
      
      final int newQueueNumber = countResponse.length + 1;

      // 2. Insert the appointment record
      final response = await _client.from('appointments').insert({
        'patient_id': patientId,
        'doctor_id': doctorId,
        'date': date,
        'slot': slot,
        'queue_number': newQueueNumber,
        'status': 'pending',
        'created_at': DateTime.now().toIso8601String(),
      }).select('*, doctors(*, pharmacies(*))').single();

      final newAppointment = AppointmentModel.fromJson(response);

      // 3. Create live audit log
      await _client.from('audit_logs').insert({
        'user_id': patientId,
        'role': 'patient',
        'action': 'Booked appointment with Doctor ID: $doctorId, Queue No: $newQueueNumber',
        'table_name': 'appointments',
        'record_id': newAppointment.id,
        'created_at': DateTime.now().toIso8601String(),
      });

      return newAppointment;
    } catch (e) {
      print('Error booking appointment: $e');
      rethrow;
    }
  }

  /// Update individual appointment status
  Future<void> updateAppointmentStatus(
    String appointmentId,
    String status, {
    String? cancellationReason,
    String? userId,
    String? role,
  }) async {
    try {
      final Map<String, dynamic> updateData = {
        'status': status,
        'updated_at': DateTime.now().toIso8601String(),
      };
      if (cancellationReason != null) {
        updateData['cancellation_reason'] = cancellationReason;
      }

      await _client
          .from('appointments')
          .update(updateData)
          .eq('id', appointmentId);

      // Logs audit event
      await _client.from('audit_logs').insert({
        'user_id': userId,
        'role': role ?? 'system',
        'action': 'Updated appointment status to: $status. Reason: ${cancellationReason ?? "None"}',
        'table_name': 'appointments',
        'record_id': appointmentId,
        'created_at': DateTime.now().toIso8601String(),
      });
    } catch (e) {
      print('Error updating appointment status: $e');
      rethrow;
    }
  }

  /// Calculates the estimated wait time (in minutes) for a patient appointment
  /// Based on average 15 mins per patient in the queue ahead of them
  Future<int> getEstimatedWaitTime(String appointmentId, String doctorId, String date, int currentQueueNum) async {
    try {
      final activeAhead = await _client
          .from('appointments')
          .select('id')
          .eq('doctor_id', doctorId)
          .eq('date', date)
          .eq('status', 'pending')
          .lt('queue_number', currentQueueNum);

      return activeAhead.length * 15; // 15 mins estimated wait time per pending patient
    } catch (e) {
      print('Error calculating wait time: $e');
      return 0;
    }
  }
}
