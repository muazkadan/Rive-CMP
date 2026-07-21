#include "jni_refs.hpp"
#include "helpers/general.hpp"
#include "helpers/jni_resource.hpp"
#include "helpers/rive_log.hpp"
#include <jni.h>

#if defined(DEBUG) || defined(LOG)

#include <thread>

#endif

#ifdef __cplusplus
extern "C" {
#endif
using namespace rive_desktop;

jint JNI_OnLoad(JavaVM *jvm, void *) {
    // Assign the global JVM
    g_JVM = jvm;
    // Standard JNI version to return on Desktop
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_Rive_cppInitialize(JNIEnv *env,
                                                     jobject jObj) {
    const auto TAG = "RiveN/Init";

    // Initialize RiveLog helper for logging from C++
    InitializeRiveLog();

    // Use the calling Rive jobject as the "anchor" to initialize the global
    // class loader.
    RiveLogD(TAG, "Initializing global class loader");
    InitJNIClassLoader(env, jObj);
}

#ifdef __cplusplus
}
#endif
