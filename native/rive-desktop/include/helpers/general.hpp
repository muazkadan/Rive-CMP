#pragma once

#include <jni.h>
#include <string>

#include "rive/factory.hpp"
#include "rive/file_asset_loader.hpp"

namespace rive_desktop
{
    extern JavaVM* g_JVM;
    jlong Import(uint8_t*,
            jint,
            rive::FileAssetLoader* = nullptr);

    rive::Fit GetFit(uint8_t ordinal);
    rive::Alignment GetAlignment(uint8_t ordinal);
    rive::Factory* GetFactory();
    JNIEnv* GetJNIEnv();

    void DetachThread();

    std::string JStringToString(JNIEnv*, jstring);
    int SizeTToInt(size_t);
    size_t JIntToSizeT(jint);
} // namespace rive_desktop
