package app.stykanimate.android

import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.webkit.WebViewAssetLoader
import java.net.URLConnection

/**
 * Android host for the locally packaged StykAnimate editor.
 * CI builds the editor from the upstream source and bundles it in the APK.
 * No remote website is used at runtime.
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
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val url = request.url
                    if (url.scheme == "https" && url.host == "appassets.androidplatform.net") {
                        return assetLoader.shouldInterceptRequest(url)
                    }
                    return if (url.isHierarchical && url.scheme == "https") {
                        notFoundResponse()
                    } else null
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    // The editor is an offline app: do not navigate to remote websites.
                    return request.url.host != "appassets.androidplatform.net"
                }
            }
        }

        setContentView(editor)
        editor.loadUrl("https://appassets.androidplatform.net/assets/stykanimate/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (editor.canGoBack()) editor.goBack() else finish()
            }
        })
    }

    private fun serveIsolatedAsset(path: String): WebResourceResponse {
        // WebViewAssetLoader removes the "/assets/" prefix before invoking this
        // handler. Therefore the requested index path is already
        // "stykanimate/index.html" and must NOT be prefixed a second time.
        if (path.isBlank() || path.startsWith("/") || path.split('/').any { it == ".." }) {
            return notFoundResponse()
        }

        return try {
            // CI copies upstream/build/web/* into app/src/main/assets/.
            // This preserves the requested /assets/stykanimate/... URL mapping.
            val stream = assets.open(path)
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
            val textAsset = extension in setOf("html", "css", "js", "mjs", "json", "webmanifest", "svg", "txt")
            val headers = mapOf(
                "Cross-Origin-Opener-Policy" to "same-origin",
                "Cross-Origin-Embedder-Policy" to "require-corp",
                "Cross-Origin-Resource-Policy" to "same-origin",
                "X-Content-Type-Options" to "nosniff",
                "Cache-Control" to "no-cache"
            )
            WebResourceResponse(
                mime,
                if (textAsset) "UTF-8" else null,
                200,
                "OK",
                headers,
                stream
            )
        } catch (_: java.io.IOException) {
            notFoundResponse()
        }
    }

    private fun notFoundResponse(): WebResourceResponse =
        WebResourceResponse(
            "text/plain",
            "UTF-8",
            404,
            "Not Found",
            mapOf("Cache-Control" to "no-store"),
            "StykAnimate asset not found in this APK".byteInputStream()
        )

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
