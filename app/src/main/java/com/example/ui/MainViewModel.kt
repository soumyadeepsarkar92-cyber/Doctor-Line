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
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                repository.allPharmacies.collect { pharmaciesList ->
                    pharmaciesList.forEach { phar ->
                        if (phar.subscriptionExpiry < today && phar.status != "Suspended") {
                            val updated = phar.copy(status = "Suspended")
                            repository.editPharmacy(updated)
                            repository.logAction("Subscription Expired", "SaaS subscription validity expired for ${phar.name} (Expiry: ${phar.subscriptionExpiry}). Account has been SUSPENDED.")
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
                    repository.getSubscriptionForPharmacy("phar_apollo").collect { sub ->
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
        onSuccess: () -> Unit
    ) {
        val doctor = _selectedDoctor.value ?: return
        val slot = _selectedTimeSlot.value ?: return

        viewModelScope.launch {
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
    fun addDoctor(name: String, specialization: String, experience: Int, fee: Double, slots: List<String>) {
        viewModelScope.launch {
            val doctor = DoctorEntity(
                name = name,
                specialization = specialization,
                experience = experience,
                fee = fee,
                rating = 4.5,
                pharmacyId = "phar_apollo", // standard
                bannerName = "doctor_male_1",
                slotsJson = slots.joinToString(", ")
            )
            repository.addDoctor(doctor)
        }
    }

    fun toggleDoctorEnabled(doctorId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleDoctorEnabled(doctorId, isEnabled)
        }
    }

    fun saveSchedule(doctorId: String, date: String, fromTime: String, toTime: String, maxPatients: Int, openBooking: Boolean) {
        viewModelScope.launch {
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
            repository.markHoliday(scheduleId, isHoliday)
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
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
                    status = "pending"
                )

                repository.addPharmacyRequest(request)
                onComplete(true, "Your registration request has been submitted successfully.\n\nPlease contact DoctorLine Admin and complete payment.\n\nYour account will be activated after approval.")
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

    fun rejectPharmacyRequest(requestId: String) {
        viewModelScope.launch {
            repository.rejectPharmacyRequest(requestId)
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
