package com.ray.learnvideo.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Author : Ray
 * Time : 2020/9/14 2:55 PM
 * Description :
 */
class TriangleDrawer : IDrawer {

    private val mVertexCoors = floatArrayOf(
        -.5f, -.5f,
        .5f, -.5f,
        0f, .5f
    )

    //纹理坐标系 和 Android屏幕坐标系一致
    private val mTextureCoors = floatArrayOf(
        0f, .5f,
        .5f, .5f,
        0.5f, .5f
    )

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    private var mTextureId = -1

    private var mVertexPosHandle = -1

    private var mTexturePosHandle = -1

    private var mProgram = -1

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
    }

    override fun draw() {
        if (mTextureId != -1) {
            create()
            drawInternal()
        }
    }

    private fun drawInternal() {
        GLES20.glEnableVertexAttribArray(mVertexPosHandle)
        GLES20.glEnableVertexAttribArray(mTexturePosHandle)
        GLES20.glVertexAttribPointer(mVertexPosHandle, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3)
    }

    private fun create() {
        if (mProgram == -1) {
            //创建顶点,纹理shader
            val vertex = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
            val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader())
            //创建 opengl program
            mProgram = GLES20.glCreateProgram()
            GLES20.glAttachShader(mProgram, vertex)
            GLES20.glAttachShader(mProgram, fragment)
            GLES20.glLinkProgram(mProgram)
            mVertexPosHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mTexturePosHandle = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
        }
        GLES20.glUseProgram(mProgram)
    }

    override fun release() {
        GLES20.glDisableVertexAttribArray(mVertexPosHandle)
        GLES20.glDisableVertexAttribArray(mTexturePosHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE0, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "void main() {" +
                "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
                "}"
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

}