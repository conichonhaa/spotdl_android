package com.spotdl.android

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spotdl.android.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var secureStorage: SecureStorage
    private var isEditMode = false
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)
        isEditMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false)

        setupToolbar()
        prefillFields()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        if (isEditMode) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.setup_edit_title)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.title = getString(R.string.setup_title)
        }
    }

    private fun prefillFields() {
        if (isEditMode && secureStorage.isConfigured()) {
            binding.editSiteUrl.setText(secureStorage.getSiteUrl())
            binding.editAuthUrl.setText(secureStorage.getAuthUrl())
            binding.editUsername.setText(secureStorage.getUsername())
            binding.editPassword.setText(secureStorage.getPassword())
            binding.editJellyfinUrl.setText(secureStorage.getJellyfinUrl())
        } else {
            binding.editSiteUrl.setText("https://spotdl.hrs-pacs.fr")
            binding.editAuthUrl.setText("https://auth.hrs-pacs.fr")
            binding.editJellyfinUrl.setText("https://jelly.hrs-pacs.fr")
        }
    }

    private fun setupListeners() {
        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                binding.editPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            } else {
                binding.editPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnTogglePassword.setImageResource(android.R.drawable.ic_secure)
            }
            binding.editPassword.setSelection(binding.editPassword.text?.length ?: 0)
        }

        binding.btnSave.setOnClickListener {
            saveConfiguration()
        }
    }

    private fun saveConfiguration() {
        val siteUrl = binding.editSiteUrl.text?.toString()?.trim() ?: ""
        val authUrl = binding.editAuthUrl.text?.toString()?.trim() ?: ""
        val username = binding.editUsername.text?.toString()?.trim() ?: ""
        val password = binding.editPassword.text?.toString() ?: ""
        val jellyfinUrl = binding.editJellyfinUrl.text?.toString()?.trim() ?: ""

        if (siteUrl.isEmpty()) {
            binding.tilSiteUrl.error = getString(R.string.error_field_required)
            return
        } else {
            binding.tilSiteUrl.error = null
        }

        if (authUrl.isEmpty()) {
            binding.tilAuthUrl.error = getString(R.string.error_field_required)
            return
        } else {
            binding.tilAuthUrl.error = null
        }

        if (!siteUrl.startsWith("https://")) {
            binding.tilSiteUrl.error = getString(R.string.error_https_required)
            return
        } else {
            binding.tilSiteUrl.error = null
        }

        if (!authUrl.startsWith("https://")) {
            binding.tilAuthUrl.error = getString(R.string.error_https_required)
            return
        } else {
            binding.tilAuthUrl.error = null
        }

        if (username.isEmpty()) {
            binding.tilUsername.error = getString(R.string.error_field_required)
            return
        } else {
            binding.tilUsername.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_field_required)
            return
        } else {
            binding.tilPassword.error = null
        }

        if (jellyfinUrl.isNotEmpty() && !jellyfinUrl.startsWith("https://") && !jellyfinUrl.startsWith("http://")) {
            binding.tilJellyfinUrl.error = getString(R.string.error_https_required)
            return
        } else {
            binding.tilJellyfinUrl.error = null
        }

        secureStorage.save(siteUrl, authUrl, username, password, jellyfinUrl)
        Toast.makeText(this, getString(R.string.setup_saved), Toast.LENGTH_SHORT).show()

        if (isEditMode) {
            setResult(RESULT_OK)
            finish()
        } else {
            startActivity(Intent(this, WebAppActivity::class.java))
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!isEditMode && !secureStorage.isConfigured()) {
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_EDIT_MODE = "edit_mode"
    }
}
