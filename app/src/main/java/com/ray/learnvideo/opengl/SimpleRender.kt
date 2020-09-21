package com.ray.learnvideo.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Author : Ray
 * Time : 2020/9/14 2:57 PM
 * Description :
 */
class SimpleRender(private val mDrawer: IDrawer) : GLSurfaceView.Renderer {

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        mDrawer.setWorldSize(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        mDrawer.setTextureId(OpenGlUtils.createTexture(1)[0])
    }

    override fun onDrawFrame(gl: GL10?) {
        mDrawer.draw()
    }

}