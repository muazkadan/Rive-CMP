#include "jni_refs.hpp"
#include "helpers/general.hpp"

#include "rive/assets/image_asset.hpp"
#include "rive/simple_array.hpp"

#include <jni.h>

extern "C" {
using namespace rive_desktop;

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_FileAsset_cppName(JNIEnv *env,
                                                    jobject,
                                                    jlong address) {
    auto *fileAsset = reinterpret_cast<rive::FileAsset *>(address);
    return env->NewStringUTF(fileAsset->name().c_str());
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_FileAsset_cppDecode(
    JNIEnv *env,
    jobject,
    jlong ref,
    jbyteArray assetBytes) {
    auto *fileAsset = reinterpret_cast<rive::FileAsset *>(ref);

    auto *fileFactory = GetFactory();
    auto *jAssetBytes = env->GetByteArrayElements(assetBytes, nullptr);
    if (jAssetBytes == nullptr) return JNI_FALSE;
    auto length = JIntToSizeT(env->GetArrayLength(assetBytes));

    // Turn into a SimpleArray so audio files can steal the bytes if they
    // want/need to.
    rive::SimpleArray bytesToDecode(
        reinterpret_cast<uint8_t *>(jAssetBytes),
        length);
    auto res = fileAsset->decode(bytesToDecode, fileFactory);
    env->ReleaseByteArrayElements(assetBytes, jAssetBytes, JNI_ABORT);
    return res;
}

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_FileAsset_cppCDNUrl(JNIEnv *env,
                                                      jobject,
                                                      jlong ref) {
    auto *fileAsset = reinterpret_cast<rive::FileAsset *>(ref);
    auto uuid = fileAsset->cdnUuidStr();
    if (uuid.empty()) {
        return env->NewStringUTF("");
    }

    auto cdnUrl = fileAsset->cdnBaseUrl();
    auto lastChar = cdnUrl.back();
    if (lastChar != '/') {
        cdnUrl += ('/');
    }
    cdnUrl += uuid;
    return env->NewStringUTF(cdnUrl.c_str());
}
}
