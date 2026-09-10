#include "models/jni_file_asset_loader.hpp"
#include <jni.h>


#ifdef __cplusplus
extern "C" {
#endif

using namespace rive_desktop;

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_FileAssetLoader_constructor(
    JNIEnv *env,
    jobject ktObject) {
    // FileAssetLoader is now a RefCnt. Upon creation its count is 1.
    auto *fileAssetLoader = new JNIFileAssetLoader(ktObject, env);
    return (jlong) fileAssetLoader;
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_FileAssetLoader_cppDelete(JNIEnv *,
                                                            jobject,
                                                            jlong ref) {
    auto *fileAssetLoader = reinterpret_cast<JNIFileAssetLoader *>(ref);
    fileAssetLoader->unref();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_FileAssetLoader_cppRef(JNIEnv *,
                                                         jobject,
                                                         jlong ref) {
    auto *fileAssetLoader = reinterpret_cast<JNIFileAssetLoader *>(ref);
    fileAssetLoader->ref();
}
#ifdef __cplusplus
}
#endif
