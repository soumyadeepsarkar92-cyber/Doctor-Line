package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel

// ============================================
// MAIN PHARMACY MODULE SCREEN (GREEN THEME)
// ============================================
@Composable
fun SuspendedPharmacyBlocker() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color(0xFFFEF2F2), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFFECACA), shape = RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = "Access Blocked",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pharmacy Status: SUSPENDED",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF991B1B)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Your SaaS subscription validity has expired. To restore normal operations and access your doctor roster, scheduling, and active client list, please renew your subscription or contact DoctorLine SaaS Administrator.",
            fontSize = 13.sp,
            color = Color(0xFF7F1D1D),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEF4444))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Contact Support: billing@doctorline.com",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PharmacyModuleScreen(
    viewModel: MainViewModel,
    logout: () -> Unit
) {
    var selectedBottomTab by remember { mutableStateOf("dashboard") } // "dashboard", "doctors", "schedules", "bookings", "subscription"

    val activeUser by viewModel.activeUser.collectAsState()
    val pharmacies by viewModel.allPharmacies.collectAsState()
    
    val myPharmacy = pharmacies.find { it.id == "phar_apollo" }
    val isSuspended = myPharmacy?.status == "Suspended"

    val primaryGreen = Color(0xFF00A86B)
    val lightGreenBackground = Color(0xFFEFFDF5)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == "dashboard",
                    onClick = { selectedBottomTab = "dashboard" },
                    icon = { Icon(Icons.Rounded.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryGreen,
                        selectedTextColor = primaryGreen,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == "doctors",
                    onClick = { selectedBottomTab = "doctors" },
                    icon = { Icon(Icons.Rounded.Healing, contentDescription = "Doctors") },
                    label = { Text("Doctors", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryGreen,
                        selectedTextColor = primaryGreen,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == "schedules",
                    onClick = { selectedBottomTab = "schedules" },
                    icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Schedule") },
                    label = { Text("Shifts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryGreen,
                        selectedTextColor = primaryGreen,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == "bookings",
                    onClick = { selectedBottomTab = "bookings" },
                    icon = { Icon(Icons.Rounded.Assignment, contentDescription = "Appointments") },
                    label = { Text("Slots", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryGreen,
                        selectedTextColor = primaryGreen,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == "subscription",
                    onClick = { selectedBottomTab = "subscription" },
                    icon = { Icon(Icons.Rounded.WorkspacePremium, contentDescription = "Premium Subscription") },
                    label = { Text("Plans", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6C5DD3), // Unique purple for plans
                        selectedTextColor = Color(0xFF6C5DD3),
                        indicatorColor = Color(0xFFF3E8FF),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSuspended && selectedBottomTab != "subscription") {
                SuspendedPharmacyBlocker()
            } else {
                when (selectedBottomTab) {
                    "dashboard" -> PharmacyHomeDashboard(viewModel = viewModel, logout = logout)
                    "doctors" -> PharmacyDoctorsScreen(viewModel = viewModel)
                    "schedules" -> PharmacyScheduleScreen(viewModel = viewModel)
                    "bookings" -> PharmacyAppointmentsListScreen(viewModel = viewModel)
                    "subscription" -> PharmacySubscriptionScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// ============================================
// 1. PHARMACY DASHBOARD SCREEN
// ============================================
@Composable
fun PharmacyHomeDashboard(
    viewModel: MainViewModel,
    logout: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsState()
    val doctors by viewModel.allDoctors.collectAsState()
    val bookings by viewModel.allBookings.collectAsState()

    val primaryGreen = Color(0xFF00A86B)
    val primaryBlue = Color(0xFF0F52BA)

    // Sub-tab selection state
    var currentSubTab by remember { mutableStateOf("Overview") } // "Overview", "Earnings"
    var showCsvExportSuccess by remember { mutableStateOf(false) }

    // Dynamic Date Calculations
    val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val currentMonthPrefix = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())

    // 1. Calculate aggregated metrics (Overview & Earnings)
    val totalEarnings = bookings.filter { it.paymentStatus == "Paid" }.sumOf { booking -> doctors.find { it.id == booking.doctorId }?.fee ?: 500.0 }
    val todayEarnings = bookings.filter { it.dateStr == todayDateStr && it.paymentStatus == "Paid" }.sumOf { booking -> doctors.find { it.id == booking.doctorId }?.fee ?: 500.0 }
    val monthlyEarnings = bookings.filter { it.dateStr.startsWith(currentMonthPrefix) && it.paymentStatus == "Paid" }.sumOf { booking -> doctors.find { it.id == booking.doctorId }?.fee ?: 500.0 }
    
    val todayAppointments = bookings.filter { it.dateStr == todayDateStr }.size
    val totalDoctorsCount = doctors.size
    val availableSlotsCount = doctors.sumOf { it.slotsJson.split(",").size }

    val totalApptsCount = bookings.size
    val completedApptsCount = bookings.count { it.status == "Completed" }
    val cancelledApptsCount = bookings.count { it.status == "Cancelled" }

    // 2. Doctor ranking & stats leaderboard
    val doctorStats = doctors.map { doc ->
        val docBookings = bookings.filter { it.doctorId == doc.id }
        val completedCount = docBookings.count { it.status == "Completed" }
        val revenue = docBookings.filter { it.paymentStatus == "Paid" }.sumOf { doc.fee }
        Triple(doc, completedCount, revenue)
    }.sortedByDescending { it.third } // Top performing doctors first

    // 3. Dynamic Last 6 Months Earnings Calculation for Chart
    val calendar = java.util.Calendar.getInstance()
    val sdfMonthLabel = java.text.SimpleDateFormat("MMM", java.util.Locale.US)
    val sdfYearMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
    val last6MonthsList = (0..5).map { i ->
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, -i) }
        sdfMonthLabel.format(cal.time) to sdfYearMonth.format(cal.time)
    }.reversed()
    val last6MonthsEarnings = last6MonthsList.map { (_, prefix) ->
        bookings.filter { it.dateStr.startsWith(prefix) && it.paymentStatus == "Paid" }.sumOf { b -> doctors.find { it.id == b.doctorId }?.fee ?: 500.0 }
    }

    // 4. Dynamic Last 7 Days Appointment Volume trend for Chart
    val sdfDayLabel = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
    val sdfDayValue = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val last7DaysList = (0..6).map { i ->
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -i) }
        sdfDayLabel.format(cal.time) to sdfDayValue.format(cal.time)
    }.reversed()
    val last7DaysCounts = last7DaysList.map { (_, dateStr) ->
        bookings.count { it.dateStr == dateStr }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Logout Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Apollo Pharmacy Hub",
                        fontSize = 13.sp,
                        color = primaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Good Day, Operator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                }

                IconButton(
                    onClick = logout,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2))
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = "Logout", tint = Color(0xFFEF4444))
                }
            }
        }

        // Segmented tab row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Overview", "Realtime Earnings").forEach { tab ->
                    val isSel = currentSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) Color.White else Color.Transparent)
                            .clickable { currentSubTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tab == "Overview") Icons.Rounded.Dashboard else Icons.Rounded.Payments,
                                contentDescription = null,
                                tint = if (isSel) primaryBlue else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab,
                                color = if (isSel) Color(0xFF0F172A) else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        if (currentSubTab == "Overview") {
            // Aggregate Grid Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardMetricCard(
                            title = "Today's Appts",
                            value = todayAppointments.toString(),
                            icon = Icons.Rounded.EventAvailable,
                            color = primaryGreen,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Total Earnings",
                            value = "₹${totalEarnings.toInt()}",
                            icon = Icons.Rounded.Payments,
                            color = Color(0xFF0F52BA),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardMetricCard(
                            title = "Clinic Clinicians",
                            value = totalDoctorsCount.toString(),
                            icon = Icons.Rounded.Group,
                            color = Color(0xFFD97706),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Open Shift Slots",
                            value = availableSlotsCount.toString(),
                            icon = Icons.Rounded.Timer,
                            color = Color(0xFF5D3FD3),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Header for Recent Bookings list
            item {
                Text(
                    text = "Recent Booking Influx",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            // Bookings items
            if (bookings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No patient appointments booked today.", color = Color(0xFF64748B))
                        }
                    }
                }
            } else {
                items(bookings.take(5)) { booking ->
                    val doctor = doctors.find { it.id == booking.doctorId }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Token #${booking.tokenNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = primaryGreen,
                                    fontSize = 13.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (booking.status) {
                                                "Upcoming" -> Color(0xFFEFF6FF)
                                                "Completed" -> Color(0xFFECFDF5)
                                                else -> Color(0xFFFEF2F2)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = booking.status,
                                        color = when (booking.status) {
                                            "Upcoming" -> Color(0xFF2563EB)
                                            "Completed" -> Color(0xFF059669)
                                            else -> Color(0xFFDC2626)
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = booking.patientName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Doctor: ${doctor?.name ?: "Dr. Specialist"}  •  Time: ${booking.timeStr}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // REALTIME FINANCIAL EARNINGS DASHBOARD SUBTAB
            
            // CSV Export Notification Banner
            if (showCsvExportSuccess) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCsvExportSuccess = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "CSV Report Exported!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                Text(text = "Saved earnings file with booking metadata and doctor commission sheets.", fontSize = 12.sp, color = Color(0xFF047857))
                            }
                            IconButton(onClick = { showCsvExportSuccess = false }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color(0xFF047857))
                            }
                        }
                    }
                }
            }

            // High Fidelity Realtime Metrics List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardMetricCard(
                            title = "Today's Revenue",
                            value = "₹${todayEarnings.toInt()}",
                            icon = Icons.Rounded.TrendingUp,
                            color = primaryGreen,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Monthly Revenue",
                            value = "₹${monthlyEarnings.toInt()}",
                            icon = Icons.Rounded.Payments,
                            color = primaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Bookings", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(totalApptsCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Completed", fontSize = 11.sp, color = Color(0xFF00A86B), fontWeight = FontWeight.Bold)
                                Text(completedApptsCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF00A86B), modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cancelled", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                Text(cancelledApptsCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444), modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            // Export CSV Report Action Button
            item {
                Button(
                    onClick = {
                        showCsvExportSuccess = true
                        viewModel.logAction("Export Earnings CSV", "Pharmacy Operator exported the realtime CSV report ($todayDateStr)")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.CloudDownload, contentDescription = "CSV", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Financial CSV Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Monthly Revenue Chart (Using custom Canvas)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monthly Revenue Trend (Last 6 Months)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Dynamic earnings update based on paid clinical bookings",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Draw Chart Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sizeWidth = size.width
                                val sizeHeight = size.height
                                val maxVal = last6MonthsEarnings.maxOrNull()?.coerceAtLeast(1000.0) ?: 1000.0
                                
                                val points = last6MonthsEarnings.mapIndexed { idx, value ->
                                    val x = (sizeWidth / 5) * idx
                                    val y = sizeHeight - (sizeHeight * 0.8f * (value / maxVal).toFloat()) - 10f
                                    Offset(x, y)
                                }

                                val path = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }

                                // Draw Path curve line
                                drawPath(
                                    path = path,
                                    color = Color(0xFF0F52BA),
                                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                                )

                                // Gradient fill shading
                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(sizeWidth, sizeHeight)
                                    lineTo(0f, sizeHeight)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF0F52BA).copy(alpha = 0.25f), Color.White.copy(alpha = 0f))
                                    )
                                )

                                // Circles
                                points.forEach { pt ->
                                    drawCircle(
                                        color = Color(0xFF0F52BA),
                                        radius = 8f,
                                        center = pt
                                    )
                                }
                            }
                        }

                        // Labels Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            last6MonthsList.forEach { (label, _) ->
                                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }

            // Appointment Volume Trend Chart (Canvas line trend)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Appointment Influx Volume (Last 7 Days)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Cumulative count of scheduled doctor consultations",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Draw Chart Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sizeWidth = size.width
                                val sizeHeight = size.height
                                val maxVal = last7DaysCounts.maxOrNull()?.coerceAtLeast(5) ?: 5
                                
                                val points = last7DaysCounts.mapIndexed { idx, value ->
                                    val x = (sizeWidth / 6) * idx
                                    val y = sizeHeight - (sizeHeight * 0.75f * (value.toFloat() / maxVal.toFloat())) - 10f
                                    Offset(x, y)
                                }

                                val path = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = Color(0xFF00A86B),
                                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                                )

                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(sizeWidth, sizeHeight)
                                    lineTo(0f, sizeHeight)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF00A86B).copy(alpha = 0.2f), Color.White.copy(alpha = 0f))
                                    )
                                )

                                points.forEach { pt ->
                                    drawCircle(
                                        color = Color(0xFF00A86B),
                                        radius = 7f,
                                        center = pt
                                    )
                                }
                            }
                        }

                        // Labels Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            last7DaysList.forEach { (label, _) ->
                                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }

            // Top Performing Doctors & Revenue by Doctor Leaders Section
            item {
                Text(
                    text = "Doctor Revenue & Performance Leaderboard",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            if (doctorStats.isEmpty()) {
                item {
                    Text("No doctors registered yet.", color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                }
            } else {
                items(doctorStats) { (doc, completed, revenue) ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEEF5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = doc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(text = "${doc.specialization}  •  $completed Consultations", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "₹${revenue.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = primaryBlue)
                                Text(text = "Total Revenue", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A86B))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(text = title, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ============================================
// 2. DOCTORS LIST & ADD SCREEN
// ============================================
@Composable
fun PharmacyDoctorsScreen(viewModel: MainViewModel) {
    val doctors by viewModel.allDoctors.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }

    // Inputs inside Form
    var docName by remember { mutableStateOf("") }
    var docSpec by remember { mutableStateOf("General Physician") }
    var docExp by remember { mutableStateOf("") }
    var docFee by remember { mutableStateOf("") }
    var docSlots by remember { mutableStateOf("09:00 AM, 11:30 AM, 03:00 PM, 05:00 PM") }

    val primaryGreen = Color(0xFF00A86B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pharmacy Clinicians",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )

                Button(
                    onClick = { showAddForm = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Doctor", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // List of doctors
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(doctors) { doctor ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(primaryGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(32.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = doctor.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                Text(text = doctor.specialization, color = primaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Exp: ${doctor.experience} yrs  •  Fee: ₹${doctor.fee}", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp))
                            }

                            // Enable Toggle Switch
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (doctor.isEnabled) "ACTIVE" else "DISABLED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (doctor.isEnabled) primaryGreen else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Switch(
                                    checked = doctor.isEnabled,
                                    onCheckedChange = { viewModel.toggleDoctorEnabled(doctor.id, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = primaryGreen, checkedTrackColor = primaryGreen.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Simple Form Slide over / overlay Card
        AnimatedVisibility(
            visible = showAddForm,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Doctor to Roster", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))
                        IconButton(onClick = { showAddForm = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    OutlinedTextField(
                        value = docName,
                        onValueChange = { docName = it },
                        label = { Text("Dr. Full Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = docSpec,
                        onValueChange = { docSpec = it },
                        label = { Text("Specialization Specialty") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        OutlinedTextField(
                            value = docExp,
                            onValueChange = { docExp = it },
                            label = { Text("Experience (Yrs)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = docFee,
                            onValueChange = { docFee = it },
                            label = { Text("Consultation Fee") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    OutlinedTextField(
                        value = docSlots,
                        onValueChange = { docSlots = it },
                        label = { Text("Booking Time Slots (Comma lists)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )

                    Button(
                        onClick = {
                            if (docName.isNotBlank() && docExp.isNotBlank() && docFee.isNotBlank()) {
                                viewModel.addDoctor(
                                    name = if (docName.startsWith("Dr. ")) docName else "Dr. $docName",
                                    specialization = docSpec,
                                    experience = docExp.toIntOrNull() ?: 5,
                                    fee = docFee.toDoubleOrNull() ?: 500.0,
                                    slots = docSlots.split(",").map { it.trim() }
                                )
                                showAddForm = false
                                docName = ""
                                docExp = ""
                                docFee = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Register Clinician profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================
// 3. SCHEDULE MANAGEMENT SCREEN
// ============================================
@Composable
fun PharmacyScheduleScreen(viewModel: MainViewModel) {
    val doctors by viewModel.allDoctors.collectAsState()

    var selectedDoctor by remember { mutableStateOf<DoctorEntity?>(null) }
    var selectedDate by remember { mutableStateOf("2026-06-21") } // Simulating nice default
    var fromTime by remember { mutableStateOf("09:00 AM") }
    var toTime by remember { mutableStateOf("01:00 PM") }
    var maxPatients by remember { mutableStateOf("15") }
    var openBooking by remember { mutableStateOf(true) }

    val primaryGreen = Color(0xFF00A86B)

    LaunchedEffect(doctors) {
        if (doctors.isNotEmpty() && selectedDoctor == null) {
            selectedDoctor = doctors.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Clinic Shift Schedules",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Dropdown Selector (Simulated dropdown panel for ease of use)
        Text("SELECT DOCTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9))
                .clickable { /* Select target from list */ }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = primaryGreen)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = selectedDoctor?.name ?: "No Doctors Set", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text(text = selectedDoctor?.specialization ?: "Choose Doctor Profile", fontSize = 11.sp, color = Color(0xFF64748B))
            }
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
        }

        // Quick Doctor pills to switch selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            doctors.forEach { doc ->
                val isSel = selectedDoctor?.id == doc.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) primaryGreen else Color(0xFFF8FAFC))
                        .border(1.dp, if (isSel) Color.Transparent else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .clickable { selectedDoctor = doc }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = doc.name.split(" ").lastOrNull() ?: "",
                        color = if (isSel) Color.White else Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fields
        Text("SHIFT PARAMETERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 6.dp))

        OutlinedTextField(
            value = selectedDate,
            onValueChange = { selectedDate = it },
            label = { Text("Consultation Date (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = primaryGreen) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            OutlinedTextField(
                value = fromTime,
                onValueChange = { fromTime = it },
                label = { Text("From Time") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = toTime,
                onValueChange = { toTime = it },
                label = { Text("To Time") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        OutlinedTextField(
            value = maxPatients,
            onValueChange = { maxPatients = it },
            label = { Text("Maximum Patient Capacity Count") },
            leadingIcon = { Icon(Icons.Rounded.Group, contentDescription = null, tint = primaryGreen) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Toggle slot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEFFDF5))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Open shift for reservation", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                Text("Allows patients to book slots reactively.", fontSize = 11.sp, color = Color(0xFF047857))
            }
            Switch(
                checked = openBooking,
                onCheckedChange = { openBooking = it },
                colors = SwitchDefaults.colors(checkedThumbColor = primaryGreen)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val doctor = selectedDoctor ?: return@Button
                    viewModel.saveSchedule(
                        doctorId = doctor.id,
                        date = selectedDate,
                        fromTime = fromTime,
                        toTime = toTime,
                        maxPatients = maxPatients.toIntOrNull() ?: 15,
                        openBooking = openBooking
                    )
                },
                modifier = Modifier.weight(1.5f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("Save Shift", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    // Mark Holiday action
                    selectedDoctor?.let {
                        viewModel.saveSchedule(
                            doctorId = it.id,
                            date = selectedDate,
                            fromTime = fromTime,
                            toTime = toTime,
                            maxPatients = 0,
                            openBooking = false
                        )
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Text("Mark Holiday", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ============================================
// 4. APPOINTMENTS LIST SCREEN
// ============================================
@Composable
fun PharmacyAppointmentsListScreen(viewModel: MainViewModel) {
    val bookings by viewModel.allBookings.collectAsState()
    val doctors by viewModel.allDoctors.collectAsState()

    var activeTab by remember { mutableStateOf("All") } // "All", "Confirmed", "Cancelled"

    val filteredBookings = bookings.filter {
        when (activeTab) {
            "Confirmed" -> it.status == "Upcoming" || it.status == "Completed"
            "Cancelled" -> it.status == "Cancelled"
            else -> true
        }
    }

    val primaryGreen = Color(0xFF00A86B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text(
            text = "Clinic Booked Slots",
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
            listOf("All", "Confirmed", "Cancelled").forEach { tab ->
                val isSel = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) primaryGreen else Color.White)
                        .clickable { activeTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSel) Color.White else Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (filteredBookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching slots in roster.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBookings) { booking ->
                    val doctor = doctors.find { it.id == booking.doctorId }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(primaryGreen.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Token #${booking.tokenNumber}", color = primaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (booking.paymentStatus == "Paid") Color(0xFFECFDF5) else Color(0xFFFFFBEB))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(booking.paymentStatus, color = if (booking.paymentStatus == "Paid") Color(0xFF047857) else Color(0xFFB45309), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(text = booking.dateStr, fontSize = 12.sp, color = Color(0xFF64748B))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(text = "Patient: ${booking.patientName} (${booking.gender}, ${booking.age})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Clinician: ${doctor?.name ?: "Unknown"}  • Slot: ${booking.timeStr}", fontSize = 12.sp, color = Color(0xFF64748B))

                            if (booking.status == "Upcoming") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "Completed") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Mark Completed", color = Color(0xFF2563EB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "Cancelled") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Cancel Slot", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
// 5. SUBSCRIPTION SCREEN - PURPLE GRADIENT (PREMIUM VIEWS)
// ============================================
@Composable
fun PharmacySubscriptionScreen(viewModel: MainViewModel) {
    val subState by viewModel.currentPharmacySubscription.collectAsState()

    var autoRenewal by remember { mutableStateOf(subState?.autoRenewal ?: true) }

    LaunchedEffect(subState) {
        subState?.let {
            autoRenewal = it.autoRenewal
        }
    }

    // Gorgeous Luxury Purple Gradient brush
    val purpleGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6C5DD3),
            Color(0xFF4B3EC4)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Pharmacy Subscriptions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Luxury Purple Gradient Card
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(purpleGradient)
                    .padding(24.dp)
            ) {
                // Background Vector Accents
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.width * 0.35f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.2f)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT ACTIVE PLAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRO ACTIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "₹299 / Month",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Divider(color = Color.White.copy(alpha = 0.15f))

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("License Validity ID", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(text = subState?.validityDate ?: "2026-07-20", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Billed Platform", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(text = "Razorpay Integration", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("MEMBERSHIP BENEFITS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 12.dp))

        BenefitRow(text = "Register unlimited practicing doctors on portal")
        BenefitRow(text = "Unlocks live slot booking with dynamic counter queues")
        BenefitRow(text = "Sends notification SMS alerts securely")

        Spacer(modifier = Modifier.height(32.dp))

        Divider(color = Color(0xFFE2E8F0))

        Spacer(modifier = Modifier.height(20.dp))

        // Auto Renewal Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Automatic Renewal Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text("Deducted from link card every month", fontSize = 11.sp, color = Color(0xFF64748B))
            }
            Switch(
                checked = autoRenewal,
                onCheckedChange = {
                    autoRenewal = it
                    subState?.let { sub ->
                        viewModel.toggleSubscriptionRenewal(sub.id, it)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF6C5DD3),
                    checkedTrackColor = Color(0xFF6C5DD3).copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Razorpay simulated payment gateway */ },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5DD3)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Renew Subscription Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF00A86B),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, color = Color(0xFF475569), fontSize = 13.sp)
    }
}
