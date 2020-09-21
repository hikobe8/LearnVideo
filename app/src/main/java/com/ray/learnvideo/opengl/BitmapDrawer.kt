package com.ray.learnvideo.opengl

import android.graphics.Bitmap
import android.opengl.GLES20.*
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Author : Ray
 * Time : 2020/9/14 2:55 PM
 * Description : 使用OpenGL 绘制图片
 */
class BitmapDrawer(private val mBitmap: Bitmap) : IDrawer {

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
            activeTexture()
            bitmap2Texture()
            drawInternal()
        }
    }

    private fun bitmap2Texture() {
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, mBitmap, 0)
    }

    private fun activeTexture() {
        //激活
        glActiveTexture(GL_TEXTURE0)
        //绑定texture
        glBindTexture(GL_TEXTURE_2D, mTextureId)
        //传递纹理单元到着色器
        glUniform1i(mTextureHandle, 0)
        //配置纹理过滤模式
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR.toFloat())
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR.toFloat())
        //配置纹理环绕方式
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
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

    override fun setWorldSize(width: Int, height: Int) {
    }

    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "  vCoordinate = aCoordinate;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  vec4 color = texture2D(uTexture, vCoordinate);" +
                "  gl_FragColor = color;" +
                "}"
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, shaderCode)
        glCompileShader(shader)
        return shader
    }

}