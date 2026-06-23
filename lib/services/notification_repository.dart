import 'package:supabase_flutter/supabase_flutter.dart';

/// Model class representing individual live notification item
class NotificationModel {
  final String id;
  final String? userId; // Null means targeting Master Admin
  final String title;
  final String message;
  final bool isRead;
  final bool isDeleted;
  final DateTime createdAt;

  NotificationModel({
    required this.id,
    this.userId,
    required this.title,
    required this.message,
    required this.isRead,
    required this.isDeleted,
    required this.createdAt,
  });

  factory NotificationModel.fromJson(Map<String, dynamic> json) {
    return NotificationModel(
      id: json['id'] as String,
      userId: json['user_id'] as String?,
      title: json['title'] as String? ?? '',
      message: json['message'] as String? ?? '',
      isRead: json['is_read'] as bool? ?? false,
      isDeleted: json['is_deleted'] as bool? ?? false,
      createdAt: DateTime.parse(json['created_at'] as String? ?? DateTime.now().toIso8601String()),
    );
  }
}

/// Repository responsible for executing CRUD operations on Notifications.
class NotificationRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetch active notifications for a specific user (or Master Admin if null)
  Future<List<NotificationModel>> fetchNotifications(String? userId) async {
    try {
      final query = _client
          .from('notifications')
          .select()
          .eq('is_deleted', false)
          .order('created_at', ascending: false);

      final List<dynamic> response = (userId != null)
          ? await query.or('user_id.eq.$userId,user_id.is.null')
          : await query.isFilter('user_id', null);

      return response.map((json) => NotificationModel.fromJson(json)).toList();
    } catch (e) {
      print('Error fetching notifications: $e');
      rethrow;
    }
  }

  /// Create a new notification
  Future<NotificationModel> createNotification({
    String? userId,
    required String title,
    required String message,
  }) async {
    try {
      final response = await _client.from('notifications').insert({
        'user_id': userId,
        'title': title,
        'message': message,
        'is_read': false,
        'is_deleted': false,
        'created_at': DateTime.now().toIso8601String(),
      }).select().single();

      return NotificationModel.fromJson(response);
    } catch (e) {
      print('Error creating notification: $e');
      rethrow;
    }
  }

  /// Mark specific notification as read
  Future<void> markAsRead(String notificationId) async {
    try {
      await _client
          .from('notifications')
          .update({'is_read': true})
          .eq('id', notificationId);
    } catch (e) {
      print('Error marking notification as read: $e');
      rethrow;
    }
  }

  /// Soft delete notification
  Future<void> deleteNotification(String notificationId) async {
    try {
      await _client
          .from('notifications')
          .update({'is_deleted': true})
          .eq('id', notificationId);
    } catch (e) {
      print('Error deleting notification: $e');
      rethrow;
    }
  }
}
