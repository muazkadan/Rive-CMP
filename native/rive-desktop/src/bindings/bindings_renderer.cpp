#include <jni.h>
#include "models/canvas_renderer.hpp"

#ifdef __cplusplus
extern "C" {
#endif


// Renderer
JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_Renderer_constructor(JNIEnv *, jobject) {
    return reinterpret_cast<jlong>(new CanvasRenderer());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_Renderer_cppDelete(JNIEnv *,
                                                                jobject,
                                                                jlong rendererRef) {
    if (auto *renderer = reinterpret_cast<CanvasRenderer *>(rendererRef)) {
        renderer->unbind();
        delete renderer;
    }
}

// Maps the ordinal of the Kotlin PixelFormat enum to the Skia colour type with
// the same byte order.
static bool GetColorType(jint pixelFormatOrdinal, SkColorType *colorType) {
    switch (pixelFormatOrdinal) {
        case 0:
            *colorType = kRGBA_8888_SkColorType;
            return true;
        case 1:
            *colorType = kBGRA_8888_SkColorType;
            return true;
        default:
            rive_desktop::RiveLogE("RiveN/Renderer",
                                   "Invalid pixel format ordinal: %d",
                                   pixelFormatOrdinal);
            return false;
    }
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_Renderer_cppSetBuffer(
    JNIEnv *env,
    jobject,
    jobject directPixels,
    jint width,
    jint height,
    jint rowBytes,
    jint pixelFormatOrdinal,
    jlong rendererPtr) {
    auto *renderer = reinterpret_cast<CanvasRenderer *>(rendererPtr);
    if (!renderer) {
        return;
    }

    SkColorType colorType;
    if (!GetColorType(pixelFormatOrdinal, &colorType)) {
        return;
    }

    void *pixels = env->GetDirectBufferAddress(directPixels);
    if (pixels) {
        renderer->bind(pixels, width, height, static_cast<size_t>(rowBytes), colorType);
    }
}

#ifdef __cplusplus
}
#endif
