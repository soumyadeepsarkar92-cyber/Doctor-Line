package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.SupabaseClient

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle Supabase deep links for OAuth
        com.example.data.SupabaseManager.client?.handleDeeplinks(intent = intent)

        // Acquire repositories from Application container
        val app = application as DoctorLineApplication
        val repository = app.repository

        // Initializing Unified Viewmodel
        val viewModel: MainViewModel by viewModels {
            MainViewModel.Factory(application, repository)
        }

        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val useDarkTheme = when (appTheme) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DoctorLineAppContent(viewModel)
                }
            }
        }
    }
}

@Composable
fun DoctorLineAppContent(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeUser by viewModel.activeUser.collectAsState()
    
    // Splash screen view state
    var showSplash by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSplash) {
            SplashScreen(
                isUserLoggedIn = activeUser != null,
                onNavigateToLogin = { showSplash = false },
                onNavigateToHome = { showSplash = false }
            )
        } else {
            val user = activeUser
            if (user == null) {
                // Not authenticated yet - load Login system
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { name, email, phone, role, profilePhotoUrl ->
                        viewModel.login(
                            name, email, phone, role, profilePhotoUrl,
                            onError = { errorMsg ->
                                android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            } else {
                // Route based on authenticated user Role level
                when (user.role) {
                    "Patient" -> {
                        PatientModuleScreen(
                            viewModel = viewModel,
                            logout = { viewModel.logout() }
                        )
                    }
                    "Pharmacy" -> {
                        PharmacyModuleScreen(
                            viewModel = viewModel,
                            logout = { viewModel.logout() }
                        )
                    }
                    "Admin" -> {
                        AdminModuleScreen(
                            viewModel = viewModel,
                            logout = { viewModel.logout() }
                        )
                    }
                    else -> {
                        // Fallback security card
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { name, email, phone, role, profilePhotoUrl ->
                                viewModel.login(
                                    name, email, phone, role, profilePhotoUrl,
                                    onError = { errorMsg ->
                                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
