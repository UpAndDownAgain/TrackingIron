//
// Created by Musil Adam on 4/4/20.
//

#ifndef PLAYGROUND_UTILITY_H
#define PLAYGROUND_UTILITY_H

#include <jni.h>
#include <string>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>


namespace MyUtils{
    /**
     * pomocna metoda na prevod java string do std::string
     * @param env jni environment
     * @param input jstring
     * @return std::string
     */
    std::string convertJStringToStdString(JNIEnv *env, jstring input);

    /**
     * pomocna metoda na vykresleni drahy do snimku
     * @param path vektor bodu drahy
     * @param frame snimek k vykresleni
     * @param color barva drahy
     * @param size velikost drahy
     */
    void drawBarPath(std::vector<cv::Point> &path, cv::Mat &frame, cv::Scalar &color, int size);

}

#endif //PLAYGROUND_UTILITY_H
