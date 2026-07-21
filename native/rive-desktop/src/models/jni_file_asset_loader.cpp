#include "models/jni_file_asset_loader.hpp"
#include "helpers/jni_exception_handler.hpp"
#include "helpers/rive_log.hpp"

namespace rive_desktop {
    JNIFileAssetLoader::JNIFileAssetLoader(jobject ktObject, JNIEnv *env) {
        m_ktFileAssetLoader = env->NewGlobalRef(ktObject);
        jclass ktClass = env->GetObjectClass(ktObject);
        m_ktLoadContentsFn =
                env->GetMethodID(ktClass,
                                 "loadContents",
                                 "(Ldev/muazkadan/rivecmp/native/FileAsset;[B)Z");
    }

    JNIFileAssetLoader::~JNIFileAssetLoader() {
        JNIEnv *env = GetJNIEnv();
        if (m_ktFileAssetLoader) {
            env->DeleteGlobalRef(m_ktFileAssetLoader);
        }
        m_ktLoadContentsFn = nullptr;
    }

    bool JNIFileAssetLoader::loadContents(rive::FileAsset &asset,
                                          rive::Span<const uint8_t> inBandBytes,
                                          rive::Factory * /* unused atm */) {
        JNIEnv *env = GetJNIEnv();

        jobject ktFileAsset = MakeKtAsset(env, asset);
        if (!ktFileAsset) {
            RiveLogE("RiveN/AssetLoader",
                     "loadContents() failed to create FileAsset");
            return false;
        }

        // Is inBandBytes always defined? Can it ever be a nullptr?
        jbyteArray byteArray =
                env->NewByteArray(SizeTToInt(inBandBytes.size()));
        if (!byteArray) {
            RiveLogE("RiveN/AssetLoader",
                     "loadContents() failed to allocate NewByteArray");
            return false;
        }
        env->SetByteArrayRegion(byteArray,
                                0,
                                SizeTToInt(inBandBytes.size()),
                                (jbyte *) inBandBytes.data());
        jboolean result =
                JNIExceptionHandler::CallBooleanMethod(env,
                                                       m_ktFileAssetLoader,
                                                       m_ktLoadContentsFn,
                                                       ktFileAsset,
                                                       byteArray);

        env->DeleteLocalRef(byteArray);
        env->DeleteLocalRef(ktFileAsset);
        return result;
    }
} // namespace rive_desktop
