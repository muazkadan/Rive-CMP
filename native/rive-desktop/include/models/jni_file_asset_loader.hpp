#pragma once

#include <jni.h>

#include "helpers/general.hpp"
#include "helpers/rive_log.hpp"
#include "rive/factory.hpp"
#include "rive/file_asset_loader.hpp"
#include "rive/assets/image_asset.hpp"
#include "rive/assets/font_asset.hpp"
#include "rive/assets/audio_asset.hpp"

namespace rive_desktop {
    class JNIFileAssetLoader : public rive::FileAssetLoader {
    public:
        JNIFileAssetLoader(jobject, JNIEnv *);

        ~JNIFileAssetLoader() override;

        bool loadContents(rive::FileAsset &,
                          rive::Span<const uint8_t>,
                          rive::Factory *) override;

        static jobject MakeKtAsset(JNIEnv *env,
                                   rive::FileAsset &asset) {
            jclass assetClass = nullptr;
            if (asset.is<rive::ImageAsset>()) {
                assetClass =
                        env->FindClass("dev/muazkadan/rivecmp/native/ImageAsset");
            } else if (asset.is<rive::FontAsset>()) {
                assetClass =
                        env->FindClass("dev/muazkadan/rivecmp/native/FontAsset");
            } else if (asset.is<rive::AudioAsset>()) {
                assetClass =
                        env->FindClass("dev/muazkadan/rivecmp/native/AudioAsset");
            } else {
                RiveLogW("RiveN/AssetLoader",
                         "Trying to make unknown file asset type %d",
                         asset.typeKey);
            }

            if (!assetClass) {
                RiveLogE("RiveN/AssetLoader",
                         "MakeKtAsset() failed to find FileAsset class");
                return nullptr;
            }

            jmethodID fileAssetConstructor =
                    env->GetMethodID(assetClass, "<init>", "(J)V");
            if (!fileAssetConstructor) {
                RiveLogE("RiveN/AssetLoader",
                         "MakeKtAsset() failed to find FileAsset constructor");
                env->DeleteLocalRef(assetClass);
                return nullptr;
            }

            jobject ktFileAsset = env->NewObject(assetClass,
                                                 fileAssetConstructor,
                                                 reinterpret_cast<jlong>(&asset));
            if (!ktFileAsset) {
                RiveLogE("RiveN/AssetLoader",
                         "MakeKtAsset() failed to create FileAsset");
                env->DeleteLocalRef(assetClass);
                return nullptr;
            }
            return ktFileAsset;
        }

    private:
        jobject m_ktFileAssetLoader = nullptr;
        jmethodID m_ktLoadContentsFn;
    };
} // namespace rive_desktop
