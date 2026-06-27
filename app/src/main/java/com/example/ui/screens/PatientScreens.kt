package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

// ============================================
// MAIN PATIENT NAVIGATION WRAPPER
// ============================================
@Composable
fun PatientModuleScreen(
    viewModel: MainViewModel,
    logout: () -> Unit
) {
    var currentSubScreen by remember { mutableStateOf("home") } // "home", "details", "booking_form", "success"
    
    // Bottom Navbar state
    var selectedBottomTab by remember { mutableStateOf("home") } // "home", "appointments", "notifications", "profile"

    val activeUser by viewModel.activeUser.collectAsState()
    val selectedDoctor by viewModel.selectedDoctor.collectAsState()
    val lastBooking by viewModel.lastCreatedBooking.collectAsState()

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xFF1E293B).copy(alpha = 0.95f), Color(0xFF0F172A).copy(alpha = 0.95f))
                                } else {
                                    listOf(Color.White, Color(0xFFF8FAFC))
                                }
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val tabs = listOf(
                        Triple("home", "Home", Icons.Rounded.Home),
                        Triple("appointments", "Bookings", Icons.Rounded.CalendarMonth),
                        Triple("notifications", "Notifs", Icons.Rounded.Notifications),
                        Triple("profile", "Profile", Icons.Rounded.Person)
                    )

                    tabs.forEach { (tabId, label, icon) ->
                        val isSelected = if (tabId == "home") {
                            selectedBottomTab == "home" && currentSubScreen == "home"
                        } else {
                            selectedBottomTab == tabId
                        }

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "nav_scale"
                        )

                        val activeColor = if (isDark) Color(0xFF10B981) else Color(0xFF7C5DFA)
                        val inactiveColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        val activeBgColor = if (isDark) Color(0xFF151D30) else Color(0xFFF3EFFF)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        selectedBottomTab = tabId
                                        if (tabId == "appointments") {
                                            currentSubScreen = "home"
                                        } else if (tabId == "home") {
                                            currentSubScreen = "home"
                                        }
                                    }
                                )
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(width = 46.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) activeBgColor else Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) activeColor else inactiveColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) activeColor else inactiveColor
                            )
                        }
                    }
                }
            }
        },
        containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            when (selectedBottomTab) {
                "home" -> {
                    when (currentSubScreen) {
                        "home" -> {
                            PatientHomeScreen(
                                viewModel = viewModel,
                                onSelectDoctor = { doctor ->
                                    viewModel.selectDoctor(doctor)
                                    currentSubScreen = "details"
                                },
                                onOpenNotifications = {
                                    selectedBottomTab = "notifications"
                                }
                            )
                        }
                        "details" -> {
                            selectedDoctor?.let { doctor ->
                                DoctorDetailsScreen(
                                    doctor = doctor,
                                    viewModel = viewModel,
                                    onBack = { currentSubScreen = "home" },
                                    onNavigateToBooking = { currentSubScreen = "booking_form" }
                                )
                            }
                        }
                        "booking_form" -> {
                            selectedDoctor?.let { doctor ->
                                BookingFormScreen(
                                    doctor = doctor,
                                    viewModel = viewModel,
                                    onBack = { currentSubScreen = "details" },
                                    onConfirmed = { currentSubScreen = "success" }
                                )
                            }
                        }
                        "success" -> {
                            lastBooking?.let { booking ->
                                BookingSuccessScreen(
                                    booking = booking,
                                    viewModel = viewModel,
                                    onBackToBookings = {
                                        selectedBottomTab = "appointments"
                                        currentSubScreen = "home"
                                    }
                                )
                            }
                        }
                    }
                }
                "appointments" -> {
                    PatientAppointmentsScreen(viewModel = viewModel)
                }
                "notifications" -> {
                    PatientNotificationsScreen(viewModel = viewModel)
                }
                "profile" -> {
                    PatientProfileScreen(
                        user = activeUser,
                        onLogout = logout,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ============================================
// 1. PATIENT HOME SCREEN
// ============================================
@Composable
fun PatientHomeScreen(
    viewModel: MainViewModel,
    onSelectDoctor: (DoctorEntity) -> Unit,
    onOpenNotifications: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsState()
    val doctors by viewModel.activeDoctors.collectAsState()
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val patientFavourites by viewModel.patientFavourites.collectAsState()
    val patientBookings by viewModel.patientBookings.collectAsState()
    val allBookingsState by viewModel.allBookings.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf("All") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var includeOtherCities by remember { mutableStateOf(false) }

    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()

    val specialties = listOf("All", "Cardiologist", "Pediatrician", "Dermatologist", "General Physician")

    val filteredDoctors = doctors.filter { doctor ->
        val matchesSpecialty = selectedSpecialty == "All" || doctor.specialization == selectedSpecialty
        val matchesSearch = doctor.name.contains(searchQuery, ignoreCase = true) ||
                doctor.specialization.contains(searchQuery, ignoreCase = true)
        
        val matchesCity = includeOtherCities || run {
            val pharmacy = pharmacies.find { it.id == doctor.pharmacyId }
            val pharmacyCity = if (pharmacy != null) getPharmacyCity(pharmacy.address) else "Kolkata"
            pharmacyCity.equals(selectedCity, ignoreCase = true)
        }
        matchesSpecialty && matchesSearch && matchesCity
    }

    val todayDateStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val activeDelayedBooking = patientBookings.find { booking ->
        booking.dateStr == todayDateStr && booking.status == "Upcoming" && run {
            val doc = doctors.find { it.id == booking.doctorId }
            doc?.availabilityStatus == "Running Late"
        }
    }

    if (showLocationDialog) {
        LocationSelectorDialog(
            viewModel = viewModel,
            isDark = isDark,
            onDismiss = { showLocationDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Location and Info Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF))
                            .clickable { showLocationDialog = true }
                            .padding(vertical = 6.dp, horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$selectedCity, $selectedCountry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Open Selector",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Hello, ${activeUser?.name ?: "Guest"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (com.example.data.SupabaseManager.isConfigured) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (com.example.data.SupabaseManager.isConfigured) "Live Connection Active" else "Offline Sandbox Cache",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (com.example.data.SupabaseManager.isConfigured) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
                
                // Notification bell with unread badge count
                val unreadCount = notifications.count { !it.isRead }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onOpenNotifications),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = "Notifications",
                        tint = if (unreadCount > 0) Color(0xFF2563EB) else Color(0xFF475569)
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Search inputs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search doctors or specializations...", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                // Scope Selectors Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = "Selected City",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "City: $selectedCity",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF1E3A8A)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { includeOtherCities = !includeOtherCities }
                            .background(if (includeOtherCities) Color(0xFF7C5DFA) else (if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (includeOtherCities) Icons.Rounded.Language else Icons.Rounded.Home,
                            contentDescription = "Search Scope",
                            tint = if (includeOtherCities) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (includeOtherCities) "Search All Cities" else "This City Only",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (includeOtherCities) Color.White else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569))
                        )
                    }
                }
            }
        }

        // Realtime Notifications Banner (Blue Glass Card)
        val unreadAlerts = notifications.filter { !it.isRead }
        if (unreadAlerts.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF).copy(alpha = 0.8f)),
                    border = BorderStroke(1.2.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(18.dp))
                        .clickable { onOpenNotifications() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = unreadAlerts.first().title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = unreadAlerts.first().message,
                                fontSize = 11.sp,
                                color = Color(0xFF2563EB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${unreadAlerts.size} New",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Realtime Delay Alert Banner
        activeDelayedBooking?.let { booking ->
            val doc = doctors.find { it.id == booking.doctorId }
            if (doc != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Delay warning",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Delay Alert: Dr. ${doc.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Expected arrival at ${doc.expectedStartTime} instead of scheduled time. Reason: ${doc.delayReason}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Appointments & Queue Status Section (Premium Blue Glass Card)
        val upcomingBookings = patientBookings.filter { it.status == "Upcoming" }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Upcoming Appointments & Queue Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                
                if (upcomingBookings.isNotEmpty()) {
                    upcomingBookings.forEach { booking ->
                        val doc = doctors.find { it.id == booking.doctorId }
                        val pharm = pharmacies.find { it.id == doc?.pharmacyId }
                        val docBookings = allBookingsState.filter { it.doctorId == booking.doctorId && it.dateStr == booking.dateStr }
                        val servedBookings = docBookings.filter { it.status == "Completed" }
                        val servingToken = (servedBookings.maxOfOrNull { it.tokenNumber } ?: 0) + 1
                        val queueAhead = booking.tokenNumber - servingToken

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF).copy(alpha = 0.8f)),
                            border = BorderStroke(1.5.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, shape = RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFDBEAFE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Rounded.Event, contentDescription = null, tint = Color(0xFF1D4ED8))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = "Dr. ${doc?.name ?: "Clinician"}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                            Text(text = doc?.specialization ?: "General Clinician", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF2563EB))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Token #${booking.tokenNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFBFDBFE).copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("DATE & TIME", fontSize = 10.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                        Text("${booking.dateStr} | ${booking.timeStr}", fontSize = 12.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("PHARMACY", fontSize = 10.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                        Text(pharm?.name ?: "Local Pharmacy", fontSize = 12.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Live Queue Status Inner Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFDBEAFE).copy(alpha = 0.7f))
                                        .border(1.dp, Color(0xFF93C5FD).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.People, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Queue Status", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (queueAhead > 0) "$queueAhead patient(s) ahead of you" else "You're next in line! Proceed to cabin.",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1E3A8A),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("CURRENT SERVING", fontSize = 9.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                            Text("#$servingToken", fontSize = 13.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF).copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.EventAvailable, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Upcoming Appointments", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                            Text("Book real-time tokens with live serving queue updates instantly.", fontSize = 11.sp, color = Color(0xFF2563EB), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Quick Booking Section (Interactive Shortcuts)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Booking Shortcuts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Triple("General", Icons.Rounded.HealthAndSafety, "General Physician"),
                        Triple("Heart", Icons.Rounded.Favorite, "Cardiologist"),
                        Triple("Child", Icons.Rounded.ChildCare, "Pediatrician"),
                        Triple("Skin", Icons.Rounded.Face, "Dermatologist")
                    ).forEach { (label, icon, specName) ->
                        val isSelected = selectedSpecialty == specName
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF2563EB).copy(alpha = 0.15f) else Color(0xFFEFF6FF).copy(alpha = 0.6f)),
                            border = BorderStroke(1.2.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFF3B82F6).copy(alpha = 0.25f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedSpecialty = if (isSelected) "All" else specName
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, contentDescription = label, tint = Color(0xFF1D4ED8), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                            }
                        }
                    }
                }
            }
        }

        // Promo Banner card (Upgraded with Glass styling)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D4ED8)),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.width * 0.4f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.3f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "Instantly Secure Your Clinic Slots",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reduce waiting times and avoid crowded waiting rooms.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "⚡ Instant Tokens Allocated",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }

        // Favourite Doctors Section (Premium Glass Cards)
        if (patientFavourites.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your Favourite Doctors",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val favDoctors = doctors.filter { doc -> patientFavourites.any { it.doctorId == doc.id } }
                        items(favDoctors) { doc ->
                            val matchingPharm = pharmacies.find { it.id == doc.pharmacyId }
                            val isSusp = matchingPharm?.status == "Suspended"
                            
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF).copy(alpha = 0.75f)),
                                border = BorderStroke(1.2.dp, Color(0xFF3B82F6).copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .width(185.dp)
                                    .clickable {
                                        if (!isSusp) {
                                            onSelectDoctor(doc)
                                        }
                                    }
                                    .shadow(2.dp, shape = RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFDBEAFE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (doc.bannerName.contains("female")) Icons.Rounded.Face else Icons.Rounded.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF1D4ED8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Rounded.Favorite,
                                            contentDescription = "Remove Favourite",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { viewModel.toggleFavourite(doc.id) }
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(
                                        text = doc.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Text(
                                        text = doc.specialization,
                                        fontSize = 11.sp,
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (doc.availabilityStatus) {
                                                    "Running Late" -> Color(0xFFFEF3C7)
                                                    "On Holiday" -> Color(0xFFFEF2F2)
                                                    else -> Color(0xFFECFDF5)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = when (doc.availabilityStatus) {
                                                "Running Late" -> "⏳ Late"
                                                "On Holiday" -> "🌴 Holiday"
                                                else -> "🟢 Live"
                                            },
                                            color = when (doc.availabilityStatus) {
                                                "Running Late" -> Color(0xFFD97706)
                                                "On Holiday" -> Color(0xFFDC2626)
                                                else -> Color(0xFF059669)
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "One Click Booking ⚡",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selector Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Specializations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(specialties) { spec ->
                        val isSelected = selectedSpecialty == spec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF7C5DFA) else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedSpecialty = spec }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = spec,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Header for Doctor List
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Doctors Today",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "${filteredDoctors.size} found",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        if (filteredDoctors.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = "Search Off",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No clinicians found for '$selectedSpecialty'",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredDoctors) { doctor ->
                val matchingPharmacy = pharmacies.find { it.id == doctor.pharmacyId }
                val isSuspended = matchingPharmacy?.status == "Suspended"
                val isFavourite = patientFavourites.any { it.doctorId == doctor.id }
                DoctorCard(
                    doctor = doctor,
                    pharmacyName = matchingPharmacy?.name ?: "Apollo Pharmacy",
                    isSuspended = isSuspended,
                    isFavourite = isFavourite,
                    onFavouriteToggle = { viewModel.toggleFavourite(doctor.id) },
                    onClick = { 
                        if (!isSuspended) {
                            onSelectDoctor(doctor) 
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DoctorCard(
    doctor: DoctorEntity,
    pharmacyName: String,
    isSuspended: Boolean = false,
    isFavourite: Boolean = false,
    onFavouriteToggle: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSuspended) Color(0xFFFEE2E2).copy(alpha = 0.6f) else Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSuspended) 0.dp else 2.dp, shape = RoundedCornerShape(18.dp)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder Image with unique visual style
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isSuspended) listOf(Color(0xFFFCA5A5), Color(0xFFFECACA)) else listOf(Color(0xFFEEF5FF), Color(0xFFD0E1FD))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (doctor.bannerName.contains("female")) Icons.Rounded.Face else Icons.Rounded.Person,
                    contentDescription = doctor.name,
                    tint = if (isSuspended) Color(0xFFB91C1C) else Color(0xFF7C5DFA),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = doctor.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuspended) Color(0xFF7F1D1D) else Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = if (isSuspended) Color(0xFFEF4444) else Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = doctor.rating.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSuspended) Color(0xFF7F1D1D) else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavourite) Color(0xFFEF4444) else Color(0xFF94A3B8),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onFavouriteToggle() }
                        )
                    }
                }

                Text(
                    text = doctor.specialization,
                    fontSize = 13.sp,
                    color = if (isSuspended) Color(0xFFEF4444) else Color(0xFF7C5DFA),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Availability Badge
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (doctor.availabilityStatus) {
                                "Running Late" -> Color(0xFFFEF3C7)
                                "On Holiday" -> Color(0xFFFEF2F2)
                                else -> Color(0xFFECFDF5)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (doctor.availabilityStatus) {
                            "Running Late" -> "⏳ Running Late" + (if (doctor.expectedStartTime.isNotEmpty()) " (${doctor.expectedStartTime})" else "")
                            "On Holiday" -> "🌴 On Holiday"
                            else -> "🟢 Available Today"
                        },
                        color = when (doctor.availabilityStatus) {
                            "Running Late" -> Color(0xFFD97706)
                            "On Holiday" -> Color(0xFFDC2626)
                            else -> Color(0xFF059669)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "💼 ${doctor.experience} Years Experience",
                    fontSize = 12.sp,
                    color = if (isSuspended) Color(0xFF991B1B) else Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = if (isSuspended) "📍 This Pharmacy is temporarily unavailable." else "📍 $pharmacyName",
                    fontSize = 11.sp,
                    color = if (isSuspended) Color(0xFFEF4444) else Color(0xFF475569),
                    fontWeight = if (isSuspended) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${doctor.fee}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSuspended) Color(0xFF991B1B) else Color(0xFF0F172A)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSuspended) Color(0xFFFCA5A5) else Color(0xFFF3EFFF))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isSuspended) "Unavailable" else "Book Appointment",
                            color = if (isSuspended) Color(0xFF7F1D1D) else Color(0xFF7C5DFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// 2. DOCTOR DETAILS SCREEN
// ============================================
@Composable
fun DoctorDetailsScreen(
    doctor: DoctorEntity,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToBooking: () -> Unit
) {
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val matchingPharmacy = pharmacies.find { it.id == doctor.pharmacyId }
    val selectedSlot by viewModel.selectedTimeSlot.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val doctorReviews = allReviews.filter { it.doctorId == doctor.id }
    val computedRating = if (doctorReviews.isNotEmpty()) String.format(java.util.Locale.US, "%.1f", doctorReviews.map { it.rating }.average()) else String.format(java.util.Locale.US, "%.1f", doctor.rating)

    val availableSlots = doctor.slotsJson.split(",").map { it.trim() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp) // Leave height for panel
        ) {
            // Header actions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                }
                Text(
                    text = "Clinician Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.align(Alignment.Center)
                )
                val patientFavourites by viewModel.patientFavourites.collectAsState()
                val isFavourite = patientFavourites.any { it.doctorId == doctor.id }
                IconButton(
                    onClick = { viewModel.toggleFavourite(doctor.id) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavourite) Color(0xFFEF4444) else Color(0xFF64748B)
                    )
                }
            }

            // Doctor Main Stats Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFF3EFFF), Color(0xFFE5DBFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (doctor.bannerName.contains("female")) Icons.Rounded.Face else Icons.Rounded.Person,
                        contentDescription = doctor.name,
                        tint = Color(0xFF7C5DFA),
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = doctor.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = doctor.specialization,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C5DFA),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$computedRating  •  ${doctor.experience} Yrs Experience  •  (${doctorReviews.size} reviews)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    }
                    
                    // Availability Status Badge on Doctor Profile!
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (doctor.availabilityStatus) {
                                    "Running Late" -> Color(0xFFFEF3C7)
                                    "On Holiday" -> Color(0xFFFEF2F2)
                                    else -> Color(0xFFECFDF5)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (doctor.availabilityStatus) {
                                "Running Late" -> "⏳ Running Late" + (if (doctor.expectedStartTime.isNotEmpty()) " (${doctor.expectedStartTime})" else "")
                                "On Holiday" -> "🌴 On Holiday"
                                else -> "🟢 Available Today"
                            },
                            color = when (doctor.availabilityStatus) {
                                "Running Late" -> Color(0xFFD97706)
                                "On Holiday" -> Color(0xFFDC2626)
                                else -> Color(0xFF059669)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (doctor.availabilityStatus == "Running Late" && doctor.delayReason.isNotEmpty()) {
                        Text(
                            text = "Reason: ${doctor.delayReason}",
                            color = Color(0xFFB45309),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // High-fidelity Stats Grid section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPanel(
                    label = "Consultation Fee",
                    value = "₹${doctor.fee}",
                    icon = Icons.Rounded.Payments,
                    color = Color(0xFF7C5DFA),
                    modifier = Modifier.weight(1f)
                )
                StatPanel(
                    label = "Hospital / Pharmacy",
                    value = matchingPharmacy?.name?.split("-")?.firstOrNull()?.trim() ?: "Apollo RX",
                    icon = Icons.Rounded.LocalPharmacy,
                    color = Color(0xFF00A86B),
                    modifier = Modifier.weight(1f)
                )
            }

            // Location with Call / Maps actions
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pharmacy & Clinic Address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = matchingPharmacy?.address ?: "Sector 5 medical road",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* Simulated action: open Google Maps */ },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Rounded.Map, contentDescription = "Map", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Maps", color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { /* Simulated action: Call Pharmacy Number */ },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = "Call", tint = Color(0xFF00A86B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Clinic", color = Color(0xFF00A86B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Calendar Selection Row
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Select Appointment Date",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val selectedDate by viewModel.selectedDate.collectAsState()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val sdfDay = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                    val sdfNum = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault())
                    val sdfYear = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    
                    val cal = java.util.Calendar.getInstance()
                    for (i in 0 until 7) {
                        val date = cal.time
                        val dayName = sdfDay.format(date)
                        val dayNum = sdfNum.format(date)
                        val fullDateStr = sdfYear.format(date)
                        
                        val isDateSel = selectedDate == fullDateStr
                        
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, if (isDateSel) Color(0xFF7C5DFA) else Color(0xFFE2E8F0)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDateSel) Color(0xFF7C5DFA) else Color(0xFFF8FAFC)
                            ),
                            modifier = Modifier
                                .width(64.dp)
                                .clickable { viewModel.selectDate(fullDateStr) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayName.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDateSel) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayNum,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDateSel) Color.White else Color(0xFF1E293B)
                                )
                            }
                        }
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available slots section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Select Time Slot",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                FlowRowLayout(
                    horizontalGap = 8.dp,
                    verticalGap = 8.dp
                ) {
                    availableSlots.forEach { slot ->
                        val isSelected = selectedSlot == slot
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF7C5DFA) else Color(0xFFF8FAFC))
                                .clickable { viewModel.selectTimeSlot(slot) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = slot,
                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ratings and reviews section
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Patient Ratings & Reviews",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (doctorReviews.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No reviews submitted yet for this clinical specialist. Be the first to consult and share your experience!",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        doctorReviews.forEach { review ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF3EFFF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = review.patientName.take(1).uppercase(),
                                                    color = Color(0xFF7C5DFA),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = review.patientName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                        }

                                        // Stars
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            for (star in 1..5) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (star <= review.rating) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (review.review.isNotBlank()) {
                                        Text(
                                            text = review.review,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected slot summary bottom panel (Only display if a slot has been chosen)
        AnimatedVisibility(
            visible = selectedSlot != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Selected Time Slot", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(
                                text = selectedSlot ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Fee (Pay Later/Now)", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(
                                text = "₹${doctor.fee}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF7C5DFA),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToBooking,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5DFA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Proceed to Booking Form",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatPanel(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// Custom flow-layout since direct compose FlowRow is in a distinct library package
@Composable
fun FlowRowLayout(
    horizontalGap: androidx.compose.ui.unit.Dp,
    verticalGap: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        
        var currentY = 0
        var currentX = 0
        var maxRowHeight = 0
        
        val itemPositions = mutableListOf<Triple<androidx.compose.ui.layout.Placeable, Int, Int>>()
        
        placeables.forEach { placeable ->
            if (currentX + placeable.width > layoutWidth) {
                // Break to next row
                currentY += maxRowHeight + verticalGap.roundToPx()
                currentX = 0
                maxRowHeight = 0
            }
            
            itemPositions.add(Triple(placeable, currentX, currentY))
            currentX += placeable.width + horizontalGap.roundToPx()
            if (placeable.height > maxRowHeight) {
                maxRowHeight = placeable.height
            }
        }
        
        layout(
            width = layoutWidth,
            height = currentY + maxRowHeight
        ) {
            itemPositions.forEach { (placeable, x, y) ->
                placeable.placeRelative(x, y)
            }
        }
    }
}

// ============================================
// 3. BOOKING SCREEN (FORM)
// ============================================
@Composable
fun BookingFormScreen(
    doctor: DoctorEntity,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onConfirmed: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsState()
    val selectedSlot by viewModel.selectedTimeSlot.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var name by remember { mutableStateOf(activeUser?.name ?: "Patient") }
    var phone by remember { mutableStateOf(activeUser?.phone ?: "+91 98765 43210") }
    var age by remember { mutableStateOf("28") }
    var gender by remember { mutableStateOf("Male") }
    var paymentMode by remember { mutableStateOf("Pay at Pharmacy") }

    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Toolbar actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Finalize Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }

        // Summary box of consultation
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF5FF)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Healing, contentDescription = null, tint = Color(0xFF7C5DFA))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = doctor.name, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = "${doctor.specialization}  •  ${selectedSlot}", fontSize = 12.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (doctor.availabilityStatus) {
                                    "Running Late" -> Color(0xFFFEF3C7)
                                    "On Holiday" -> Color(0xFFFEF2F2)
                                    else -> Color(0xFFECFDF5)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (doctor.availabilityStatus) {
                                "Running Late" -> "⏳ Running Late" + (if (doctor.expectedStartTime.isNotEmpty()) " (${doctor.expectedStartTime})" else "")
                                "On Holiday" -> "🌴 On Holiday"
                                else -> "🟢 Available Today"
                            },
                            color = when (doctor.availabilityStatus) {
                                "Running Late" -> Color(0xFFD97706)
                                "On Holiday" -> Color(0xFFDC2626)
                                else -> Color(0xFF059669)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(text = "PATIENT INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 8.dp))

        // Form Inputs
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Patient Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF7C5DFA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF7C5DFA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Column(modifier = Modifier.weight(1.5f)) {
                Text("Gender", fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.padding(start = 2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Male", "Female").forEach { g ->
                        val isSel = gender == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFF047857) else Color(0xFFF1F5F9))
                                .clickable { gender = g }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = g, color = if (isSel) Color.White else Color(0xFF1E293B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Confirm Button
        Button(
            onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    isSubmitting = true
                    viewModel.confirmBooking(
                        patientName = name,
                        patientPhone = phone,
                        age = age.toIntOrNull() ?: 28,
                        gender = gender,
                        paymentMode = paymentMode,
                        onSuccess = {
                            isSubmitting = false
                            onConfirmed()
                        },
                        onError = { error ->
                            isSubmitting = false
                            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5DFA))
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm & Set Appointment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ============================================
// 4. BOOKING SUCCESS
// ============================================
@Composable
fun BookingSuccessScreen(
    booking: BookingEntity,
    viewModel: MainViewModel,
    onBackToBookings: () -> Unit
) {
    val doctors by viewModel.activeDoctors.collectAsState()
    val doctor = doctors.find { it.id == booking.doctorId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simulated gorgeous Tick animation / badge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = "Success",
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Booking Confirmed!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF15803D),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Show this digital slip status counter for access",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp),
                textAlign = TextAlign.Center
            )

            // High Fidelity Ticket/Slip Render
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Token Number Main Accent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "QUEUE COUNTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7C5DFA).copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Token #${booking.tokenNumber}",
                                color = Color(0xFF7C5DFA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Clinician Doctor", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(text = doctor?.name ?: "Professional Specialist", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(text = "Appointment Date", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(text = booking.dateStr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Column(modifier = Modifier.weight(0.8f)) {
                            Text(text = "Assigned Time", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(text = booking.timeStr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color(0xFFE2E8F0))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Payment Mode", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(text = booking.paymentMode, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Bill Status", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (booking.paymentStatus == "Paid") Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = booking.paymentStatus,
                                    color = if (booking.paymentStatus == "Paid") Color(0xFF15803D) else Color(0xFFD97706),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBackToBookings,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "View My Bookings Dashboard")
            }
        }
    }
}

// ============================================
// 5. MY APPOINTMENTS SCREEN
// ============================================
@Composable
fun PatientAppointmentsScreen(viewModel: MainViewModel) {
    val patientBookings by viewModel.patientBookings.collectAsState()
    val activeDoctors by viewModel.activeDoctors.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    var selectedTab by remember { mutableStateOf("Upcoming") } // "Upcoming", "History"

    // Dialog state
    var showReviewDialog by remember { mutableStateOf(false) }
    var selectedDoctorIdForReview by remember { mutableStateOf<String?>(null) }
    var selectedDoctorNameForReview by remember { mutableStateOf("") }
    var ratingState by remember { mutableStateOf(5) }
    var reviewTextState by remember { mutableStateOf("") }

    val filtered = patientBookings.filter {
        if (selectedTab == "Upcoming") it.status == "Upcoming" else it.status != "Upcoming"
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Text(
                text = "My Consultative Bookings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = titleColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Custom segment row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Upcoming", "History").forEach { tab ->
                    val isSel = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) Color(0xFF7C5DFA) else cardColor)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSel) Color.White else textMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.EventBusy,
                            contentDescription = "No appointments",
                            tint = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No clinical bookings under '$selectedTab'",
                            fontSize = 14.sp,
                            color = textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { booking ->
                        val clinician = activeDoctors.find { it.id == booking.doctorId }
                        val patientId = activeUser?.id ?: ""
                        val existingReview = allReviews.find { it.patientId == patientId && it.doctorId == booking.doctorId }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDark) Color(0xFF334155) else Color(0xFFF3EFFF))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Token #${booking.tokenNumber}",
                                            color = if (isDark) Color(0xFF10B981) else Color(0xFF7C5DFA),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Text(
                                        text = booking.dateStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = clinician?.name ?: "Qualified Specialist",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "Specialty: ${clinician?.specialization ?: "Pediatrician"}  •  Slot: ${booking.timeStr}",
                                    fontSize = 12.sp,
                                    color = textMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Status",
                                            tint = when (booking.status) {
                                                "Upcoming", "Confirmed" -> Color(0xFF10B981)
                                                "Waiting" -> Color(0xFFF59E0B)
                                                "Cancelled" -> Color(0xFFEF4444)
                                                "Completed" -> Color(0xFF3B82F6)
                                                else -> Color(0xFF10B981)
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = booking.status,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (booking.status) {
                                                "Upcoming", "Confirmed" -> Color(0xFF10B981)
                                                "Waiting" -> Color(0xFFF59E0B)
                                                "Cancelled" -> Color(0xFFEF4444)
                                                "Completed" -> Color(0xFF3B82F6)
                                                else -> Color(0xFF10B981)
                                            }
                                        )
                                    }

                                    // Actions: Cancel if upcoming, Review if completed
                                    if (booking.status == "Upcoming") {
                                        TextButton(
                                            onClick = { viewModel.updateBookingStatus(booking.id, "Cancelled") },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text("Cancel Slot", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (booking.status == "Completed") {
                                        if (existingReview != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Reviewed (${existingReview.rating} ★)",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF059669),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                TextButton(
                                                    onClick = {
                                                        selectedDoctorIdForReview = booking.doctorId
                                                        selectedDoctorNameForReview = clinician?.name ?: "Doctor"
                                                        ratingState = existingReview.rating
                                                        reviewTextState = existingReview.review
                                                        showReviewDialog = true
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Edit Review", color = Color(0xFF7C5DFA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            TextButton(
                                                onClick = {
                                                    selectedDoctorIdForReview = booking.doctorId
                                                    selectedDoctorNameForReview = clinician?.name ?: "Doctor"
                                                    ratingState = 5
                                                    reviewTextState = ""
                                                    showReviewDialog = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text("Submit Review", color = Color(0xFFD97706), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ratings Dialog
        if (showReviewDialog && selectedDoctorIdForReview != null) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showReviewDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rate & Review",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = selectedDoctorNameForReview,
                            fontSize = 14.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Stars row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            for (star in 1..5) {
                                val isSelected = star <= ratingState
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Star else Icons.Rounded.StarOutline,
                                    contentDescription = "$star Stars",
                                    tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { ratingState = star }
                                )
                            }
                        }

                        // Text Input
                        OutlinedTextField(
                            value = reviewTextState,
                            onValueChange = { reviewTextState = it },
                            placeholder = { Text("Write about your clinical consultation experience...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7C5DFA),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showReviewDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Cancel", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.submitOrUpdateReview(
                                        patientId = activeUser?.id ?: "",
                                        patientName = activeUser?.name ?: "Patient",
                                        doctorId = selectedDoctorIdForReview!!,
                                        rating = ratingState,
                                        reviewText = reviewTextState
                                    )
                                    showReviewDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5DFA)),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// NOTIFICATIONS VIEW
// ============================================
@Composable
fun PatientNotificationsScreen(viewModel: MainViewModel) {
    val notifs by viewModel.allNotifications.collectAsState()

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = "Alert Notifications",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = titleColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (notifs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No alerts found.", color = textMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifs) { note ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF334155) else Color(0xFFF3EFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Badge, contentDescription = null, tint = if (isDark) Color(0xFF10B981) else Color(0xFF7C5DFA), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = note.title, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                                Text(text = note.message, color = textMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// PATIENT PROFILE
// ============================================
@Composable
fun PatientProfileScreen(
    user: UserEntity?,
    onLogout: () -> Unit,
    viewModel: MainViewModel
) {
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color.White
    val cardColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val buttonBg = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val buttonText = if (isDark) Color.White else Color(0xFF334155)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF3EFFF)),
            contentAlignment = Alignment.Center
        ) {
            if (user?.profilePhotoUrl != null) {
                coil.compose.AsyncImage(
                    model = user.profilePhotoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = if (isDark) Color(0xFF10B981) else Color(0xFF7C5DFA), modifier = Modifier.size(54.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = user?.name ?: "Patient profile", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textColor)
        Text(text = user?.email ?: "patient@doctorline.com", fontSize = 13.sp, color = textMuted)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ProfileFieldRow(icon = Icons.Rounded.Phone, label = "Mobile Line", value = user?.phone ?: "+91 98765 43210", viewModel = viewModel)
                ProfileFieldRow(icon = Icons.Rounded.VerifiedUser, label = "Role Level", value = user?.role ?: "Patient", viewModel = viewModel)
                ProfileFieldRow(icon = Icons.Rounded.LocationOn, label = "Demographics", value = "Kolkata, India", viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "APP THEME SETTINGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textMuted,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Light", "Dark", "System Default").forEach { themeName ->
                        val isSelected = activeTheme == themeName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF7C5DFA) else buttonBg)
                                .clickable { viewModel.updateTheme(themeName) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = themeName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else buttonText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Rounded.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout & Exit Profile", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ProfileFieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    viewModel: MainViewModel
) {
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF334155))
        }
    }
}

// ============================================
// OPTIONAL LOCATION SYSTEM UTILITIES
// ============================================

fun getPharmacyCity(address: String): String {
    val addrLower = address.lowercase()
    if (addrLower.contains("balurghat")) return "Balurghat"
    if (addrLower.contains("gangarampur")) return "Gangarampur"
    if (addrLower.contains("english bazar") || addrLower.contains("englishbazar")) return "English Bazar"
    if (addrLower.contains("kaliachak")) return "Kaliachak"
    if (addrLower.contains("siliguri")) return "Siliguri"
    if (addrLower.contains("darjeeling")) return "Darjeeling"
    if (addrLower.contains("kurseong")) return "Kurseong"
    if (addrLower.contains("kolkata")) return "Kolkata"
    if (addrLower.contains("jalpaiguri")) return "Jalpaiguri"
    if (addrLower.contains("malbazar")) return "Malbazar"
    if (addrLower.contains("mumbai")) return "Mumbai East"
    if (addrLower.contains("pune")) return "Pune"
    if (addrLower.contains("bengaluru") || addrLower.contains("bangalore")) return "Bengaluru"
    if (addrLower.contains("dwarka")) return "Dwarka"
    if (addrLower.contains("new delhi") || addrLower.contains("delhi")) return "New Delhi"
    
    // Check district
    if (addrLower.contains("dakshin dinajpur")) return "Balurghat"
    if (addrLower.contains("malda")) return "Malda"
    if (addrLower.contains("darjeeling")) return "Siliguri"
    if (addrLower.contains("kolkata")) return "Kolkata"
    if (addrLower.contains("jalpaiguri")) return "Jalpaiguri"
    
    return "Kolkata"
}

fun retrieveGPSLocation(
    context: android.content.Context,
    viewModel: MainViewModel,
    onResult: (Boolean, String) -> Unit
) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
        
        if (!isGpsEnabled && !isNetworkEnabled) {
            onResult(false, "GPS and Network location services are currently disabled.")
            return
        }
        
        val provider = if (isNetworkEnabled) android.location.LocationManager.NETWORK_PROVIDER else android.location.LocationManager.GPS_PROVIDER
        
        // Use last known location for quick response
        val lastKnown = locationManager.getLastKnownLocation(provider)
        if (lastKnown != null) {
            reverseGeocode(context, lastKnown.latitude, lastKnown.longitude, viewModel, onResult)
            return
        }
        
        // Request single update fallback
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                provider,
                null,
                context.mainExecutor
            ) { location ->
                if (location != null) {
                    reverseGeocode(context, location.latitude, location.longitude, viewModel, onResult)
                } else {
                    onResult(false, "Could not acquire current GPS location. Please choose manually.")
                }
            }
        } else {
            locationManager.requestSingleUpdate(
                provider,
                object : android.location.LocationListener {
                    override fun onLocationChanged(location: android.location.Location) {
                        reverseGeocode(context, location.latitude, location.longitude, viewModel, onResult)
                    }
                    override fun onStatusChanged(p0: String?, p1: Int, p2: android.os.Bundle?) {}
                    override fun onProviderEnabled(p0: String) {}
                    override fun onProviderDisabled(p0: String) {}
                },
                null
            )
        }
    } catch (e: SecurityException) {
        onResult(false, "Location permissions have been denied. Please select your location manually.")
    } catch (e: Exception) {
        onResult(false, "Error accessing GPS: ${e.message}")
    }
}

fun reverseGeocode(
    context: android.content.Context,
    lat: Double,
    lng: Double,
    viewModel: MainViewModel,
    onResult: (Boolean, String) -> Unit
) {
    // Run Geocoder network query on background thread
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val address = addresses?.firstOrNull()
            
            if (address != null) {
                val country = address.countryName ?: "India"
                val state = address.adminArea ?: "West Bengal"
                val district = address.subAdminArea ?: address.locality ?: "Kolkata"
                val city = address.locality ?: address.subAdminArea ?: "Kolkata"
                
                val finalCountry = if (country.isNotBlank()) country else "India"
                val finalState = if (state.isNotBlank()) state else "West Bengal"
                val finalDistrict = if (district.isNotBlank()) district else "Kolkata"
                val finalCity = if (city.isNotBlank()) city else "Kolkata"
                
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    viewModel.updateLocation(finalCountry, finalState, finalDistrict, finalCity)
                    onResult(true, "Successfully located: $finalCity, $finalState")
                }
            } else {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(false, "Could not resolve address details for GPS coordinates. Please select manually.")
                }
            }
        } catch (e: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onResult(false, "Address lookup failed: ${e.message}. Please select manually.")
            }
        }
    }
}

@Composable
fun LocationSelectorDialog(
    viewModel: MainViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedState by viewModel.selectedState.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    var tempCountry by remember(selectedCountry) { mutableStateOf(selectedCountry) }
    var tempState by remember(selectedState) { mutableStateOf(selectedState) }
    var tempDistrict by remember(selectedDistrict) { mutableStateOf(selectedDistrict) }
    var tempCity by remember(selectedCity) { mutableStateOf(selectedCity) }

    val countries = listOf("India", "United States")

    val statesOfCountry = mapOf(
        "India" to listOf("West Bengal", "Maharashtra", "Delhi", "Karnataka"),
        "United States" to listOf("California", "New York", "Texas")
    )

    val districtsOfState = mapOf(
        "West Bengal" to listOf("Dakshin Dinajpur", "Malda", "Darjeeling", "Kolkata", "Jalpaiguri"),
        "Maharashtra" to listOf("Mumbai", "Pune"),
        "Delhi" to listOf("New Delhi"),
        "Karnataka" to listOf("Bangalore Urban"),
        "California" to listOf("Los Angeles County", "San Francisco County"),
        "New York" to listOf("New York County"),
        "Texas" to listOf("Travis County")
    )

    val citiesOfDistrict = mapOf(
        "Dakshin Dinajpur" to listOf("Balurghat", "Gangarampur"),
        "Malda" to listOf("Malda", "English Bazar", "Kaliachak"),
        "Darjeeling" to listOf("Siliguri", "Darjeeling", "Kurseong"),
        "Kolkata" to listOf("Kolkata"),
        "Jalpaiguri" to listOf("Jalpaiguri", "Dhupguri"),
        "Mumbai" to listOf("Mumbai East", "Mumbai West"),
        "Pune" to listOf("Pune", "Pimpri"),
        "New Delhi" to listOf("New Delhi", "Dwarka"),
        "Bangalore Urban" to listOf("Bengaluru", "Whitefield"),
        "Los Angeles County" to listOf("Los Angeles", "Santa Monica"),
        "San Francisco County" to listOf("San Francisco", "Oakland"),
        "New York County" to listOf("New York City", "Manhattan"),
        "Travis County" to listOf("Austin")
    )

    // State expansion controls for dropdown menus
    var countryExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var isLocating by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isLocating = true
            retrieveGPSLocation(context, viewModel) { success, msg ->
                isLocating = false
                if (success) {
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    onDismiss()
                } else {
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        } else {
            android.widget.Toast.makeText(context, "Location permission denied. Please select manually.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Location",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Selector",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GPS Location Selector
                Button(
                    onClick = {
                        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasFine || hasCoarse) {
                            isLocating = true
                            retrieveGPSLocation(context, viewModel) { success, msg ->
                                isLocating = false
                                if (success) {
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLocating
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = "Use Current Location",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Use Current Location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Selector Mode Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)))
                    Text(
                        text = "OR CHOOSE MANUALLY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Hierarchy Selectors
                // 1. Country Selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Country",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { countryExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tempCountry,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Country Menu",
                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        }
                        DropdownMenu(
                            expanded = countryExpanded,
                            onDismissRequest = { countryExpanded = false },
                            modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color.White)
                        ) {
                            countries.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = if (isDark) Color.White else Color(0xFF0F172A)) },
                                    onClick = {
                                        tempCountry = item
                                        tempState = statesOfCountry[item]?.firstOrNull() ?: ""
                                        tempDistrict = districtsOfState[tempState]?.firstOrNull() ?: ""
                                        tempCity = citiesOfDistrict[tempDistrict]?.firstOrNull() ?: ""
                                        countryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. State Selection
                val states = statesOfCountry[tempCountry] ?: emptyList()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "State",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { stateExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tempState,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "State Menu",
                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        }
                        DropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false },
                            modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color.White)
                        ) {
                            states.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = if (isDark) Color.White else Color(0xFF0F172A)) },
                                    onClick = {
                                        tempState = item
                                        tempDistrict = districtsOfState[item]?.firstOrNull() ?: ""
                                        tempCity = citiesOfDistrict[tempDistrict]?.firstOrNull() ?: ""
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. District Selection
                val districts = districtsOfState[tempState] ?: emptyList()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "District",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { districtExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tempDistrict,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "District Menu",
                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        }
                        DropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false },
                            modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color.White)
                        ) {
                            districts.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = if (isDark) Color.White else Color(0xFF0F172A)) },
                                    onClick = {
                                        tempDistrict = item
                                        tempCity = citiesOfDistrict[item]?.firstOrNull() ?: ""
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. City Selection
                val cities = citiesOfDistrict[tempDistrict] ?: emptyList()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "City",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { cityExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tempCity,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "City Menu",
                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        }
                        DropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false },
                            modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color.White)
                        ) {
                            cities.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = if (isDark) Color.White else Color(0xFF0F172A)) },
                                    onClick = {
                                        tempCity = item
                                        cityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Apply Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.updateLocation(tempCountry, tempState, tempDistrict, tempCity)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5DFA)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

