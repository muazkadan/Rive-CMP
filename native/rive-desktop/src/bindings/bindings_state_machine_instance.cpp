// #include "jni_refs.hpp"
#include "helpers/general.hpp"
#include "rive/animation/state_machine_instance.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_runtime.hpp"
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
using namespace rive_desktop;

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_StateMachineInstance_cppAdvance(
    JNIEnv *,
    jobject,
    jlong ref,
    jfloat elapsedTime) {
    auto stateMachineInstance =
            reinterpret_cast<rive::StateMachineInstance *>(ref);
    return stateMachineInstance->advanceAndApply(elapsedTime);
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_StateMachineInstance_cppInputCount(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto stateMachineInstance =
            reinterpret_cast<rive::StateMachineInstance *>(ref);
    return SizeTToInt(stateMachineInstance->inputCount());
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_StateMachineInstance_cppSMIInputByIndex(
    JNIEnv *,
    jobject,
    jlong ref,
    jint index) {
    auto stateMachineInstance =
            reinterpret_cast<rive::StateMachineInstance *>(ref);

    return (jlong) stateMachineInstance->input(index);
}


// ANIMATION
JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_StateMachineInstance_cppName(JNIEnv *env,
                                                               jobject,
                                                               jlong ref) {
    auto stateMachineInstance =
            reinterpret_cast<rive::StateMachineInstance *>(ref);
    return env->NewStringUTF(
        stateMachineInstance->stateMachine()->name().c_str());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_StateMachineInstance_cppSetViewModelInstance(
    JNIEnv *,
    jobject,
    jlong ref,
    jlong viewModelInstanceRef) {
    auto stateMachine = reinterpret_cast<rive::StateMachineInstance *>(ref);
    auto instance = reinterpret_cast<rive::ViewModelInstanceRuntime *>(
        viewModelInstanceRef);
    stateMachine->bindViewModelInstance(instance->instance());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_StateMachineInstance_cppDelete(JNIEnv *,
                                                                 jobject,
                                                                 jlong ref) {
    auto stateMachineInstance =
            reinterpret_cast<rive::StateMachineInstance *>(ref);
    delete stateMachineInstance;
}

#ifdef __cplusplus
}
#endif
