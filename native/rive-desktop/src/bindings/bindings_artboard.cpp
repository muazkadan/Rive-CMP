#include <jni.h>
#include "helpers/rive_log.hpp"
#include "models/canvas_renderer.hpp"
#include "helpers/general.hpp"
#include "rive/artboard.hpp"
#include "rive/viewmodel/runtime/viewmodel_instance_runtime.hpp"
#include "rive/animation/state_machine_instance.hpp"

#ifdef __cplusplus
extern "C" {
#endif

using namespace rive_desktop;

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppName(JNIEnv *env,
                                                              jobject,
                                                              jlong ref) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    return env->NewStringUTF(artboard->name().c_str());
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppAnimationByName(JNIEnv *env,
                                                                         jobject,
                                                                         jlong ref,
                                                                         jstring name) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    // Creates a new instance.
    return (jlong) artboard->animationNamed(JStringToString(env, name))
            .release();
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppAnimationCount(JNIEnv *,
                                                                        jobject,
                                                                        jlong ref) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);

    return (jint) artboard->animationCount();
}

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppAnimationNameByIndex(
    JNIEnv *env,
    jobject,
    jlong ref,
    jint index) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    auto *animation = artboard->animation(index);
    auto name = animation->name();

    return env->NewStringUTF(name.c_str());
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppStateMachineByName(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring name) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    // Creates a new instance.

    return (jlong) artboard->stateMachineNamed(JStringToString(env, name))
            .release();
}

JNIEXPORT jint JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppStateMachineCount(JNIEnv *,
                                                                           jobject,
                                                                           jlong ref) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);

    return (jint) artboard->stateMachineCount();
}

JNIEXPORT jstring JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppStateMachineNameByIndex(
    JNIEnv *env,
    jobject,
    jlong ref,
    jint index) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);

    auto *stateMachine = artboard->stateMachine(index);
    auto name = stateMachine->name();

    return env->NewStringUTF(name.c_str());
}

JNIEXPORT jlong JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppInputByNameAtPath(
    JNIEnv *env,
    jobject,
    jlong ref,
    jstring name,
    jstring path) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    return (jlong) artboard->input(JStringToString(env, name),
                                   JStringToString(env, path));
}

JNIEXPORT jboolean JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppAdvance(JNIEnv *,
                                                                 jobject,
                                                                 jlong ref,
                                                                 jfloat elapsedTime) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    return artboard->advance(elapsedTime);
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppDrawAligned(
    JNIEnv *,
    jobject,
    jlong artboardRef,
    jlong rendererRef,
    jint fitOrdinal,
    jint alignmentOrdinal,
    jfloat scaleFactor) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(artboardRef);
    auto *renderer = reinterpret_cast<CanvasRenderer *>(rendererRef);
    if (!renderer->isBound()) return;

    auto fit = GetFit(static_cast<uint8_t>(fitOrdinal));
    auto alignment = GetAlignment(static_cast<uint8_t>(alignmentOrdinal));

    renderer->save();
    renderer->align(fit,
                    alignment,
                    rive::AABB(
                        0,
                        0,
                        static_cast<float>(renderer->width()),
                        static_cast<float>(renderer->height())
                    ),
                    artboard->bounds(),
                    scaleFactor);
    artboard->draw(renderer);
    renderer->restore();
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppSetViewModelInstance(
    JNIEnv *,
    jobject,
    jlong ref,
    jlong viewModelInstanceRef) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    auto instance = reinterpret_cast<rive::ViewModelInstanceRuntime *>(
        viewModelInstanceRef);

    artboard->bindViewModelInstance(instance->instance());
}

JNIEXPORT void JNICALL
Java_dev_muazkadan_rivecmp_native_Artboard_cppDelete(JNIEnv *,
                                                                jobject,
                                                                jlong ref) {
    auto artboard = reinterpret_cast<rive::ArtboardInstance *>(ref);
    delete artboard;
}

#ifdef __cplusplus
}
#endif
