//! JNI bridge for the upstream StykAnimate Rust engine.
//! The Android app calls the same document, evaluation and CPU rendering crates as upstream.

use std::sync::Arc;
use std::sync::OnceLock;
use std::time::Instant;

use jni::JNIEnv;
use jni::objects::{JByteArray, JObject, JString};
use jni::sys::{jbyteArray, jint, jlong, jstring};

use styk_engine::frame_builder::{FrameBuilder, Options};
use styk_engine::{Budgets, Engine, Generation};
use styk_raster::{Edges, Images, Raster, ViewParams, decode_all};

static START: OnceLock<Instant> = OnceLock::new();

fn clock_ms() -> f64 {
    START.get_or_init(Instant::now).elapsed().as_secs_f64() * 1000.0
}

struct NativeSession {
    engine: Engine,
    generation: Option<Arc<Generation>>,
    builder: FrameBuilder,
    raster: Raster,
    images: Images,
    total_ticks: i32,
}

impl NativeSession {
    fn new() -> Self {
        Self {
            engine: Engine::new(clock_ms, Budgets::for_device(None)),
            generation: None,
            builder: FrameBuilder::default(),
            raster: Raster::new(Edges::Smooth),
            images: Images::default(),
            total_ticks: 1,
        }
    }

    fn open(&mut self, bytes: &[u8], extension: &str, name: &str) -> Result<String, String> {
        let opened = self.engine.open(bytes, extension, name).map_err(|error| error.to_string())?;
        let generation = self.engine.generation().ok_or_else(|| "The engine did not publish the opened document".to_string())?;
        self.images = decode_all(&generation.prepared.doc, &generation.assets);
        self.total_ticks = opened.ticks.max(1) as i32;
        self.generation = Some(generation);
        self.builder = FrameBuilder::default();
        Ok(format!("Opened {} — {} frames, {} figures, {} fps", opened.name, opened.ticks, opened.figures, opened.fps))
    }

    fn render(&mut self, tick: f64, width: u32, height: u32) -> Result<Vec<u8>, String> {
        let generation = self.generation.as_ref().ok_or_else(|| "Open a project first".to_string())?.clone();
        let frame = self.builder.prepare(&generation, tick.clamp(0.0, self.total_ticks.saturating_sub(1) as f64), &Options::default(), clock_ms, || false)
            .ok_or_else(|| "Frame preparation was cancelled".to_string())?;
        let canvas = generation.prepared.doc.canvas;
        let scale = (width as f64 / canvas[0].max(1) as f64).min(height as f64 / canvas[1].max(1) as f64);
        let offset_x = (width as f64 - canvas[0] as f64 * scale) * 0.5;
        let offset_y = (height as f64 - canvas[1] as f64 * scale) * 0.5;
        let view = ViewParams {
            matrix: [scale, 0.0, 0.0, scale, offset_x, offset_y],
            artboard: [0.0, 0.0, canvas[0] as f64, canvas[1] as f64],
            clip_to_artboard: true,
            clear: [0.0, 0.0, 0.0, 0.0],
            zoom: scale,
            pan: [offset_x, offset_y],
            stretch: 1.0,
            camera_view: false,
            camera_limit: 2048.0,
        };
        let rendered = self.raster.render(&frame.list, &self.images, &view, [width.max(1), height.max(1)]);
        Ok(rendered.straight())
    }
}

unsafe fn session_mut<'a>(handle: jlong) -> Option<&'a mut NativeSession> {
    if handle == 0 { return None; }
    // The handle is created by nativeCreate and released exactly once by nativeClose.
    Some(unsafe { &mut *(handle as *mut NativeSession) })
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_app_stykanimate_android_NativeEngine_nativeCreate(_env: JNIEnv, _this: JObject) -> jlong {
    Box::into_raw(Box::new(NativeSession::new())) as jlong
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_app_stykanimate_android_NativeEngine_nativeOpenProject(
    mut env: JNIEnv, _this: JObject, handle: jlong, bytes: JByteArray, extension: JString, name: JString
) -> jstring {
    let result = (|| {
        let session = unsafe { session_mut(handle) }.ok_or_else(|| "Native engine is not initialised".to_string())?;
        let bytes = env.convert_byte_array(bytes).map_err(|error| error.to_string())?;
        let extension: String = env.get_string(&extension).map_err(|error| error.to_string())?.into();
        let name: String = env.get_string(&name).map_err(|error| error.to_string())?.into();
        session.open(&bytes, &extension, &name)
    })();
    let message = result.unwrap_or_else(|error| format!("Unable to open project: {error}"));
    env.new_string(message).map(|value| value.into_raw()).unwrap_or(std::ptr::null_mut())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_app_stykanimate_android_NativeEngine_nativeGetTotalTicks(_env: JNIEnv, _this: JObject, handle: jlong) -> jint {
    unsafe { session_mut(handle) }.map_or(0, |session| session.total_ticks)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_app_stykanimate_android_NativeEngine_nativeRenderFrame(
    mut env: JNIEnv, _this: JObject, handle: jlong, tick: jint, width: jint, height: jint
) -> jbyteArray {
    let result = unsafe { session_mut(handle) }.ok_or_else(|| "Native engine is not initialised".to_string())
        .and_then(|session| session.render(tick.max(0) as f64, width.clamp(1, 4096) as u32, height.clamp(1, 4096) as u32));
    match result {
        Ok(pixels) => env.byte_array_from_slice(&pixels).map(|array| array.into_raw()).unwrap_or(std::ptr::null_mut()),
        Err(_) => env.new_byte_array(0).map(|array| array.into_raw()).unwrap_or(std::ptr::null_mut()),
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_app_stykanimate_android_NativeEngine_nativeClose(_env: JNIEnv, _this: JObject, handle: jlong) {
    if handle != 0 {
        // The Kotlin wrapper zeroes its handle after this call; close must be called once.
        drop(unsafe { Box::from_raw(handle as *mut NativeSession) });
    }
}
