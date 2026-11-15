package app.stykanimate.android

/** Kotlin owner for one native StykAnimate Rust engine instance. */
internal class NativeEngine : AutoCloseable {
    companion object {
        init { System.loadLibrary("stykanimate_android") }
    }

    private var handle: Long = nativeCreate()

    @Synchronized
    fun openProject(bytes: ByteArray, extension: String, name: String): String {
        check(handle != 0L) { "Native engine is closed" }
        return nativeOpenProject(handle, bytes, extension, name)
    }

    @Synchronized
    fun totalTicks(): Int {
        check(handle != 0L) { "Native engine is closed" }
        return nativeGetTotalTicks(handle)
    }

    @Synchronized
    fun renderFrame(tick: Int, width: Int, height: Int): ByteArray {
        check(handle != 0L) { "Native engine is closed" }
        return nativeRenderFrame(handle, tick, width, height)
    }

    @Synchronized
    override fun close() {
        if (handle != 0L) {
            nativeClose(handle)
            handle = 0L
        }
    }

    private external fun nativeCreate(): Long
    private external fun nativeOpenProject(handle: Long, bytes: ByteArray, extension: String, name: String): String
    private external fun nativeGetTotalTicks(handle: Long): Int
    private external fun nativeRenderFrame(handle: Long, tick: Int, width: Int, height: Int): ByteArray
    private external fun nativeClose(handle: Long)
}
