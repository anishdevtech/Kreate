package app.kreate.android.extensions.spotify

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
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

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
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

                CookieManager.getInstance().apply {
                    removeAllCookies(null)
                    flush()
                }

                WebStorage.getInstance().deleteAllData()

                addJavascriptInterface(object {
                    @JavascriptInterface
                    @Suppress("unused")
                    fun onRetrieveToken( token: String ) {
                        Preferences.SPOTIFY_ACCESS_TOKEN.value = token
                        Toaster.s(R.string.spotify_login_success)
                        onDone()
                    }

                    @JavascriptInterface
                    @Suppress("unused")
                    fun onFailure( message: String ) {
                        if ( message == "null" )
                            Toaster.e( R.string.error_failed_to_extract_spotify_token )
                        else
                            Toaster.e( message )

                        onDone()
                    }

                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        // Check if user has reached Spotify main page after login
                        if ( url.contains("open.spotify.com") && !url.contains("accounts.spotify.com") ) {
                            view.evaluateJavascript(
                                """
                                (function() {
                                    try {
                                        // Extract sp_dc from cookies
                                        var cookies = document.cookie;
                                        var cookieMatch = cookies.match(/(^| )sp_dc=([^;]+)/);
                                        
                                        if (cookieMatch && cookieMatch[2]) {
                                            Android.onRetrieveToken(cookieMatch[2]);
                                        } else {
                                            Android.onFailure("null");
                                        }
                                    } catch (err) {
                                        Android.onFailure(err.message);
                                    }
                                })();
                                """.trimIndent(), null
                            )
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean = false
                }

                webView = this
                // Load Spotify login page
                loadUrl( "https://accounts.spotify.com/login?continue=https://open.spotify.com/" )
            }
        }
    )

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }
}