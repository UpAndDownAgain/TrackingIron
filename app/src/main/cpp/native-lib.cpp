#include <jni.h>
#include <string>
#include <memory>
#include <android/log.h>

#include <opencv2/core.hpp>
#include <opencv2/tracking.hpp>

#include "Detektor.h"
#include "Utility.h"

std::unique_ptr<Detektor> detektor;
cv::Ptr<cv::Tracker> tracker;
bool trackerIsInit = false;
std::vector<cv::Point> barPath;

bool drawBox = false;
cv::Scalar boxColor = cv::Scalar(255,0,255);
int boxSize = 2;
cv::Scalar barPathColor = cv::Scalar(0,0,255);
int barPathSize = 2;

/**
 * vytvori trackeru a detektoru pomoci cesty k cfg a weights predaneho z javy
 */
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_MainActivity_init_1jni(JNIEnv *env, jobject thiz, jstring cfg,
                                                    jstring weights) {
    std::string cfgPath = MyUtils::convertJStringToStdString(env, cfg);
    std::string weightsPath = MyUtils::convertJStringToStdString(env, weights);

    detektor = std::make_unique<Detektor>(Detektor(cfgPath, weightsPath));
    tracker = cv::TrackerKCF::create();
}
/**
 * metoda k detekci, pokud je validni pouzije tracker
 * jinak pouzije detektor a pomoci nej nastavi tracker
 * vysledek zakresli do poskytnuteho Mat objektu
 */
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_VideoProcessor_detectAndDraw_1jni(JNIEnv *env, jobject thiz,
                                                               jlong matAddress) {
    //pointer na mat z javy
    auto originalMat = (cv::Mat*)matAddress;
    cv::Rect2d detection;
    cv::Mat mat;

    //prevod na 3kanalovou mat
    cv::cvtColor(*originalMat, mat, CV_BGRA2BGR);

    if(!trackerIsInit){
        __android_log_write(ANDROID_LOG_INFO, "Detector", "Using YOLO Detector");
        detection = detektor->detectObject(mat);
        tracker->init(mat, detection);
        trackerIsInit = true;
    }else{
        __android_log_write(ANDROID_LOG_INFO, "Detector", "Using Tracker");
        bool ok = tracker->update(mat, detection);
        //tracker selhal
        if(!ok){
            __android_log_write(ANDROID_LOG_INFO, "Detector", "Tracker Failed");
            detection = detektor->detectObject(mat);
            tracker->init(mat, detection);
        }
    }
    /**
     * vystupem detekce je box ohranicujici objekt
     * x,y - souradnice leveho horniho rohu
     * height a weight udava vysku a sirku boxu
     * jako bod pro drahu povazuji stred ohranicujiciho boxu
     */
    barPath.emplace_back(
            cv::Point((int)(detection.x + (detection.width/2)),
                      (int)(detection.y + (detection.height/2))));

    if(drawBox){
        cv::rectangle(*originalMat, detection, boxColor, boxSize);
    }

    MyUtils::drawBarPath(barPath, *originalMat, barPathColor, barPathSize);

}
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_MainActivity_setDrawBox_1jni(JNIEnv *env, jobject thiz, jboolean draw_box) {
    drawBox = draw_box;
}
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_MainActivity_setBoxSize_1jni(JNIEnv *env, jobject thiz, jint size) {
    boxSize = size;
}
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_MainActivity_setBoxColor_1jni(JNIEnv *env, jobject thiz, jint r, jint g,
                                                      jint b) {
    boxColor = cv::Scalar(r, g, b);
}
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_MainActivity_setBarPathSize_1jni(JNIEnv *env, jobject thiz, jint size) {
    barPathSize = size;
}
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_MainActivity_setBarPathColor_1jni(JNIEnv *env, jobject thiz, jint r, jint g,
                                                          jint b) {
    barPathColor = cv::Scalar(r, g, b);
}
extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_MainActivity_cleanUp_1jni(JNIEnv *env, jobject thiz) {
    detektor.release();
    tracker.release();
}extern "C"
JNIEXPORT void JNICALL
Java_musil_adam_trackingiron_VideoProcessor_clearBarPath_1jni(JNIEnv *env, jobject thiz) {
    barPath.clear();
    trackerIsInit = false;
}