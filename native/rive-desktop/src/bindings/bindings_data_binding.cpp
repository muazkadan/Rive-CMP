#include <jni.h>

#include "helpers/jni_resource.hpp"
#include "rive/animation/state_machine_instance.hpp"
#include "rive/refcnt.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_runtime.hpp"
#include "rive/viewmodel/runtime/viewmodel_runtime.hpp"

#ifdef __cplusplus
extern "C" {
#endif
using namespace rive_desktop;

// ViewModel

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModel_cppName(JNIEnv *env,
                                                    jobject,
                                                    jlong ref) {
    auto vm = reinterpret_cast<rive::ViewModelRuntime *>(ref);
    return env->NewStringUTF(vm->name().c_str());
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModel_cppCreateDefaultInstance(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto vm = reinterpret_cast<rive::ViewModelRuntime *>(ref);
    auto vmi = vm->createDefaultInstance();
    return reinterpret_cast<jlong>(vmi.release());
}

// ViewModelInstance

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppName(JNIEnv *env,
                                                            jobject,
                                                            jlong ref) {
    auto vm = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    return env->NewStringUTF(vm->name().c_str());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppRef(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    vmi->ref();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppDelete(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    vmi->unref();
}

// Properties

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelProperty_cppName(JNIEnv *env,
                                                            jobject,
                                                            jlong ref) {
    auto property =
            reinterpret_cast<rive::ViewModelInstanceValueRuntime *>(ref);
    return env->NewStringUTF(property->name().c_str());
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelProperty_cppHasChanged(JNIEnv *,
                                                                  jobject,
                                                                  jlong ref) {
    auto property =
            reinterpret_cast<rive::ViewModelInstanceValueRuntime *>(ref);
    return property->hasChanged();
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelProperty_cppFlushChanges(
    JNIEnv *,
    jobject,
    jlong ref) {
    auto property =
            reinterpret_cast<rive::ViewModelInstanceValueRuntime *>(ref);
    return property->flushChanges();
}

#ifdef __cplusplus
}
#endif
