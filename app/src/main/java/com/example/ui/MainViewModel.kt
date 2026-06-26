package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DoctorLineApplication
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application, private val repository: DoctorLineRepository) : AndroidViewModel(application) {

    // Persistent Theme Support
    private val sharedPrefs = application.getSharedPreferences("doctorline_theme_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _appTheme = MutableStateFlow(sharedPrefs.getString("app_theme", "System Default") ?: "System Default")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    fun updateTheme(themeStr: String) {
        _appTheme.value = themeStr
        sharedPrefs.edit().putString("app_theme", themeStr).apply()
        viewModelScope.launch {
            repository.logAction("Change Theme", "User changed theme preference to '$themeStr'")
        }
    }

    // Optional Location System States (Country, State, District, City)
    val selectedCountry = MutableStateFlow(sharedPrefs.getString("selected_country", "India") ?: "India")
    val selectedState = MutableStateFlow(sharedPrefs.getString("selected_state", "West Bengal") ?: "West Bengal")
    val selectedDistrict = MutableStateFlow(sharedPrefs.getString("selected_district", "Kolkata") ?: "Kolkata")
    val selectedCity = MutableStateFlow(sharedPrefs.getString("selected_city", "Kolkata") ?: "Kolkata")

    fun updateLocation(country: String, state: String, district: String, city: String) {
        selectedCountry.value = country
        selectedState.value = state
        selectedDistrict.value = district
        selectedCity.value = city
        sharedPrefs.edit().apply {
            putString("selected_country", country)
            putString("selected_state", state)
            putString("selected_district", district)
            putString("selected_city", city)
            apply()
        }
        viewModelScope.launch {
            repository.logAction("Update Location", "User updated location selection to: $city, $district, $state, $country")
        }
    }

    // Active logged in user
    val activeUser: StateFlow<UserEntity?> = repository.activeUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Lists
    val allPharmacies: StateFlow<List<PharmacyEntity>> = repository.allPharmacies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDoctors: StateFlow<List<DoctorEntity>> = repository.activeDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDoctors: StateFlow<List<DoctorEntity>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<ReviewEntity>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPharmacyRequests: StateFlow<List<PharmacyRequestEntity>> = repository.allPharmacyRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pricingSettings: StateFlow<PricingSettingsEntity?> = repository.pricingSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PricingSettingsEntity())

    // Selection States for patient booking flows
    private val _selectedDoctor = MutableStateFlow<DoctorEntity?>(null)
    val selectedDoctor: StateFlow<DoctorEntity?> = _selectedDoctor.asStateFlow()

    private val _selectedTimeSlot = MutableStateFlow<String?>(null)
    val selectedTimeSlot: StateFlow<String?> = _selectedTimeSlot.asStateFlow()

    private val _selectedDate = MutableStateFlow<String>(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    private val _lastCreatedBooking = MutableStateFlow<BookingEntity?>(null)
    val lastCreatedBooking: StateFlow<BookingEntity?> = _lastCreatedBooking.asStateFlow()

    private val _currentPharmacySubscription = MutableStateFlow<SubscriptionEntity?>(null)
    val currentPharmacySubscription: StateFlow<SubscriptionEntity?> = _currentPharmacySubscription.asStateFlow()

    private val _patientBookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    val patientBookings: StateFlow<List<BookingEntity>> = _patientBookings.asStateFlow()

    private val _patientFavourites = MutableStateFlow<List<FavouriteDoctorEntity>>(emptyList())
    val patientFavourites: StateFlow<List<FavouriteDoctorEntity>> = _patientFavourites.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.fetchAndSyncDoctors()
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error fetching doctors on init: ${e.message}")
            }
        }
        viewModelScope.launch {
            // Simulated daily subscription cron check (at launch / updates)
            try {
                if (com.example.data.SupabaseManager.isConfigured) {
                    repository.fetchAndSyncPharmacies()
                }
                repository.allPharmacies.collect { pharmaciesList ->
                    val notificationsList = repository.allNotifications.firstOrNull() ?: emptyList()
                    val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    
                    pharmaciesList.forEach { phar ->
                        if (!phar.trialStarted) return@forEach
                        
                        val daysRemaining = calculateDaysBetween(todayStr, phar.subscriptionExpiry)
                        
                        // 1. Notify 7 days before expiry
                        if (daysRemaining == 7L) {
                            val title = "Subscription Alert: 7 Days Remaining (${phar.name})"
                            if (notificationsList.none { it.title == title }) {
                                sendSubscriptionAlert(title, "Your subscription/trial for ${phar.name} will expire in 7 days on ${phar.subscriptionExpiry}.")
                            }
                        }
                        
                        // 2. Notify 3 days before expiry
                        if (daysRemaining == 3L) {
                            val title = "Subscription Alert: 3 Days Remaining (${phar.name})"
                            if (notificationsList.none { it.title == title }) {
                                sendSubscriptionAlert(title, "Your subscription/trial for ${phar.name} will expire in 3 days on ${phar.subscriptionExpiry}.")
                            }
                        }
                        
                        // 3. Notify 1 day before expiry
                        if (daysRemaining == 1L) {
                            val title = "Subscription Alert: 1 Day Remaining (${phar.name})"
                            if (notificationsList.none { it.title == title }) {
                                sendSubscriptionAlert(title, "Your subscription/trial for ${phar.name} will expire tomorrow on ${phar.subscriptionExpiry}.")
                            }
                        }
                        
                        // 4. On expiry (0 days remaining)
                        if (daysRemaining == 0L) {
                            val title = "Subscription Expired (${phar.name})"
                            if (notificationsList.none { it.title == title }) {
                                sendSubscriptionAlert(title, "Your subscription/trial for ${phar.name} has expired today (${phar.subscriptionExpiry}). 5-day Grace Period has started.")
                            }
                        }
                        
                        // 5. On grace period start (daysRemaining < 0 and within 5 days)
                        if (daysRemaining < 0 && daysRemaining > -5) {
                            val title = "Grace Period Active (${phar.name})"
                            val graceDaysLeft = 5 + daysRemaining
                            if (notificationsList.none { it.title == "$title - Day $graceDaysLeft" }) {
                                sendSubscriptionAlert("$title - Day $graceDaysLeft", "Your subscription/trial has expired. You are now in a 5-day grace period. $graceDaysLeft days remaining before account suspension.")
                            }
                        }
                        
                        // 6. On suspension (after 5 days)
                        if (daysRemaining <= -5L && phar.status != "Suspended") {
                            val title = "Account Suspended (${phar.name})"
                            if (notificationsList.none { it.title == title }) {
                                sendSubscriptionAlert(title, "Your grace period has ended. Your pharmacy account has been suspended.")
                            }
                            // Suspend pharmacy!
                            viewModelScope.launch {
                                val updated = phar.copy(status = "Suspended")
                                repository.editPharmacy(updated)
                                repository.logAction("Subscription Suspended", "SaaS subscription grace period expired for ${phar.name}. Account has been SUSPENDED.")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "SaaS Cron Check Error: ${e.message}")
            }
        }
        viewModelScope.launch {
            activeUser.collect { user ->
                if (user != null && user.role == "Pharmacy") {
                    try {
                        if (com.example.data.SupabaseManager.isConfigured) {
                            repository.fetchAndSyncPharmacies()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainViewModel", "Fetch pharmacies error on activeUser collect: ${e.message}")
                    }

                    val pharList = repository.allPharmacies.firstOrNull() ?: emptyList()
                    val phar = pharList.find { it.id == user.id } ?: pharList.firstOrNull()
                    if (phar != null) {
                        if (!phar.trialStarted) {
                            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            val calendar = java.util.Calendar.getInstance()
                            calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
                            val expiryDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                            
                            val updatedPharmacy = phar.copy(
                                trialStarted = true,
                                trialStartDate = today,
                                trialEndDate = expiryDate,
                                subscriptionStart = today,
                                subscriptionExpiry = expiryDate,
                                status = "Active"
                            )
                            repository.editPharmacy(updatedPharmacy)
                            repository.logAction("Trial Started", "Pharmacy ${phar.name} logged in for the first time. 30-day Free Trial started. Expiry: $expiryDate.")
                            
                            repository.addNotification(
                                NotificationEntity(
                                    title = "Trial Started (${phar.name})",
                                    message = "Your 30-day free trial has successfully started! It will expire on $expiryDate.",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }

                    repository.getSubscriptionForPharmacy(user.id).collect { sub ->
                        _currentPharmacySubscription.value = sub
                    }
                } else {
                    _currentPharmacySubscription.value = null
                }
            }
        }
        viewModelScope.launch {
            activeUser.collect { user ->
                if (user != null && user.role == "Patient") {
                    repository.getBookingsByPatientPhone(user.phone).collect { bookings ->
                        _patientBookings.value = bookings
                    }
                } else {
                    _patientBookings.value = emptyList()
                }
            }
        }
        viewModelScope.launch {
            activeUser.collect { user ->
                if (user != null && user.role == "Patient") {
                    launch {
                        try {
                            if (com.example.data.SupabaseManager.isConfigured) {
                                repository.fetchAndSyncFavourites(user.id)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainViewModel", "Initial favourites sync error: ${e.message}")
                        }
                    }
                    repository.getFavouritesForPatient(user.id).collect { favs ->
                        _patientFavourites.value = favs
                    }
                } else {
                    _patientFavourites.value = emptyList()
                }
            }
        }
        // Realtime Polling Synchronization Loop for Live updates
        viewModelScope.launch {
            while (kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]?.isActive == true) {
                kotlinx.coroutines.delay(4000)
                try {
                    if (com.example.data.SupabaseManager.isConfigured) {
                        repository.fetchAndSyncDoctors()
                        activeUser.value?.let { user ->
                            if (user.role == "Patient") {
                                repository.fetchAndSyncFavourites(user.id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainViewModel", "Realtime polling error: ${e.message}")
                }
            }
        }
    }

    fun toggleFavourite(doctorId: String) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.toggleFavouriteDoctor(user.id, doctorId)
        }
    }

    fun updateDoctorAvailability(doctorId: String, status: String, expectedStartTime: String, delayReason: String) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to update doctor availability but subscription/trial has expired.")
                return@launch
            }
            repository.updateDoctorAvailability(doctorId, status, expectedStartTime, delayReason)
        }
    }

    fun selectDoctor(doctor: DoctorEntity) {
        _selectedDoctor.value = doctor
        _selectedTimeSlot.value = null // reset slot
    }

    fun selectTimeSlot(slot: String) {
        _selectedTimeSlot.value = slot
    }

    // AUTH ACTIONS
    fun login(name: String, email: String, phone: String, role: String) {
        viewModelScope.launch {
            repository.loginUser(name, email, phone, role)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            _selectedDoctor.value = null
            _selectedTimeSlot.value = null
            _lastCreatedBooking.value = null
        }
    }

    // BOOKING ACTION
    fun confirmBooking(
        patientName: String,
        patientPhone: String,
        age: Int,
        gender: String,
        paymentMode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val doctor = _selectedDoctor.value ?: return
        val slot = _selectedTimeSlot.value ?: return

        viewModelScope.launch {
            val pharmaciesList = repository.allPharmacies.firstOrNull() ?: emptyList()
            val phar = pharmaciesList.find { it.id == doctor.pharmacyId }
            if (phar != null && phar.trialStarted) {
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                val daysRemaining = calculateDaysBetween(todayStr, phar.subscriptionExpiry)
                if (daysRemaining < 0) {
                    val errorMsg = if (daysRemaining <= -5) "This pharmacy is suspended due to subscription expiry." else "This pharmacy's trial/subscription has expired. Bookings are disabled."
                    repository.logAction("Booking Blocked", "Patient tried to book with ${doctor.name} under ${phar.name}, but subscription/trial has expired.")
                    onError(errorMsg)
                    return@launch
                }
            }

            val dateStr = _selectedDate.value
            val booking = repository.createBooking(
                patientName = patientName,
                patientPhone = patientPhone,
                age = age,
                gender = gender,
                doctorId = doctor.id,
                dateStr = dateStr,
                timeStr = slot,
                paymentMode = paymentMode
            )
            _lastCreatedBooking.value = booking
            onSuccess()
        }
    }

    // ADMIN PHARMACY CRUD ACTIONS
    fun addPharmacy(
        name: String, 
        address: String, 
        phone: String, 
        license: String, 
        ownerName: String, 
        status: String, 
        createdDate: String,
        subscriptionPlan: String = "Basic",
        subscriptionStart: String = "2026-06-22",
        subscriptionExpiry: String = "2026-07-22",
        subscriptionAmount: Double = 499.0,
        subscriptionPaymentStatus: String = "Paid"
    ) {
        viewModelScope.launch {
            val pharmacy = PharmacyEntity(
                name = name,
                address = address,
                phone = phone,
                license = license,
                bannerName = "standard_pharmacy_banner",
                ownerName = ownerName,
                status = status,
                createdDate = createdDate,
                subscriptionPlan = subscriptionPlan,
                subscriptionStart = subscriptionStart,
                subscriptionExpiry = subscriptionExpiry,
                subscriptionAmount = subscriptionAmount,
                subscriptionPaymentStatus = subscriptionPaymentStatus
            )
            repository.addPharmacy(pharmacy)
        }
    }

    fun editPharmacy(
        id: String, 
        name: String, 
        address: String, 
        phone: String, 
        license: String, 
        ownerName: String, 
        status: String, 
        createdDate: String,
        subscriptionPlan: String = "Basic",
        subscriptionStart: String = "2026-06-22",
        subscriptionExpiry: String = "2026-07-22",
        subscriptionAmount: Double = 499.0,
        subscriptionPaymentStatus: String = "Paid"
    ) {
        viewModelScope.launch {
            val pharmacy = PharmacyEntity(
                id = id,
                name = name,
                address = address,
                phone = phone,
                license = license,
                bannerName = "standard_pharmacy_banner",
                ownerName = ownerName,
                status = status,
                createdDate = createdDate,
                subscriptionPlan = subscriptionPlan,
                subscriptionStart = subscriptionStart,
                subscriptionExpiry = subscriptionExpiry,
                subscriptionAmount = subscriptionAmount,
                subscriptionPaymentStatus = subscriptionPaymentStatus
            )
            repository.editPharmacy(pharmacy)
        }
    }

    fun removePharmacy(id: String) {
        viewModelScope.launch {
            repository.removePharmacy(id)
        }
    }

    // PHARMACY SCHEDULE MANAGEMENT ACTIONS
    suspend fun isCurrentPharmacyExpired(): Boolean {
        val user = activeUser.value ?: return false
        if (user.role != "Pharmacy") return false
        val pharList = repository.allPharmacies.firstOrNull() ?: emptyList()
        val phar = pharList.find { it.id == user.id } ?: pharList.firstOrNull() ?: return false
        if (!phar.trialStarted) return false
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val daysRemaining = calculateDaysBetween(todayStr, phar.subscriptionExpiry)
        return daysRemaining < 0
    }

    fun addDoctor(name: String, specialization: String, experience: Int, fee: Double, slots: List<String>) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to add doctor but trial/subscription has expired.")
                return@launch
            }
            val pharmacyId = activeUser.value?.id ?: ""
            val doctor = DoctorEntity(
                name = name,
                specialization = specialization,
                experience = experience,
                fee = fee,
                rating = 4.5,
                pharmacyId = pharmacyId,
                bannerName = "doctor_male_1",
                slotsJson = slots.joinToString(", ")
            )
            repository.addDoctor(doctor)
        }
    }

    fun toggleDoctorEnabled(doctorId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to toggle doctor but trial/subscription has expired.")
                return@launch
            }
            repository.toggleDoctorEnabled(doctorId, isEnabled)
        }
    }

    fun softDeleteDoctor(doctorId: String) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to soft-delete doctor but trial/subscription has expired.")
                return@launch
            }
            repository.softDeleteDoctor(doctorId)
        }
    }

    fun restoreDoctor(doctorId: String) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to restore doctor but trial/subscription has expired.")
                return@launch
            }
            repository.restoreDoctor(doctorId)
        }
    }

    fun permanentlyDeleteDoctor(doctorId: String) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to permanently-delete doctor but trial/subscription has expired.")
                return@launch
            }
            repository.permanentlyDeleteDoctor(doctorId)
        }
    }

    fun saveSchedule(doctorId: String, date: String, fromTime: String, toTime: String, maxPatients: Int, openBooking: Boolean) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to save shift schedule but trial/subscription has expired.")
                return@launch
            }
            val schedule = ScheduleEntity(
                doctorId = doctorId,
                dateStr = date,
                fromTimeStr = fromTime,
                toTimeStr = toTime,
                maxPatients = maxPatients,
                isOpenForBooking = openBooking
            )
            repository.addSchedule(schedule)
        }
    }

    fun markHoliday(scheduleId: String, isHoliday: Boolean) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to mark holiday but trial/subscription has expired.")
                return@launch
            }
            repository.markHoliday(scheduleId, isHoliday)
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            if (isCurrentPharmacyExpired()) {
                repository.logAction("Action Blocked", "Tried to update booking status but trial/subscription has expired.")
                return@launch
            }
            repository.updateBookingStatus(bookingId, status)
        }
    }

    fun logAction(action: String, details: String) {
        viewModelScope.launch {
            repository.logAction(action, details)
        }
    }

    fun toggleSubscriptionRenewal(id: String, active: Boolean) {
        viewModelScope.launch {
            repository.toggleSubscriptionRenewal(id, active)
        }
    }

    fun submitOrUpdateReview(patientId: String, patientName: String, doctorId: String, rating: Int, reviewText: String) {
        viewModelScope.launch {
            val existing = allReviews.value.find { it.patientId == patientId && it.doctorId == doctorId }
            if (existing != null) {
                val updated = existing.copy(rating = rating, review = reviewText, createdAt = System.currentTimeMillis())
                repository.editReview(updated)
            } else {
                val newReview = ReviewEntity(
                    patientId = patientId,
                    patientName = patientName,
                    doctorId = doctorId,
                    rating = rating,
                    review = reviewText
                )
                repository.addReview(newReview)
            }
        }
    }

    fun deleteReview(reviewId: String, doctorId: String) {
        viewModelScope.launch {
            repository.deleteReview(reviewId, doctorId)
        }
    }

    suspend fun getPharmacyRequestByEmail(email: String): PharmacyRequestEntity? {
        return repository.getPharmacyRequestByEmail(email)
    }

    fun updatePharmacyPassword(email: String, passwordHash: String) {
        viewModelScope.launch {
            repository.updatePharmacyPassword(email, passwordHash)
        }
    }

    fun submitPharmacyRequest(
        pharmacyName: String,
        ownerName: String,
        licenseNo: String,
        mobile: String,
        email: String,
        passwordPlain: String,
        address: String,
        licenseImage: String,
        pharmacyPhoto: String?,
        paymentId: String,
        paymentStatus: String,
        paymentAmount: Double,
        paymentDate: Long,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (pharmacyName.isBlank() || ownerName.isBlank() || licenseNo.isBlank() ||
                    mobile.isBlank() || email.isBlank() || passwordPlain.isBlank() || address.isBlank()
                ) {
                    onComplete(false, "All fields marked with * are strictly required.")
                    return@launch
                }

                if (passwordPlain.length < 6) {
                    onComplete(false, "Password must be at least 6 characters.")
                    return@launch
                }

                val existingEmailRequest = repository.getPharmacyRequestByEmail(email)
                if (existingEmailRequest != null) {
                    onComplete(false, "This Email Address has already been submitted or is in use.")
                    return@launch
                }

                val existingLicenseRequest = repository.getPharmacyRequestByLicense(licenseNo)
                if (existingLicenseRequest != null) {
                    onComplete(false, "This Drug License Number has already been submitted or is in use.")
                    return@launch
                }

                val existingPharmacies = repository.allPharmacies.firstOrNull() ?: emptyList()
                if (existingPharmacies.any { it.license.lowercase() == licenseNo.lowercase() }) {
                    onComplete(false, "This Drug License Number belongs to an already active pharmacy.")
                    return@launch
                }

                val request = PharmacyRequestEntity(
                    pharmacyName = pharmacyName,
                    ownerName = ownerName,
                    licenseNo = licenseNo,
                    mobile = mobile,
                    email = email,
                    passwordHash = passwordPlain,
                    address = address,
                    licenseImage = licenseImage,
                    pharmacyPhoto = pharmacyPhoto,
                    status = "pending_verification",
                    paymentId = paymentId,
                    paymentStatus = paymentStatus,
                    paymentAmount = paymentAmount,
                    paymentDate = paymentDate
                )

                repository.addPharmacyRequest(request)

                // Notify Master Admin
                repository.addNotification(
                    NotificationEntity(
                        title = "New Pharmacy Registration",
                        message = "Pharmacy '${pharmacyName}' has submitted a registration request with payment ID: $paymentId.",
                        timestamp = System.currentTimeMillis()
                    )
                )

                onComplete(true, "Your registration request has been submitted successfully.\n\nRegistration fee ₹${paymentAmount.toInt()} was successfully processed (ID: $paymentId).\n\nYour request is pending verification by Master Admin.")
            } catch (e: Exception) {
                onComplete(false, "Error submitting request: ${e.message}")
            }
        }
    }

    fun approvePharmacyRequest(requestId: String, currentAdminId: String) {
        viewModelScope.launch {
            repository.approvePharmacyRequest(requestId, currentAdminId)
        }
    }

    fun rejectPharmacyRequest(requestId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectPharmacyRequest(requestId, reason)
        }
    }

    fun requestCorrectionPharmacyRequest(requestId: String, notes: String) {
        viewModelScope.launch {
            repository.requestCorrectionPharmacyRequest(requestId, notes)
        }
    }

    fun calculateDaysBetween(startDateStr: String, endDateStr: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val start = sdf.parse(startDateStr)
            val end = sdf.parse(endDateStr)
            if (start != null && end != null) {
                val diff = end.time - start.time
                diff / (1000L * 60 * 60 * 24)
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun sendSubscriptionAlert(title: String, message: String) {
        viewModelScope.launch {
            repository.addNotification(
                NotificationEntity(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun savePricingSettings(registrationFee: Double, monthlyFee: Double, quarterlyFee: Double, yearlyFee: Double) {
        viewModelScope.launch {
            val current = repository.getPricingSettings() ?: PricingSettingsEntity()
            val updated = current.copy(
                registrationFee = registrationFee,
                monthlySubscriptionFee = monthlyFee,
                quarterlySubscriptionFee = quarterlyFee,
                yearlySubscriptionFee = yearlyFee
            )
            repository.savePricingSettings(updated)
        }
    }

    // --- PAYMENT FOUNDATION (PHASE A) ---
    val paymentRepository: com.example.data.payment.PaymentRepository by lazy {
        (getApplication() as com.example.DoctorLineApplication).paymentRepository
    }

    val razorpayConfig: com.example.data.payment.RazorpayConfig by lazy {
        com.example.data.payment.RazorpayConfig.fromBuildConfig()
    }

    val allPaymentHistory: StateFlow<List<com.example.data.payment.PaymentHistoryRecord>> = paymentRepository.allPaymentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getPaymentHistoryByPharmacy(pharmacyId: String): Flow<List<com.example.data.payment.PaymentHistoryRecord>> {
        return paymentRepository.getPaymentHistoryByPharmacy(pharmacyId)
    }

    fun initiatePharmacyRegistrationFee(
        pharmacyRequestId: String,
        amount: Double,
        email: String,
        onResult: (com.example.data.payment.PaymentResult<com.example.data.payment.PaymentOrder>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(com.example.data.payment.PaymentResult.Loading)
            val res = paymentRepository.initiateRegistrationFee(pharmacyRequestId, amount, email)
            onResult(res)
        }
    }

    fun initiateSubscriptionRenewal(
        pharmacyId: String,
        amount: Double,
        onResult: (com.example.data.payment.PaymentResult<com.example.data.payment.PaymentOrder>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(com.example.data.payment.PaymentResult.Loading)
            val res = paymentRepository.initiateSubscriptionRenewal(pharmacyId, amount)
            onResult(res)
        }
    }

    fun verifyAndCompletePayment(
        orderId: String,
        paymentId: String,
        signature: String,
        pharmacyId: String?,
        type: com.example.data.payment.PaymentType,
        amount: Double,
        paymentMethod: String = "UPI",
        failureReason: String? = null,
        onResult: (com.example.data.payment.PaymentResult<Boolean>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(com.example.data.payment.PaymentResult.Loading)
            val res = paymentRepository.verifyAndCompletePayment(
                orderId = orderId,
                paymentId = paymentId,
                signature = signature,
                pharmacyId = pharmacyId,
                type = type,
                amount = amount,
                paymentMethod = paymentMethod,
                failureReason = failureReason
            )
            onResult(res)
        }
    }

    fun renewPharmacySubscription(
        pharmacyId: String,
        amount: Double,
        paymentId: String,
        orderId: String,
        signature: String,
        paymentMethod: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val pharList = repository.allPharmacies.firstOrNull() ?: emptyList()
                val phar = pharList.find { it.id == pharmacyId }
                if (phar == null) {
                    onComplete(false, "Pharmacy not found.")
                    return@launch
                }

                // Calculate next due date / expiry
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                val calendar = java.util.Calendar.getInstance()
                
                val plan = phar.subscriptionPlan.lowercase()
                if (plan.contains("quarterly")) {
                    calendar.add(java.util.Calendar.MONTH, 3)
                } else if (plan.contains("yearly")) {
                    calendar.add(java.util.Calendar.YEAR, 1)
                } else {
                    calendar.add(java.util.Calendar.MONTH, 1) // default monthly
                }
                
                val nextDueDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)

                // Update pharmacy entity
                val updatedPharmacy = phar.copy(
                    status = "Active", // unsuspend automatically
                    subscriptionStart = today,
                    subscriptionExpiry = nextDueDate,
                    subscriptionAmount = amount,
                    subscriptionPaymentStatus = "Paid",
                    trialStarted = false // move to full plan
                )
                repository.editPharmacy(updatedPharmacy)

                // Update subscription details entity
                val subFlow = repository.getSubscriptionForPharmacy(pharmacyId).firstOrNull()
                val newSub = subFlow?.copy(
                    validityDate = nextDueDate,
                    price = amount
                ) ?: com.example.data.SubscriptionEntity(
                    id = "sub_" + java.util.UUID.randomUUID().toString().take(8),
                    pharmacyId = pharmacyId,
                    currentPlan = phar.subscriptionPlan,
                    price = amount,
                    validityDate = nextDueDate,
                    autoRenewal = true
                )
                repository.updateSubscription(newSub)

                // Register successful payment in payment history
                val successRecord = com.example.data.payment.PaymentHistoryRecord(
                    paymentId = paymentId,
                    orderId = orderId,
                    amount = amount,
                    paymentType = com.example.data.payment.PaymentType.MONTHLY_SUBSCRIPTION_RENEWAL.name,
                    paymentStatus = com.example.data.payment.PaymentStatus.success.name,
                    createdDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                    completedDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                    pharmacyId = pharmacyId,
                    signature = signature,
                    method = paymentMethod
                )
                paymentRepository.insertPaymentHistory(successRecord)

                // Trigger notification
                val notification = com.example.data.NotificationEntity(
                    title = "Subscription Renewed Successfully",
                    message = "SaaS Access reactivated for ${phar.name}. Renewed successfully (Paid: ₹${amount.toInt()} via $paymentMethod, ID: $paymentId). Expiry: $nextDueDate.",
                    timestamp = System.currentTimeMillis()
                )
                repository.addNotification(notification)

                // Log audit action
                repository.logAction("Subscription Renewal", "Pharmacy ${phar.name} successfully renewed. Expiry: $nextDueDate.")

                onComplete(true, "Your SaaS subscription has been renewed successfully!\n\nAccess reactivated automatically.\n\nNext Billing Date: $nextDueDate")
            } catch (e: Exception) {
                onComplete(false, "Renewal failed: ${e.message}")
            }
        }
    }

    fun retryPayment(
        orderId: String,
        record: com.example.data.payment.PaymentHistoryRecord,
        onResult: (com.example.data.payment.PaymentResult<com.example.data.payment.PaymentOrder>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(com.example.data.payment.PaymentResult.Loading)
            val res = paymentRepository.retryPayment(orderId, record)
            onResult(res)
        }
    }

    fun getInvoiceUrl(paymentId: String, onResult: (com.example.data.payment.PaymentResult<String>) -> Unit) {
        viewModelScope.launch {
            onResult(com.example.data.payment.PaymentResult.Loading)
            val res = paymentRepository.getInvoiceUrl(paymentId)
            onResult(res)
        }
    }

    fun getReceiptUrl(paymentId: String, onResult: (com.example.data.payment.PaymentResult<String>) -> Unit) {
        viewModelScope.launch {
            onResult(com.example.data.payment.PaymentResult.Loading)
            val res = paymentRepository.getReceiptUrl(paymentId)
            onResult(res)
        }
    }

    // ViewModel Factory
    class Factory(
        private val application: Application,
        private val repository: DoctorLineRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
