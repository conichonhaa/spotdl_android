package com.spotdl.android

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
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
    private var authUrlDomain: String = ""
    private var hasInjectedOnCurrentPage = false

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Settings were changed, reload WebView with new URL
            loadSiteUrl()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebappBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)
        authUrlDomain = extractDomain(secureStorage.getAuthUrl())

        setupWebView()
        setupFab()

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
        } else {
            loadSiteUrl()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)

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
            userAgentString = userAgentString.replace("wv", "")
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Stay in WebView for all URLs
                return false
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                hasInjectedOnCurrentPage = false
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                CookieManager.getInstance().flush()

                if (url != null && isAutheliaUrl(url) && !hasInjectedOnCurrentPage) {
                    hasInjectedOnCurrentPage = true
                    injectAutoLogin(view)
                }
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        }
    }

    private fun loadSiteUrl() {
        val siteUrl = secureStorage.getSiteUrl()
        authUrlDomain = extractDomain(secureStorage.getAuthUrl())
        hasInjectedOnCurrentPage = false
        binding.webView.loadUrl(siteUrl)
    }

    private fun isAutheliaUrl(url: String): Boolean {
        return authUrlDomain.isNotEmpty() && url.contains(authUrlDomain)
    }

    private fun extractDomain(url: String): String {
        return try {
            val withoutScheme = url.removePrefix("https://").removePrefix("http://")
            withoutScheme.substringBefore("/").substringBefore("?")
        } catch (e: Exception) {
            url
        }
    }

    private fun escapeForJs(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun injectAutoLogin(webView: WebView) {
        val username = escapeForJs(secureStorage.getUsername())
        val password = escapeForJs(secureStorage.getPassword())

        val script = """
            (function() {
              function tryFill() {
                var uField = document.querySelector('input[id="username"], input[name="username"], input[autocomplete="username"]');
                var pField = document.querySelector('input[id="password"], input[name="password"], input[type="password"]');
                if (!uField || !pField) { setTimeout(tryFill, 300); return; }
                var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                nativeInputValueSetter.call(uField, '$username');
                uField.dispatchEvent(new Event('input', {bubbles: true}));
                nativeInputValueSetter.call(pField, '$password');
                pField.dispatchEvent(new Event('input', {bubbles: true}));
                setTimeout(function() {
                  var btn = document.querySelector('button[type="submit"], input[type="submit"]');
                  if (btn) btn.click();
                }, 200);
              }
              tryFill();
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
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
