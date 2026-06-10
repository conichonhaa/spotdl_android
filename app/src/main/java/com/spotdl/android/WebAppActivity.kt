package com.spotdl.android

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.spotdl.android.databinding.ActivityWebappBinding

class WebAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebappBinding
    private lateinit var secureStorage: SecureStorage

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadSiteUrl()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebappBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)

        setupWebView()

        binding.fabSettings.setOnClickListener {
            settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
        }

        binding.fabJellyfin.setOnClickListener {
            openJellyfin()
        }

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
        } else {
            loadSiteUrl()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(binding.webView, true)
        }

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportMultipleWindows(true)
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            // Remove "wv" from user agent so the site doesn't treat it as a stripped WebView
            userAgentString = userAgentString.replace("; wv", "")
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                CookieManager.getInstance().flush()
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                binding.progressBar.progress = newProgress
            }
        }
    }

    private fun loadSiteUrl() {
        binding.webView.loadUrl(secureStorage.getSiteUrl())
    }

    private fun openJellyfin() {
        val jellyfinUrl = secureStorage.getJellyfinUrl()
        val appPackage = "org.jellyfin.mobile"

        // 1. Deep link: opens Jellyfin app directly on the configured server
        val serverHost = Uri.parse(jellyfinUrl).host ?: ""
        if (serverHost.isNotEmpty()) {
            val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("jellyfin://$serverHost"))
            deepLinkIntent.setPackage(appPackage)
            if (deepLinkIntent.resolveActivity(packageManager) != null) {
                startActivity(deepLinkIntent)
                return
            }
        }

        // 2. Fallback: launch app on its home screen
        packageManager.getLaunchIntentForPackage(appPackage)?.let {
            startActivity(it)
            return
        }

        // 3. Fallback: open URL in browser
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(jellyfinUrl)))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}
