package app.stykanimate.android

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

/**
 * Android host for the locally packaged StykAnimate editor.
 *
 * The editor files are generated from the upstream source during the Android
 * release workflow and copied into app/src/main/assets/stykanimate/.
 * This activity never navigates to the public StykAnimate website.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var editor: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
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
                ) = assetLoader.shouldInterceptRequest(request.url)

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    // Keep the editor inside the packaged app. External links are
                    // intentionally not opened automatically from editor content.
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
