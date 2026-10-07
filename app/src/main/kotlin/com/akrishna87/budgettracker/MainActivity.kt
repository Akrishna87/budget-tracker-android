package com.akrishna87.budgettracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.akrishna87.budgettracker.security.PinHashing
import com.akrishna87.budgettracker.security.SecurityPrefs
import com.akrishna87.budgettracker.ui.BudgetViewModelFactory
import com.akrishna87.budgettracker.ui.lock.LockScreen
import com.akrishna87.budgettracker.ui.navigation.BudgetNavHost
import com.akrishna87.budgettracker.ui.theme.BudgetTrackerTheme

class MainActivity : FragmentActivity() {
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    private lateinit var securityPrefs: SecurityPrefs

    // Hoisted at the Activity level (not inside setContent) so onPause can
    // re-lock the app from outside the composition, not just from a tap on
    // the lock screen's own Unlock button.
    private val isUnlocked = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as BudgetTrackerApp
        securityPrefs = app.securityPrefs
        isUnlocked.value = !securityPrefs.isLockEnabled
        val factory = BudgetViewModelFactory(app.repository, securityPrefs)

        setContent {
            BudgetTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val unlocked by isUnlocked
                    if (unlocked) {
                        BudgetNavHost(factory)
                    } else {
                        LockScreen(
                            onVerifyPin = { pin ->
                                val correct = verifyPin(pin)
                                if (correct) isUnlocked.value = true
                                correct
                            },
                            onRequestBiometric = ::showBiometricPrompt,
                            biometricAvailable = isBiometricAvailable()
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (securityPrefs.isLockEnabled) {
            isUnlocked.value = false
        }
    }

    private fun isBiometricAvailable(): Boolean {
        return BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun verifyPin(pin: String): Boolean {
        val salt = securityPrefs.pinSalt ?: return false
        val hash = securityPrefs.pinHash ?: return false
        return PinHashing.hash(pin, salt) == hash
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Budget Tracker")
            .setSubtitle("Use your fingerprint or face to continue")
            .setNegativeButtonText("Use PIN instead")
            .build()

        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isUnlocked.value = true
                }
            }
        )
        biometricPrompt.authenticate(promptInfo)
    }
}
