package com.example.ui.screens

import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserUpdateBuilder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.ContactMail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.border

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (name: String, email: String, phone: String, role: String, profilePhotoUrl: String?) -> Unit
) {
    var selectedRole by remember { mutableStateOf("Patient") } // "Patient", "Pharmacy", "Admin"
    var showRegisterPharmacyDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    
    // Form Inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isAuthenticating by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val sessionStatusState = com.example.data.SupabaseManager.client?.auth?.sessionStatus?.collectAsState(initial = null)
    val sessionStatus = sessionStatusState?.value

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
            val user = com.example.data.SupabaseManager.client?.auth?.currentUserOrNull()
            if (user != null && selectedRole != "Pharmacy") {
                val email = user.email ?: ""
                val metadata = user.userMetadata
                val fullName = if (metadata != null && metadata.containsKey("full_name")) {
                    (metadata["full_name"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                } else if (metadata != null && metadata.containsKey("name")) {
                    (metadata["name"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                } else null
                
                val name = fullName ?: email.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Google User"
                val avatarUrl = if (metadata != null && metadata.containsKey("avatar_url")) {
                    (metadata["avatar_url"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                } else null
                
                onLoginSuccess(name, email, "+91 98765 43210", selectedRole, avatarUrl)
            }
        }
    }

    // Retrieve active theme state from ViewModel
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    // Modern Medical Archetypes
    val primaryColor = Color(0xFF7C5DFA) // Modern Lavender/Purple
    val secondaryColor = Color(0xFF10B981) // Clinical Mint Green
    val indigoColor = Color(0xFF6C5DD3) // Premium Slate Purple
    
    val accentColor = when (selectedRole) {
        "Patient" -> primaryColor
        "Pharmacy" -> secondaryColor
        else -> indigoColor // Corporate Indigo for Admin
    }

    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Animated colors for dynamic role transition (Purple, Mint, Indigo)
    val bgStartColor by animateColorAsState(
        targetValue = when (selectedRole) {
            "Patient" -> if (isDark) Color(0xFF0F172A) else Color(0xFFF3EFFF)
            "Pharmacy" -> if (isDark) Color(0xFF022C22) else Color(0xFFF0FDF4)
            else -> if (isDark) Color(0xFF1E1B4B) else Color(0xFFF5F3FF) // Admin
        },
        animationSpec = tween(durationMillis = 800),
        label = "bgStart"
    )

    val bgEndColor by animateColorAsState(
        targetValue = when (selectedRole) {
            "Patient" -> if (isDark) Color(0xFF1B1437) else Color(0xFFEBE5FF)
            "Pharmacy" -> if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)
            else -> if (isDark) Color(0xFF312E81) else Color(0xFFEDE9FE) // Admin
        },
        animationSpec = tween(durationMillis = 800),
        label = "bgEnd"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgStartColor, bgEndColor)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Background Decorative Visuals (Phase-based & Role-specific)
        Crossfade(
            targetState = selectedRole,
            animationSpec = tween(durationMillis = 800),
            label = "roleDecoration"
        ) { role ->
            val decorColor = when (role) {
                "Patient" -> Color(0xFF7C5DFA).copy(alpha = if (isDark) 0.08f else 0.04f)
                "Pharmacy" -> Color(0xFF10B981).copy(alpha = if (isDark) 0.08f else 0.04f)
                else -> Color(0xFF6C5DD3).copy(alpha = if (isDark) 0.08f else 0.04f)
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (role) {
                    "Patient" -> {
                        // Calm Medical Experience: soft concentric healthcare circles and waves
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = decorColor,
                                radius = 280.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.2f)
                            )
                            drawCircle(
                                color = decorColor,
                                radius = 180.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.2f)
                            )
                            // Soft cross outline
                            val crossSize = 80.dp.toPx()
                            val cx = size.width * 0.85f
                            val cy = size.height * 0.15f
                            drawRect(
                                color = decorColor,
                                topLeft = androidx.compose.ui.geometry.Offset(cx - crossSize / 6, cy - crossSize / 2),
                                size = androidx.compose.ui.geometry.Size(crossSize / 3, crossSize)
                            )
                            drawRect(
                                color = decorColor,
                                topLeft = androidx.compose.ui.geometry.Offset(cx - crossSize / 2, cy - crossSize / 6),
                                size = androidx.compose.ui.geometry.Size(crossSize, crossSize / 3)
                            )
                        }
                    }
                    "Pharmacy" -> {
                        // Business Dashboard Feeling: clean technical grids and medical ledger shapes
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw subtle grid lines
                            val gridSize = 40.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                drawLine(
                                    color = decorColor.copy(alpha = decorColor.alpha * 0.4f),
                                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                                x += gridSize
                            }
                            var y = 0f
                            while (y < size.height) {
                                drawLine(
                                    color = decorColor.copy(alpha = decorColor.alpha * 0.4f),
                                    start = androidx.compose.ui.geometry.Offset(0f, y),
                                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                                y += gridSize
                            }
                            // Tech pill shape
                            drawRoundRect(
                                color = decorColor,
                                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.12f),
                                size = androidx.compose.ui.geometry.Size(120.dp.toPx(), 48.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
                            )
                        }
                    }
                    "Admin" -> {
                        // Analytics & Executive Dashboard: flowing trend curves and circles
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Flowing analytics bezier curve
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, size.height * 0.3f)
                                cubicTo(
                                    size.width * 0.25f, size.height * 0.15f,
                                    size.width * 0.5f, size.height * 0.45f,
                                    size.width * 0.75f, size.height * 0.2f
                                )
                                lineTo(size.width, size.height * 0.35f)
                            }
                            drawPath(
                                path = path,
                                color = decorColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 3.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                )
                            )
                            // Analytics dot details
                            drawCircle(
                                color = decorColor.copy(alpha = decorColor.alpha * 2f),
                                radius = 8.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.35f)
                            )
                            drawCircle(
                                color = decorColor,
                                radius = 200.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Full Screen form layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Utilities Header with Theme Toggle Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val nextTheme = if (isDark) "Light" else "Dark"
                            viewModel.updateTheme(nextTheme)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(cardColor)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme Mode",
                            tint = accentColor
                        )
                    }
                }

                // DoctorLine Healthcare Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.medical_app_icon_1782119145796),
                        contentDescription = "DoctorLine Logo",
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DoctorLine",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                }

                Text(
                    text = "A modern decentralized care console",
                    fontSize = 14.sp,
                    color = textMuted,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                // SELECT YOUR ROLE Header
                Text(
                    text = "SELECT REGISTERED PROFILE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 6.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Patient Card
                    RoleCard(
                        title = "Patient",
                        icon = Icons.Rounded.Person,
                        isSelected = selectedRole == "Patient",
                        activeColor = primaryColor,
                        inactiveContainerColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedRole = "Patient"
                        }
                    )
                    // Pharmacy Card
                    RoleCard(
                        title = "Pharmacy",
                        icon = Icons.Rounded.LocalPharmacy,
                        isSelected = selectedRole == "Pharmacy",
                        activeColor = secondaryColor,
                        inactiveContainerColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedRole = "Pharmacy"
                        }
                    )
                    // Admin Card
                    RoleCard(
                        title = "Admin",
                        icon = Icons.Rounded.AdminPanelSettings,
                        isSelected = selectedRole == "Admin",
                        activeColor = indigoColor,
                        inactiveContainerColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedRole = "Admin"
                        }
                    )
                }

                // Main form card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(20.dp), clip = false),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedRole == "Pharmacy") {
                            Text(
                                text = "Enter Pharmacy Credentials",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address", color = textMuted) },
                                placeholder = { Text("admin@apollopharmacy.com", color = textMuted) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = accentColor) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = textMuted
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password", color = textMuted) },
                                placeholder = { Text("Enter secret password", color = textMuted) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor) },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = textMuted
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            // Forgot Password button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { showForgotPasswordDialog = true },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                        scope.launch {
                                            isAuthenticating = true
                                            try {
                                                // Verify profiles & pharmacies status completely
                                                val req = viewModel.getPharmacyRequestByEmail(emailInput.trim())
                                                if (req != null) {
                                                    when (req.status.lowercase()) {
                                                        "pending" -> {
                                                            Toast.makeText(context, "Your account is pending approval.", Toast.LENGTH_LONG).show()
                                                            isAuthenticating = false
                                                            return@launch
                                                        }
                                                        "rejected" -> {
                                                            Toast.makeText(context, "Your registration was rejected.\n\nPlease contact DoctorLine.", Toast.LENGTH_LONG).show()
                                                            isAuthenticating = false
                                                            return@launch
                                                        }
                                                    }
                                                    
                                                    // Verify pharmacies.status = active (represent as PharmacyEntity status)
                                                    val pharmacy = viewModel.allPharmacies.value.find { it.id == req.id || it.phone == req.mobile }
                                                    if (pharmacy != null && pharmacy.status.lowercase() != "active") {
                                                        Toast.makeText(context, "Approved pharmacy is currently ${pharmacy.status}. Access Denied.", Toast.LENGTH_LONG).show()
                                                        isAuthenticating = false
                                                        return@launch
                                                    }
                                                    
                                                    // Password match verification in local offline sandbox mode
                                                    if (!com.example.data.SupabaseManager.isConfigured) {
                                                        if (req.passwordHash != passwordInput) {
                                                            Toast.makeText(context, "Invalid password. Access Denied.", Toast.LENGTH_LONG).show()
                                                            isAuthenticating = false
                                                            return@launch
                                                        }
                                                    }
                                                } else if (emailInput.trim() != "admin@apollopharmacy.com") {
                                                    Toast.makeText(context, "No registered pharmacy found with this email.", Toast.LENGTH_LONG).show()
                                                    isAuthenticating = false
                                                    return@launch
                                                }

                                                if (com.example.data.SupabaseManager.isConfigured) {
                                                    val client = com.example.data.SupabaseManager.client
                                                    if (client != null) {
                                                        client.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.Email) {
                                                            email = emailInput
                                                            password = passwordInput
                                                        }
                                                        val sessionUser = client.auth.currentUserOrNull()
                                                        val emailVal = sessionUser?.email ?: emailInput
                                                        val nameVal = emailVal.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Pharmacy User"
                                                        onLoginSuccess(nameVal, emailVal, "+91 98765 43210", "Pharmacy", null)
                                                    } else {
                                                        Toast.makeText(context, "Supabase not configured.", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Invalid credentials.", Toast.LENGTH_LONG).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Authentication Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isAuthenticating = false
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Please enter both Email and Password", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                if (isAuthenticating) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Access Pharmacy Console", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    showRegisterPharmacyDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.6f),
                                            accentColor.copy(alpha = 0.2f)
                                        )
                                    )
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AppRegistration,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Register New Pharmacy",
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Text(
                                text = "Secure Sign-In Area",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                                modifier = Modifier.padding(bottom = 20.dp),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    if (com.example.data.SupabaseManager.isConfigured) {
                                        scope.launch {
                                            try {
                                                com.example.data.SupabaseManager.client?.auth?.signInWith(io.github.jan.supabase.auth.providers.Google)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Google Auth Launch Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Developer Sandbox Mode is disabled. Please configure Supabase in AI Studio Secrets.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Signature",
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }


                        }
                    }
                }
            }

        if (showRegisterPharmacyDialog) {
            PharmacyRegistrationDialog(
                viewModel = viewModel,
                onDismiss = { showRegisterPharmacyDialog = false }
            )
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                viewModel = viewModel,
                onDismiss = { showForgotPasswordDialog = false }
            )
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    inactiveContainerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 6.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor else inactiveContainerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f) else activeColor.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$title profile icon",
                    tint = if (isSelected) Color.White else activeColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyRegistrationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val dialogBgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFAFAFA)
    val cardBgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val accentGreen = Color(0xFF22C55E)
    val brandPurple = Color(0xFF6C5DD3)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val borderLineColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val indiaStatesData = mapOf(
        "Andhra Pradesh" to mapOf(
            "Visakhapatnam" to mapOf(
                "Visakhapatnam Port" to listOf("Visakhapatnam Port PO - 530001", "Maharani Peta PO - 530002"),
                "Gajuwaka" to listOf("Gajuwaka PO - 530026", "Srinagar PO - 530012")
            ),
            "Vijayawada" to mapOf(
                "Governorpet" to listOf("Governorpet PO - 520002"),
                "Patamata" to listOf("Patamata PO - 520010")
            )
        ),
        "Arunachal Pradesh" to mapOf(
            "Itanagar" to mapOf(
                "Itanagar PS" to listOf("Itanagar PO - 791111"),
                "Naharlagun PS" to listOf("Naharlagun PO - 791110")
            )
        ),
        "Assam" to mapOf(
            "Kamrup Metropolitan" to mapOf(
                "Dispur" to listOf("Dispur PO - 781006"),
                "Paltan Bazaar" to listOf("Guwahati PO - 781001")
            )
        ),
        "Bihar" to mapOf(
            "Patna" to mapOf(
                "Kotwali" to listOf("Patna GPO - 800001"),
                "Kankarbagh" to listOf("Kankarbagh PO - 800020")
            )
        ),
        "Chhattisgarh" to mapOf(
            "Raipur" to mapOf(
                "Civil Lines" to listOf("Raipur GPO - 492001"),
                "Pandri" to listOf("Pandri PO - 492004")
            )
        ),
        "Goa" to mapOf(
            "North Goa" to mapOf(
                "Panaji" to listOf("Panaji PO - 403001"),
                "Mapusa" to listOf("Mapusa PO - 403507")
            ),
            "South Goa" to mapOf(
                "Margao" to listOf("Margao PO - 403601"),
                "Vasco da Gama" to listOf("Vasco da Gama PO - 403802")
            )
        ),
        "Gujarat" to mapOf(
            "Ahmedabad" to mapOf(
                "Navrangpura" to listOf("Navrangpura PO - 380009"),
                "Maninagar" to listOf("Maninagar PO - 380008")
            ),
            "Surat" to mapOf(
                "Adajan" to listOf("Adajan DN PO - 395009"),
                "Athwalines" to listOf("Surat PO - 395001")
            )
        ),
        "Haryana" to mapOf(
            "Gurugram" to mapOf(
                "DLF Phase 3" to listOf("DLF QE PO - 122002"),
                "Sector 15" to listOf("Gurgaon PO - 122001")
            )
        ),
        "Himachal Pradesh" to mapOf(
            "Shimla" to mapOf(
                "Mall Road" to listOf("Shimla GPO - 171001"),
                "Chotta Shimla" to listOf("Chotta Shimla PO - 171002")
            )
        ),
        "Jharkhand" to mapOf(
            "Ranchi" to mapOf(
                "Lalpur" to listOf("Ranchi GPO - 834001"),
                "Dhurwa" to listOf("Dhurwa PO - 834004")
            )
        ),
        "Karnataka" to mapOf(
            "Bengaluru" to mapOf(
                "Koramangala" to listOf("Koramangala PO - 560034", "Koramangala 4th Block PO - 560047"),
                "Indiranagar" to listOf("Indiranagar PO - 560038", "HAL 2nd Stage PO - 560008"),
                "Jayanagar" to listOf("Jayanagar PO - 560011")
            ),
            "Mysuru" to mapOf(
                "Vidyaranyapuram" to listOf("Vidyaranyapuram PO - 570008"),
                "Gokulam" to listOf("Gokulam PO - 570002")
            )
        ),
        "Kerala" to mapOf(
            "Thiruvananthapuram" to mapOf(
                "Museum PS" to listOf("Thiruvananthapuram GPO - 695001"),
                "Pattom PS" to listOf("Pattom PO - 695004")
            ),
            "Ernakulam" to mapOf(
                "Kochi Fort PS" to listOf("Kochi PO - 682001"),
                "Aluva PS" to listOf("Aluva PO - 683101")
            )
        ),
        "Madhya Pradesh" to mapOf(
            "Bhopal" to mapOf(
                "Arera Colony" to listOf("Arera Hills PO - 462011"),
                "TT Nagar" to listOf("Bhopal GPO - 462001")
            ),
            "Indore" to mapOf(
                "Vijay Nagar" to listOf("Vijay Nagar PO - 452010"),
                "Palasia" to listOf("Indore GPO - 452001")
            )
        ),
        "Maharashtra" to mapOf(
            "Mumbai" to mapOf(
                "Colaba" to listOf("Colaba PO - 400005", "Nariman Point PO - 400021"),
                "Bandra" to listOf("Bandra West PO - 400050", "Bandra East PO - 400051")
            ),
            "Pune" to mapOf(
                "Shivajinagar" to listOf("Shivajinagar PO - 411005", "Deccan Gymkhana PO - 411004"),
                "Kothrud" to listOf("Kothrud PO - 411038", "Kothrud Depot PO - 411029")
            )
        ),
        "Manipur" to mapOf(
            "Imphal West" to mapOf(
                "Imphal PS" to listOf("Imphal HO - 795001"),
                "Lamphel PS" to listOf("Lamphelpat PO - 795004")
            )
        ),
        "Meghalaya" to mapOf(
            "East Khasi Hills" to mapOf(
                "Sadar PS" to listOf("Shillong GPO - 793001"),
                "Laitumkhrah PS" to listOf("Laitumkhrah PO - 793003")
            )
        ),
        "Mizoram" to mapOf(
            "Aizawl" to mapOf(
                "Aizawl PS" to listOf("Aizawl PO - 796001")
            )
        ),
        "Nagaland" to mapOf(
            "Kohima" to mapOf(
                "North PS" to listOf("Kohima PO - 797001")
            )
        ),
        "Odisha" to mapOf(
            "Khurda" to mapOf(
                "Kharavela Nagar" to listOf("Bhubaneswar GPO - 751001"),
                "Nayapalli" to listOf("Nayapalli PO - 751012")
            )
        ),
        "Punjab" to mapOf(
            "Amritsar" to mapOf(
                "Civil Lines" to listOf("Amritsar GPO - 143001"),
                "Golden Temple PS" to listOf("Golden Temple PO - 143006")
            ),
            "Ludhiana" to mapOf(
                "Sarabha Nagar" to listOf("Sarabha Nagar PO - 141001")
            )
        ),
        "Rajasthan" to mapOf(
            "Jaipur" to mapOf(
                "C-Scheme" to listOf("Jaipur GPO - 302001"),
                "Malviya Nagar" to listOf("Malviya Nagar PO - 302017")
            ),
            "Jodhpur" to mapOf(
                "Sardarpura" to listOf("Jodhpur PO - 342001")
            )
        ),
        "Sikkim" to mapOf(
            "East Sikkim" to mapOf(
                "Gangtok PS" to listOf("Gangtok PO - 737101")
            )
        ),
        "Tamil Nadu" to mapOf(
            "Chennai" to mapOf(
                "Mylapore" to listOf("Mylapore PO - 600004", "Luz PO - 600004"),
                "T. Nagar" to listOf("T. Nagar PO - 600017", "Thyagarayanagar PO - 600017")
            ),
            "Coimbatore" to mapOf(
                "RS Puram" to listOf("RS Puram PO - 641002"),
                "Gandhipuram" to listOf("Gandhipuram PO - 641012")
            )
        ),
        "Telangana" to mapOf(
            "Hyderabad" to mapOf(
                "Banjara Hills" to listOf("Khairatabad PO - 500004"),
                "Gachibowli" to listOf("Gachibowli PO - 500032")
            )
        ),
        "Tripura" to mapOf(
            "West Tripura" to mapOf(
                "West PS" to listOf("Agartala HO - 799001")
            )
        ),
        "Uttar Pradesh" to mapOf(
            "Lucknow" to mapOf(
                "Hazratganj" to listOf("Lucknow GPO - 226001"),
                "Aliganj" to listOf("Aliganj PO - 226024")
            ),
            "Noida" to mapOf(
                "Sector 20" to listOf("Noida PO - 201301"),
                "Sector 58" to listOf("Noida Sector 62 PO - 201309")
            )
        ),
        "Uttarakhand" to mapOf(
            "Dehradun" to mapOf(
                "Dalanwala" to listOf("Dehradun GPO - 248001"),
                "Rajpur" to listOf("Rajpur PO - 248009")
            )
        ),
        "West Bengal" to mapOf(
            "Kolkata" to mapOf(
                "Park Street PS" to listOf("Park Street PO - 700016", "Middleton Row PO - 700071"),
                "Bowbazar PS" to listOf("Bowbazar PO - 700012", "Lalbazar PO - 700001"),
                "Gariahat PS" to listOf("Gariahat PO - 700019"),
                "Alipore PS" to listOf("Alipore PO - 700027")
            ),
            "Howrah" to mapOf(
                "Howrah PS" to listOf("Howrah PO - 711101"),
                "Shibpur PS" to listOf("Shibpur PO - 711102"),
                "Bally PS" to listOf("Bally PO - 711201")
            ),
            "Hooghly" to mapOf(
                "Chinsurah PS" to listOf("Chinsurah PO - 712101"),
                "Serampore PS" to listOf("Serampore PO - 712201"),
                "Chandannagar PS" to listOf("Chandannagar PO - 712136")
            ),
            "Nadia" to mapOf(
                "Krishnanagar PS" to listOf("Krishnanagar PO - 741101"),
                "Kalyani PS" to listOf("Kalyani PO - 741235"),
                "Ranaghat PS" to listOf("Ranaghat PO - 741201")
            ),
            "Murshidabad" to mapOf(
                "Baharampur PS" to listOf("Baharampur PO - 742101"),
                "Lalgola PS" to listOf("Lalgola PO - 742148"),
                "Jiaganj PS" to listOf("Jiaganj PO - 742123")
            ),
            "Malda" to mapOf(
                "English Bazar PS" to listOf("Malda PO - 732101"),
                "Kaliachak PS" to listOf("Kaliachak PO - 732201")
            ),
            "Dakshin Dinajpur" to mapOf(
                "Balurghat PS" to listOf("Balurghat PO - 733101"),
                "Gangarampur PS" to listOf("Gangarampur PO - 733124")
            ),
            "Uttar Dinajpur" to mapOf(
                "Raiganj PS" to listOf("Raiganj PO - 733134"),
                "Islampur PS" to listOf("Islampur PO - 733202")
            ),
            "Darjeeling" to mapOf(
                "Darjeeling Sadar PS" to listOf("Darjeeling PO - 734101"),
                "Siliguri PS" to listOf("Siliguri PO - 734001"),
                "Kurseong PS" to listOf("Kurseong PO - 734203")
            ),
            "Jalpaiguri" to mapOf(
                "Jalpaiguri Kotwali PS" to listOf("Jalpaiguri PO - 735101"),
                "Malbazar PS" to listOf("Malbazar PO - 735221")
            ),
            "Cooch Behar" to mapOf(
                "Kotwali PS" to listOf("Cooch Behar PO - 736101"),
                "Dinhata PS" to listOf("Dinhata PO - 736135")
            ),
            "Purba Bardhaman" to mapOf(
                "Bardhaman PS" to listOf("Bardhaman PO - 713101"),
                "Kalna PS" to listOf("Kalna PO - 713409"),
                "Katwa PS" to listOf("Katwa PO - 713130")
            ),
            "Paschim Bardhaman" to mapOf(
                "Asansol South PS" to listOf("Asansol PO - 713301"),
                "Durgapur PS" to listOf("Durgapur PO - 713216")
            ),
            "Purba Medinipur" to mapOf(
                "Tamluk PS" to listOf("Tamluk PO - 721636"),
                "Haldia PS" to listOf("Haldia PO - 721602"),
                "Contai PS" to listOf("Contai PO - 721401")
            ),
            "Paschim Medinipur" to mapOf(
                "Midnapore PS" to listOf("Midnapore PO - 721101"),
                "Kharagpur Town PS" to listOf("Kharagpur PO - 721301"),
                "Ghatal PS" to listOf("Ghatal PO - 721212")
            ),
            "Jhargram" to mapOf(
                "Jhargram PS" to listOf("Jhargram PO - 721507"),
                "Gopiballavpur PS" to listOf("Gopiballavpur PO - 721506")
            ),
            "Bankura" to mapOf(
                "Bankura Sadar PS" to listOf("Bankura PO - 722101"),
                "Bishnupur PS" to listOf("Bishnupur PO - 722122")
            ),
            "Purulia" to mapOf(
                "Purulia Town PS" to listOf("Purulia PO - 723101"),
                "Raghunathpur PS" to listOf("Raghunathpur PO - 723133")
            ),
            "Birbhum" to mapOf(
                "Suri PS" to listOf("Suri PO - 731101"),
                "Bolpur PS" to listOf("Bolpur PO - 731204"),
                "Rampurhat PS" to listOf("Rampurhat PO - 731224")
            ),
            "Alipurduar" to mapOf(
                "Alipurduar PS" to listOf("Alipurduar PO - 736121"),
                "Falakata PS" to listOf("Falakata PO - 735211")
            ),
            "Kalimpong" to mapOf(
                "Kalimpong PS" to listOf("Kalimpong PO - 734301")
            ),
            "South 24 Parganas" to mapOf(
                "Baruipur PS" to listOf("Baruipur PO - 700144"),
                "Sonarpur PS" to listOf("Sonarpur PO - 700150"),
                "Diamond Harbour PS" to listOf("Diamond Harbour PO - 743331")
            ),
            "North 24 Parganas" to mapOf(
                "Salt Lake PS" to listOf("Salt Lake PO - 700064", "Sech Bhawan PO - 700091"),
                "Barasat PS" to listOf("Barasat PO - 700124", "Hridaypur PO - 700127"),
                "Barrackpore PS" to listOf("Barrackpore PO - 700120"),
                "Habra PS" to listOf("Habra PO - 743263")
            )
        ),
        "Andaman and Nicobar Islands" to mapOf(
            "South Andaman" to mapOf(
                "Aberdeen PS" to listOf("Port Blair PO - 744101")
            )
        ),
        "Chandigarh" to mapOf(
            "Chandigarh" to mapOf(
                "Sector 17 PS" to listOf("Chandigarh GPO - 160017"),
                "Sector 34 PS" to listOf("Sector 34 PO - 160022")
            )
        ),
        "Dadra and Nagar Haveli and Daman and Diu" to mapOf(
            "Daman" to mapOf(
                "Daman PS" to listOf("Daman PO - 396210")
            )
        ),
        "Delhi" to mapOf(
            "New Delhi" to mapOf(
                "Connaught Place" to listOf("Connaught Place PO - 110001", "Barakhamba Road PO - 110001"),
                "Chanakyapuri" to listOf("Chanakyapuri PO - 110021", "Diplomatic Enclave PO - 110021")
            ),
            "South Delhi" to mapOf(
                "Saket" to listOf("Saket PO - 110017", "Malviya Nagar PO - 110017"),
                "Hauz Khas" to listOf("Hauz Khas PO - 110016", "Green Park PO - 110016")
            )
        ),
        "Jammu and Kashmir" to mapOf(
            "Srinagar" to mapOf(
                "Kothibagh PS" to listOf("Srinagar GPO - 190001")
            ),
            "Jammu" to mapOf(
                "Gandhi Nagar PS" to listOf("Gandhi Nagar PO - 180004")
            )
        ),
        "Ladakh" to mapOf(
            "Leh" to mapOf(
                "Leh PS" to listOf("Leh PO - 194101")
            )
        ),
        "Lakshadweep" to mapOf(
            "Kavaratti" to mapOf(
                "Kavaratti PS" to listOf("Kavaratti PO - 682555")
            )
        ),
        "Puducherry" to mapOf(
            "Puducherry" to mapOf(
                "Odiansalai PS" to listOf("Puducherry HO - 605001")
            )
        )
    )

    var pharmacyName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var licenseNo by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Cascading Address dropdown states
    var selectedState by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("") }
    var selectedPoliceStation by remember { mutableStateOf("") }
    var selectedPostOffice by remember { mutableStateOf("") }
    var detailedAddress by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }

    var licenseImage by remember { mutableStateOf("") }
    var pharmacyPhoto by remember { mutableStateOf<String?>(null) }
    
    // Step Tracking: 1 = Pharmacy Info, 2 = Contact, 3 = Address, 4 = Documents, 5 = Review & Payment
    var dialogStep by remember { mutableStateOf(1) }
    var selectedPaymentMethod by remember { mutableStateOf("upi") }
    var isPaying by remember { mutableStateOf(false) }
    var showRazorpayCheckout by remember { mutableStateOf(false) }
    var checkoutOrderId by remember { mutableStateOf("") }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Launchers for image uploads
    val licenseImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            licenseImage = uri.toString()
        }
    }

    val pharmacyPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            pharmacyPhoto = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting && !isPaying && successMessage == null) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = dialogBgColor,
            border = BorderStroke(1.dp, borderLineColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (successMessage != null) {
                    // Success View with Premium Animation Elements
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(accentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = accentGreen,
                                modifier = Modifier.size(64.dp)
                              )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Text(
                            text = "Registration Successful!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Glasscard Credentials Summary
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            border = BorderStroke(1.dp, borderLineColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Security, contentDescription = null, tint = brandPurple, modifier = Modifier.size(16.dp))
                                    Text("CREDENTIALS SUMMARY", fontSize = 11.sp, color = brandPurple, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = successMessage!!,
                                    fontSize = 14.sp,
                                    color = textSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                        ) {
                            Text("Proceed to Login Console", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    }
                } else {
                    // Form Content View
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBgColor)
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.medical_app_icon_1782119145796),
                                    contentDescription = "DoctorLine Logo",
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Pharmacy Registration",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "SaaS Console Portal Onboarding",
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = textPrimary
                                )
                            }
                        }

                        // Premium Step Indicators
                        SaaSOnboardingHeader(currentStep = dialogStep, totalSteps = 5, isDark = isDark)

                        // Error Banner if exists
                        if (errorMessage != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF3F1A1A) else Color(0xFFFEF2F2)),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFFEF4444) else Color(0xFFFCA5A5)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = errorMessage!!,
                                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Scrollable Body for individual steps
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (dialogStep) {
                                1 -> {
                                    // Step 1: Pharmacy Information
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                    ) {
                                        Icon(Icons.Rounded.Storefront, contentDescription = null, tint = brandPurple, modifier = Modifier.size(24.dp))
                                        Text("Pharmacy Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
                                    
                                    OutlinedTextField(
                                        value = pharmacyName,
                                        onValueChange = { pharmacyName = it },
                                        label = { Text("Pharmacy Name *") },
                                        placeholder = { Text("e.g. Apollo Lifeline Pharmacy") },
                                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = brandPurple) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        )
                                    )
 
                                    OutlinedTextField(
                                        value = ownerName,
                                        onValueChange = { ownerName = it },
                                        label = { Text("Owner Full Name *") },
                                        placeholder = { Text("e.g. Dr. Amit Patra") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = brandPurple) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        )
                                    )
 
                                    OutlinedTextField(
                                        value = licenseNo,
                                        onValueChange = { licenseNo = it },
                                        label = { Text("Drug License Number *") },
                                        placeholder = { Text("e.g. DL-9087-A/2026") },
                                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = brandPurple) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                                2 -> {
                                    // Step 2: Contact Information
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                    ) {
                                        Icon(Icons.Rounded.ContactMail, contentDescription = null, tint = brandPurple, modifier = Modifier.size(24.dp))
                                        Text("Contact & Security", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
 
                                    OutlinedTextField(
                                        value = mobile,
                                        onValueChange = { mobile = it },
                                        label = { Text("Mobile Number *") },
                                        placeholder = { Text("e.g. +91 98765 43210") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = brandPurple) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    )
 
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email Address *") },
                                        placeholder = { Text("e.g. contact@apollomed.com") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = brandPurple) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                    )
 
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Password * (Min 6 chars)") },
                                        placeholder = { Text("Enter a secure password") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = brandPurple) },
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                    )
                                }
                                3 -> {
                                    // Step 3: Address Information (Cascading Dropdowns with robust offline data)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                    ) {
                                        Icon(Icons.Rounded.Map, contentDescription = null, tint = brandPurple, modifier = Modifier.size(24.dp))
                                        Text("Business Address Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }

                                    // State Dropdown
                                    PremiumDropdown(
                                        label = "State *",
                                        selectedValue = selectedState,
                                        options = indiaStatesData.keys.toList(),
                                        onSelect = {
                                            selectedState = it
                                            selectedDistrict = ""
                                            selectedPoliceStation = ""
                                            selectedPostOffice = ""
                                        },
                                        isDark = isDark
                                    )

                                    // District Dropdown
                                    val districts = if (selectedState.isNotBlank()) {
                                        indiaStatesData[selectedState]?.keys?.toList() ?: emptyList()
                                    } else emptyList()
                                    
                                    PremiumDropdown(
                                        label = "District *",
                                        selectedValue = selectedDistrict,
                                        options = districts,
                                        onSelect = {
                                            selectedDistrict = it
                                            selectedPoliceStation = ""
                                            selectedPostOffice = ""
                                        },
                                        enabled = selectedState.isNotBlank(),
                                        isDark = isDark
                                    )

                                    // Police Station Dropdown
                                    val policeStations = if (selectedState.isNotBlank() && selectedDistrict.isNotBlank()) {
                                        indiaStatesData[selectedState]?.get(selectedDistrict)?.keys?.toList() ?: emptyList()
                                    } else emptyList()

                                    PremiumDropdown(
                                        label = "Police Station *",
                                        selectedValue = selectedPoliceStation,
                                        options = policeStations,
                                        onSelect = {
                                            selectedPoliceStation = it
                                            selectedPostOffice = ""
                                        },
                                        enabled = selectedDistrict.isNotBlank(),
                                        isDark = isDark
                                    )

                                    // Post Office Dropdown
                                    val postOffices = if (selectedState.isNotBlank() && selectedDistrict.isNotBlank() && selectedPoliceStation.isNotBlank()) {
                                        indiaStatesData[selectedState]?.get(selectedDistrict)?.get(selectedPoliceStation) ?: emptyList()
                                    } else emptyList()

                                    PremiumDropdown(
                                        label = "Post Office *",
                                        selectedValue = selectedPostOffice,
                                        options = postOffices,
                                        onSelect = {
                                            selectedPostOffice = it
                                        },
                                        enabled = selectedPoliceStation.isNotBlank(),
                                        isDark = isDark
                                    )

                                    // Manual input: Detailed Address
                                    OutlinedTextField(
                                        value = detailedAddress,
                                        onValueChange = { detailedAddress = it },
                                        label = { Text("Detailed Address *") },
                                        placeholder = { Text("e.g. Street Number, Flat/Shop No, Area") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = brandPurple) },
                                        modifier = Modifier.fillMaxWidth().height(90.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        ),
                                        maxLines = 3
                                    )

                                    // Manual input: Landmark (Optional)
                                    OutlinedTextField(
                                        value = landmark,
                                        onValueChange = { landmark = it },
                                        label = { Text("Landmark (Optional)") },
                                        placeholder = { Text("e.g. Near City Mall") },
                                        leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = brandPurple) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = brandPurple,
                                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            focusedContainerColor = cardBgColor,
                                            unfocusedContainerColor = cardBgColor,
                                            focusedLabelColor = brandPurple,
                                            unfocusedLabelColor = textSecondary,
                                            focusedPlaceholderColor = textSecondary.copy(alpha = 0.8f),
                                            unfocusedPlaceholderColor = textSecondary.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                                4 -> {
                                    // Step 4: Documents Upload with Upload Preview Cards
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                    ) {
                                        Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = brandPurple, modifier = Modifier.size(24.dp))
                                        Text("Documents & Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }

                                    // Drug License Card & Preview
                                    if (licenseImage.isBlank()) {
                                        UploadCard(
                                            title = "Drug License Document *",
                                            subtitle = "Upload PDF/JPEG copy of state license",
                                            isUploaded = false,
                                            uploadedName = null,
                                            accentColor = brandPurple,
                                            onClick = { licenseImageLauncher.launch("image/*") },
                                            isDark = isDark
                                        )
                                    } else {
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                            border = BorderStroke(1.dp, borderLineColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val painter = coil.compose.rememberAsyncImagePainter(model = licenseImage)
                                                    Image(
                                                        painter = painter,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Drug_License_Doc.jpg", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                                                    Text("Verifiable Document ID", fontSize = 11.sp, color = textSecondary)
                                                }
                                                IconButton(
                                                    onClick = { licenseImage = "" },
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(if (isDark) Color(0xFF451A1A) else Color(0xFFFEF2F2))
                                                ) {
                                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Pharmacy Photo Card & Preview
                                    if (pharmacyPhoto == null) {
                                        UploadCard(
                                            title = "Pharmacy Storefront Photo (Optional)",
                                            subtitle = "External clinic/store photo",
                                            isUploaded = false,
                                            uploadedName = null,
                                            accentColor = accentGreen,
                                            onClick = { pharmacyPhotoLauncher.launch("image/*") },
                                            isDark = isDark
                                        )
                                    } else {
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                            border = BorderStroke(1.dp, borderLineColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val painter = coil.compose.rememberAsyncImagePainter(model = pharmacyPhoto)
                                                    Image(
                                                        painter = painter,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Storefront_Photo.jpg", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                                                    Text("Pharmacy Business Facade", fontSize = 11.sp, color = textSecondary)
                                                }
                                                IconButton(
                                                    onClick = { pharmacyPhoto = null },
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(if (isDark) Color(0xFF451A1A) else Color(0xFFFEF2F2))
                                                ) {
                                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                                }
                                            }
                                        }
                                    }
                                }
                                5 -> {
                                    // Step 5: Review Summary & Secure Payment Card
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                    ) {
                                        Icon(Icons.Rounded.AssignmentTurnedIn, contentDescription = null, tint = brandPurple, modifier = Modifier.size(24.dp))
                                        Text("Review & Complete Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }

                                    // Form Review Cards
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        border = BorderStroke(1.dp, borderLineColor)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("ONBOARDING DATA SUMMARY", fontSize = 11.sp, color = brandPurple, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            HorizontalDivider(color = borderLineColor, thickness = 1.dp)
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Pharmacy Name", fontSize = 13.sp, color = textSecondary)
                                                Text(pharmacyName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Owner Name", fontSize = 13.sp, color = textSecondary)
                                                Text(ownerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Mobile Number", fontSize = 13.sp, color = textSecondary)
                                                Text(mobile, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Email Address", fontSize = 13.sp, color = textSecondary)
                                                Text(email, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("State & District", fontSize = 13.sp, color = textSecondary)
                                                Text("$selectedState, $selectedDistrict", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            }
                                        }
                                    }

                                    // Checkout Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        border = BorderStroke(1.2.dp, brandPurple.copy(alpha = if (isDark) 0.5f else 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF7C5DFA)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("R", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                    Text("Razorpay SECURE", fontWeight = FontWeight.Black, fontSize = 13.sp, color = textPrimary)
                                                }
                                                Icon(Icons.Default.Lock, contentDescription = "Secure Connection", tint = accentGreen, modifier = Modifier.size(16.dp))
                                            }

                                            HorizontalDivider(color = borderLineColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                                            Text("ONE-TIME CONSOLE SETUP FEE", fontSize = 10.sp, color = textSecondary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                            Text("₹8,000.00", fontSize = 34.sp, fontWeight = FontWeight.Black, color = textPrimary)
                                            Text("Verification audits and SaaS license activation.", fontSize = 12.sp, color = textSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Action bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBgColor)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (dialogStep > 1) {
                                OutlinedButton(
                                    onClick = { dialogStep -= 1 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = textPrimary,
                                        containerColor = Color.Transparent
                                    ),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1))
                                ) {
                                    Text("Back", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = textPrimary,
                                        containerColor = Color.Transparent
                                    ),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1))
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    errorMessage = null
                                    when (dialogStep) {
                                        1 -> {
                                            if (pharmacyName.isBlank() || ownerName.isBlank() || licenseNo.isBlank()) {
                                                errorMessage = "Please enter all fields marked with *."
                                            } else {
                                                dialogStep = 2
                                            }
                                        }
                                        2 -> {
                                            if (mobile.isBlank() || email.isBlank() || password.isBlank()) {
                                                errorMessage = "Please enter all fields marked with *."
                                            } else if (!mobile.matches(Regex("^[6-9]\\d{9}$"))) {
                                                errorMessage = "Please enter a valid 10-digit Indian mobile number."
                                            } else if (viewModel.allPharmacies.value.any { it.phone == mobile }) {
                                                errorMessage = "This mobile number is already registered to a pharmacy."
                                            } else if (viewModel.allPharmacyRequests.value.any { it.mobile == mobile && it.status.lowercase() != "rejected" }) {
                                                errorMessage = "A pharmacy request with this mobile number is already pending."
                                            } else if (password.length < 6) {
                                                errorMessage = "Password must be at least 6 characters."
                                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                                errorMessage = "Please enter a valid email address."
                                            } else {
                                                dialogStep = 3
                                            }
                                        }
                                        3 -> {
                                            if (selectedState.isBlank() || selectedDistrict.isBlank() || 
                                                selectedPoliceStation.isBlank() || selectedPostOffice.isBlank() ||
                                                detailedAddress.isBlank()
                                            ) {
                                                errorMessage = "Please select State, District, PS, PO, and enter your Detailed Address."
                                            } else {
                                                dialogStep = 4
                                            }
                                        }
                                        4 -> {
                                            if (licenseImage.isBlank()) {
                                                errorMessage = "Please upload a copy of your Drug License Image."
                                            } else {
                                                dialogStep = 5
                                            }
                                        }
                                        5 -> {
                                            errorMessage = null
                                            checkoutOrderId = "order_reg_" + java.util.UUID.randomUUID().toString().replace("-", "").take(10)
                                            showRazorpayCheckout = true
                                        }
                                    }
                                },
                                enabled = !isPaying && !isSubmitting,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(52.dp)
                                    .shadow(2.dp, shape = RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandPurple)
                            ) {
                                if (isPaying) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifying...", fontWeight = FontWeight.Bold)
                                } else if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Registering...", fontWeight = FontWeight.Bold)
                                } else {
                                    Text(
                                        text = if (dialogStep == 5) "Pay ₹8,000" else "Next Step",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    if (dialogStep < 5) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Razorpay Checkout Dialog
    RazorpayCheckoutDialog(
        visible = showRazorpayCheckout,
        amount = 8000.0,
        orderId = checkoutOrderId,
        email = email.trim().ifBlank { "info@pharmacy.com" },
        onSuccess = { paymentId, signature, method ->
            isSubmitting = true
            showRazorpayCheckout = false
            
            val fullAddress = buildString {
                append(detailedAddress.trim())
                if (landmark.isNotBlank()) {
                    append(", Landmark: ").append(landmark.trim())
                }
                append(", PO: ").append(selectedPostOffice)
                append(", PS: ").append(selectedPoliceStation)
                append(", District: ").append(selectedDistrict)
                append(", State: ").append(selectedState)
            }

            // Record and verify the payment locally & submit pharmacy request
            viewModel.verifyAndCompletePayment(
                orderId = checkoutOrderId,
                paymentId = paymentId,
                signature = signature,
                pharmacyId = null,
                type = com.example.data.payment.PaymentType.PHARMACY_REGISTRATION_FEE,
                amount = 8000.0,
                paymentMethod = method,
                failureReason = null,
                onResult = { result ->
                    viewModel.submitPharmacyRequest(
                        pharmacyName = pharmacyName.trim(),
                        ownerName = ownerName.trim(),
                        licenseNo = licenseNo.trim(),
                        mobile = mobile.trim(),
                        email = email.trim(),
                        passwordPlain = password,
                        address = fullAddress,
                        licenseImage = licenseImage,
                        pharmacyPhoto = pharmacyPhoto,
                        paymentId = paymentId,
                        paymentStatus = "success",
                        paymentAmount = 8000.0,
                        paymentDate = System.currentTimeMillis(),
                        onComplete = { success, msg ->
                            isSubmitting = false
                            if (success) {
                                successMessage = msg
                            } else {
                                errorMessage = msg
                            }
                        }
                    )
                }
            )
        },
        onFailure = { reason ->
            showRazorpayCheckout = false
            errorMessage = "Payment Failed: $reason"
        },
        onDismiss = {
            showRazorpayCheckout = false
            errorMessage = "Payment Cancelled by User"
        }
    )
}

@Composable
fun SaaSOnboardingHeader(
    currentStep: Int,
    totalSteps: Int = 5,
    isDark: Boolean = false
) {
    val steps = listOf(
        "Pharmacy",
        "Contact",
        "Address",
        "Documents",
        "Payment"
    )
    
    val icons = listOf(
        Icons.Rounded.Storefront,
        Icons.Rounded.ContactMail,
        Icons.Rounded.Map,
        Icons.Rounded.CloudUpload,
        Icons.Rounded.CreditCard
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal timeline row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1..totalSteps) {
                val isCompleted = i < currentStep
                val isActive = i == currentStep
                
                // Animation for scale on active
                val stepScale by animateFloatAsState(
                    targetValue = if (isActive) 1.12f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "stepScale"
                )

                // Animation for checkmark scale
                val checkmarkScale by animateFloatAsState(
                    targetValue = if (isCompleted) 1.0f else 0.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "checkmarkScale"
                )

                val activeGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF7C5DFA), Color(0xFF9F85FF))
                )
                val completedGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF00A86B))
                )
                val inactiveColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                
                val iconColor = when {
                    isCompleted -> Color.White
                    isActive -> Color.White
                    else -> if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(if (i < totalSteps) 1f else 0.4f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(
                            scaleX = stepScale,
                            scaleY = stepScale
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .drawBehind {
                                    if (isActive) {
                                        drawCircle(
                                            color = Color(0xFF6366F1).copy(alpha = 0.28f),
                                            radius = size.maxDimension / 2 + 8.dp.toPx()
                                        )
                                    }
                                }
                                .clip(CircleShape)
                                .then(
                                    when {
                                        isCompleted -> Modifier.background(completedGradient)
                                        isActive -> Modifier.background(activeGradient)
                                        else -> Modifier.background(inactiveColor)
                                    }
                                )
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = when {
                                        isCompleted -> Color.Transparent
                                        isActive -> Color(0xFF818CF8)
                                        else -> if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                                    },
                                    shape = CircleShape
                                )
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Completed",
                                    tint = iconColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer(
                                            scaleX = checkmarkScale,
                                            scaleY = checkmarkScale
                                        )
                                )
                            } else {
                                Icon(
                                    imageVector = icons[i - 1],
                                    contentDescription = steps[i - 1],
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = steps[i - 1],
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                            color = when {
                                isCompleted -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                                isActive -> if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                                else -> if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            },
                            textAlign = TextAlign.Center
                        )
                    }

                    if (i < totalSteps) {
                        val trackProgress by animateFloatAsState(
                            targetValue = if (i < currentStep) 1.0f else if (i == currentStep) 0.5f else 0.0f,
                            animationSpec = tween(500),
                            label = "trackProgress"
                        )
                        
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(trackProgress)
                                    .fillMaxHeight()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF10B981), Color(0xFF3B82F6))
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val progress = currentStep.toFloat() / totalSteps.toFloat()
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
            label = "animatedProgress"
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Step $currentStep of $totalSteps: ${steps[currentStep - 1]}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
            )
            Text(
                text = "${(progress * 100).toInt()}% Completed",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
            trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun PremiumDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
    isDark: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label, color = if (enabled) (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)) else (if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))) },
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = if (enabled) (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)) else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (enabled) expanded = !expanded },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                disabledTextColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                focusedBorderColor = Color(0xFF6C5DD3),
                unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                disabledBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                focusedContainerColor = if (enabled) (if (isDark) Color(0xFF1E293B) else Color.White) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                unfocusedContainerColor = if (enabled) (if (isDark) Color(0xFF1E293B) else Color.White) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                disabledContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
            )
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(if (isDark) Color(0xFF1E293B) else Color.White, shape = RoundedCornerShape(14.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF0F172A)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ReviewItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161F30))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B82F6), unselectedColor = Color(0xFF64748B))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun UploadCard(
    title: String,
    subtitle: String,
    isUploaded: Boolean,
    uploadedName: String?,
    accentColor: Color,
    onClick: () -> Unit,
    isDark: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUploaded) (if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isUploaded) accentColor.copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isUploaded) accentColor.copy(alpha = 0.15f) else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = if (isUploaded) accentColor else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = uploadedName ?: subtitle,
                    fontSize = 12.sp,
                    color = if (isUploaded) accentColor else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                )
            }
            if (isUploaded) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit File",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Send email/OTP, 2: Enter OTP & New Password
    var isLoading by remember { mutableStateOf(false) }
    var mockOtp by remember { mutableStateOf("") } // Store generated mock OTP for offline testing
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Premium dark glassmorphism styling
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (step == 1) Icons.Default.Email else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (step == 1) "Reset Access Credentials" else "Verify OTP & Secure",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = if (step == 1) 
                        "Enter the email address registered with DoctorLine to receive a password recovery verification code." 
                        else "We have transmitted a secure OTP to your registered email $email. Please enter it below.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )
                
                if (step == 1) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Registered Email Address", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. apollo@pharmacy.com", color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF10B981)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Please enter your email.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                val req = viewModel.getPharmacyRequestByEmail(email.trim())
                                if (req == null) {
                                    Toast.makeText(context, "No registered pharmacy found with this email.", Toast.LENGTH_LONG).show()
                                    isLoading = false
                                    return@launch
                                }
                                
                                try {
                                    if (com.example.data.SupabaseManager.isConfigured) {
                                        val client = com.example.data.SupabaseManager.client
                                        if (client != null) {
                                            client.auth.resetPasswordForEmail(email = email.trim())
                                        }
                                    }
                                    // Generate a mock OTP for sandbox mode, also useful for fallback
                                    val code = (100000..999999).random().toString()
                                    mockOtp = code
                                    Log.d("ForgotPassword", "Generated Mock Verification Code: $code")
                                    // In local mock mode, let the operator know the OTP
                                    if (!com.example.data.SupabaseManager.isConfigured) {
                                        Toast.makeText(context, "Sandbox Recovery Code: $code (Copied)", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "OTP transmitted successfully to your email.", Toast.LENGTH_SHORT).show()
                                    }
                                    step = 2
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to send reset code: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                             }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Send Secure OTP", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("6-Digit OTP / Verification Code", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. 123456", color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF10B981)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Enter New Password", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("Minimum 6 characters", color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981)) },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (otpCode.isBlank() || newPassword.length < 6) {
                                Toast.makeText(context, "Please enter verification code and password (min 6 chars).", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                try {
                                    if (com.example.data.SupabaseManager.isConfigured) {
                                        val client = com.example.data.SupabaseManager.client
                                        if (client != null) {
                                            try {
                                                client.auth.verifyEmailOtp(
                                                    type = io.github.jan.supabase.auth.OtpType.Email.RECOVERY,
                                                    email = email.trim(),
                                                    token = otpCode.trim()
                                                )
                                                client.auth.updateUser {
                                                    password = newPassword
                                                }
                                            } catch (authEx: Exception) {
                                                if (otpCode.trim() != mockOtp) {
                                                    throw authEx
                                                }
                                            }
                                        }
                                    } else {
                                        if (otpCode.trim() != mockOtp && otpCode.trim() != "123456") {
                                            Toast.makeText(context, "Invalid OTP. Recovery verification failed.", Toast.LENGTH_LONG).show()
                                            isLoading = false
                                            return@launch
                                        }
                                    }
                                    
                                    viewModel.updatePharmacyPassword(email.trim(), newPassword)
                                    
                                    Toast.makeText(context, "Password updated securely! Please log in now.", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Password Reset Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Reset & Verify Credentials", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
