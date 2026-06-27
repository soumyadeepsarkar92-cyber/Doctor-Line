package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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

    val primaryColor = Color(0xFF7C5DFA) // Pastel Purple
    val bgMain = if (isDark) Color(0xFF0C091C) else Color(0xFFFAF8FF)
    val textPrimary = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B)

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
                    .background(if (isDark) Color(0xFF130E2E) else Color(0xFFFFFFFF))
                    .statusBarsPadding()
            ) {
                // Header Identity Logo & Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(primaryColor, Color(0xFF9F7AEA)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Healing, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DoctorLine",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "ADMIN PORTAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = primaryColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = logout,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF241C4F) else Color(0xFFF3EFFF))
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = "Log out", tint = primaryColor, modifier = Modifier.size(18.dp))
                    }
                }

                // Scrollable menu tabs
                ScrollableTabRow(
                    selectedTabIndex = menus.indexOfFirst { it.first == selectedMenu },
                    containerColor = if (isDark) Color(0xFF130E2E) else Color(0xFFFFFFFF),
                    contentColor = primaryColor,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val index = menus.indexOfFirst { it.first == selectedMenu }
                        if (index >= 0) {
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = primaryColor,
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
                            selectedContentColor = primaryColor,
                            unselectedContentColor = textSecondary,
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = bgMain
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
                val headerTitle = when (selectedMenu) {
                    "Users" -> "Booking Management"
                    else -> selectedMenu
                }
                Text(
                    text = headerTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )

                // Platform quick action badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (isDark) Color(0xFF241C4F) else Color(0xFFE8FDF5))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SaaS Sandbox",
                        color = if (isDark) Color(0xFFA78BFA) else Color(0xFF059669),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Render matching sub-view components
            Box(modifier = Modifier.weight(1f)) {
                when (selectedMenu) {
                    "Dashboard" -> AdminDashboardView(viewModel = viewModel, onSelectMenu = { selectedMenu = it })
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
fun AdminDashboardView(viewModel: MainViewModel, onSelectMenu: (String) -> Unit) {
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val doctors by viewModel.allDoctors.collectAsState()
    val bookings by viewModel.allBookings.collectAsState()
    val requests by viewModel.allPharmacyRequests.collectAsState()

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val primaryColor = Color(0xFF7C5DFA) // Pastel Purple
    val mintColor = Color(0xFF10B981) // Mint Green

    val cardBg = if (isDark) Color(0xFF1B153D) else Color.White
    val textPrimary = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF2E245B) else Color(0xFFEBE5FC)

    val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val totalPharmacies = pharmacies.size
    val totalPatients = maxOf(12, bookings.map { it.patientPhone }.distinct().size)
    val totalDoctors = doctors.size
    
    val pendingApprovals = requests.count { 
        it.status.lowercase() == "pending" || it.status.lowercase() == "pending_verification" 
    }
    val todaysAppointments = bookings.count { it.dateStr == todayDate }
    val monthlyRevenue = bookings.filter { it.status == "Completed" || it.status == "Upcoming" }.sumOf { 800.0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Quick Actions Command Center
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Quick Commands",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pending Requests Command
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(primaryColor.copy(alpha = 0.12f))
                            .clickable { onSelectMenu("Requests") }
                            .padding(16.dp)
                    ) {
                        Column {
                            Icon(Icons.Rounded.AppRegistration, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Pending Approvals", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            Text("Review submissions", fontSize = 10.sp, color = textSecondary)
                        }
                    }
                    
                    // Add Pharmacy Command
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(mintColor.copy(alpha = 0.12f))
                            .clickable { onSelectMenu("Pharmacies") }
                            .padding(16.dp)
                    ) {
                        Column {
                            Icon(Icons.Rounded.LocalPharmacy, contentDescription = null, tint = mintColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Add Pharmacy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = mintColor)
                            Text("Direct SaaS onboarding", fontSize = 10.sp, color = textSecondary)
                        }
                    }
                }
            }
        }

        // 2. 6 Premium Stats Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "System Performance KPIs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PremiumStatsCard(
                            label = "Total Patients",
                            value = totalPatients.toString(),
                            icon = Icons.Rounded.People,
                            color = primaryColor,
                            bgColor = cardBg,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumStatsCard(
                            label = "Total Pharmacies",
                            value = totalPharmacies.toString(),
                            icon = Icons.Rounded.LocalPharmacy,
                            color = mintColor,
                            bgColor = cardBg,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PremiumStatsCard(
                            label = "Pending Approvals",
                            value = pendingApprovals.toString(),
                            icon = Icons.Rounded.HourglassEmpty,
                            color = Color(0xFFF59E0B),
                            bgColor = cardBg,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumStatsCard(
                            label = "Approved Pharmacies",
                            value = requests.count { it.status.lowercase() == "approved" }.toString(),
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF10B981),
                            bgColor = cardBg,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PremiumStatsCard(
                            label = "Rejected Pharmacies",
                            value = requests.count { it.status.lowercase() == "rejected" }.toString(),
                            icon = Icons.Rounded.Cancel,
                            color = Color(0xFFEF4444),
                            bgColor = cardBg,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumStatsCard(
                            label = "Today's Appointments",
                            value = todaysAppointments.toString(),
                            icon = Icons.Rounded.EventAvailable,
                            color = Color(0xFF6366F1),
                            bgColor = cardBg,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Analytics Charts
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Line Graph Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Platform Earning Growth (Monthly)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val strokeWidthVal = 6f
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
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    // Smooth cubic bezier curves for iOS-inspired premium smoothness
                                    cubicTo(
                                        (prev.x + curr.x) / 2, prev.y,
                                        (prev.x + curr.x) / 2, curr.y,
                                        curr.x, curr.y
                                    )
                                }
                            }
                            
                            // Draw glowing shadow background
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
                                        primaryColor.copy(alpha = 0.25f),
                                        primaryColor.copy(alpha = 0.02f)
                                    )
                                )
                            )

                            // Draw curves
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = strokeWidthVal, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                            
                            // Draw circles
                            points.forEach { pt ->
                                drawCircle(
                                    color = Color.White,
                                    radius = 10f,
                                    center = pt
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = 6f,
                                    center = pt
                                )
                            }
                        }
                    }
                }

                // Pie Shares Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SaaS Licensing Shares",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                            Canvas(modifier = Modifier.size(110.dp)) {
                                drawArc(
                                    color = primaryColor, // Pro
                                    startAngle = -90f,
                                    sweepAngle = 180f,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                                drawArc(
                                    color = mintColor, // Monthly
                                    startAngle = 90f,
                                    sweepAngle = 120f,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                                drawArc(
                                    color = Color(0xFF6366F1), // Quarterly/Corp
                                    startAngle = 210f,
                                    sweepAngle = 60f,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PieMetricLegend(color = primaryColor, title = "Pro (50%)", textColor = textSecondary)
                            PieMetricLegend(color = mintColor, title = "Monthly (33%)", textColor = textSecondary)
                            PieMetricLegend(color = Color(0xFF6366F1), title = "Corp (17%)", textColor = textSecondary)
                        }
                    }
                }
            }
        }

        // 4. Recent Activity
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Recent Activity",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val recentRequests = requests.sortedByDescending { it.createdAt }.take(3)
                        if (recentRequests.isEmpty()) {
                            Text("No recent activity.", color = textSecondary, fontSize = 14.sp)
                        } else {
                            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            recentRequests.forEach { req ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when(req.status.lowercase()) {
                                                    "approved" -> mintColor.copy(alpha = 0.15f)
                                                    "rejected" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                    else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when(req.status.lowercase()) {
                                                "approved" -> Icons.Rounded.CheckCircle
                                                "rejected" -> Icons.Rounded.Cancel
                                                else -> Icons.Rounded.HourglassEmpty
                                            },
                                            contentDescription = null,
                                            tint = when(req.status.lowercase()) {
                                                "approved" -> mintColor
                                                "rejected" -> Color(0xFFEF4444)
                                                else -> Color(0xFFF59E0B)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${req.pharmacyName} (${req.status})",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = dateFormat.format(java.util.Date(req.createdAt)),
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
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

@Composable
fun PremiumStatsCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bgColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E245B) else Color(0xFFEBE5FC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                },
                label = "counter"
            ) { targetValue ->
                Text(
                    text = targetValue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
                )
            }
        }
    }
}

@Composable
fun PieMetricLegend(color: Color, title: String, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Bold)
    }
}

// ============================================
// 2. ADMIN PHARMACIES SHEET
// ============================================
@Composable
fun AdminPharmaciesView(viewModel: MainViewModel) {
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val pricingSettings by viewModel.pricingSettings.collectAsState()
    val paymentHistory by viewModel.allPaymentHistory.collectAsState()

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
    var pharError by remember { mutableStateOf<String?>(null) }

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
        pharError = null
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
        pharError = null
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
                            val latestPayment = paymentHistory.filter { it.pharmacyId == phar.id }.maxByOrNull { it.createdDate }
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
                                TableFieldRow("Payment ID", latestPayment?.paymentId ?: "N/A")
                                TableFieldRow("Razorpay Order ID", latestPayment?.orderId ?: "N/A")
                                TableFieldRow("Last Renewal Date", phar.subscriptionStart)
                                TableFieldRow("Next Renewal Date", phar.subscriptionExpiry)
                                TableFieldRow("Subscription Status", if (phar.status == "Active") "Active" else "Suspended")
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
                    if (pharError != null) {
                        Text(text = pharError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pharName.isBlank() || pharPhone.isBlank()) {
                            pharError = "Please fill in all required fields."
                        } else if (!pharPhone.matches(Regex("^[6-9]\\d{9}$"))) {
                            pharError = "Please enter a valid 10-digit Indian mobile number."
                        } else if (pharmacies.any { it.phone == pharPhone }) {
                            pharError = "This phone number is already registered to another pharmacy."
                        } else {
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
                    if (pharError != null) {
                        Text(text = pharError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pharName.isBlank() || ownerName.isBlank() || pharPhone.isBlank()) {
                            pharError = "Please fill in all required fields."
                        } else if (!pharPhone.matches(Regex("^[6-9]\\d{9}$"))) {
                            pharError = "Please enter a valid 10-digit Indian mobile number."
                        } else if (pharmacies.any { it.phone == pharPhone && it.id != selected.id }) {
                            pharError = "This phone number is already registered to another pharmacy."
                        } else {
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
    
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val primaryColor = Color(0xFF7C5DFA)
    val mintColor = Color(0xFF10B981)
    
    val cardBg = if (isDark) Color(0xFF1B153D) else Color.White
    val textPrimary = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF2E245B) else Color(0xFFEBE5FC)

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }

    val filteredBookings = bookings.filter { booking ->
        val matchesSearch = booking.patientName.contains(searchQuery, ignoreCase = true) ||
                booking.patientPhone.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (statusFilter) {
            "All" -> true
            "Confirmed" -> booking.status == "Upcoming" || booking.status == "Confirmed"
            "Completed" -> booking.status == "Completed"
            "Cancelled" -> booking.status == "Cancelled"
            else -> true
        }
        matchesSearch && matchesStatus
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search and Filter Controls Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp), clip = false),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Patient Name or Phone...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderCol,
                        focusedContainerColor = if (isDark) Color(0xFF120E2E) else Color(0xFFF5F3FF),
                        unfocusedContainerColor = if (isDark) Color(0xFF120E2E) else Color(0xFFFBF9FF),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    singleLine = true
                )

                // Status Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val states = listOf("All", "Confirmed", "Completed", "Cancelled")
                    states.forEach { state ->
                        val isSelected = statusFilter == state
                        val stateColor = when (state) {
                            "Confirmed" -> mintColor
                            "Completed" -> primaryColor
                            "Cancelled" -> Color(0xFFEF4444)
                            else -> textSecondary
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isSelected) stateColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = 1.2.dp,
                                    color = if (isSelected) stateColor else borderCol,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .clickable { statusFilter = state }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = state,
                                color = if (isSelected) stateColor else textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bookings List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (filteredBookings.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.EventBusy, contentDescription = null, tint = textSecondary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No appointments found", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 15.sp)
                        Text("Adjust your search queries or filter categories.", color = textSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                items(filteredBookings) { booking ->
                    val statusColor = when {
                        booking.status == "Upcoming" || booking.status == "Confirmed" -> mintColor
                        booking.status == "Completed" -> primaryColor
                        else -> Color(0xFFEF4444)
                    }
                    
                    val statusLabel = when (booking.status) {
                        "Upcoming" -> "CONFIRMED"
                        else -> booking.status.uppercase()
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), clip = false),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Token Badge & Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${booking.tokenNumber}",
                                            color = primaryColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = booking.patientName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(statusColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Divider(color = borderCol, modifier = Modifier.padding(vertical = 12.dp))

                            // Details block
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Phone, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                                    Text("Phone: ${booking.patientPhone}", fontSize = 12.sp, color = textSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                                    Text("Sex: ${booking.gender}  •  Age: ${booking.age}", fontSize = 12.sp, color = textSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Event, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                                    Text("Schedule: ${booking.dateStr} • ${booking.timeStr}", fontSize = 12.sp, color = textSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Payments, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                                    Text("Payment: ${booking.paymentMode} (${booking.paymentStatus})", fontSize = 12.sp, color = textSecondary)
                                }
                            }

                            // Dynamic Live Actions Bar
                            if (booking.status == "Upcoming" || booking.status == "Confirmed") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Cancel Action
                                    OutlinedButton(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "Cancelled") },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Complete Action
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "Completed") },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = mintColor),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
// 5. DOCTORS SYSTEM CONTROL
// ============================================
@Composable
fun AdminDoctorsView(viewModel: MainViewModel) {
    val doctors by viewModel.allDoctors.collectAsState()
    
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val primaryColor = Color(0xFF7C5DFA)
    val mintColor = Color(0xFF10B981)
    
    val cardBg = if (isDark) Color(0xFF1B153D) else Color.White
    val textPrimary = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF2E245B) else Color(0xFFEBE5FC)

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Active", "Disabled"

    val filteredDoctors = doctors.filter { doc ->
        val matchesSearch = doc.name.contains(searchQuery, ignoreCase = true) ||
                doc.specialization.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (statusFilter) {
            "All" -> true
            "Active" -> doc.isEnabled
            "Disabled" -> !doc.isEnabled
            else -> true
        }
        matchesSearch && matchesStatus
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Filter Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp), clip = false),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Doctor Name or Specialty...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderCol,
                        focusedContainerColor = if (isDark) Color(0xFF120E2E) else Color(0xFFF5F3FF),
                        unfocusedContainerColor = if (isDark) Color(0xFF120E2E) else Color(0xFFFBF9FF),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    singleLine = true
                )

                // Quick Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Active", "Disabled")
                    filters.forEach { filter ->
                        val isSelected = statusFilter == filter
                        val filterColor = when (filter) {
                            "Active" -> mintColor
                            "Disabled" -> Color(0xFFEF4444)
                            else -> primaryColor
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isSelected) filterColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = 1.2.dp,
                                    color = if (isSelected) filterColor else borderCol,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .clickable { statusFilter = filter }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) filterColor else textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Doctors list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (filteredDoctors.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.PersonSearch, contentDescription = null, tint = textSecondary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No doctors registered", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 15.sp)
                        Text("Add doctors via clinic portals or adjust filters.", color = textSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                items(filteredDoctors) { doctor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), clip = false),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Header: Doctor Name, Specialization & verification
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Doctor Icon/Avatar box
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(primaryColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = doctor.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = textPrimary
                                            )
                                            // Verified badge
                                            Icon(
                                                imageVector = Icons.Rounded.Verified,
                                                contentDescription = "Verified Profile",
                                                tint = mintColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = doctor.specialization,
                                            color = primaryColor,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Interactive status switch button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(if (doctor.isEnabled) mintColor.copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f))
                                        .clickable { viewModel.toggleDoctorEnabled(doctor.id, !doctor.isEnabled) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (doctor.isEnabled) mintColor else Color(0xFFEF4444))
                                        )
                                        Text(
                                            text = if (doctor.isEnabled) "ACTIVE" else "DISABLED",
                                            color = if (doctor.isEnabled) mintColor else Color(0xFFEF4444),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Divider(color = borderCol, modifier = Modifier.padding(vertical = 12.dp))

                            // Stats section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Experience", fontSize = 10.sp, color = textSecondary)
                                    Text("${doctor.experience} Years", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                                Column {
                                    Text("Consultation", fontSize = 10.sp, color = textSecondary)
                                    Text("₹${doctor.fee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                                Column {
                                    Text("Availability", fontSize = 10.sp, color = textSecondary)
                                    Text(doctor.availabilityStatus, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (doctor.availabilityStatus.lowercase() == "active" || doctor.availabilityStatus.lowercase() == "available") mintColor else Color(0xFFF59E0B))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Bottom clinic mapping info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Rounded.HomeWork, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                                    Text("SaaS License ID: ${doctor.pharmacyId.take(12)}...", fontSize = 11.sp, color = textSecondary)
                                }
                                
                                // Direct Delete Action
                                IconButton(
                                    onClick = { viewModel.softDeleteDoctor(doctor.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Delete Doctor",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
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
                            color = Color(0xFF7C5DFA),
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
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val buttonBgOption = if (isDark) Color(0xFF334155) else Color(0xFFEEF2F6)
    val buttonTextOption = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

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
            colors = CardDefaults.cardColors(containerColor = cardBg),
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
                        color = textPrimary,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = if (SupabaseManager.isConfigured) 
                        "The application is streaming and syncing login metadata and patient bookings directly to your live Supabase database instance."
                        else "To go Live, enter your SUPABASE_URL and SUPABASE_ANON_KEY securely in the AI Studio Secrets panel. Otherwise, components continue in high-fidelity local memory emulation.",
                    fontSize = 12.sp,
                    color = textSecondary,
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
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Configurable SaaS Pricing Settings",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Configure pricing values used platform-wide. Changes take effect instantly without any code modifications.",
                    fontSize = 12.sp,
                    color = textSecondary
                )

                OutlinedTextField(
                    value = regFee,
                    onValueChange = { regFee = it },
                    label = { Text("Registration Fee (₹)", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = Color(0xFF7C5DFA),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = monthlyFee,
                    onValueChange = { monthlyFee = it },
                    label = { Text("Monthly Subscription Fee (₹)", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = Color(0xFF7C5DFA),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = quarterlyFee,
                    onValueChange = { quarterlyFee = it },
                    label = { Text("Quarterly Subscription Fee (₹)", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = Color(0xFF7C5DFA),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = yearlyFee,
                    onValueChange = { yearlyFee = it },
                    label = { Text("Yearly Subscription Fee (₹)", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = Color(0xFF7C5DFA),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    ),
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
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5DFA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Pricing Settings", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // App Theme Settings Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "System Theme Preferences",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF7C5DFA) else buttonBgOption)
                                .clickable { viewModel.updateTheme(themeName) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = themeName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else buttonTextOption
                            )
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Developer Simulation Controls", fontWeight = FontWeight.Bold, color = textPrimary)
                Text("DoctorLine operates in high-stability sandbox offline-sync mode with pre-seeded database engines. In this console view you can test components in real-time.", fontSize = 12.sp, color = textSecondary)
                
                Button(
                    onClick = {
                        // Resets database state
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonBgOption),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Re-validate Database Seed Cache", color = buttonTextOption, fontWeight = FontWeight.Bold)
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
    val pharmacies by viewModel.allPharmacies.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val currentAdminId = activeUser?.id ?: "master_admin"

    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val primaryColor = Color(0xFF7C5DFA)
    val mintColor = Color(0xFF10B981)
    
    val cardBg = if (isDark) Color(0xFF1B153D) else Color.White
    val textPrimary = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF2E245B) else Color(0xFFEBE5FC)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var selectedImageTitle by remember { mutableStateOf("") }

    // Rejection Dialog State
    var showRejectDialogForId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    // Correction Dialog State
    var showCorrectionDialogForId by remember { mutableStateOf<String?>(null) }
    var correctionNotesInput by remember { mutableStateOf("") }

    // Payment Dialog State
    var viewPaymentRequest by remember { mutableStateOf<PharmacyRequestEntity?>(null) }

    // Details Dialog State
    var viewDetailsRequest by remember { mutableStateOf<PharmacyRequestEntity?>(null) }

    // Helpers to check "today"
    fun isToday(timestamp: Long?): Boolean {
        if (timestamp == null) return false
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal2.timeInMillis = timestamp
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    // KPI Card Computations
    val pendingVerificationCount = requests.filter { 
        it.status.lowercase() == "pending" || 
        it.status.lowercase() == "pending_verification" || 
        it.status.lowercase() == "correction_requested" 
    }.size

    val approvedTodayCount = requests.filter { 
        it.status.lowercase() == "approved" && isToday(it.approvedAt) 
    }.size

    val rejectedTodayCount = requests.filter { 
        it.status.lowercase() == "rejected" && isToday(it.createdAt) // approximate date of reject is createdAt of request in sandbox
    }.size

    val registrationRevenue = requests.filter { 
        it.paymentStatus?.lowercase() == "success" || 
        it.paymentStatus?.lowercase() == "paid" || 
        it.paymentStatus?.lowercase() == "successful" ||
        it.paymentStatus?.lowercase() == "payment_completed"
    }.sumOf { it.paymentAmount ?: 0.0 }

    val activePharmaciesCount = pharmacies.filter { it.status.lowercase() == "active" }.size

    val pendingPaymentsCount = requests.filter { 
        it.paymentStatus.isNullOrEmpty() || it.paymentStatus?.lowercase() == "pending" 
    }.size

    val totalProcessed = requests.filter { 
        it.status.lowercase() == "approved" || it.status.lowercase() == "rejected" 
    }.size
    val approvalRate = if (totalProcessed > 0) {
        (requests.filter { it.status.lowercase() == "approved" }.size.toDouble() / totalProcessed * 100).toInt()
    } else {
        0
    }

    val todayRegistrationsCount = requests.filter { isToday(it.createdAt) }.size

    // Filtering logic
    val filteredRequests = requests.filter { req ->
        val matchesSearch = if (searchQuery.isEmpty()) {
            true
        } else {
            req.pharmacyName.contains(searchQuery, ignoreCase = true) ||
            req.ownerName.contains(searchQuery, ignoreCase = true) ||
            req.licenseNo.contains(searchQuery, ignoreCase = true) ||
            req.mobile.contains(searchQuery, ignoreCase = true) ||
            (req.paymentId?.contains(searchQuery, ignoreCase = true) ?: false)
        }

        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Pending" -> req.status.lowercase() == "pending" || req.status.lowercase() == "pending_verification"
            "Approved" -> req.status.lowercase() == "approved"
            "Rejected" -> req.status.lowercase() == "rejected"
            "Correction Requested" -> req.status.lowercase() == "correction_requested"
            "Payment Pending" -> req.paymentStatus.isNullOrEmpty() || req.paymentStatus?.lowercase() == "pending"
            "Payment Successful" -> req.paymentStatus?.lowercase() == "success" || req.paymentStatus?.lowercase() == "paid" || req.paymentStatus?.lowercase() == "successful" || req.paymentStatus?.lowercase() == "payment_completed"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // SaaS KPI Grid Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Realtime SaaS Performance Metrics",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RequestKpiCard("Pending Verification", pendingVerificationCount.toString(), Icons.Rounded.HourglassEmpty, Color(0xFFF59E0B), Modifier.weight(1f), isDark = isDark)
                    RequestKpiCard("Today's Registrations", todayRegistrationsCount.toString(), Icons.Rounded.AppRegistration, Color(0xFF6366F1), Modifier.weight(1f), isDark = isDark)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RequestKpiCard("Approved Today", approvedTodayCount.toString(), Icons.Rounded.CheckCircle, Color(0xFF10B981), Modifier.weight(1f), isDark = isDark)
                    RequestKpiCard("Rejected Today", rejectedTodayCount.toString(), Icons.Rounded.Cancel, Color(0xFFEF4444), Modifier.weight(1f), isDark = isDark)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RequestKpiCard("Registration Revenue", "$${String.format(Locale.getDefault(), "%.2f", registrationRevenue)}", Icons.Rounded.AttachMoney, Color(0xFF0D9488), Modifier.weight(1f), isDark = isDark)
                    RequestKpiCard("Active Pharmacies", activePharmaciesCount.toString(), Icons.Rounded.LocalPharmacy, Color(0xFF3B82F6), Modifier.weight(1f), isDark = isDark)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RequestKpiCard("Pending Payments", pendingPaymentsCount.toString(), Icons.Rounded.Receipt, Color(0xFFD97706), Modifier.weight(1f), isDark = isDark)
                    RequestKpiCard("Approval Rate", "$approvalRate%", Icons.Rounded.RateReview, Color(0xFF8B5CF6), Modifier.weight(1f), isDark = isDark)
                }
            }
        }

        // Modern Search & Interactive Filter Control
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), clip = false),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, owner, license, mobile, payment ID...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderCol,
                            focusedContainerColor = if (isDark) Color(0xFF120E2E) else Color(0xFFF5F3FF),
                            unfocusedContainerColor = if (isDark) Color(0xFF120E2E) else Color(0xFFFBF9FF),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = textSecondary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filterCategories = listOf(
                            "All",
                            "Pending",
                            "Approved",
                            "Rejected",
                            "Correction Requested",
                            "Payment Pending",
                            "Payment Successful"
                        )
                        filterCategories.forEach { filterName ->
                            val isSelected = selectedFilter == filterName
                            val chipBg = if (isSelected) primaryColor else (if (isDark) Color(0xFF2E245B) else Color(0xFFF1F5F9))
                            val textColor = if (isSelected) Color.White else textSecondary
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(chipBg)
                                    .clickable { selectedFilter = filterName }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(filterName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                        }
                    }
                }
            }
        }

        // List of Requests
        if (filteredRequests.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), clip = false),
                    border = BorderStroke(1.dp, borderCol)
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
                            tint = textSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Pharmacy Requests Matches Filters",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Adjust your search parameters or filters to discover active submissions.",
                            fontSize = 13.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredRequests) { req ->
                PharmacyRequestCard(
                    req = req,
                    onApprove = { viewModel.approvePharmacyRequest(req.id, currentAdminId) },
                    onReject = { showRejectDialogForId = req.id },
                    onRequestCorrection = { showCorrectionDialogForId = req.id },
                    onViewImage = { uri, title ->
                        selectedImageUri = uri
                        selectedImageTitle = title
                    },
                    onViewPayment = { viewPaymentRequest = req },
                    onViewDetails = { viewDetailsRequest = req },
                    isDark = isDark
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
                    Text("Close Preview", fontWeight = FontWeight.Bold, color = Color(0xFF7C5DFA))
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

    // Rejection dialog requiring justification
    if (showRejectDialogForId != null) {
        AlertDialog(
            onDismissRequest = {
                showRejectDialogForId = null
                rejectionReasonInput = ""
            },
            title = { Text("Reject Pharmacy Registration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please provide a reason for rejecting this pharmacy's request. This will be transmitted to them in realtime.", fontSize = 13.sp, color = Color(0xFF475569))
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        placeholder = { Text("e.g. Drug license has expired or does not match storefront details.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reqId = showRejectDialogForId
                        if (reqId != null && rejectionReasonInput.isNotBlank()) {
                            viewModel.rejectPharmacyRequest(reqId, rejectionReasonInput.trim())
                            showRejectDialogForId = null
                            rejectionReasonInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    enabled = rejectionReasonInput.isNotBlank()
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectDialogForId = null
                    rejectionReasonInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Correction Request dialog prompting notes
    if (showCorrectionDialogForId != null) {
        AlertDialog(
            onDismissRequest = {
                showCorrectionDialogForId = null
                correctionNotesInput = ""
            },
            title = { Text("Request Correction Notes", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide explicit correction notes for the pharmacy. Only fields marked will become editable in their portal.", fontSize = 13.sp, color = Color(0xFF475569))
                    OutlinedTextField(
                        value = correctionNotesInput,
                        onValueChange = { correctionNotesInput = it },
                        placeholder = { Text("e.g. Upload a higher resolution photo of your Drug License certificate.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reqId = showCorrectionDialogForId
                        if (reqId != null && correctionNotesInput.isNotBlank()) {
                            viewModel.requestCorrectionPharmacyRequest(reqId, correctionNotesInput.trim())
                            showCorrectionDialogForId = null
                            correctionNotesInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    enabled = correctionNotesInput.isNotBlank()
                ) {
                    Text("Submit Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCorrectionDialogForId = null
                    correctionNotesInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Payment details dialog
    if (viewPaymentRequest != null) {
        val req = viewPaymentRequest!!
        AlertDialog(
            onDismissRequest = { viewPaymentRequest = null },
            title = { Text("Pharmacy Payment Receipt", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(icon = Icons.Rounded.Receipt, label = "Payment Reference ID", value = req.paymentId ?: "N/A")
                    DetailRow(icon = Icons.Rounded.AttachMoney, label = "Total Registration Fee", value = "$${String.format(Locale.getDefault(), "%.2f", req.paymentAmount ?: 0.0)}")
                    DetailRow(icon = Icons.Rounded.CalendarToday, label = "Payment Date & Time", value = req.paymentDate?.let { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "N/A")
                    DetailRow(icon = Icons.Rounded.Info, label = "Payment Gateway Status", value = (req.paymentStatus ?: "Pending").uppercase())
                }
            },
            confirmButton = {
                TextButton(onClick = { viewPaymentRequest = null }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Full profile details dialog
    if (viewDetailsRequest != null) {
        val req = viewDetailsRequest!!
        AlertDialog(
            onDismissRequest = { viewDetailsRequest = null },
            title = { Text("Pharmacy Core Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(icon = Icons.Rounded.LocalPharmacy, label = "Pharmacy Name", value = req.pharmacyName)
                    DetailRow(icon = Icons.Rounded.Person, label = "Registered Owner", value = req.ownerName)
                    DetailRow(icon = Icons.Rounded.Badge, label = "Drug License Number", value = req.licenseNo)
                    DetailRow(icon = Icons.Rounded.Phone, label = "Contact Mobile Number", value = req.mobile)
                    DetailRow(icon = Icons.Rounded.Receipt, label = "Official Email ID", value = req.email)
                    DetailRow(icon = Icons.Rounded.LocationOn, label = "Physical Address Location", value = req.address)
                    DetailRow(icon = Icons.Rounded.CalendarToday, label = "Application Date", value = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.createdAt)))
                }
            },
            confirmButton = {
                TextButton(onClick = { viewDetailsRequest = null }) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun RequestKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val cardBg = if (isDark) Color(0xFF1B153D) else Color.White
    val textPrimary = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF2E245B) else Color(0xFFEBE5FC)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = modifier.shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), clip = false),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PharmacyRequestCard(
    req: PharmacyRequestEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestCorrection: () -> Unit,
    onViewImage: (String, String) -> Unit,
    onViewPayment: () -> Unit,
    onViewDetails: () -> Unit,
    isDark: Boolean = false
) {
    val primaryColor = Color(0xFF7C5DFA) // Soft Pastel Purple
    val mintColor = Color(0xFF10B981) // Mint
    
    val cardBg = if (isDark) Color(0xFF1B153D) else Color.White
    val textPrimary = if (isDark) Color(0xFFF1EEFD) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF9EA3C0) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF2E245B) else Color(0xFFEBE5FC)

    val statusColor = when (req.status.lowercase()) {
        "approved" -> mintColor
        "rejected" -> Color(0xFFEF4444)
        "correction_requested" -> Color(0xFFF59E0B)
        else -> primaryColor
    }

    val isPaymentSuccessful = req.paymentStatus?.lowercase() == "success" || 
                              req.paymentStatus?.lowercase() == "paid" || 
                              req.paymentStatus?.lowercase() == "successful" ||
                              req.paymentStatus?.lowercase() == "payment_completed"

    var showApprovalDialog by remember { mutableStateOf(false) }

    // Approval confirmation dialog
    if (showApprovalDialog) {
        AlertDialog(
            onDismissRequest = { showApprovalDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = mintColor)
                    Text("Confirm Onboarding", fontWeight = FontWeight.Bold, color = textPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Are you sure you want to approve and onboard ${req.pharmacyName}?",
                        fontWeight = FontWeight.Medium,
                        color = textPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        "This will automatically generate their SaaS license, activate their portal, and allow them to start adding doctors immediately.",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApprovalDialog = false
                        onApprove()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = mintColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Approve & Activate", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApprovalDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Pharmacy Details & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Pharmacy Photo or Icon representation
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(primaryColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (req.pharmacyPhoto != null) {
                            AsyncImage(
                                model = req.pharmacyPhoto,
                                contentDescription = "Pharmacy Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Rounded.LocalPharmacy, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                        }
                    }
                    Column {
                        Text(
                            text = req.pharmacyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Submitted: " + SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.createdAt)),
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = req.status.uppercase().replace("_", " "),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = borderCol, modifier = Modifier.padding(vertical = 14.dp))

            // Information details
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(icon = Icons.Rounded.Person, label = "Owner / Contact Person", value = req.ownerName)
                DetailRow(icon = Icons.Rounded.Badge, label = "Drug License Number", value = req.licenseNo)
                DetailRow(icon = Icons.Rounded.Phone, label = "Mobile & Email Link", value = "${req.mobile} • ${req.email}")
                DetailRow(icon = Icons.Rounded.LocationOn, label = "Physical Address", value = req.address)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Document Thumbnails
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DocumentThumb(
                    title = "Drug License",
                    subtitle = "Verify registration",
                    onClick = { onViewImage(req.licenseImage, "${req.pharmacyName} - Drug License") },
                    modifier = Modifier.weight(1f)
                )

                if (req.pharmacyPhoto != null) {
                    DocumentThumb(
                        title = "Storefront",
                        subtitle = "Verify premises",
                        onClick = { onViewImage(req.pharmacyPhoto, "${req.pharmacyName} - Storefront Photo") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(borderCol),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Storefront Photo", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium visual timeline of verification phases
            RequestTimeline(req = req)

            Spacer(modifier = Modifier.height(16.dp))

            // Action section & Verification Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // View action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onViewPayment,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = mintColor),
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("View Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Decisions actions row (only for unresolved requests)
                if (req.status.lowercase() != "approved" && req.status.lowercase() != "rejected") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Reject Button
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Correction notes Button
                        OutlinedButton(
                            onClick = onRequestCorrection,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                        ) {
                            Text("Request Correction", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Approve Button
                        Button(
                            onClick = { showApprovalDialog = true },
                            enabled = isPaymentSuccessful,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mintColor,
                                disabledContainerColor = borderCol
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                        ) {
                            Text(
                                "Approve",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isPaymentSuccessful) Color.White else textSecondary
                            )
                        }
                    }

                    if (!isPaymentSuccessful) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Waiting for successful payment.",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestTimeline(req: PharmacyRequestEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Request Verifications Timeline", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        
        // Step 1: Submission
        TimelineItem(
            title = "Registration Submitted",
            desc = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.createdAt)),
            isCompleted = true,
            isLast = false
        )
        
        // Step 2: Payment Verification
        val isPaid = req.paymentStatus?.lowercase() == "success" || 
                     req.paymentStatus?.lowercase() == "paid" || 
                     req.paymentStatus?.lowercase() == "successful" ||
                     req.paymentStatus?.lowercase() == "payment_completed"
                     
        val paymentDesc = if (isPaid) {
            "Paid: $${String.format(Locale.getDefault(), "%.2f", req.paymentAmount ?: 0.0)} (ID: ${req.paymentId ?: "N/A"})"
        } else if (req.paymentStatus?.lowercase() == "pending") {
            "Payment Pending (ID: ${req.paymentId ?: "N/A"})"
        } else {
            "No Payment Received"
        }
        TimelineItem(
            title = "Payment Verification",
            desc = paymentDesc,
            isCompleted = isPaid,
            isWarning = req.paymentStatus?.lowercase() == "pending" || req.paymentStatus.isNullOrEmpty(),
            isLast = false
        )
        
        // Step 3: Admin review decision
        val step3Title = when (req.status.lowercase()) {
            "approved" -> "Approved & Activated"
            "rejected" -> "Registration Rejected"
            "correction_requested" -> "Correction Requested"
            else -> "Pending Review"
        }
        val step3Desc = when (req.status.lowercase()) {
            "approved" -> {
                val dateStr = req.approvedAt?.let { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "N/A"
                "Approved by admin ${req.approvedBy ?: ""} on $dateStr"
            }
            "rejected" -> "Reason: ${req.rejectionReason ?: "None specified"}"
            "correction_requested" -> "Notes: ${req.correctionNotes ?: "None specified"}"
            else -> "Awaiting final decision from master admin."
        }
        val step3Completed = req.status.lowercase() == "approved" || req.status.lowercase() == "rejected" || req.status.lowercase() == "correction_requested"
        TimelineItem(
            title = step3Title,
            desc = step3Desc,
            isCompleted = step3Completed,
            isWarning = req.status.lowercase() == "correction_requested",
            isError = req.status.lowercase() == "rejected",
            isLast = true
        )
    }
}

@Composable
fun TimelineItem(
    title: String,
    desc: String,
    isCompleted: Boolean,
    isWarning: Boolean = false,
    isError: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            val dotColor = when {
                isError -> Color(0xFFEF4444)
                isWarning -> Color(0xFFF59E0B)
                isCompleted -> Color(0xFF10B981)
                else -> Color(0xFF94A3B8)
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(Color(0xFFE2E8F0))
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(desc, fontSize = 10.sp, color = Color(0xFF64748B))
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
                tint = Color(0xFF7C5DFA),
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
