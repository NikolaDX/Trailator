package com.nikoladx.trailator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nikoladx.trailator.services.cloudinary.CloudinaryConfig
import com.nikoladx.trailator.data.repositories.AuthenticationRepositoryImpl
import com.nikoladx.trailator.services.firebase.FirebaseAuthService
import com.nikoladx.trailator.services.firebase.FirebaseUserService
import com.nikoladx.trailator.ui.navigation.TrailatorNavHost
import com.nikoladx.trailator.ui.screens.authentication.viewmodels.LoginViewModelFactory
import com.nikoladx.trailator.ui.screens.authentication.viewmodels.RegisterViewModelFactory
import com.nikoladx.trailator.ui.theme.TrailatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryConfig.initialize(this)
        enableEdgeToEdge()
        val authService = FirebaseAuthService()
        val userService = FirebaseUserService()
        val authRepository = AuthenticationRepositoryImpl(authService, userService)

        val registerViewModelFactory = RegisterViewModelFactory(authRepository, application)
        val loginViewModelFactory = LoginViewModelFactory(authRepository)

        setContent {
            TrailatorTheme {
                TrailatorNavHost(
                    registerFactory = registerViewModelFactory,
                    loginFactory = loginViewModelFactory,
                    authRepository = authRepository
                )
            }
        }
    }
}
