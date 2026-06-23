import 'dart:async';
import 'package:supabase_flutter/supabase_flutter.dart';

/// Service responsible for managing Supabase Realtime Postgres Changes subscriptions.
/// Subscribes to notifications, appointments, queues, and doctor_holidays.
/// Automatically handles stream exposure, reconnection assistance, and proper resource disposal.
class RealtimeService {
  final SupabaseClient _client = Supabase.instance.client;

  RealtimeChannel? _notificationsChannel;
  RealtimeChannel? _appointmentsChannel;
  RealtimeChannel? _queuesChannel;
  RealtimeChannel? _holidaysChannel;

  // StreamControllers to broadcast specific table changes realtime
  final StreamController<PostgresChangeEvent> _notificationController =
      StreamController<PostgresChangeEvent>.broadcast();
  final StreamController<PostgresChangeEvent> _appointmentController =
      StreamController<PostgresChangeEvent>.broadcast();
  final StreamController<PostgresChangeEvent> _queueController =
      StreamController<PostgresChangeEvent>.broadcast();
  final StreamController<PostgresChangeEvent> _holidayController =
      StreamController<PostgresChangeEvent>.broadcast();

  // Public Streams
  Stream<PostgresChangeEvent> get notificationsStream => _notificationController.stream;
  Stream<PostgresChangeEvent> get appointmentsStream => _appointmentController.stream;
  Stream<PostgresChangeEvent> get queuesStream => _queueController.stream;
  Stream<PostgresChangeEvent> get doctorHolidaysStream => _holidayController.stream;

  /// Subscribe to all core tables in parallel
  void subscribeAll({String? userId, String? pharmacyId}) {
    subscribeNotifications(userId: userId);
    subscribeAppointments(pharmacyId: pharmacyId);
    subscribeQueues(pharmacyId: pharmacyId);
    subscribeDoctorHolidays(pharmacyId: pharmacyId);
  }

  /// Subscribe to realtime notification changes
  void subscribeNotifications({String? userId}) {
    unsubscribeNotifications();

    try {
      final channelName = 'realtime:public:notifications${userId != null ? ':$userId' : ''}';
      _notificationsChannel = _client.channel(channelName);

      _notificationsChannel!.onPostgresChanges(
        event: PostgresChangeEvent.all,
        schema: 'public',
        table: 'notifications',
        callback: (PostgresChangeEvent payload) {
          // If a specific userId is specified, filter client-side or use policies
          if (userId != null) {
            final record = payload.newRecord;
            if (record['user_id'] != null && record['user_id'] != userId) {
              return; // Ignore other user's notification
            }
          }
          _notificationController.add(payload);
        },
      ).subscribe((status, [error]) {
        print('Notifications Realtime Status: $status, error: $error');
        if (status == RealtimeSubscribeStatus.closed || status == RealtimeSubscribeStatus.timedOut) {
          // Handle reconnection fallback
          Future.delayed(const Duration(seconds: 5), () {
            if (_notificationsChannel != null) subscribeNotifications(userId: userId);
          });
        }
      });
    } catch (e) {
      print('Error subscribing to notifications realtime: $e');
    }
  }

  /// Subscribe to realtime appointments changes
  void subscribeAppointments({String? pharmacyId}) {
    unsubscribeAppointments();

    try {
      final channelName = 'realtime:public:appointments${pharmacyId != null ? ':$pharmacyId' : ''}';
      _appointmentsChannel = _client.channel(channelName);

      _appointmentsChannel!.onPostgresChanges(
        event: PostgresChangeEvent.all,
        schema: 'public',
        table: 'appointments',
        callback: (PostgresChangeEvent payload) {
          _appointmentController.add(payload);
        },
      ).subscribe((status, [error]) {
        print('Appointments Realtime Status: $status, error: $error');
        if (status == RealtimeSubscribeStatus.closed || status == RealtimeSubscribeStatus.timedOut) {
          Future.delayed(const Duration(seconds: 5), () {
            if (_appointmentsChannel != null) subscribeAppointments(pharmacyId: pharmacyId);
          });
        }
      });
    } catch (e) {
      print('Error subscribing to appointments realtime: $e');
    }
  }

  /// Subscribe to realtime queue changes
  void subscribeQueues({String? pharmacyId}) {
    unsubscribeQueues();

    try {
      final channelName = 'realtime:public:queues${pharmacyId != null ? ':$pharmacyId' : ''}';
      _queuesChannel = _client.channel(channelName);

      _queuesChannel!.onPostgresChanges(
        event: PostgresChangeEvent.all,
        schema: 'public',
        table: 'queues',
        callback: (PostgresChangeEvent payload) {
          _queueController.add(payload);
        },
      ).subscribe((status, [error]) {
        print('Queues Realtime Status: $status, error: $error');
        if (status == RealtimeSubscribeStatus.closed || status == RealtimeSubscribeStatus.timedOut) {
          Future.delayed(const Duration(seconds: 5), () {
            if (_queuesChannel != null) subscribeQueues(pharmacyId: pharmacyId);
          });
        }
      });
    } catch (e) {
      print('Error subscribing to queues realtime: $e');
    }
  }

  /// Subscribe to realtime doctor holiday changes
  void subscribeDoctorHolidays({String? pharmacyId}) {
    unsubscribeDoctorHolidays();

    try {
      final channelName = 'realtime:public:doctor_holidays${pharmacyId != null ? ':$pharmacyId' : ''}';
      _holidaysChannel = _client.channel(channelName);

      _holidaysChannel!.onPostgresChanges(
        event: PostgresChangeEvent.all,
        schema: 'public',
        table: 'doctor_holidays',
        callback: (PostgresChangeEvent payload) {
          _holidayController.add(payload);
        },
      ).subscribe((status, [error]) {
        print('Doctor Holidays Realtime Status: $status, error: $error');
        if (status == RealtimeSubscribeStatus.closed || status == RealtimeSubscribeStatus.timedOut) {
          Future.delayed(const Duration(seconds: 5), () {
            if (_holidaysChannel != null) subscribeDoctorHolidays(pharmacyId: pharmacyId);
          });
        }
      });
    } catch (e) {
      print('Error subscribing to doctor holidays realtime: $e');
    }
  }

  /// Unsubscribe & clean up channels individually
  void unsubscribeNotifications() {
    if (_notificationsChannel != null) {
      _client.removeChannel(_notificationsChannel!);
      _notificationsChannel = null;
    }
  }

  void unsubscribeAppointments() {
    if (_appointmentsChannel != null) {
      _client.removeChannel(_appointmentsChannel!);
      _appointmentsChannel = null;
    }
  }

  void unsubscribeQueues() {
    if (_queuesChannel != null) {
      _client.removeChannel(_queuesChannel!);
      _queuesChannel = null;
    }
  }

  void unsubscribeDoctorHolidays() {
    if (_holidaysChannel != null) {
      _client.removeChannel(_holidaysChannel!);
      _holidaysChannel = null;
    }
  }

  /// Unsubscribe from all channels
  void unsubscribeAll() {
    unsubscribeNotifications();
    unsubscribeAppointments();
    unsubscribeQueues();
    unsubscribeDoctorHolidays();
  }

  /// Fully dispose and close Stream Controllers
  void dispose() {
    unsubscribeAll();
    _notificationController.close();
    _appointmentController.close();
    _queueController.close();
    _holidayController.close();
  }
}
