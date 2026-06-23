import 'package:supabase_flutter/supabase_flutter.dart';

/// Models representing declared Doctor Holiday intervals
class DoctorHolidayModel {
  final String id;
  final String doctorId;
  final String date;
  final String reason;
  final DateTime createdAt;

  DoctorHolidayModel({
    required this.id,
    required this.doctorId,
    required this.date,
    required this.reason,
    required this.createdAt,
  });

  factory DoctorHolidayModel.fromJson(Map<String, dynamic> json) {
    return DoctorHolidayModel(
      id: json['id'] as String,
      doctorId: json['doctor_id'] as String,
      date: json['date'] as String,
      reason: json['reason'] as String? ?? 'Holiday declared',
      createdAt: DateTime.parse(json['created_at'] as String),
    );
  }
}

/// Service class containing business rules for declaring holidays and automatically cancelling bookings
class DoctorHolidayService {
  final SupabaseClient _client = Supabase.instance.client;

  /// Pharmacy declares holiday for a specific doctor.
  /// Automatically cascades cancellation across all active appointments on that date.
  Future<void> declareDoctorHoliday({
    required String doctorId,
    required String date,
    required String reason,
    required String pharmacyId,
  }) async {
    try {
      // 1. Double check / register holiday inside `doctor_holidays` table
      final holidayRes = await _client.from('doctor_holidays').insert({
        'doctor_id': doctorId,
        'date': date,
        'reason': reason,
        'created_at': DateTime.now().toIso8601String(),
      }).select().single();

      final holidayId = holidayRes['id'] as String;

      // 2. Query all appointments scheduled with this doctor on the selected date
      final List<dynamic> affectedAppointments = await _client
          .from('appointments')
          .select('id, patient_id, slot, doctors(name)')
          .eq('doctor_id', doctorId)
          .eq('date', date)
          .or('status.eq.pending,status.eq.called'); // only cancel active/pending statuses

      final String doctorName = affectedAppointments.isNotEmpty
          ? (affectedAppointments.first['doctors']?['name'] as String? ?? 'Doctor')
          : 'Doctor';

      if (affectedAppointments.isNotEmpty) {
        final List<String> apptIds = affectedAppointments.map((e) => e['id'] as String).toList();

        // 3. Update appointment records in database status to 'cancelled' with proper reason
        await _client
            .from('appointments')
            .update({
              'status': 'cancelled',
              'cancellation_reason': 'Doctor Holiday declared: $reason',
              'updated_at': DateTime.now().toIso8601String(),
            })
            .in('id', apptIds);

        // 4. Send real-time custom notification to every affected patient
        for (final appt in affectedAppointments) {
          final String patientId = appt['patient_id'] as String;
          final String slot = appt['slot'] as String;

          await _client.from('notifications').insert({
            'user_id': patientId,
            'title': 'Appointment Cancelled - Doctor Holiday',
            'message': 'Your appointment with $doctorName on $date ($slot) has been cancelled due to: $reason. Please reschedule.',
            'is_read': false,
            'created_at': DateTime.now().toIso8601String(),
          });
        }
      }

      // 5. Generate unified Audit Log entry
      await _client.from('audit_logs').insert({
        'user_id': pharmacyId,
        'role': 'pharmacy',
        'action': 'Declared Doctor Holiday on $date for Doctor $doctorName. Cancelled ${affectedAppointments.length} appointments.',
        'table_name': 'doctor_holidays',
        'record_id': holidayId,
        'created_at': DateTime.now().toIso8601String(),
      });

    } catch (e) {
      print('Exception declaring doctor holiday: $e');
      rethrow;
    }
  }

  /// Retrieve declared holidays for a pharmacy's doctors
  Future<List<DoctorHolidayModel>> fetchHolidaysForPharmacy(String pharmacyId) async {
    try {
      final List<dynamic> response = await _client
          .from('doctor_holidays')
          .select('*, doctors!inner(*)')
          .eq('doctors.pharmacy_id', pharmacyId)
          .order('date', ascending: false);

      return response.map((json) => DoctorHolidayModel.fromJson(json)).toList();
    } catch (e) {
      print('Error fetching holidays: $e');
      rethrow;
    }
  }
}
