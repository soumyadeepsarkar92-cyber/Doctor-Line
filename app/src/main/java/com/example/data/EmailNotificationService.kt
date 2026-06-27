package com.example.data

import android.util.Log

object EmailNotificationService {
    fun sendRegistrationStatusUpdate(toEmail: String, pharmacyName: String, status: String, additionalNotes: String? = null) {
        val subject = "Pharmacy Registration Status Update"
        val bodyBuilder = java.lang.StringBuilder()
        bodyBuilder.append("Dear Pharmacy Owner,\n\n")
        bodyBuilder.append("Your registration request for '$pharmacyName' has been updated.\n\n")
        bodyBuilder.append("New Status: ${status.uppercase()}\n\n")
        
        if (!additionalNotes.isNullOrBlank()) {
            bodyBuilder.append("Admin Notes: $additionalNotes\n\n")
        }
        
        bodyBuilder.append("Thank you,\nDoctorLine Admin Team")

        // In a real application, this would integrate with a backend email provider (e.g. SendGrid, AWS SES)
        Log.i("EmailNotificationService", "Sending Email to: $toEmail\nSubject: $subject\nBody:\n$bodyBuilder")
    }
}
