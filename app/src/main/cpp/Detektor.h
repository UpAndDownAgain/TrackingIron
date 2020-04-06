//
// Created by Musil Adam on 4/2/20.
//

#ifndef PLAYGROUND_DETEKTOR_H
#define PLAYGROUND_DETEKTOR_H

#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/dnn.hpp>


class Detektor {
private:
    double threshold;
    cv::dnn::Net net;
    double scaleFactor = 1.0/255;
    cv::Size size = cv::Size(416, 416); //hodnoty z konfiguracniho souboru YOLO
    std::vector<cv::Mat> preprocess(cv::Mat &frame);
    cv::Rect postProcess(cv::Mat &frame, std::vector<cv::Mat> &outs);
    cv::Rect closestDetection(std::vector<cv::Rect> &detections);
    std::vector<std::string> outNames;

public:
    Detektor(const std::string &cfgPath, const std::string &weightsPath);
    cv::Rect2d detectObject(cv::Mat &frame);
};


#endif //PLAYGROUND_DETEKTOR_H
