#include <jni.h>

#include "helpers/general.hpp"
#include "rive/file.hpp"
#include "rive/viewmodel/runtime/viewmodel_runtime.hpp"

#ifdef __cplusplus
extern "C" {
#endif
using namespace rive_desktop;

// FILE
JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_File_import(JNIEnv *env,
                                                         jobject,
                                                         jbyteArray bytes,
                                                         jint length,
                                                         jlong fileAssetLoader) {
    auto *assetLoader =
            reinterpret_cast<rive::FileAssetLoader *>(fileAssetLoader);

    auto *byte_array = env->GetByteArrayElements(bytes, nullptr);
    auto file = Import(reinterpret_cast<uint8_t *>(byte_array),
                       length,
                       assetLoader);
    env->ReleaseByteArrayElements(bytes, byte_array, JNI_ABORT);
    return file;
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_File_cppArtboardByName(JNIEnv *env,
                                                                    jobject,
                                                                    jlong ref,
                                                                    jstring name) {
    auto file = reinterpret_cast<rive::File *>(ref);
    // Creates a new Artboard instance.
    auto artboard = file->artboardNamed(JStringToString(env, name));
    if (artboard != nullptr) {
        artboard->advance(0.0);
    }
    return reinterpret_cast<jlong>(artboard.release());
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_File_cppArtboardByIndex(JNIEnv *,
                                                                     jobject,
                                                                     jlong ref,
                                                                     jint index) {
    auto file = reinterpret_cast<rive::File *>(ref);
    // Creates a new Artboard instance.
    auto artboard = file->artboardAt(index);
    if (artboard != nullptr) {
        artboard->advance(0.0);
    }
    return (jlong) artboard.release();
}

JNIEXPORT

jstring JNICALL
Java_dev_muazkadan_rivecmp_native_File_cppArtboardNameByIndex(JNIEnv *env,
                                                                         jobject,
                                                                         jlong ref,
                                                                         jint index) {
    auto file = reinterpret_cast<rive::File *>(ref);

    auto artboard = file->artboard(index);
    auto name = artboard->name();
    return env->NewStringUTF(name.c_str());
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_File_cppArtboardCount(JNIEnv *,
                                                                   jobject,
                                                                   jlong ref) {
    auto file = reinterpret_cast<rive::File *>(ref);

    return (jint) file->artboardCount();
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_File_cppDefaultViewModelForArtboard(
    JNIEnv *,
    jobject,
    jlong fileRef,
    jlong artboardRef) {
    auto file = reinterpret_cast<rive::File *>(fileRef);
    auto artboard = reinterpret_cast<rive::Artboard *>(artboardRef);
    auto viewModel = file->defaultArtboardViewModel(artboard);
    return reinterpret_cast<jlong>(viewModel);
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_File_cppDelete(JNIEnv *,
                                                            jobject,
                                                            jlong ref) {
    auto file = reinterpret_cast<rive::File *>(ref);
    // Because files are created as an RCP type that has been released, we
    // have an extra ref count to un-ref here.
    file->unref();
}

#ifdef __cplusplus
}
#endif
