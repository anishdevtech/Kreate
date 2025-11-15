package app.kreate.android.extensions.spotify

import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.kreate.android.Preferences
import app.kreate.android.R
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import me.knighthat.utils.Toaster
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun SpotifyLoginAndGetToken( onDone: () -> Unit ) {
    var webView: WebView? = null

    AndroidView(
        modifier = Modifier.windowInsetsPadding( LocalPlayerAwareWindowInsets.current )
                           .fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Enable debugging for easier troubleshooting
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true)
                }

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                
                // Third-party cookies - handle API differences
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                
                // For Android 21+ (Lollipop), use the modern method
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                }

                // Clear previous session - compatible with all API levels
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                
                // Remove cookies - handle different API methods
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.removeAllCookies(null)
                    cookieManager.flush()
                } else {
                    // For older Android versions
                    @Suppress("DEPRECATION")
                    cookieManager.removeAllCookie()
                    @Suppress("DEPRECATION") 
                    cookieManager.removeSessionCookie()
                }

                // Clear other storage
                WebStorage.getInstance().deleteAllData()
                clearCache(true)
                clearFormData()
                clearHistory()

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        // Check if user has reached Spotify main page after login
                        if (url.contains("open.spotify.com") && 
                            !url.contains("accounts.spotify.com")) {
                            
                            // Use coroutine to check cookies multiple times with delays
                            GlobalScope.launch {
                                var attempts = 0
                                val maxAttempts = 10 // Increased for reliability
                                
                                while (attempts < maxAttempts) {
                                    delay(1000) // Wait 1 second between attempts
                                    
                                    val spDc = extractSpDcCookie()
                                    if (spDc != null) {
                                        // Successfully got the sp_dc token
                                        Preferences.SPOTIFY_ACCESS_TOKEN.value = spDc
                                        Toaster.s(R.string.spotify_login_success)
                                        onDone()
                                        return@launch
                                    }
                                    
                                    attempts++
                                    // Log progress for debugging
                                    android.util.Log.d("SpotifyLogin", "Attempt $attempts/$maxAttempts - No sp_dc found yet")
                                }
                                
                                // If we get here, extraction failed after all attempts
                                android.util.Log.e("SpotifyLogin", "Failed to extract sp_dc after $maxAttempts attempts")
                                Toaster.e(R.string.error_failed_to_extract_spotify_token)
                                onDone()
                            }
                        }
                    }

                    @Suppress("OVERRIDE_DEPRECATION")
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        url: String
                    ): Boolean {
                        return false
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        return false
                    }

                    @Suppress("DEPRECATION")
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        android.util.Log.e("SpotifyLogin", "WebView error: $description")
                        Toaster.e("WebView error: $description")
                    }
                }

                webView = this
                // Load Spotify login page
                loadUrl("https://accounts.spotify.com/login?continue=https://open.spotify.com/")
            }
        }
    )

    BackHandler(enabled = true) {
        onDone()
    }
}

/**
 * Extract sp_dc cookie using Android's CookieManager - focuses on .spotify.com domain
 */
private fun extractSpDcCookie(): String? {
    return try {
        val cookieManager = CookieManager.getInstance()
        
        // Focus specifically on .spotify.com domain where sp_dc is stored
        val domainsToCheck = listOf(
            ".spotify.com",  // Primary domain where sp_dc is stored
            "https://.spotify.com",
            "https://open.spotify.com",
            "open.spotify.com",
            "spotify.com"
        )
        
        for (domain in domainsToCheck) {
            try {
                val cookies = cookieManager.getCookie(domain)
                if (!cookies.isNullOrBlank()) {
                    android.util.Log.d("SpotifyLogin", "Found cookies for $domain: ${cookies.take(100)}...")
                    val spDc = extractSpDcFromCookieString(cookies)
                    if (spDc != null) {
                        android.util.Log.d("SpotifyLogin", "Successfully extracted sp_dc: ${spDc.take(20)}...")
                        return spDc
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("SpotifyLogin", "Error getting cookies for $domain: ${e.message}")
            }
        }
        
        // Try a broader approach - get all cookies and search
        try {
            val allCookies = cookieManager.getCookie("https://spotify.com") ?: 
                           cookieManager.getCookie("http://spotify.com") ?:
                           cookieManager.getCookie("spotify.com")
            
            if (!allCookies.isNullOrBlank()) {
                android.util.Log.d("SpotifyLogin", "All spotify cookies: ${allCookies.take(200)}...")
                return extractSpDcFromCookieString(allCookies)
            }
        } catch (e: Exception) {
            android.util.Log.w("SpotifyLogin", "Error getting all cookies: ${e.message}")
        }
        
        null
    } catch (e: Exception) {
        android.util.Log.e("SpotifyLogin", "Error in extractSpDcCookie: ${e.message}")
        null
    }
}

/**
 * Extract sp_dc value from cookie string with better parsing
 */
private fun extractSpDcFromCookieString(cookieString: String): String? {
    try {
        // Method 1: Split by semicolon and look for sp_dc
        val cookies = cookieString.split(";")
        
        for (cookie in cookies) {
            val trimmed = cookie.trim()
            if (trimmed.startsWith("sp_dc=")) {
                val value = trimmed.substring(6).trim()
                if (value.isNotBlank()) {
                    return value
                }
            }
        }
        
        // Method 2: Use regex to find sp_dc (more robust)
        val patterns = listOf(
            "sp_dc=([^;]+)".toRegex(),
            "sp_dc\\s*=\\s*([^;]+)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(cookieString)
            if (match != null) {
                val value = match.groupValues[1].trim()
                if (value.isNotBlank()) {
                    return value
                }
            }
        }
        
        return null
    } catch (e: Exception) {
        android.util.Log.e("SpotifyLogin", "Error parsing cookie string: ${e.message}")
        return null
    }
}