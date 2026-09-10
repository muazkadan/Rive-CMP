#include "jni_refs.hpp"
#include "rive/animation/loop.hpp"
#include "rive/animation/linear_animation_instance.hpp"
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
using namespace rive_desktop;

JNIEXPORT jobject JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppAdvanceAndGetResult(
    JNIEnv *env,
    jobject,
    jlong ref,
    jfloat elapsedTime) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);

    bool keepGoing = animationInstance->advance(elapsedTime);
    bool didLoop = animationInstance->didLoop();

    jfieldID resultFieldId = nullptr;

    if (didLoop) {
        switch (animationInstance->loop()) {
            case rive::Loop::oneShot:
                resultFieldId = GetAdvanceResultOneShotField();
                break;
            case rive::Loop::loop:
                resultFieldId = GetAdvanceResultLoopField();
                break;
            case rive::Loop::pingPong:
                resultFieldId = GetAdvanceResultPingPongField();
                break;
        }
    } else if (keepGoing) {
        resultFieldId = GetAdvanceResultAdvancedField();
    } else {
        resultFieldId = GetAdvanceResultNoneField();
    }

    jclass jAdvanceResultClass = GetAdvanceResultClass();

    jobject advanceResultValue =
            env->GetStaticObjectField(jAdvanceResultClass, resultFieldId);
    env->DeleteLocalRef(jAdvanceResultClass);

    return advanceResultValue;
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppApply(
    JNIEnv *,
    jobject,
    jlong ref,
    jfloat mix) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);
    animationInstance->apply(mix);
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppSetTime(
    JNIEnv *,
    jobject,
    jlong ref,
    jfloat time) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);
    animationInstance->time(time);
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppGetDirection(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);
    return static_cast<int>(animationInstance->direction());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppSetDirection(
    JNIEnv *,
    jobject,
    jlong ref,
    jint direction) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);
    animationInstance->direction(direction);
}


JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppGetLoop(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);
    return (jint) animationInstance->loop();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppSetLoop(
    JNIEnv *,
    jobject,
    jlong ref,
    jint loopType) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);

    animationInstance->loopValue(loopType);
}

// ANIMATION
JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppName(
    JNIEnv *env,
    jobject,
    jlong ref) {
    auto animationInstance =
            reinterpret_cast<const rive::LinearAnimationInstance *>(ref);
    return env->NewStringUTF(
        animationInstance->animation()->name().c_str());
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppDuration(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto animationInstance =
            reinterpret_cast<const rive::LinearAnimationInstance *>(ref);
    return (jint) animationInstance->animation()->duration();
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppFps(JNIEnv *,
                                                                 jobject,
                                                                 jlong ref) {
    auto animationInstance =
            reinterpret_cast<const rive::LinearAnimationInstance *>(ref);
    return (jint) animationInstance->animation()->fps();
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppWorkEnd(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto animationInstance =
            reinterpret_cast<const rive::LinearAnimationInstance *>(ref);
    return (jint) animationInstance->animation()->workEnd();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_LinearAnimationInstance_cppDelete(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto animationInstance =
            reinterpret_cast<rive::LinearAnimationInstance *>(ref);
    delete animationInstance;
}

#ifdef __cplusplus
}
#endif
