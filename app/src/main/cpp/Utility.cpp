//
// Created by Musil Adam on 4/4/20.
//

#include "Utility.h"

namespace MyUtils{

    std::string MyUtils::convertJStringToStdString(JNIEnv *env, jstring input) {
        if(!input) return"";

        const jclass stringClass = env->GetObjectClass(input);
        const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
        const auto stringJbytes =
            (jbyteArray) env->CallObjectMethod(input, getBytes, env->NewStringUTF("UTF-8"));

        auto length = (size_t) env->GetArrayLength(stringJbytes);
        jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

        auto out = std::string((char *)pBytes, length);

        env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);
        env->DeleteLocalRef(stringClass);
        env->DeleteLocalRef(stringJbytes);

        return out;
    }

    void drawBarPath(std::vector<cv::Point> &path, cv::Mat &frame, cv::Scalar &color, int size) {
        for(size_t i = 1; i < path.size(); ++i){
            cv::line(frame, path[i-1], path[i], color, size);
        }
    }

}
