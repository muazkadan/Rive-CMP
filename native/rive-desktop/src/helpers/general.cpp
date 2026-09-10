#include "helpers/general.hpp"

#include "jni_refs.hpp"
#include "models/canvas_factory.hpp"
#include "helpers/jni_exception_handler.hpp"
#include "helpers/rive_log.hpp"
#include "rive/file.hpp"

namespace rive_desktop {
    /**
     * Global factories that are used to instantiate render objects (paths, buffers,
     * textures, etc.)
     */
    static rive::CanvasFactory g_SkiaFactory;

    JavaVM *g_JVM = nullptr;

    JNIEnv *GetJNIEnv() {
        // double check it's all ok
        JNIEnv *g_env = nullptr;
        int getEnvStat = g_JVM->GetEnv(reinterpret_cast<void **>(&g_env), JNI_VERSION_1_6);
        if (getEnvStat == JNI_EDETACHED) {
            RiveLogW("RiveN/GetJNIEnv", "GetJNIEnv - Not Attached.");
            if (g_JVM->AttachCurrentThread(reinterpret_cast<void **>(&g_env), nullptr) != 0) {
                RiveLogE("RiveN/GetJNIEnv", "Failed to attach current thread.");
            }
        } else if (getEnvStat == JNI_OK) {
            //
        } else if (getEnvStat == JNI_EVERSION) {
            RiveLogE("RiveN/GetJNIEnv",
                     "GetJNIEnv: unsupported version %d",
                     getEnvStat);
        }
        return g_env;
    }

    void DetachThread() {
        if (g_JVM->DetachCurrentThread() != JNI_OK) {
            RiveLogE("RiveN/GetJNIEnv", "DetachCurrentThread failed.");
        }
    }

    rive::Fit GetFit(uint8_t ordinal) {
        switch (ordinal) {
            case 0:
                return rive::Fit::fill;
            case 1:
                return rive::Fit::contain;
            case 2:
                return rive::Fit::cover;
            case 3:
                return rive::Fit::fitWidth;
            case 4:
                return rive::Fit::fitHeight;
            case 5:
                return rive::Fit::layout;
            case 6:
                return rive::Fit::none;
            default:
                RiveLogE("RiveN/GetFit", "Invalid fit ordinal: %u", ordinal);
                return rive::Fit::none;
        }
    }

    rive::Alignment GetAlignment(uint8_t ordinal) {
        switch (ordinal) {
            case 0:
                return rive::Alignment::topLeft;
            case 1:
                return rive::Alignment::topCenter;
            case 2:
                return rive::Alignment::topRight;
            case 3:
                return rive::Alignment::centerLeft;
            case 4:
                return rive::Alignment::center;
            case 5:
                return rive::Alignment::centerRight;
            case 6:
                return rive::Alignment::bottomLeft;
            case 7:
                return rive::Alignment::bottomCenter;
            case 8:
                return rive::Alignment::bottomRight;
            default:
                RiveLogE("RiveN/GetAlignment",
                         "Invalid alignment ordinal: %u",
                         ordinal);
                assert(false && "Invalid rive::Alignment ordinal");
                std::abort();
        }
    }

    rive::Factory *GetFactory() {
        return &g_SkiaFactory;
    }

    jlong Import(uint8_t *bytes,
                 jint length,
                 rive::FileAssetLoader *assetLoader) {
        rive::ImportResult result;
        auto *fileFactory = GetFactory();
        auto *file = rive::File::import(rive::Span<const uint8_t>(bytes, length),
                                        fileFactory,
                                        &result,
                                        assetLoader)
                // Release the RCP to a raw pointer.
                // We will un-ref when deleting the file.
                .release();
        if (result == rive::ImportResult::success) {
            return reinterpret_cast<jlong>(file);
        } else if (result == rive::ImportResult::unsupportedVersion) {
            return ThrowUnsupportedRuntimeVersionException(
                "Unsupported Rive File Version.");
        } else if (result == rive::ImportResult::malformed) {
            return ThrowMalformedFileException("Malformed Rive File.");
        } else {
            return ThrowRiveException("Unknown error loading file.");
        }
    }

    std::string JStringToString(JNIEnv *env, jstring jStr) {
        if (jStr == nullptr) {
            return {};
        }
        auto *cStr = env->GetStringUTFChars(jStr, nullptr);
        if (cStr == nullptr) {
            return {};
        }
        auto str = std::string(cStr);
        env->ReleaseStringUTFChars(jStr, cStr);
        return str;
    }

    int SizeTToInt(size_t sizeT) {
        return sizeT > INT_MAX ? INT_MAX : static_cast<int>(sizeT);
    }

    size_t JIntToSizeT(jint jintValue) {
        if (jintValue < 0) {
            RiveLogW("RiveN/JIntToSizeT", "Value is a negative number %d", jintValue);
            return 0;
        }
        return jintValue > SIZE_T_MAX ? SIZE_T_MAX : static_cast<size_t>(jintValue);
    }
} // namespace rive_desktop
