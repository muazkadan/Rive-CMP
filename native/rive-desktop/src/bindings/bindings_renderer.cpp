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

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_Renderer_cppSetBuffer(
    JNIEnv *env, jobject, jobject directPixels, jint width, jint height, jlong rendererPtr) {
    auto *renderer = reinterpret_cast<CanvasRenderer *>(rendererPtr);
    if (!renderer) {
        return;
    }

    void *pixels = env->GetDirectBufferAddress(directPixels);
    if (pixels) {
        renderer->bind(pixels, width, height);
    }
}

#ifdef __cplusplus
}
#endif
