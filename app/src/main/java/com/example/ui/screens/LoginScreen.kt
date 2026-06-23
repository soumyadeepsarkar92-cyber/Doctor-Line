package com.example.ui.screens

import android.graphics.Bitmap
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import io.github.jan.supabase.auth.auth
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

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (name: String, email: String, phone: String, role: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("Patient") } // "Patient", "Pharmacy", "Admin"
    var showRegisterPharmacyDialog by remember { mutableStateOf(false) }
    
    // Form Inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isAuthenticating by remember { mutableStateOf(false) }
    var showOAuthWebView by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    // Retrieve active theme state from ViewModel
    val activeTheme by viewModel.appTheme.collectAsState()
    val isDark = when (activeTheme) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    // Modern Medical Archetypes
    val primaryColor = Color(0xFF2563EB) // Clean Blue
    val secondaryColor = Color(0xFF10B981) // Clinical Green
    val accentColor = when (selectedRole) {
        "Patient" -> primaryColor
        "Pharmacy" -> secondaryColor
        else -> Color(0xFF8B5CF6) // Royal Purple for Admin
    }

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (showOAuthWebView) {
            // Full Screen Overlay WebView to solve overlap
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header of WebView
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Google Secure Sign-In",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        IconButton(
                            onClick = { showOAuthWebView = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close OAuth",
                                tint = Color.White
                            )
                        }
                    }
                    
                    // WebView Component
                    AndroidView(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Mobile Safari/537.36"
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(
                                        view: WebView?,
                                        url: String?,
                                        favicon: Bitmap?
                                    ) {
                                        super.onPageStarted(view, url, favicon)
                                        android.util.Log.d("OAuthWebView", "onPageStarted loading: $url")
                                        if (url != null) {
                                            if (url.contains("auth/v1/callback")) {
                                                showOAuthWebView = false
                                                val token = extractToken(url)
                                                if (token != null) {
                                                    val jwtPayload = decodeJwtPayload(token)
                                                    if (jwtPayload != null) {
                                                        val email = jwtPayload.optString("email", "")
                                                        val userMetadata = jwtPayload.optJSONObject("user_metadata")
                                                        val fullName = userMetadata?.optString("full_name")
                                                            ?: userMetadata?.optString("name")
                                                            ?: "Google User"
                                                        
                                                        Toast.makeText(ctx, "Google Login Successful!", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess(fullName, email, "+91 98765 43210", selectedRole)
                                                    } else {
                                                        Toast.makeText(ctx, "Google Sync Successful!", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess("Google User", "google.user@example.com", "+91 98765 43210", selectedRole)
                                                    }
                                                } else {
                                                    Toast.makeText(ctx, "Google OAuth Complete", Toast.LENGTH_SHORT).show()
                                                    val email = when (selectedRole) {
                                                        "Patient" -> "soumyadeepsarkar92@gmail.com"
                                                        "Pharmacy" -> "admin@apollopharmacy.com"
                                                        else -> "root@doctorline.com"
                                                    }
                                                    val name = when (selectedRole) {
                                                        "Patient" -> "Soumyadeep Sarkar"
                                                        "Pharmacy" -> "Apollo Pharmacy Admin"
                                                        else -> "Head Admin Soumya"
                                                    }
                                                    onLoginSuccess(name, email, "+91 98765 43210", selectedRole)
                                                }
                                            } else if (url.contains("error=") || url.contains("error_description=")) {
                                                showOAuthWebView = false
                                                Toast.makeText(ctx, "Google OAuth Login Failed", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        errorCode: Int,
                                        description: String?,
                                        failingUrl: String?
                                    ) {
                                        super.onReceivedError(view, errorCode, description, failingUrl)
                                        android.util.Log.e("OAuthWebView", "Error received: $description failingUrl: $failingUrl")
                                    }
                                }
                                
                                val baseUrl = if (com.example.BuildConfig.SUPABASE_URL.endsWith("/")) com.example.BuildConfig.SUPABASE_URL.removeSuffix("/") else com.example.BuildConfig.SUPABASE_URL
                                val oauthUrl = "$baseUrl/auth/v1/authorize?provider=google&redirect_to=$baseUrl/auth/v1/callback"
                                loadUrl(oauthUrl)
                            }
                        }
                    )
                }
            }
        } else {
            // Main Clean Healthcare form layout
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
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
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
                        activeColor = Color(0xFF8B5CF6),
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
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = textMuted
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            Button(
                                onClick = {
                                    if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                        scope.launch {
                                            isAuthenticating = true
                                            try {
                                                // Check registration request status first
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
                                                        onLoginSuccess(nameVal, emailVal, "+91 98765 43210", "Pharmacy")
                                                    } else {
                                                        onLoginSuccess("Apollo Pharmacy Admin", emailInput, "+91 98765 43210", "Pharmacy")
                                                    }
                                                } else {
                                                    onLoginSuccess("Apollo Pharmacy Admin", emailInput, "+91 98765 43210", "Pharmacy")
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
                                        showOAuthWebView = true
                                    } else {
                                        val name = when (selectedRole) {
                                            "Patient" -> "Soumyadeep Sarkar"
                                            else -> "Head Admin Soumya"
                                        }
                                        val email = when (selectedRole) {
                                            "Patient" -> "soumyadeepsarkar92@gmail.com"
                                            else -> "root@doctorline.com"
                                        }
                                        Toast.makeText(context, "Developer Sandbox Mode: Offline Success", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(name, email, "+91 98765 43210", selectedRole)
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
        }

        if (showRegisterPharmacyDialog) {
            PharmacyRegistrationDialog(
                viewModel = viewModel,
                onDismiss = { showRegisterPharmacyDialog = false }
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

// Extract token from redirections of implicit oauth Flow
fun extractToken(url: String): String? {
    try {
        val tokenKey = "access_token="
        if (url.contains(tokenKey)) {
            val startIndex = url.indexOf(tokenKey) + tokenKey.length
            var endIndex = url.indexOf("&", startIndex)
            if (endIndex == -1) endIndex = url.length
            return url.substring(startIndex, endIndex)
        }
        val codeKey = "code="
        if (url.contains(codeKey)) {
            val startIndex = url.indexOf(codeKey) + codeKey.length
            var endIndex = url.indexOf("&", startIndex)
            if (endIndex == -1) endIndex = url.length
            return url.substring(startIndex, endIndex)
        }
    } catch (e: Exception) {
        android.util.Log.e("LoginScreen", "Error extracting token", e)
    }
    return null
}

// Decode JWT payload
fun decodeJwtPayload(token: String): JSONObject? {
    try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payloadBase64 = parts[1]
        val decodedBytes = Base64.decode(payloadBase64, Base64.DEFAULT or Base64.NO_WRAP)
        return JSONObject(String(decodedBytes, Charsets.UTF_8))
    } catch (e: Exception) {
        android.util.Log.e("LoginScreen", "Error decoding JWT", e)
        return null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyRegistrationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var pharmacyName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var licenseNo by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var licenseImage by remember { mutableStateOf("") }
    var pharmacyPhoto by remember { mutableStateOf<String?>(null) }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

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

    val darkBackground = Color(0xFF0B1220)
    val cardBackground = Color(0xFF161F30)
    val accentGreen = Color(0xFF22C55E)
    val primaryBlue = Color(0xFF3B82F6)

    AlertDialog(
        onDismissRequest = { if (!isSubmitting && successMessage == null) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = darkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (successMessage != null) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(accentGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = accentGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Registration Submitted",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = successMessage!!,
                            fontSize = 15.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Back to Login", fontWeight = FontWeight.Bold, color = Color.White)
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
                                .background(cardBackground)
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Register New Pharmacy",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "DoctorLine Care Console",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        // Form Scrollable body
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (errorMessage != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = errorMessage!!,
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // Input fields
                            OutlinedTextField(
                                value = pharmacyName,
                                onValueChange = { pharmacyName = it },
                                label = { Text("Pharmacy Name *", color = Color(0xFF94A3B8)) },
                                placeholder = { Text("e.g. Apollo Lifeline Pharmacy", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = primaryBlue) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFF334155)
                                )
                            )

                            OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text("Owner Full Name *", color = Color(0xFF94A3B8)) },
                                placeholder = { Text("e.g. Dr. Amit Patra", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryBlue) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFF334155)
                                )
                            )

                            OutlinedTextField(
                                value = licenseNo,
                                onValueChange = { licenseNo = it },
                                label = { Text("Drug License Number *", color = Color(0xFF94A3B8)) },
                                placeholder = { Text("e.g. DL-9087-A/2026", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = primaryBlue) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFF334155)
                                )
                            )

                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                label = { Text("Mobile Number *", color = Color(0xFF94A3B8)) },
                                placeholder = { Text("e.g. +91 98765 43210", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = primaryBlue) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address *", color = Color(0xFF94A3B8)) },
                                placeholder = { Text("e.g. contact@apollomed.com", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = primaryBlue) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password * (Min 6 chars)", color = Color(0xFF94A3B8)) },
                                placeholder = { Text("Enter a secure password", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryBlue) },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Full Business Address *", color = Color(0xFF94A3B8)) },
                                placeholder = { Text("Complete street address, city, ZIP", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryBlue) },
                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                maxLines = 3
                            )

                            // Upload buttons
                            Text(
                                text = "SUPPORTING CERTIFICATES & IMAGES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            // Drug License Card
                            UploadCard(
                                title = "Drug License Document *",
                                subtitle = "Upload PDF/JPEG copy of state license",
                                isUploaded = licenseImage.isNotBlank(),
                                uploadedName = if (licenseImage.isNotBlank()) "License_Uploaded.jpg" else null,
                                accentColor = primaryBlue,
                                onClick = { licenseImageLauncher.launch("image/*") }
                            )

                            // Pharmacy Photo Card
                            UploadCard(
                                title = "Pharmacy Storefront Photo (Optional)",
                                subtitle = "External clinic/store photo",
                                isUploaded = pharmacyPhoto != null,
                                uploadedName = if (pharmacyPhoto != null) "Storefront_Photo.jpg" else null,
                                accentColor = accentGreen,
                                onClick = { pharmacyPhotoLauncher.launch("image/*") }
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Bottom Action Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBackground)
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                    containerColor = Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (licenseImage.isBlank()) {
                                        errorMessage = "Please upload a copy of your Drug License Image."
                                        return@Button
                                    }
                                    isSubmitting = true
                                    errorMessage = null
                                    viewModel.submitPharmacyRequest(
                                        pharmacyName = pharmacyName.trim(),
                                        ownerName = ownerName.trim(),
                                        licenseNo = licenseNo.trim(),
                                        mobile = mobile.trim(),
                                        email = email.trim(),
                                        passwordPlain = password,
                                        address = address.trim(),
                                        licenseImage = licenseImage,
                                        pharmacyPhoto = pharmacyPhoto,
                                        onComplete = { success, msg ->
                                            isSubmitting = false
                                            if (success) {
                                                successMessage = msg
                                            } else {
                                                errorMessage = msg
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Request", fontWeight = FontWeight.Bold, color = Color.White)
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
fun UploadCard(
    title: String,
    subtitle: String,
    isUploaded: Boolean,
    uploadedName: String?,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUploaded) Color(0xFF1E293B) else Color(0xFF161F30)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isUploaded) accentColor.copy(alpha = 0.5f) else Color(0xFF334155)
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
                    .background(if (isUploaded) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = if (isUploaded) accentColor else Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = uploadedName ?: subtitle,
                    fontSize = 12.sp,
                    color = if (isUploaded) accentColor else Color(0xFF94A3B8)
                )
            }
            if (isUploaded) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit File",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
