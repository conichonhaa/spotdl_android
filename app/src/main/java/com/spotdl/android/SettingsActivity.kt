package com.spotdl.android

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.spotdl.android.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        binding.btnModifier.setOnClickListener {
            val intent = Intent(this, SetupActivity::class.java).apply {
                putExtra(SetupActivity.EXTRA_EDIT_MODE, true)
            }
            startActivityForResult(intent, REQUEST_EDIT)
        }

        binding.btnReinitialiser.setOnClickListener {
            showResetConfirmation()
        }
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_confirm_title))
            .setMessage(getString(R.string.reset_confirm_message))
            .setPositiveButton(getString(R.string.reset_confirm_yes)) { _, _ ->
                secureStorage.clear()
                val intent = Intent(this, SetupActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    companion object {
        private const val REQUEST_EDIT = 100
    }
}
