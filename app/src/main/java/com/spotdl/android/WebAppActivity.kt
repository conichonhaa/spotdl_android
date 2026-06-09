package com.spotdl.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.spotdl.android.databinding.ActivityWebappBinding

class WebAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebappBinding
    private lateinit var secureStorage: SecureStorage
    private var customTabLaunched = false

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            customTabLaunched = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebappBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)

        binding.fabSettings.setOnClickListener {
            settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
        }

        binding.btnOpen.setOnClickListener {
            launchCustomTab()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!customTabLaunched) {
            launchCustomTab()
        } else {
            // Returned from Custom Tab – show the home screen
            binding.layoutHome.visibility = View.VISIBLE
        }
    }

    private fun launchCustomTab() {
        val siteUrl = secureStorage.getSiteUrl().ifBlank { return }

        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
            .setDefaultColorSchemeParams(colorSchemeParams)
            .build()

        customTabLaunched = true
        binding.layoutHome.visibility = View.GONE
        customTabsIntent.launchUrl(this, Uri.parse(siteUrl))
    }
}
