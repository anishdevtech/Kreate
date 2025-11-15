package app.kreate.android.extensions.spotify

import android.annotation.SuppressLint
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

                WebView.setWebContentsDebuggingEnabled(true)

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true

                // Clear previous session
                CookieManager.getInstance().apply {
                    removeAllCookies(null)
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(this@apply, true)
                    flush()
                }
                WebStorage.getInstance().deleteAllData()
                clearCache(true)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        // Check if user has reached Spotify main page after login
                        if (url.contains("open.spotify.com") && 
                            !url.contains("accounts.spotify.com")) {
                            
                            // Use coroutine to check cookies multiple times with delays
                            GlobalScope.launch {
                                var attempts = 0
                                val maxAttempts = 8 // Increased attempts
                                
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
                                    println("Spotify login: Attempt $attempts/$maxAttempts - No sp_dc found yet")
                                }
                                
                                // If we get here, extraction failed after all attempts
                                Toaster.e(R.string.error_failed_to_extract_spotify_token)
                                onDone()
                            }
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean = false

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
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
            "open.spotify.com"
        )
        
        for (domain in domainsToCheck) {
            val cookies = cookieManager.getCookie(domain)
            if (!cookies.isNullOrBlank()) {
                println("Found cookies for $domain: $cookies") // Debug log
                val spDc = extractSpDcFromCookieString(cookies)
                if (spDc != null) {
                    println("Successfully extracted sp_dc: ${spDc.take(20)}...") // Debug log
                    return spDc
                }
            } else {
                println("No cookies found for domain: $domain") // Debug log
            }
        }
        
        // Debug: List all available domains with cookies
        println("Checking all available cookies:")
        val allCookies = cookieManager.getCookie("spotify.com") ?: "No spotify.com cookies"
        println("spotify.com cookies: $allCookies")
        
        null
    } catch (e: Exception) {
        e.printStackTrace()
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
                return trimmed.substring(6).trim() // Get value after "sp_dc="
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
                return match.groupValues[1].trim()
            }
        }
        
        return null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}