#include <jni.h>
#include "rive/animation/state_machine_trigger.hpp"
#include "rive/animation/state_machine_number.hpp"
#include "rive/animation/state_machine_input.hpp"
#include "rive/animation/state_machine_input_instance.hpp"
#include "rive/animation/state_machine_bool.hpp"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_SMIInput_cppName(JNIEnv *env,
                                                   jobject,
                                                   jlong ref) {
    auto *input = reinterpret_cast<rive::SMIInput *>(ref);
    return env->NewStringUTF(input->name().c_str());
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_SMIInput_cppIsBoolean(JNIEnv *,
                                                        jobject,
                                                        jlong ref) {
    auto *input = reinterpret_cast<rive::SMIInput *>(ref);
    return input->input()->is<rive::StateMachineBool>();
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_SMIInput_cppIsTrigger(JNIEnv *,
                                                        jobject,
                                                        jlong ref) {
    auto *input = reinterpret_cast<rive::SMIInput *>(ref);
    return input->input()->is<rive::StateMachineTrigger>();
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_SMIInput_cppIsNumber(JNIEnv *,
                                                       jobject,
                                                       jlong ref) {
    auto *input = reinterpret_cast<rive::SMIInput *>(ref);
    return input->input()->is<rive::StateMachineNumber>();
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_SMIBoolean_cppValue(JNIEnv *,
                                                      jobject,
                                                      jlong ref) {
    auto *input = reinterpret_cast<rive::SMIBool *>(ref);
    return input->value();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_SMIBoolean_cppSetValue(JNIEnv *,
                                                         jobject,
                                                         jlong ref,
                                                         jboolean newValue) {
    auto *input = reinterpret_cast<rive::SMIBool *>(ref);
    input->value(newValue);
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_SMITrigger_cppFire(JNIEnv *,
                                                     jobject,
                                                     jlong ref) {
    auto *input = reinterpret_cast<rive::SMITrigger *>(ref);
    input->fire();
}

JNIEXPORT jfloat JNICALL
Java_dev_muazkadan_rivecmp_native_SMINumber_cppValue(JNIEnv *,
                                                     jobject,
                                                     jlong ref) {
    auto *input = reinterpret_cast<rive::SMINumber *>(ref);
    return input->value();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_SMINumber_cppSetValue(JNIEnv *,
                                                        jobject,
                                                        jlong ref,
                                                        jfloat newValue) {
    auto *input = reinterpret_cast<rive::SMINumber *>(ref);
    input->value(newValue);
}

#ifdef __cplusplus
}
#endif
