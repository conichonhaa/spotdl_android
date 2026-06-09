package com.spotdl.android

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.spotdl.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)

        if (!secureStorage.isConfigured()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        checkBiometricAndPrompt()
    }

    private fun checkBiometricAndPrompt() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> showBiometricPrompt()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // No biometric available, go directly to WebApp
                startWebApp()
            }
            else -> {
                startWebApp()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                startWebApp()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        finish()
                    }
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.biometric_lockout),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    else -> {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.biometric_error, errString),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.biometric_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val biometricPrompt = BiometricPrompt(this, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun startWebApp() {
        startActivity(Intent(this, WebAppActivity::class.java))
        finish()
    }
}
