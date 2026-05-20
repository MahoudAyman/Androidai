package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme
import com.example.vault.CalculatorScreen
import com.example.vault.VaultScreen
import com.example.vault.VaultViewModel
import com.example.vault.VaultViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: VaultViewModel by viewModels { 
        VaultViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "calculator") {
                        composable("calculator") {
                            CalculatorScreen(
                                viewModel = viewModel,
                                onNavigateToVault = {
                                    navController.navigate("vault") {
                                        popUpTo("calculator") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("vault") {
                            VaultScreen(
                                viewModel = viewModel,
                                onLock = {
                                    viewModel.onVaultExited()
                                    navController.navigate("calculator") {
                                        popUpTo("vault") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
