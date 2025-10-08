package com.psyfen.taskapplication


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.psyfen.taskapplication.navigation.AppNavigation
import com.psyfen.taskapplication.ui.theme.TaskApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "=== onCreate called ===")

        try {
            enableEdgeToEdge()
            Log.d("MainActivity", "enableEdgeToEdge() success")

            setContent {
                Log.d("MainActivity", "setContent() composable block called")

                TaskApplicationTheme {
                    Log.d("MainActivity", "Inside TaskApplicationTheme")

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Log.d("MainActivity", "About to call AppNavigation")
                        AppNavigation()
                        Log.d("MainActivity", "AppNavigation called")
                    }
                }
            }

            Log.d("MainActivity", "=== setContent completed ===")

        } catch (e: Exception) {
            Log.e("MainActivity", "=== ERROR in onCreate ===", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
    }
}