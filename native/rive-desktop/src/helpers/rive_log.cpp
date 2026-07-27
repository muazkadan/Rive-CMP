#include "helpers/rive_log.hpp"
#include "helpers/general.hpp"
#include "helpers/jni_resource.hpp"
#include <cstdio>

// Bootstrap stderr fallback, used only before RiveLog's JNI bridge is
// initialized or when it fails. Everything else must go through RiveLog*.
#define LOG_TAG "rive-desktop-jni"
#define LOGE(...) fprintf(stderr, "[%s][E] ", LOG_TAG), fprintf(stderr, __VA_ARGS__), fprintf(stderr, "\n")
#define LOGW(...) fprintf(stderr, "[%s][W] ", LOG_TAG), fprintf(stderr, __VA_ARGS__), fprintf(stderr, "\n")
#define LOGD(...) fprintf(stderr, "[%s][D] ", LOG_TAG), fprintf(stderr, __VA_ARGS__), fprintf(stderr, "\n")
#define LOGI(...) fprintf(stdout, "[%s][I] ", LOG_TAG), fprintf(stdout, __VA_ARGS__), fprintf(stdout, "\n")

namespace rive_desktop {
    // Cached class and method IDs for RiveLog
    static jclass g_riveLogClass = nullptr;
    static jmethodID g_riveLogVMethod = nullptr;
    static jmethodID g_riveLogDMethod = nullptr;
    static jmethodID g_riveLogIMethod = nullptr;
    static jmethodID g_riveLogWMethod = nullptr;
    static jmethodID g_riveLogEMethod = nullptr;
    static std::mutex g_riveLogMutex;
    static bool g_riveLogInitialized = false;

    void InitializeRiveLog() {
        std::lock_guard lock(g_riveLogMutex);
        // Idempotent initialization
        if (g_riveLogInitialized) {
            return;
        }

        JNIEnv *env = GetJNIEnv();
        if (env == nullptr) {
            LOGE("RiveLog initialization failed: Unable to get JNIEnv");
            return;
        }

        // Find the RiveLog class
        auto riveLogClass = FindClass(env, "dev/muazkadan/rivecmp/native/RiveLog");
        if (riveLogClass.get() == nullptr) {
            LOGE("RiveLog initialization failed: RiveLog class not found");
            return;
        }

        // Cache the class as a global reference
        g_riveLogClass =
                reinterpret_cast<jclass>(env->NewGlobalRef(riveLogClass.get()));
        if (g_riveLogClass == nullptr) {
            LOGE("RiveLog initialization failed: Unable to create global RiveLog class ref");
            return;
        }

        // Get method IDs for the logging methods
        // These call the @JvmStatic methods that take (String, String)
        g_riveLogVMethod =
                env->GetStaticMethodID(g_riveLogClass,
                                       "v",
                                       "(Ljava/lang/String;Ljava/lang/String;)V");
        g_riveLogDMethod =
                env->GetStaticMethodID(g_riveLogClass,
                                       "d",
                                       "(Ljava/lang/String;Ljava/lang/String;)V");
        g_riveLogIMethod =
                env->GetStaticMethodID(g_riveLogClass,
                                       "i",
                                       "(Ljava/lang/String;Ljava/lang/String;)V");
        g_riveLogWMethod =
                env->GetStaticMethodID(g_riveLogClass,
                                       "w",
                                       "(Ljava/lang/String;Ljava/lang/String;)V");
        g_riveLogEMethod =
                env->GetStaticMethodID(g_riveLogClass,
                                       "e",
                                       "(Ljava/lang/String;Ljava/lang/String;)V");

        // Check if any method ID lookup failed
        if (env->ExceptionCheck()) {
            LOGE("RiveLog initialization failed: Error getting logging method IDs");
            env->ExceptionDescribe(); // Log the exception details
            env->ExceptionClear();
            // Method IDs will be nullptr, and we'll fallback to android log
        }

        g_riveLogInitialized = true;
    }

    // Internal helper to format and log a message
    static void LogMessage(jmethodID methodID,
                           const char *tag,
                           const char *format,
                           va_list args,
                           int logLevel) {
        // Format the message using vsnprintf
        char buffer[512];
        va_list argsCopy;
        va_copy(argsCopy, args);
        int result = vsnprintf(buffer, sizeof(buffer), format, argsCopy);
        va_end(argsCopy);

        if (result < 0) {
            LOGE("Logging error: vsnprintf failed");
            return;
        }

        // Ensure null termination
        if (static_cast<size_t>(result) >= sizeof(buffer)) {
            buffer[sizeof(buffer) - 1] = '\0';
        }

        // Try to use RiveLog if initialized, otherwise fallback to android log
        if (methodID != nullptr && g_riveLogInitialized) {
            JNIEnv *env = nullptr;
            auto getEnvStat = g_JVM->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
            if (getEnvStat != JNI_OK) {
                LOGE("Logging error: Unable to get JNIEnv for RiveLog");
                // Fall through to LOG macros fallback so the message is still emitted.
            } else {
                // Create Kotlin strings for tag and message
                auto jTag = MakeJString(env, tag);
                auto jMessage = MakeJString(env, buffer);

                // Call the static method
                env->CallStaticVoidMethod(g_riveLogClass,
                                          methodID,
                                          jTag.get(),
                                          jMessage.get());

                // Check for exceptions (but don't throw - logging shouldn't crash)
                if (env->ExceptionCheck()) {
                    LOGE("Logging error: Exception occurred in RiveLog method");
                    env->ExceptionDescribe(); // Log the exception details
                    env->ExceptionClear();
                    // Fall through to LOG macros fallback
                } else {
                    return; // Successfully logged via RiveLog
                }
            }
        }

        // Fallback to LOG macros from general.hpp
        switch (logLevel) {
            case 0: LOGD("[%s] %s", tag, buffer);
                break;
            case 1: LOGD("[%s] %s", tag, buffer);
                break;
            case 2: LOGI("[%s] %s", tag, buffer);
                break;
            case 3: LOGW("[%s] %s", tag, buffer);
                break;
            case 4: LOGE("[%s] %s", tag, buffer);
                break;
            default: LOGD("[%s] %s", tag, buffer);
        }
    }

    void RiveLogV(const char *tag, const char *format, ...) {
        va_list args;
        va_start(args, format);
        LogMessage(g_riveLogVMethod, tag, format, args, 0);
        va_end(args);
    }

    void RiveLogD(const char *tag, const char *format, ...) {
        va_list args;
        va_start(args, format);
        LogMessage(g_riveLogDMethod, tag, format, args, 1);
        va_end(args);
    }

    void RiveLogI(const char *tag, const char *format, ...) {
        va_list args;
        va_start(args, format);
        LogMessage(g_riveLogIMethod, tag, format, args, 2);
        va_end(args);
    }

    void RiveLogW(const char *tag, const char *format, ...) {
        va_list args;
        va_start(args, format);
        LogMessage(g_riveLogWMethod, tag, format, args, 3);
        va_end(args);
    }

    void RiveLogE(const char *tag, const char *format, ...) {
        va_list args;
        va_start(args, format);
        LogMessage(g_riveLogEMethod, tag, format, args, 4);
        va_end(args);
    }
} // namespace rive_desktop
