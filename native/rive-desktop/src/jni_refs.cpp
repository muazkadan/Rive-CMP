#include <jni.h>
#include "jni_refs.hpp"
#include "helpers/general.hpp"

namespace rive_desktop {
    jclass GetClass(const char *name) { return GetJNIEnv()->FindClass(name); }

    jmethodID GetMethodId(jclass clazz, const char *name, const char *sig) {
        JNIEnv *env = GetJNIEnv();
        jmethodID output = env->GetMethodID(clazz, name, sig);
        env->DeleteLocalRef(clazz);
        return output;
    }

    jmethodID GetStaticMethodId(jclass clazz, const char *name, const char *sig) {
        JNIEnv *env = GetJNIEnv();
        jmethodID output = env->GetStaticMethodID(clazz, name, sig);
        env->DeleteLocalRef(clazz);
        return output;
    }

    jfieldID GetStaticFieldId(jclass clazz, const char *name, const char *sig) {
        JNIEnv *env = GetJNIEnv();
        jfieldID output = env->GetStaticFieldID(clazz, name, sig);
        env->DeleteLocalRef(clazz);
        return output;
    }

    jfieldID GetFieldId(jclass clazz, const char *name, const char *sig) {
        JNIEnv *env = GetJNIEnv();
        jfieldID output = env->GetFieldID(clazz, name, sig);
        env->DeleteLocalRef(clazz);
        return output;
    }

    jint ThrowRiveException(const char *message) {
        jclass exClass =
                GetClass("dev/muazkadan/rivecmp/native/RiveException");
        return GetJNIEnv()->ThrowNew(exClass, message);
    }

    jint ThrowMalformedFileException(const char *message) {
        jclass exClass =
                GetClass("dev/muazkadan/rivecmp/native/MalformedFileException");
        return GetJNIEnv()->ThrowNew(exClass, message);
    }

    jint ThrowUnsupportedRuntimeVersionException(const char *message) {
        jclass exClass = GetClass("dev/muazkadan/rivecmp/native/"
            "UnsupportedRuntimeVersionException");
        return GetJNIEnv()->ThrowNew(exClass, message);
    }

    jclass GetHashMapClass() { return GetClass("java/util/HashMap"); }

    jmethodID GetHashMapConstructorId() {
        return GetMethodId(GetHashMapClass(), "<init>", "()V");
    }

    jclass GetFloatClass() { return GetClass("java/lang/Float"); }

    jmethodID GetFloatConstructor() {
        return GetMethodId(GetFloatClass(), "<init>", "(F)V");
    }

    jclass GetBooleanClass() { return GetClass("java/lang/Boolean"); }

    jmethodID GetBooleanConstructor() {
        return GetMethodId(GetBooleanClass(), "<init>", "(Z)V");
    }

    jclass GetShortClass() { return GetClass("java/lang/Short"); }

    jmethodID GetShortConstructor() {
        return GetMethodId(GetShortClass(), "<init>", "(S)V");
    }

    jclass GetLoopClass() { return GetClass("dev/muazkadan/rivecmp/native/Loop"); }

    jfieldID GetNoneLoopField() {
        return GetStaticFieldId(GetLoopClass(),
                                "NONE",
                                "Ldev/muazkadan/rivecmp/native/Loop;");
    }

    jfieldID GetOneShotLoopField() {
        return GetStaticFieldId(GetLoopClass(),
                                "ONESHOT",
                                "Ldev/muazkadan/rivecmp/native/Loop;");
    }

    jfieldID GetLoopLoopField() {
        return GetStaticFieldId(GetLoopClass(),
                                "LOOP",
                                "Ldev/muazkadan/rivecmp/native/Loop;");
    }

    jfieldID GetPingPongLoopField() {
        return GetStaticFieldId(GetLoopClass(),
                                "PINGPONG",
                                "Ldev/muazkadan/rivecmp/native/Loop;");
    }

    jclass GetAdvanceResultClass() {
        return GetClass("dev/muazkadan/rivecmp/native/AdvanceResult");
    }

    jfieldID GetAdvanceResultAdvancedField() {
        return GetStaticFieldId(GetAdvanceResultClass(),
                                "ADVANCED",
                                "Ldev/muazkadan/rivecmp/native/AdvanceResult;");
    }

    jfieldID GetAdvanceResultOneShotField() {
        return GetStaticFieldId(GetAdvanceResultClass(),
                                "ONESHOT",
                                "Ldev/muazkadan/rivecmp/native/AdvanceResult;");
    }

    jfieldID GetAdvanceResultLoopField() {
        return GetStaticFieldId(GetAdvanceResultClass(),
                                "LOOP",
                                "Ldev/muazkadan/rivecmp/native/AdvanceResult;");
    }

    jfieldID GetAdvanceResultPingPongField() {
        return GetStaticFieldId(GetAdvanceResultClass(),
                                "PINGPONG",
                                "Ldev/muazkadan/rivecmp/native/AdvanceResult;");
    }

    jfieldID GetAdvanceResultNoneField() {
        return GetStaticFieldId(GetAdvanceResultClass(),
                                "NONE",
                                "Ldev/muazkadan/rivecmp/native/AdvanceResult;");
    }
} // namespace rive_desktop
