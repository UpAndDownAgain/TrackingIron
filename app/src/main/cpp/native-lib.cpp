#include <jni.h>
#include <string>
#include <memory>
#include <android/log.h>

#include <opencv2/core.hpp>
#include <opencv2/tracking.hpp>

#include "Detektor.h"



extern "C" JNIEXPORT jstring JNICALL
Java_musil_adam_trackingiron_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
