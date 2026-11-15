package app.stykanimate.android

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.webkit.WebViewAssetLoader
import java.net.URLConnection

/**
 * Android host for the locally packaged StykAnimate editor.
 * The editor bundle is built from the upstream source during CI and packaged
 * inside the APK. The app never loads the public StykAnimate website.
 */
class MainActivity : ComponentActivity() {
    private lateinit var editor: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.PathHandler { path ->
                serveIsolatedAsset(path)
            })
            .build()

        editor = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.mediaPlaybackRequiresUserGesture = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    // Keep navigation inside the locally packaged application.
                    return request.url.host != "appassets.androidplatform.net"
                }
            }
        }

        setContentView(editor)
        editor.loadUrl("https://appassets.androidplatform.net/assets/stykanimate/index.html")
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (editor.canGoBack()) editor.goBack() else finish()
            }
        })
    }

    private fun serveIsolatedAsset(path: String): WebResourceResponse? {
        if (path.contains("..") || path.startsWith("/")) return null
        return try {
            val assetPath = "stykanimate/$path"
            val stream = assets.open(assetPath)
            val extension = path.substringAfterLast('.', "").lowercase()
            val mime = when (extension) {
                "wasm" -> "application/wasm"
                "js", "mjs" -> "text/javascript"
                "json", "webmanifest" -> "application/json"
                "svg" -> "image/svg+xml"
                "html" -> "text/html"
                "css" -> "text/css"
                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: URLConnection.guessContentTypeFromName(path)
                    ?: "application/octet-stream"
            }
            val headers = mapOf(
                "Cross-Origin-Opener-Policy" to "same-origin",
                "Cross-Origin-Embedder-Policy" to "require-corp",
                "Cross-Origin-Resource-Policy" to "same-origin",
                "X-Content-Type-Options" to "nosniff"
            )
            WebResourceResponse(mime, "UTF-8", 200, "OK", headers, stream)
        } catch (_: Exception) {
            null
        }
    }

    override fun onPause() {
        editor.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        editor.onResume()
    }

    override fun onDestroy() {
        editor.destroy()
        super.onDestroy()
    }
}
