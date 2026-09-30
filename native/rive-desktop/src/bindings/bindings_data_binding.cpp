#include <jni.h>

#include "helpers/general.hpp"
#include "helpers/jni_resource.hpp"
#include "rive/animation/state_machine_instance.hpp"
#include "rive/refcnt.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_boolean_runtime.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_color_runtime.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_enum_runtime.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_number_runtime.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_runtime.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_string_runtime.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_trigger_runtime.hpp"
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

// Property lookup. Each returns null when the path has no property of that type.

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppPropertyNumber(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring path) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    return reinterpret_cast<jlong>(
        vmi->propertyNumber(JStringToString(env, path)));
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppPropertyString(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring path) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    return reinterpret_cast<jlong>(
        vmi->propertyString(JStringToString(env, path)));
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppPropertyBoolean(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring path) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    return reinterpret_cast<jlong>(
        vmi->propertyBoolean(JStringToString(env, path)));
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppPropertyColor(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring path) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    return reinterpret_cast<jlong>(
        vmi->propertyColor(JStringToString(env, path)));
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppPropertyEnum(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring path) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    return reinterpret_cast<jlong>(
        vmi->propertyEnum(JStringToString(env, path)));
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelInstance_cppPropertyTrigger(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring path) {
    auto vmi = reinterpret_cast<rive::ViewModelInstanceRuntime *>(ref);
    return reinterpret_cast<jlong>(
        vmi->propertyTrigger(JStringToString(env, path)));
}

// Number

JNIEXPORT jfloat JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelNumberProperty_cppGetValue(
    JNIEnv *,
    jobject,
    jlong ref) {
    return reinterpret_cast<rive::ViewModelInstanceNumberRuntime *>(ref)
        ->value();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelNumberProperty_cppSetValue(
    JNIEnv *,
    jobject,
    jlong ref,
    jfloat value) {
    reinterpret_cast<rive::ViewModelInstanceNumberRuntime *>(ref)->value(value);
}

// String

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelStringProperty_cppGetValue(
    JNIEnv *env,
    jobject,
    jlong ref) {
    auto property =
        reinterpret_cast<rive::ViewModelInstanceStringRuntime *>(ref);
    return env->NewStringUTF(property->value().c_str());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelStringProperty_cppSetValue(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring value) {
    reinterpret_cast<rive::ViewModelInstanceStringRuntime *>(ref)->value(
        JStringToString(env, value));
}

// Boolean

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelBooleanProperty_cppGetValue(
    JNIEnv *,
    jobject,
    jlong ref) {
    return reinterpret_cast<rive::ViewModelInstanceBooleanRuntime *>(ref)
        ->value();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelBooleanProperty_cppSetValue(
    JNIEnv *,
    jobject,
    jlong ref,
    jboolean value) {
    reinterpret_cast<rive::ViewModelInstanceBooleanRuntime *>(ref)->value(
        value);
}

// Color

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelColorProperty_cppGetValue(
    JNIEnv *,
    jobject,
    jlong ref) {
    return reinterpret_cast<rive::ViewModelInstanceColorRuntime *>(ref)
        ->value();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelColorProperty_cppSetValue(
    JNIEnv *,
    jobject,
    jlong ref,
    jint value) {
    reinterpret_cast<rive::ViewModelInstanceColorRuntime *>(ref)->value(value);
}

// Enum

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelEnumProperty_cppGetValue(
    JNIEnv *env,
    jobject,
    jlong ref) {
    auto property = reinterpret_cast<rive::ViewModelInstanceEnumRuntime *>(ref);
    return env->NewStringUTF(property->value().c_str());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelEnumProperty_cppSetValue(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring value) {
    reinterpret_cast<rive::ViewModelInstanceEnumRuntime *>(ref)->value(
        JStringToString(env, value));
}

// Trigger

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_ViewModelTriggerProperty_cppTrigger(
    JNIEnv *,
    jobject,
    jlong ref) {
    reinterpret_cast<rive::ViewModelInstanceTriggerRuntime *>(ref)->trigger();
}

#ifdef __cplusplus
}
#endif
