import 'package:supabase_flutter/supabase_flutter.dart';

/// Models representing administrative statistics/analytics
class MasterAdminDashboardMetrics {
  final int totalPharmacies;
  final int activePharmacies;
  final int suspendedPharmacies;
  final int totalDoctors;
  final int totalPatients;
  final int todayAppointments;
  final double monthlyRevenue;
  final int expiringSubscriptions;

  MasterAdminDashboardMetrics({
    required this.totalPharmacies,
    required this.activePharmacies,
    required this.suspendedPharmacies,
    required this.totalDoctors,
    required this.totalPatients,
    required this.todayAppointments,
    required this.monthlyRevenue,
    required this.expiringSubscriptions,
  });
}

/// Service class responsible for Master Admin actions, metrics compilation, and license management.
class MasterAdminService {
  final SupabaseClient _client = Supabase.instance.client;

  /// Compiles all KPI figures for the Admin Dashboard
  Future<MasterAdminDashboardMetrics> getDashboardMetrics() async {
    try {
      // 1. Pharmacy status metrics
      final List<dynamic> pharRes = await _client
          .from('pharmacies')
          .select('status')
          .eq('is_deleted', false);
      
      final int total = pharRes.length;
      final int active = pharRes.where((p) => p['status'] == 'active').length;
      final int suspended = pharRes.where((p) => p['status'] == 'suspended').length;

      // 2. Total Doctors count
      final docRes = await _client.from('doctors').select('id');
      final int totalDocs = docRes.length;

      // 3. Total Patients count
      final patientRes = await _client
          .from('profiles')
          .select('id')
          .eq('role', 'patient');
      final int totalPatients = patientRes.length;

      // 4. Today's Appointments
      final String todayStr = DateTime.now().toIso8601String().split('T')[0];
      final apptRes = await _client
          .from('appointments')
          .select('id')
          .eq('date', todayStr);
      final int todayAppts = apptRes.length;

      // 5. Monthly Revenue computation
      final revRes = await _client
          .from('pharmacies')
          .select('subscription_amount')
          .eq('subscription_payment_status', 'Paid');
      double revenue = 0.0;
      for (final r in revRes) {
        revenue += (r['subscription_amount'] as num? ?? 0.0).toDouble();
      }

      // 6. Expiring Subscriptions (expires within next 7 days)
      final DateTime sevenDaysHence = DateTime.now().add(const Duration(days: 7));
      final String sevenDaysHenceStr = sevenDaysHence.toIso8601String().split('T')[0];
      
      final expiringRes = await _client
          .from('pharmacies')
          .select('id')
          .eq('status', 'active')
          .eq('is_deleted', false)
          .lte('subscription_expiry', sevenDaysHenceStr);
      final int expiring = expiringRes.length;

      return MasterAdminDashboardMetrics(
        totalPharmacies: total,
        activePharmacies: active,
        suspendedPharmacies: suspended,
        totalDoctors: totalDocs,
        totalPatients: totalPatients,
        todayAppointments: todayAppts,
        monthlyRevenue: revenue,
        expiringSubscriptions: expiring,
      );
    } catch (e) {
      print('Error fetching dashboard analytics: $e');
      // Return safe empty figures on error
      return MasterAdminDashboardMetrics(
        totalPharmacies: 0,
        activePharmacies: 0,
        suspendedPharmacies: 0,
        totalDoctors: 0,
        totalPatients: 0,
        todayAppointments: 0,
        monthlyRevenue: 0.0,
        expiringSubscriptions: 0,
      );
    }
  }

  /// Create a new SaaS Pharmacy Partner
  Future<void> createPharmacy({
    required String name,
    required String ownerName,
    required String email,
    required String phone,
    required String address,
    required String plan,
    required double amount,
    required String expiryDate,
  }) async {
    try {
      final nowStr = DateTime.now().toIso8601String().split('T')[0];

      // 1. Insert SaaS account row
      final pharRes = await _client.from('pharmacies').insert({
        'name': name,
        'owner_name': ownerName,
        'email': email,
        'phone': phone,
        'address': address,
        'license': 'LIC-${DateTime.now().millisecondsSinceEpoch}',
        'subscription_plan': plan,
        'subscription_start': nowStr,
        'subscription_expiry': expiryDate,
        'subscription_amount': amount,
        'subscription_payment_status': 'Paid',
        'status': 'active',
        'is_deleted': false,
      }).select().single();

      final String pharmacyId = pharRes['id'] as String;

      // 2. Dispatch a notification about the creation
      await _client.from('notifications').insert({
        'title': 'New Pharmacy Registered',
        'message': 'Pharmacy "$name" successfully created with plan: $plan.',
        'is_read': false,
      });

      // 3. Log Audit
      await _client.from('audit_logs').insert({
        'role': 'master_admin',
        'action': 'Registered new Pharmacy account: $name ($email)',
        'table_name': 'pharmacies',
        'record_id': pharmacyId,
      });
    } catch (e) {
      print('Error administrative creating pharmacy: $e');
      rethrow;
    }
  }

  /// Edit/Modify SaaS Pharmacy credentials or contact records
  Future<void> editPharmacy({
    required String id,
    required String name,
    required String ownerName,
    required String phone,
    required String address,
  }) async {
    try {
      await _client.from('pharmacies').update({
        'name': name,
        'owner_name': ownerName,
        'phone': phone,
        'address': address,
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id', id);

      // Audit Log
      await _client.from('audit_logs').insert({
        'role': 'master_admin',
        'action': 'Modified details for Pharmacy ID: $id',
        'table_name': 'pharmacies',
        'record_id': id,
      });
    } catch (e) {
      print('Error administrative updating pharmacy: $e');
      rethrow;
    }
  }

  /// Update subscription validity / license keys
  Future<void> extendSubscription({
    required String pharmacyId,
    required String nextPlan,
    required double nextAmount,
    required String nextExpiryDate,
  }) async {
    try {
      await _client.from('pharmacies').update({
        'subscription_plan': nextPlan,
        'subscription_amount': nextAmount,
        'subscription_expiry': nextExpiryDate,
        'subscription_payment_status': 'Paid',
        'status': 'active', // auto reactive on renewal
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id', pharmacyId);

      // Re-activate profiles
      await _client.from('profiles').update({
        'status': 'active',
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('pharmacy_id', pharmacyId);

      // Audit Log
      await _client.from('audit_logs').insert({
        'role': 'master_admin',
        'action': 'Extended subscription for pharmacy $pharmacyId. Plan: $nextPlan, Expiry: $nextExpiryDate',
        'table_name': 'pharmacies',
        'record_id': pharmacyId,
      });
    } catch (e) {
      print('Error extending subscription: $e');
      rethrow;
    }
  }

  /// Suspend Pharmacy login
  Future<void> suspendPharmacy(String id) async {
    try {
      await _client.from('pharmacies').update({
        'status': 'suspended',
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id', id);

      await _client.from('profiles').update({
        'status': 'suspended',
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('pharmacy_id', id);

      // Audit Log
      await _client.from('audit_logs').insert({
        'role': 'master_admin',
        'action': 'Suspended Pharmacy ID: $id',
        'table_name': 'pharmacies',
        'record_id': id,
      });
    } catch (e) {
      print('Error suspending pharmacy: $e');
      rethrow;
    }
  }

  /// Activate/Unsuspend Pharmacy login
  Future<void> activatePharmacy(String id) async {
    try {
      await _client.from('pharmacies').update({
        'status': 'active',
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id', id);

      await _client.from('profiles').update({
        'status': 'active',
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('pharmacy_id', id);

      // Audit Log
      await _client.from('audit_logs').insert({
        'role': 'master_admin',
        'action': 'Activated Pharmacy ID: $id',
        'table_name': 'pharmacies',
        'record_id': id,
      });
    } catch (e) {
      print('Error activating pharmacy: $e');
      rethrow;
    }
  }

  /// Soft Delete Pharmacy from schedules
  Future<void> softDeletePharmacy(String id) async {
    try {
      final now = DateTime.now().toIso8601String();
      await _client.from('pharmacies').update({
        'is_deleted': true,
        'deleted_at': now,
        'updated_at': now,
      }).eq('id', id);

      await _client.from('profiles').update({
        'is_deleted': true,
        'deleted_at': now,
        'updated_at': now,
      }).eq('pharmacy_id', id);

      // Audit Log
      await _client.from('audit_logs').insert({
        'role': 'master_admin',
        'action': 'Soft-deleted Pharmacy ID: $id',
        'table_name': 'pharmacies',
        'record_id': id,
      });
    } catch (e) {
      print('Error soft deleting pharmacy: $e');
      rethrow;
    }
  }

  /// Restore soft deleted pharmacy
  Future<void> restorePharmacy(String id) async {
    try {
      await _client.from('pharmacies').update({
        'is_deleted': false,
        'deleted_at': null,
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id', id);

      await _client.from('profiles').update({
        'is_deleted': false,
        'deleted_at': null,
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('pharmacy_id', id);

      // Audit Log
      await _client.from('audit_logs').insert({
        'role': 'master_admin',
        'action': 'Restored Pharmacy ID: $id',
        'table_name': 'pharmacies',
        'record_id': id,
      });
    } catch (e) {
      print('Error restoring pharmacy: $e');
      rethrow;
    }
  }
}
