package app.stykanimate.android

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.nio.ByteBuffer
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var engine: NativeEngine
    private lateinit var status: TextView
    private lateinit var frameImage: ImageView
    private lateinit var timeline: SeekBar
    private lateinit var placeholder: TextView
    private var totalTicks = 1
    private var currentTick = 0
    private var engineReady = false

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) openProject(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildNativeInterface()
        try {
            engine = NativeEngine()
            engineReady = true
            status.text = "Native StykAnimate engine ready · Open a project to begin"
        } catch (error: UnsatisfiedLinkError) {
            status.text = "Native engine library is missing: \${error.message}"
        } catch (error: Exception) {
            status.text = "Unable to initialise native engine: \${error.message}"
        }
    }

    private fun buildNativeInterface() {
        window.statusBarColor = Color.rgb(13, 18, 28)
        window.navigationBarColor = Color.rgb(13, 18, 28)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(13, 18, 28))
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        toolbar.addView(makeButton("Open project") { openDocument.launch(arrayOf("*/*")) })
        toolbar.addView(makeButton("‹") { moveFrame(-1) }, weighted())
        toolbar.addView(makeButton("›") { moveFrame(1) }, weighted())
        root.addView(toolbar, LinearLayout.LayoutParams.MATCH_PARENT, dp(52))

        status = TextView(this).apply {
            setTextColor(Color.rgb(206, 218, 235))
            textSize = 13f
            setPadding(dp(4), dp(8), dp(4), dp(8))
            text = "Starting native engine…"
        }
        root.addView(status)

        val stage = FrameLayout(this).apply { setBackgroundColor(Color.rgb(24, 32, 46)) }
        frameImage = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = "Animation frame rendered by the native StykAnimate engine"
        }
        placeholder = TextView(this).apply {
            text = "Open a .piv, .stk, .styk, .stkz or .stykmov project"
            setTextColor(Color.rgb(160, 176, 198))
            textSize = 16f
            gravity = Gravity.CENTER
        }
        stage.addView(frameImage, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        stage.addView(placeholder, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        root.addView(stage, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        timeline = SeekBar(this).apply {
            max = 0
            progress = 0
            isEnabled = false
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        currentTick = progress
                        renderCurrentFrame()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
        root.addView(timeline, LinearLayout.LayoutParams.MATCH_PARENT, dp(42))

        val footer = TextView(this).apply {
            text = "Native Android interface · Original Rust engine · Offline"
            setTextColor(Color.rgb(119, 139, 164))
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(4))
        }
        root.addView(footer)
        setContentView(root)
    }

    private fun openProject(uri: android.net.Uri) {
        if (!engineReady) {
            status.text = "The native engine could not be loaded."
            return
        }
        status.text = "Reading project…"
        worker.execute {
            try {
                val name = displayName(uri)
                val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
                val supported = setOf("piv", "stk", "styk", "stkz", "stykmov", "json")
                require(extension in supported) { "Unsupported file type: .$extension" }
                val bytes = contentResolver.openInputStream(uri)?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(64 * 1024)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 128 * 1024 * 1024) { "Project is larger than the current 128 MB import limit" }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                } ?: error("Android could not open this document")
                val message = engine.openProject(bytes, extension, name)
                val ticks = engine.totalTicks().coerceAtLeast(1)
                runOnUiThread {
                    if (message.startsWith("Unable to open project:")) {
                        status.text = message
                    } else {
                        totalTicks = ticks
                        currentTick = 0
                        timeline.max = (totalTicks - 1).coerceAtLeast(0)
                        timeline.progress = 0
                        timeline.isEnabled = totalTicks > 1
                        status.text = message
                        placeholder.visibility = View.GONE
                        renderCurrentFrame()
                    }
                }
            } catch (error: Exception) {
                runOnUiThread { status.text = "Could not open project: \${error.message}" }
            }
        }
    }

    private fun moveFrame(delta: Int) {
        if (!engineReady || totalTicks <= 1) return
        currentTick = (currentTick + delta).coerceIn(0, totalTicks - 1)
        timeline.progress = currentTick
        renderCurrentFrame()
    }

    private fun renderCurrentFrame() {
        if (!engineReady || !::frameImage.isInitialized) return
        val width = (frameImage.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels).coerceIn(1, 2048)
        val height = (frameImage.height.takeIf { it > 0 } ?: (resources.displayMetrics.heightPixels * 0.65f).toInt()).coerceIn(1, 2048)
        val tick = currentTick
        status.text = "Rendering frame \${tick + 1} of $totalTicks with the native Rust engine…"
        worker.execute {
            try {
                val pixels = engine.renderFrame(tick, width, height)
                require(pixels.size == width * height * 4) { "The native renderer returned an invalid frame" }
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pixels))
                runOnUiThread {
                    frameImage.setImageBitmap(bitmap)
                    status.text = "Frame \${tick + 1} / $totalTicks · \${width}×\${height} · CPU raster renderer"
                }
            } catch (error: Exception) {
                runOnUiThread { status.text = "Rendering failed: \${error.message}" }
            }
        }
    }

    private fun displayName(uri: android.net.Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index)
            }
        }
        return uri.lastPathSegment ?: "animation.piv"
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setOnClickListener { action() }
        }

    private fun weighted() = LinearLayout.LayoutParams(0, dp(48), 1f)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        worker.shutdownNow()
        if (engineReady) engine.close()
        super.onDestroy()
    }
}
