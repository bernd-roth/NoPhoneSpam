package at.bitfire.nophonespam

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.bitfire.nophonespam.ui.BlacklistScreen
import at.bitfire.nophonespam.ui.BlockedCallsScreen
import at.bitfire.nophonespam.ui.EditNumberScreen
import at.bitfire.nophonespam.ui.theme.NoPhoneSpamTheme

class MainActivity : ComponentActivity() {
    private val TAG = "NoPhoneSpam"

    private val requestCallScreeningRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                Log.w(TAG, "User declined call screening role")
            }
        }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }

        requestCallScreeningRoleIfNeeded()

        setContent {
            NoPhoneSpamTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "blacklist") {
                    composable("blacklist") {
                        BlacklistScreen(
                            onShortPress = { number ->
                                val encodedNumber = Uri.encode(number.number)
                                val encodedName = Uri.encode(number.name ?: "")
                                navController.navigate("blocked/$encodedNumber/$encodedName")
                            },
                            onLongPress = { number ->
                                val encodedNumber = Uri.encode(number.number)
                                navController.navigate("edit?number=$encodedNumber")
                            },
                            onAddClick = {
                                navController.navigate("edit")
                            }
                        )
                    }

                    composable(
                        route = "edit?number={number}",
                        arguments = listOf(
                            navArgument("number") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val existingPattern = backStackEntry.arguments?.getString("number")
                        EditNumberScreen(
                            existingPattern = existingPattern,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "blocked/{number}/{name}",
                        arguments = listOf(
                            navArgument("number") { type = NavType.StringType },
                            navArgument("name") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val pattern = backStackEntry.arguments?.getString("number") ?: ""
                        val name = backStackEntry.arguments?.getString("name")
                        BlockedCallsScreen(
                            pattern = pattern,
                            displayName = if (name.isNullOrEmpty()) null else name,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun requestCallScreeningRoleIfNeeded() {
        if (Build.VERSION.SDK_INT >= 29) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                requestCallScreeningRole.launch(intent)
            }
        }
    }
}
