package com.ray.learnvideo.opengl

import android.opengl.GLES20

/**
 * Author : Ray
 * Time : 2020/9/11 6:14 PM
 * Description :
 */
class OpenGlUtils {

    companion object {
        fun createTexture(count: Int): IntArray {
            val textures = IntArray(count)
            GLES20.glGenTextures(count, textures, 0)
            return textures
        }
    }

}