package com.ray.learnvideo

/**
 * Author : Ray
 * Time : 2020/9/4 3:29 PM
 * Description :
 */
class VideoExtractor : RayMediaExtractor() {

    override fun getDataType(): String {
        return "video/"
    }

}