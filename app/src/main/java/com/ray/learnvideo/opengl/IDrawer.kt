package com.ray.learnvideo.opengl

/**
 * Author : Ray
 * Time : 2020/9/11 5:41 PM
 * Description :
 */
interface IDrawer {

    fun setTextureId(id: Int)

    fun draw()

    fun release()

    fun setWorldSize(width: Int, height: Int)

}

const val TYPE_TRIANGLE = 0
const val TYPE_SQUARE = 1
const val TYPE_IMAGE = 2
const val TYPE_VIDEO = 3
const val TYPE_VIDEO_MULTIPLE = 4