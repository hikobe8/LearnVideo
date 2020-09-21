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
class SimpleRender(drawer: IDrawer? = null) : GLSurfaceView.Renderer {

    private val mDrawerList = ArrayList<IDrawer>()

    fun addDrawer(drawer: IDrawer) {
        mDrawerList.add(drawer)
    }

    fun addDrawers(drawers: ArrayList<IDrawer>) {
        mDrawerList.addAll(drawers)
    }

    init {
        drawer?.apply {
            mDrawerList.add(this)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mDrawerList.forEachIndexed { index, iDrawer ->
            GLES20.glViewport(0, 100*index, width, height - 100*index)
            iDrawer.setWorldSize(width, height - 100*index)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val textures = OpenGlUtils.createTexture(mDrawerList.size)
        mDrawerList.forEachIndexed { index, iDrawer ->
            iDrawer.setTextureId(textures[index])
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        for (iDrawer in mDrawerList) {
            iDrawer.draw()
        }
    }

}