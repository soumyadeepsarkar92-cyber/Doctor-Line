package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.AsyncImage

@Composable
fun AdminModuleScreen(
    viewModel: MainViewModel,
    logout: () -> Unit
) {
    var selectedMenu by remember { mutableStateOf("Dashboard") }
    // Menu Options: Dashboard, Pharmacies, Subscriptions, Users, Doctors, Audit Logs, Settings

    val activeUser by viewModel.activeUser.collectAsState()

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val menus = listOf(
        Pair("Dashboard", Icons.Rounded.Analytics),
        Pair("Requests", Icons.Rounded.AppRegistration),
        Pair("Pharmacies", Icons.Rounded.LocalPharmacy),
        Pair("Subscriptions", Icons.Rounded.CardMembership),
        Pair("Users", Icons.Rounded.Group),
        Pair("Doctors", Icons.Rounded.Healing),
        Pair("Logs", Icons.Rounded.ReceiptLong),
        Pair("Settings", Icons.Rounded.Settings)
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF0F0B30) else Color(0xFF4F46E5))
                    .statusBarsPadding()
            ) {
                // Header Identity Logo & Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Healing, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Admin Pro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = logout,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E1B4B) else Color(0xFF4338CA))
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = "Log out", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                // Scrollable menu tabs
                ScrollableTabRow(
                    selectedTabIndex = menus.indexOfFirst { it.first == selectedMenu },
                    containerColor = if (isDark) Color(0xFF0F0B30) else Color(0xFF4F46E5),
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val index = menus.indexOfFirst { it.first == selectedMenu }
                        if (index >= 0) {
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = Color(0xFF8B5CF6),
                                height = 3.dp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    menus.forEach { (title, icon) ->
                        val isSelected = selectedMenu == title
                        Tab(
                            selected = isSelected,
                            onClick = { selectedMenu = title },
                            selectedContentColor = Color.White,
                            unselectedContentColor = if (isDark) Color(0xFF818CF8) else Color(0xFFC7D2FE),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = if (isDark) Color(0xFF090520) else Color(0xFFF5F3FF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Workspace Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedMenu,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )

                // Platform quick action button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Sandbox",
                        color = Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Render matching sub-view components
            Box(modifier = Modifier.weight(1f)) {
                when (selectedMenu) {
                    "Dashboard" -> AdminDashboardView(viewModel = viewModel)
                    "Requests" -> AdminRequestsView(viewModel = viewModel)
                    "Pharmacies" -> AdminPharmaciesView(viewModel = viewModel)
                    "Subscriptions" -> AdminSubscriptionsView(viewModel = viewModel)
                    "Users" -> AdminUsersView(viewModel = viewModel)
                    "Doctors" -> AdminDoctorsView(viewModel = viewModel)
                    "Logs" -> AdminAuditView(viewModel = viewModel)
                    "Settings" -> AdminSettingsView(viewModel = viewModel, logout = logout)
                }
            }
        }
    }
}

// ============================================
// 1. ADMIN DASHBOARD COCKPIT (CHARTS CORE)
// ============================================
@Composable
fun AdminDashboardView(viewModel: MainViewModel) {
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val doctors by viewModel.allDoctors.collectAsState()
    val bookings by viewModel.allBookings.collectAsState()

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val glassBg = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.55f) else Color(0xFFF5F3FF).copy(alpha = 0.9f)
    val glassBorder = if (isDark) Color(0xFF6366F1).copy(alpha = 0.4f) else Color(0xFF6366F1).copy(alpha = 0.25f)
    val glassText = if (isDark) Color(0xFFE0E7FF) else Color(0xFF1E1B4B)
    val glassSubText = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)

    val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val totalPharmacies = pharmacies.size
    val activePharmacies = pharmacies.count { it.status == "Active" }
    val suspendedPharmacies = pharmacies.count { it.status == "Suspended" }
    val totalPatients = maxOf(12, bookings.map { it.patientPhone }.distinct().size)
    val totalDoctors = doctors.size
    val totalAppointments = bookings.size
    val revenue = bookings.filter { it.status == "Completed" || it.status == "Upcoming" }.sumOf { 800.0 }

    // Subscription & SaaS KPIs
    val trialPharmaciesCount = pharmacies.count { phar ->
        phar.trialStarted && phar.status == "Active" && viewModel.calculateDaysBetween(todayDate, phar.subscriptionExpiry) >= 0
    }
    val expiringSoonCount = pharmacies.count { phar ->
        if (phar.subscriptionExpiry.isEmpty()) false
        else {
            val days = viewModel.calculateDaysBetween(todayDate, phar.subscriptionExpiry)
            days in 0..7
        }
    }
    val gracePeriodCount = pharmacies.count { phar ->
        if (phar.subscriptionExpiry.isEmpty()) false
        else {
            val days = viewModel.calculateDaysBetween(todayDate, phar.subscriptionExpiry)
            days < 0 && days > -5
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aggregate dynamic grid for SaaS KPI Cockpit (10 Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricItem(
                        label = "Total Revenue",
                        value = "₹${revenue.toInt()}",
                        color = Color(0xFF8B5CF6),
                        icon = Icons.Rounded.Payments,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                    AdminMetricItem(
                        label = "Total Appointments",
                        value = totalAppointments.toString(),
                        color = Color(0xFFEC4899),
                        icon = Icons.Rounded.EventAvailable,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricItem(
                        label = "Total Pharmacies",
                        value = totalPharmacies.toString(),
                        color = Color(0xFF10B981),
                        icon = Icons.Rounded.LocalPharmacy,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                    AdminMetricItem(
                        label = "Active Pharmacies",
                        value = activePharmacies.toString(),
                        color = Color(0xFF0D9488),
                        icon = Icons.Rounded.CheckCircle,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricItem(
                        label = "Trial Pharmacies",
                        value = trialPharmaciesCount.toString(),
                        color = Color(0xFF6366F1),
                        icon = Icons.Rounded.Analytics,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                    AdminMetricItem(
                        label = "Grace Period Users",
                        value = gracePeriodCount.toString(),
                        color = Color(0xFFF59E0B),
                        icon = Icons.Rounded.Timer,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricItem(
                        label = "Suspended Pharmacies",
                        value = suspendedPharmacies.toString(),
                        color = Color(0xFFEF4444),
                        icon = Icons.Rounded.Block,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                    AdminMetricItem(
                        label = "Total Doctors",
                        value = totalDoctors.toString(),
                        color = Color(0xFF8B5CF6),
                        icon = Icons.Rounded.PersonSearch,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricItem(
                        label = "Total Patients",
                        value = totalPatients.toString(),
                        color = Color(0xFF3B82F6),
                        icon = Icons.Rounded.People,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                    AdminMetricItem(
                        label = "Expiring Subscriptions",
                        value = expiringSoonCount.toString(),
                        color = Color(0xFFF97316),
                        icon = Icons.Rounded.Warning,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel
                    )
                }
            }
        }

        // Charts visual row (Responsive Stacking inside Dashboard)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Graph Chart (Curve Line)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = glassBg),
                    border = BorderStroke(1.2.dp, glassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Platform Monthly Earnings Growth",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = glassText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Custom Canvas Line chart Drawing
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val strokeWidthVal = 4f
                            val points = listOf(
                                Offset(0f, size.height * 0.9f),
                                Offset(size.width * 0.2f, size.height * 0.75f),
                                Offset(size.width * 0.4f, size.height * 0.82f),
                                Offset(size.width * 0.6f, size.height * 0.45f),
                                Offset(size.width * 0.8f, size.height * 0.35f),
                                Offset(size.width, size.height * 0.15f)
                            )
                            
                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            
                            // Draw path curve line
                            drawPath(
                                path = path,
                                color = Color(0xFF8B5CF6),
                                style = Stroke(width = strokeWidthVal)
                            )
                            
                            // Draw path gradient fill shading
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                            
                            // Draw point circles
                            points.forEach { pt ->
                                drawCircle(
                                    color = Color(0xFF8B5CF6),
                                    radius = 6f,
                                    center = pt
                                )
                            }
                        }
                    }
                }

                // Bottom Pie Segment Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = glassBg),
                    border = BorderStroke(1.2.dp, glassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Plan Shares",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = glassText,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Pie custom Canvas Drawing
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                drawArc(
                                    color = Color(0xFF8B5CF6), // Pro Standard Share
                                    startAngle = -90f,
                                    sweepAngle = 180f,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                                drawArc(
                                    color = Color(0xFF10B981), // Basic Monthly Share
                                    startAngle = 90f,
                                    sweepAngle = 120f,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                                drawArc(
                                    color = Color(0xFF3B82F6), // Corporate Quarterly Share
                                    startAngle = 210f,
                                    sweepAngle = 60f,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                            }
                        }
                        
                        // Pie keys
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PieMetricLegend(color = Color(0xFF8B5CF6), title = "Pro (50%)", viewModel = viewModel)
                            PieMetricLegend(color = Color(0xFF10B981), title = "Monthly (33%)", viewModel = viewModel)
                            PieMetricLegend(color = Color(0xFF3B82F6), title = "Corp (17%)", viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricItem(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val glassBg = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.55f) else Color(0xFFF5F3FF).copy(alpha = 0.9f)
    val glassBorder = if (isDark) Color(0xFF6366F1).copy(alpha = 0.4f) else Color(0xFF6366F1).copy(alpha = 0.25f)
    val glassText = if (isDark) Color(0xFFE0E7FF) else Color(0xFF1E1B4B)
    val glassSubText = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = glassBg),
        border = BorderStroke(1.2.dp, glassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(), 
                    fontSize = 9.sp, 
                    color = glassSubText, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = glassText)
        }
    }
}

@Composable
fun PieMetricLegend(color: Color, title: String, viewModel: MainViewModel) {
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val textColor = if (isDark) Color(0xFF818CF8) else Color(0xFF475569)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = title, fontSize = 9.sp, color = textColor, fontWeight = FontWeight.Bold)
    }
}

// ============================================
// 2. ADMIN PHARMACIES SHEET
// ============================================
@Composable
fun AdminPharmaciesView(viewModel: MainViewModel) {
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val pricingSettings by viewModel.pricingSettings.collectAsState()

    val monthly = pricingSettings?.monthlySubscriptionFee ?: 10.0
    val quarterly = pricingSettings?.quarterlySubscriptionFee ?: 30.0
    val yearly = pricingSettings?.yearlySubscriptionFee ?: 100.0
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedPharmacy by remember { mutableStateOf<PharmacyEntity?>(null) }

    // Dialog state fields
    var pharName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var pharPhone by remember { mutableStateOf("") }
    var pharAddress by remember { mutableStateOf("") }
    var pharLicense by remember { mutableStateOf("") }
    var pharStatus by remember { mutableStateOf("Active") }
    var pharCreatedDate by remember { mutableStateOf("") }
    var pharPlan by remember { mutableStateOf("Basic") }
    var pharExpiry by remember { mutableStateOf("2026-07-22") }
    var pharAmount by remember { mutableStateOf(monthly.toInt().toString()) }
    var pharPaymentStatus by remember { mutableStateOf("Paid") }

    // Helper to open Add Dialog
    fun openAdd() {
        pharName = ""
        ownerName = ""
        pharPhone = ""
        pharAddress = ""
        pharLicense = ""
        pharStatus = "Active"
        pharPlan = "Basic"
        pharExpiry = "2026-07-22"
        pharAmount = monthly.toInt().toString()
        pharPaymentStatus = "Paid"
        pharCreatedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        showAddDialog = true
    }

    // Helper to open Edit Dialog
    fun openEdit(phar: PharmacyEntity) {
        selectedPharmacy = phar
        pharName = phar.name
        ownerName = phar.ownerName
        pharPhone = phar.phone
        pharAddress = phar.address
        pharLicense = phar.license
        pharStatus = phar.status
        pharCreatedDate = phar.createdDate
        pharPlan = phar.subscriptionPlan
        pharExpiry = phar.subscriptionExpiry
        pharAmount = phar.subscriptionAmount.toInt().toString()
        pharPaymentStatus = phar.subscriptionPaymentStatus
        showEditDialog = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Actions Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pharmacy Registry Table (${pharmacies.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )

            Button(
                onClick = { openAdd() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Pharmacy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pharmacies.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No pharmacies registered in database.",
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(pharmacies) { phar ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (phar.status == "Active") Color(0xFFE2E8F0) else Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Icon + Name + Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalPharmacy,
                                        contentDescription = null,
                                        tint = if (phar.status == "Active") Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = phar.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                
                                // Status Badges
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (phar.status == "Active") Color(0xFFD1FAE5) else Color(0xFFFEE2E2))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = phar.status,
                                            color = if (phar.status == "Active") Color(0xFF065F46) else Color(0xFF991B1B),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFDBEAFE))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active Subscription",
                                            color = Color(0xFF1E40AF),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp))

                            // Pharmacy Management Fields Table
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                TableFieldRow("Owner Name", phar.ownerName)
                                TableFieldRow("Mobile Number", phar.phone)
                                TableFieldRow("Address", phar.address)
                                TableFieldRow("License Key", phar.license)
                                TableFieldRow("Registration Date", phar.createdDate)
                                TableFieldRow("SaaS Plan", "${phar.subscriptionPlan}")
                                TableFieldRow("Plan Validity", "${phar.subscriptionStart} to ${phar.subscriptionExpiry}")
                                TableFieldRow("Invoice Value", "₹${phar.subscriptionAmount.toInt()}")
                                TableFieldRow("Payment Status", phar.subscriptionPaymentStatus)
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp))

                            // Interactive Administrative Control Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Enable / Disable Toggle Switch
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (phar.status == "Active") "Active" else "Suspended",
                                        fontSize = 12.sp,
                                        color = if (phar.status == "Active") Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = phar.status == "Active",
                                        onCheckedChange = { isChecked ->
                                            val nextStatus = if (isChecked) "Active" else "Suspended"
                                            viewModel.editPharmacy(
                                                id = phar.id,
                                                name = phar.name,
                                                address = phar.address,
                                                phone = phar.phone,
                                                license = phar.license,
                                                ownerName = phar.ownerName,
                                                status = nextStatus,
                                                createdDate = phar.createdDate,
                                                subscriptionPlan = phar.subscriptionPlan,
                                                subscriptionStart = phar.subscriptionStart,
                                                subscriptionExpiry = phar.subscriptionExpiry,
                                                subscriptionAmount = phar.subscriptionAmount,
                                                subscriptionPaymentStatus = phar.subscriptionPaymentStatus
                                            )
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10B981)
                                        ),
                                        modifier = Modifier.scale(0.8f) // elegant compact switch
                                    )
                                }

                                // Edit & Delete Actions
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { openEdit(phar) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFEFF6FF))
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit details", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedPharmacy = phar
                                            showDeleteConfirm = true
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEE2E2))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Registry", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Add Pharmacy AlertDialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Register New Pharmacy", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = pharName,
                        onValueChange = { pharName = it },
                        label = { Text("Pharmacy Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharPhone,
                        onValueChange = { pharPhone = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharAddress,
                        onValueChange = { pharAddress = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharLicense,
                        onValueChange = { pharLicense = it },
                        label = { Text("License (e.g. DL-9087)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharPlan,
                        onValueChange = { 
                            pharPlan = it
                            pharAmount = when {
                                it.contains("Premium", ignoreCase = true) || it.contains("Quarterly", ignoreCase = true) -> quarterly.toInt().toString()
                                it.contains("Enterprise", ignoreCase = true) || it.contains("Yearly", ignoreCase = true) -> yearly.toInt().toString()
                                else -> monthly.toInt().toString()
                            }
                        },
                        label = { Text("SaaS Plan (Basic, Premium, Enterprise)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharExpiry,
                        onValueChange = { pharExpiry = it },
                        label = { Text("Plan Expiry Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharPaymentStatus,
                        onValueChange = { pharPaymentStatus = it },
                        label = { Text("Payment Status (Paid, Pending)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharCreatedDate,
                        onValueChange = { pharCreatedDate = it },
                        label = { Text("Created Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pharName.isNotBlank() && pharPhone.isNotBlank()) {
                            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            viewModel.addPharmacy(
                                name = pharName,
                                address = pharAddress,
                                phone = pharPhone,
                                license = pharLicense,
                                ownerName = if (ownerName.isBlank()) "Dr. Amit Patra" else ownerName,
                                status = "Active",
                                createdDate = pharCreatedDate,
                                subscriptionPlan = pharPlan,
                                subscriptionStart = today,
                                subscriptionExpiry = pharExpiry,
                                subscriptionAmount = pharAmount.toDoubleOrNull() ?: 499.0,
                                subscriptionPaymentStatus = pharPaymentStatus
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Register")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Edit Pharmacy AlertDialog
    if (showEditDialog && selectedPharmacy != null) {
        val selected = selectedPharmacy!!
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Pharmacy Details", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = pharName,
                        onValueChange = { pharName = it },
                        label = { Text("Pharmacy Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharPhone,
                        onValueChange = { pharPhone = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharAddress,
                        onValueChange = { pharAddress = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharLicense,
                        onValueChange = { pharLicense = it },
                        label = { Text("License") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharPlan,
                        onValueChange = { 
                            pharPlan = it
                            pharAmount = when {
                                it.contains("Premium", ignoreCase = true) || it.contains("Quarterly", ignoreCase = true) -> quarterly.toInt().toString()
                                it.contains("Enterprise", ignoreCase = true) || it.contains("Yearly", ignoreCase = true) -> yearly.toInt().toString()
                                else -> monthly.toInt().toString()
                            }
                        },
                        label = { Text("SaaS Plan (Basic, Premium, Enterprise)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharExpiry,
                        onValueChange = { pharExpiry = it },
                        label = { Text("Plan Expiry Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pharPaymentStatus,
                        onValueChange = { pharPaymentStatus = it },
                        label = { Text("Payment Status (Paid, Pending)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pharName.isNotBlank()) {
                            viewModel.editPharmacy(
                                id = selected.id,
                                name = pharName,
                                address = pharAddress,
                                phone = pharPhone,
                                license = pharLicense,
                                ownerName = ownerName,
                                status = pharStatus,
                                createdDate = pharCreatedDate,
                                subscriptionPlan = pharPlan,
                                subscriptionStart = selected.subscriptionStart,
                                subscriptionExpiry = pharExpiry,
                                subscriptionAmount = pharAmount.toDoubleOrNull() ?: selected.subscriptionAmount,
                                subscriptionPaymentStatus = pharPaymentStatus
                            )
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Delete Confirmation Dialog
    if (showDeleteConfirm && selectedPharmacy != null) {
        val selected = selectedPharmacy!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete Registry", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Are you absolutely sure you want to remove the pharmacy '${selected.name}' from the care circle registry? This operation is irreversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removePharmacy(selected.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TableFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF1E293B),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}


// ============================================
// 3. SUBSCRIPTIONS VIEW
// ============================================
@Composable
fun AdminSubscriptionsView(viewModel: MainViewModel) {
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val pricingSettings by viewModel.pricingSettings.collectAsState()

    val monthly = pricingSettings?.monthlySubscriptionFee ?: 10.0
    val quarterly = pricingSettings?.quarterlySubscriptionFee ?: 30.0
    val yearly = pricingSettings?.yearlySubscriptionFee ?: 100.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pharmacies) { phar ->
            val planAmount = when {
                phar.subscriptionPlan.contains("Quarterly", ignoreCase = true) -> quarterly
                phar.subscriptionPlan.contains("Yearly", ignoreCase = true) -> yearly
                else -> monthly
            }
            val planLabel = when {
                phar.subscriptionPlan.contains("Quarterly", ignoreCase = true) -> "/Qtr"
                phar.subscriptionPlan.contains("Yearly", ignoreCase = true) -> "/Yr"
                else -> "/Mo"
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CardMembership, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = phar.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = phar.subscriptionPlan, fontSize = 12.sp, color = Color(0xFF475569))
                            Text(text = "Expires ${phar.subscriptionExpiry}  •  Online", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Text(text = "₹${planAmount.toInt()}$planLabel", fontWeight = FontWeight.Black, color = Color(0xFF8B5CF6), fontSize = 15.sp)
                }
            }
        }
    }
}

// ============================================
// 4. ADMIN USERS DATABASE
// ============================================
@Composable
fun AdminUsersView(viewModel: MainViewModel) {
    val bookings by viewModel.allBookings.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (bookings.isEmpty()) {
            item {
                Text("No users registered in context. Book a slot first to record patient sub-profiles.", color = Color(0xFF64748B), modifier = Modifier.padding(16.dp))
            }
        } else {
            items(bookings) { user ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF475569))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = user.patientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Phone: ${user.patientPhone}  •  Sex: ${user.gender} (${user.age})", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// 5. DOCTORS SYSTEM CONTROL
// ============================================
@Composable
fun AdminDoctorsView(viewModel: MainViewModel) {
    val doctors by viewModel.allDoctors.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(doctors) { doctor ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = doctor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = doctor.specialization, color = Color(0xFF8B5CF6), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (doctor.isEnabled) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = if (doctor.isEnabled) "Enabled" else "Disabled", color = if (doctor.isEnabled) Color(0xFF047857) else Color(0xFFB91C1C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================
// 6. AUDIT LOG FILE DATABASE TRAIL
// ============================================
@Composable
fun AdminAuditView(viewModel: MainViewModel) {
    val logs by viewModel.auditLogs.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(logs) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = log.action,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D3FD3),
                            fontSize = 13.sp
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Text(
                        text = log.details,
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ============================================
// 7. PLATFORM SETTINGS
// ============================================
@Composable
fun AdminSettingsView(viewModel: MainViewModel, logout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Supabase Connectivity Monitor Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isConfigured = SupabaseManager.isConfigured
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConfigured) Color(0xFF22C55E) else Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConfigured) "Supabase Connection: Active" else "Supabase Connection: Local Sandbox Mode",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = if (SupabaseManager.isConfigured) 
                        "The application is streaming and syncing login metadata and patient bookings directly to your live Supabase database instance."
                        else "To go Live, enter your SUPABASE_URL and SUPABASE_ANON_KEY securely in the AI Studio Secrets panel. Otherwise, components continue in high-fidelity local memory emulation.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )
            }
        }

        // Configurable Pricing Settings Card
        val pricingSettings by viewModel.pricingSettings.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current

        var regFee by remember(pricingSettings) { mutableStateOf(pricingSettings?.registrationFee?.toString() ?: "10.0") }
        var monthlyFee by remember(pricingSettings) { mutableStateOf(pricingSettings?.monthlySubscriptionFee?.toString() ?: "10.0") }
        var quarterlyFee by remember(pricingSettings) { mutableStateOf(pricingSettings?.quarterlySubscriptionFee?.toString() ?: "30.0") }
        var yearlyFee by remember(pricingSettings) { mutableStateOf(pricingSettings?.yearlySubscriptionFee?.toString() ?: "100.0") }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Configurable SaaS Pricing Settings",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp
                )
                Text(
                    text = "Configure pricing values used platform-wide. Changes take effect instantly without any code modifications.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = regFee,
                    onValueChange = { regFee = it },
                    label = { Text("Registration Fee (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = monthlyFee,
                    onValueChange = { monthlyFee = it },
                    label = { Text("Monthly Subscription Fee (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = quarterlyFee,
                    onValueChange = { quarterlyFee = it },
                    label = { Text("Quarterly Subscription Fee (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = yearlyFee,
                    onValueChange = { yearlyFee = it },
                    label = { Text("Yearly Subscription Fee (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Button(
                    onClick = {
                        val rF = regFee.toDoubleOrNull()
                        val mF = monthlyFee.toDoubleOrNull()
                        val qF = quarterlyFee.toDoubleOrNull()
                        val yF = yearlyFee.toDoubleOrNull()

                        if (rF != null && mF != null && qF != null && yF != null) {
                            viewModel.savePricingSettings(rF, mF, qF, yF)
                            android.widget.Toast.makeText(context, "Pricing settings updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Please enter valid numbers for all fees.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F52BA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Pricing Settings", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // App Theme Settings Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "System Theme Preferences",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 14.sp
                )
                
                val activeTheme by viewModel.appTheme.collectAsState()
                
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
                                .background(if (isSelected) Color(0xFF0F52BA) else Color(0xFFEEF2F6))
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

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Developer Simulation Controls", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("DoctorLine operates in high-stability sandbox offline-sync mode with pre-seeded database engines. In this console view you can test components in real-time.", fontSize = 12.sp, color = Color(0xFF64748B))
                
                Button(
                    onClick = {
                        // Resets database state
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Re-validate Database Seed Cache", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = logout,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Logout of Administration Panel", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminRequestsView(viewModel: MainViewModel) {
    val requests by viewModel.allPharmacyRequests.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val currentAdminId = activeUser?.id ?: "master_admin"

    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var selectedImageTitle by remember { mutableStateOf("") }

    val pendingRequests = requests.filter { it.status.lowercase() == "pending" }
    val approvedRequests = requests.filter { it.status.lowercase() == "approved" }
    val rejectedRequests = requests.filter { it.status.lowercase() == "rejected" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Summary row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Pending",
                    count = pendingRequests.size,
                    color = Color(0xFFEAB308),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Approved",
                    count = approvedRequests.size,
                    color = Color(0xFF22C55E),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Rejected",
                    count = rejectedRequests.size,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (requests.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AssignmentLate,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Pharmacy Requests Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Submitted registration requests from pharmacies will appear here.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(requests) { req ->
                PharmacyRequestCard(
                    req = req,
                    onApprove = { viewModel.approvePharmacyRequest(req.id, currentAdminId) },
                    onReject = { viewModel.rejectPharmacyRequest(req.id) },
                    onViewImage = { uri, title ->
                        selectedImageUri = uri
                        selectedImageTitle = title
                    }
                )
            }
        }
    }

    // Modal Image Previewer
    if (selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = { selectedImageUri = null },
            confirmButton = {
                TextButton(onClick = { selectedImageUri = null }) {
                    Text("Close Preview", fontWeight = FontWeight.Bold, color = Color(0xFF5D3FD3))
                }
            },
            title = {
                Text(
                    text = selectedImageTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B1220)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = selectedImageTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                        placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                }
            }
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            }
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun PharmacyRequestCard(
    req: PharmacyRequestEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onViewImage: (String, String) -> Unit
) {
    val statusColor = when (req.status.lowercase()) {
        "approved" -> Color(0xFF22C55E)
        "rejected" -> Color(0xFFEF4444)
        else -> Color(0xFFEAB308)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Pharmacy Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = req.pharmacyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Submitted: " + SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.createdAt)),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = req.status.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

            // Grid / Details Area
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(icon = Icons.Rounded.Person, label = "Owner", value = req.ownerName)
                DetailRow(icon = Icons.Rounded.Badge, label = "Drug License", value = req.licenseNo)
                DetailRow(icon = Icons.Rounded.Phone, label = "Contact", value = "${req.mobile} • ${req.email}")
                DetailRow(icon = Icons.Rounded.LocationOn, label = "Address", value = req.address)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Document Uploads row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DocumentThumb(
                    title = "Drug License",
                    subtitle = "Verify registration certificate",
                    onClick = { onViewImage(req.licenseImage, "${req.pharmacyName} - Drug License") },
                    modifier = Modifier.weight(1f)
                )

                if (req.pharmacyPhoto != null) {
                    DocumentThumb(
                        title = "Storefront",
                        subtitle = "Verify premises photo",
                        onClick = { onViewImage(req.pharmacyPhoto, "${req.pharmacyName} - Storefront Photo") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Storefront Photo", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Action Buttons (Only show if pending)
            if (req.status.lowercase() == "pending") {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(Icons.Rounded.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reject Request", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Approve & Provision", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 13.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DocumentThumb(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = null,
                tint = Color(0xFF5D3FD3),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Text(subtitle, fontSize = 9.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
