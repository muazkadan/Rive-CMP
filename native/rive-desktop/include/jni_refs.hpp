#pragma once

#include <jni.h>

namespace rive_desktop
{
    extern jint ThrowRiveException(const char* message);
    extern jint ThrowMalformedFileException(const char* message);
    extern jint ThrowUnsupportedRuntimeVersionException(const char* message);

    extern jclass GetHashMapClass();
    extern jmethodID GetHashMapConstructorId();
    extern jclass GetFloatClass();
    extern jmethodID GetFloatConstructor();
    extern jclass GetBooleanClass();
    extern jmethodID GetBooleanConstructor();
    extern jclass GetShortClass();
    extern jmethodID GetShortConstructor();

    extern jclass GetLoopClass();
    extern jfieldID GetOneShotLoopField();
    extern jfieldID GetLoopLoopField();
    extern jfieldID GetPingPongLoopField();

    extern jclass GetAdvanceResultClass();
    extern jfieldID GetAdvanceResultAdvancedField();
    extern jfieldID GetAdvanceResultOneShotField();
    extern jfieldID GetAdvanceResultLoopField();
    extern jfieldID GetAdvanceResultPingPongField();
    extern jfieldID GetAdvanceResultNoneField();
} // namespace rive_desktop
