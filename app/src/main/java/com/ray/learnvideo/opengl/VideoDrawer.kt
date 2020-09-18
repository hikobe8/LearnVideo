package com.ray.learnvideo.opengl

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Author : Ray
 * Time : 2020/9/14 2:55 PM
 * Description : 使用OpenGL 绘制图片
 */
class VideoDrawer(private var mSfCallback: ((SurfaceTexture) -> Unit)? = null) : IDrawer {

    private val mVertexCoors = floatArrayOf(
        -1f, 1f,
        1f, 1f,
        -1f, -1f,
        1f, -1f
    )

    //纹理坐标系 和 Android屏幕坐标系一致
    private val mTextureCoors = floatArrayOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f
    )


    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    private var mTextureId = -1

    private var mVertexPosHandle = -1

    private var mTexturePosHandle = -1

    private var mTextureHandle = -1

    private var mProgram = -1

    private var mSurfaceTexture: SurfaceTexture? = null

    init {
        initPos()
    }

    private fun initPos() {
        var buffer = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        mVertexBuffer = buffer.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        buffer = ByteBuffer.allocateDirect(mTextureCoors.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        mTextureBuffer = buffer.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)
    }

    override fun setTextureId(id: Int) {
        mTextureId = id
        mSurfaceTexture = SurfaceTexture(id)
        mSfCallback?.invoke(mSurfaceTexture!!)
    }

    override fun draw() {
        if (mTextureId != -1) {
            create()
            activeTexture()
            updateTexture()
            drawInternal()
        }
    }

    private fun updateTexture() {
        mSurfaceTexture?.updateTexImage()
    }

    private fun activeTexture() {
        //激活
        glActiveTexture(GL_TEXTURE0)
        //绑定texture
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        //传递纹理单元到着色器
        glUniform1i(mTextureHandle, 0)
        //配置纹理过滤模式
        glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR.toFloat()
        )
        glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_MAG_FILTER,
            GL_LINEAR.toFloat()
        )
        //配置纹理环绕方式
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    }

    private fun drawInternal() {
        glEnableVertexAttribArray(mVertexPosHandle)
        glEnableVertexAttribArray(mTexturePosHandle)
        glVertexAttribPointer(mVertexPosHandle, 2, GL_FLOAT, false, 8, mVertexBuffer)
        glVertexAttribPointer(mTexturePosHandle, 2, GL_FLOAT, false, 8, mTextureBuffer)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun create() {
        if (mProgram == -1) {
            //创建顶点,纹理shader
            val vertex = loadShader(GL_VERTEX_SHADER, getVertexShader())
            val fragment = loadShader(GL_FRAGMENT_SHADER, getFragmentShader())
            //创建 opengl program
            mProgram = glCreateProgram()
            glAttachShader(mProgram, vertex)
            glAttachShader(mProgram, fragment)
            glLinkProgram(mProgram)
            mVertexPosHandle = glGetAttribLocation(mProgram, "aPosition")
            mTexturePosHandle = glGetAttribLocation(mProgram, "aCoordinate")
            mTextureHandle = glGetUniformLocation(mProgram, "uTexture")
        }
        glUseProgram(mProgram)
    }

    override fun release() {
        glDisableVertexAttribArray(mVertexPosHandle)
        glDisableVertexAttribArray(mTexturePosHandle)
        glBindTexture(GL_TEXTURE0, 0)
        glDeleteTextures(1, intArrayOf(mTextureId), 0)
        glDeleteProgram(mProgram)
    }

    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "    gl_Position = aPosition;" +
                "    vCoordinate = aCoordinate;" +
                "}"
    }

    private fun getFragmentShader(): String {
        //一定要加换行"\n"，否则会和下一行的precision混在一起，导致编译出错
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "uniform samplerExternalOES uTexture;" +
                "void main() {" +
                "  gl_FragColor=texture2D(uTexture, vCoordinate);" +
                "}"
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, shaderCode)
        glCompileShader(shader)
        return shader
    }

}