package com.example.ui.screens

import androidx.compose.animation.*
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == "home" && currentSubScreen == "home",
                    onClick = {
                        selectedBottomTab = "home"
                        currentSubScreen = "home"
                    },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F52BA),
                        selectedTextColor = Color(0xFF0F52BA),
                        indicatorColor = Color(0xFFEEF5FF),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == "appointments",
                    onClick = {
                        selectedBottomTab = "appointments"
                        currentSubScreen = "home" // reset to clear detail stacks
                    },
                    icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Appointments") },
                    label = { Text("Bookings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F52BA),
                        selectedTextColor = Color(0xFF0F52BA),
                        indicatorColor = Color(0xFFEEF5FF),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == "notifications",
                    onClick = {
                        selectedBottomTab = "notifications"
                        currentSubScreen = "home"
                    },
                    icon = { Icon(Icons.Rounded.Notifications, contentDescription = "Notifications") },
                    label = { Text("Notifs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F52BA),
                        selectedTextColor = Color(0xFF0F52BA),
                        indicatorColor = Color(0xFFEEF5FF),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == "profile",
                    onClick = {
                        selectedBottomTab = "profile"
                        currentSubScreen = "home"
                    },
                    icon = { Icon(Icons.Rounded.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F52BA),
                        selectedTextColor = Color(0xFF0F52BA),
                        indicatorColor = Color(0xFFEEF5FF),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
            }
        },
        containerColor = Color(0xFFFFFFFF)
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf("All") }

    val specialties = listOf("All", "Cardiologist", "Pediatrician", "Dermatologist", "General Physician")

    val filteredDoctors = doctors.filter { doctor ->
        val matchesSpecialty = selectedSpecialty == "All" || doctor.specialization == selectedSpecialty
        val matchesSearch = doctor.name.contains(searchQuery, ignoreCase = true) ||
                doctor.specialization.contains(searchQuery, ignoreCase = true)
        matchesSpecialty && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF0F52BA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kolkata, India",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
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
                
                // Notification bell with red feedback indicator
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
                        tint = Color(0xFF475569)
                    )
                }
            }
        }

        // Search inputs
        item {
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
                    focusedBorderColor = Color(0xFF0F52BA),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )
        }

        // Promo Banner card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F52BA))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Decorative Canvas Art
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
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "Book Doctor Appointment\nEasily & Quickly",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ Real-time Token System",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF0F52BA) else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
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
                DoctorCard(
                    doctor = doctor,
                    pharmacyName = matchingPharmacy?.name ?: "Apollo Pharmacy",
                    isSuspended = isSuspended,
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
                    tint = if (isSuspended) Color(0xFFB91C1C) else Color(0xFF0F52BA),
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
                        overflow = TextOverflow.Ellipsis
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
                    }
                }

                Text(
                    text = doctor.specialization,
                    fontSize = 13.sp,
                    color = if (isSuspended) Color(0xFFEF4444) else Color(0xFF0F52BA),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

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
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSuspended) Color(0xFFFCA5A5) else Color(0xFFEEF5FF))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isSuspended) "Unavailable" else "Book Appointment",
                            color = if (isSuspended) Color(0xFF7F1D1D) else Color(0xFF0F52BA),
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
                                colors = listOf(Color(0xFFEEF5FF), Color(0xFFCFE2FE))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (doctor.bannerName.contains("female")) Icons.Rounded.Face else Icons.Rounded.Person,
                        contentDescription = doctor.name,
                        tint = Color(0xFF0F52BA),
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
                        color = Color(0xFF0F52BA),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${doctor.rating}  •  ${doctor.experience} Yrs Experience",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
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
                    color = Color(0xFF0F52BA),
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
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isDateSel) Color(0xFF0F52BA) else Color(0xFFE2E8F0)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDateSel) Color(0xFF0F52BA) else Color(0xFFF8FAFC)
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF0F52BA) else Color(0xFFF1F5F9))
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
                                color = Color(0xFF0F52BA),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToBooking,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F52BA)),
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
                    Icon(Icons.Rounded.Healing, contentDescription = null, tint = Color(0xFF0F52BA))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = doctor.name, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = "${doctor.specialization}  •  ${selectedSlot}", fontSize = 12.sp, color = Color(0xFF475569))
                }
            }
        }

        Text(text = "PATIENT INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 8.dp))

        // Form Inputs
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Patient Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0F52BA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0F52BA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
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
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F52BA))
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F52BA).copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Token #${booking.tokenNumber}",
                                color = Color(0xFF0F52BA),
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

    var selectedTab by remember { mutableStateOf("Upcoming") } // "Upcoming", "Completed"

    val filtered = patientBookings.filter {
        if (selectedTab == "Upcoming") it.status == "Upcoming" else it.status != "Upcoming"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text(
            text = "My Consultative Bookings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Color(0xFF0F52BA) else Color.White)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSel) Color.White else Color(0xFF64748B),
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
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No clinical bookings under '$selectedTab'",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
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
                    
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEEF5FF))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Token #${booking.tokenNumber}",
                                        color = Color(0xFF0F52BA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Text(
                                    text = booking.dateStr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = clinician?.name ?: "Qualified Specialist",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Specialty: ${clinician?.specialization ?: "Pediatrician"}  •  Slot: ${booking.timeStr}",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Divider(color = Color(0xFFF1F5F9))

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
                                        tint = if (booking.status == "Upcoming") Color(0xFF0F52BA) else Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = booking.status,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (booking.status == "Upcoming") Color(0xFF0F52BA) else Color(0xFF10B981)
                                    )
                                }
                                
                                // Ability to cancel if upcoming
                                if (booking.status == "Upcoming") {
                                    TextButton(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "Cancelled") },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Cancel Slot", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

// ============================================
// NOTIFICATIONS VIEW
// ============================================
@Composable
fun PatientNotificationsScreen(viewModel: MainViewModel) {
    val notifs by viewModel.allNotifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text(
            text = "Alert Notifications",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (notifs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No alerts found.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifs) { note ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Badge, contentDescription = null, tint = Color(0xFF0F52BA), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = note.title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
                                Text(text = note.message, color = Color(0xFF475569), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF5FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF0F52BA), modifier = Modifier.size(54.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = user?.name ?: "Patient profile", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
        Text(text = user?.email ?: "patient@doctorline.com", fontSize = 13.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ProfileFieldRow(icon = Icons.Rounded.Phone, label = "Mobile Line", value = user?.phone ?: "+91 98765 43210")
                ProfileFieldRow(icon = Icons.Rounded.VerifiedUser, label = "Role Level", value = user?.role ?: "Patient")
                ProfileFieldRow(icon = Icons.Rounded.LocationOn, label = "Demographics", value = "Kolkata, India")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "APP THEME SETTINGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        val activeTheme by viewModel.appTheme.collectAsState()
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF0F52BA) else Color(0xFFE2E8F0))
                                .clickable { viewModel.updateTheme(themeName) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = themeName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF334155)
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
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        }
    }
}
