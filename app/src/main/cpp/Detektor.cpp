//
// Created by nrg on 4/2/20.
//

#include "Detektor.h"

Detektor::Detektor(const std::string &cfgPath, const std::string &weightsPath) {
    net = cv::dnn::readNetFromDarknet(cfgPath, weightsPath);
    net.setPreferableBackend(cv::dnn::DNN_BACKEND_OPENCV);
    net.setPreferableTarget(cv::dnn::DNN_TARGET_CPU);
    outNames = net.getUnconnectedOutLayersNames();
}

cv::Rect2d Detektor::detectObject(cv::Mat &frame) {
    auto detections = preprocess(frame);
    return  postProcess(frame, detections);
}

std::vector<cv::Mat> Detektor::preprocess(cv::Mat &frame) {
    std::vector<cv::Mat> outputs; //vystupy detekce
    cv::Mat blob; //predpripraveny snimek pro detektor

    cv::dnn::blobFromImage(frame, blob, scaleFactor, size,
                           cv::Scalar(0,0,0),
                           true, false);

    net.setInput(blob);
    net.forward(outputs, outNames);
    return outputs;
}

cv::Rect Detektor::postProcess(cv::Mat &frame, std::vector<cv::Mat> &outs) {
    std::vector<cv::Rect> boxes; //ohranicujici boxy detekci s vyssi jistotou nez je threshold

    if(!outs.empty()){
        for(auto &i : outs){
            auto *data = (float*) i.data;
            //projde jednotlive vystupy detektoru
            //pokud je jistoty vyssi nez detekce vytvori ohranicujici box a vlozi do boxes
            for(size_t j = 0; j < i.rows; ++j, data += i.cols){
                cv::Mat scores = i.row(j).colRange(5,i.cols);
                cv::Point classIdPoint;
                double confidence;
                cv::minMaxLoc(scores, nullptr, &confidence, nullptr, &classIdPoint);

                if(confidence > threshold){
                    int centerX = (int)(data[0] * frame.cols);
                    int centerY = (int)(data[1] * frame.rows);
                    int width = (int)(data[2] * frame.cols);
                    int height = (int)(data[3] * frame.rows);
                    int left = centerX - width / 2;
                    int top = centerY - height / 2;

                    boxes.emplace_back(left, top, width, height);
                }
            }
        }
    }
    return closestDetection(boxes);
}
/*
 * ponechani nejblizsi detekce, jako nejblizsi detekce je povazovana detekce s ohranicujicim boxem s nejvetsi plochou
 */
cv::Rect Detektor::closestDetection(std::vector<cv::Rect> &detections) {
    cv::Rect closestDetection;
    int closestDetectArea = -1;

    for(auto &box : detections){
        int area = box.width * box.height;
        if(area > closestDetectArea){
            closestDetectArea = area;
            closestDetection = box;
        }
    }
    return closestDetection;
}