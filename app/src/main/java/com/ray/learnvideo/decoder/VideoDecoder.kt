package com.ray.learnvideo.decoder

import android.media.*
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.ray.learnvideo.VideoExtractor
import java.io.File

/**
 * Author : Ray
 * Time : 2020/9/4 6:24 PM
 * Description :
 */

class VideoDecoder(private val surfaceView: SurfaceView) : Runnable {

    private var mIsRunning = true

    private var mediaExtractor: VideoExtractor? = null

    private var mCodec: MediaCodec? = null

    private val mBufferInfo = MediaCodec.BufferInfo()

    private var mSurface: Surface? = null

    private val mLock = Object()

    private var mStartDecodeTime = -1L //第一次解码数据完成的时间

    private var mIsFirst = true

    private fun waitDecode() {
        synchronized(mLock) {
            mLock.wait()
        }
    }

    private fun notifyDecode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
    }

    override fun run() {
        initExtractor()
        initCodec()
        Log.e("解码器", "开始解码")
        while (mIsRunning) {
            val inputBufferId = mCodec!!.dequeueInputBuffer(1000)
            //填充数据， 原数据喂给解码器
            if (inputBufferId >= 0) {
                val buffer = mCodec!!.getInputBuffer(inputBufferId)
                //填充数据给解码器
                val sampleSize = mediaExtractor!!.readBuffer(buffer!!)
                if (sampleSize < 0) {
                    mCodec!!.queueInputBuffer(
                        inputBufferId,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                } else {
                    mCodec!!.queueInputBuffer(
                        inputBufferId,
                        0,
                        sampleSize,
                        mediaExtractor!!.getCurrentTimestamp(),
                        0
                    )
                }

            }
            //从解码器中读取已解码的数据
            val outputBufferId = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000)
            if (outputBufferId >= 0) {
                val currentTime = mBufferInfo.presentationTimeUs / 1000
                if (mIsFirst) {
                    mStartDecodeTime = System.currentTimeMillis() - currentTime
                    mIsFirst = false
                }
                val outputBuffer = mCodec!!.getOutputBuffer(outputBufferId)
                val passTime = System.currentTimeMillis() - mStartDecodeTime
                if (passTime < currentTime) {
                    Thread.sleep(currentTime - passTime)
                }
                mCodec!!.releaseOutputBuffer(outputBufferId, true)
            }
            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                mIsRunning = false
            }
        }
        Log.e("解码器", "解码结束")
        mCodec!!.release()
        mediaExtractor!!.stop()
    }

    private fun initCodec() {
        val videoFormat = mediaExtractor?.getVideoFormat()
        if (videoFormat != null) {
            val mime = videoFormat.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(mime!!)
            mSurface = surfaceView.holder.surface
            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                    mSurface = holder.surface
                    mCodec!!.configure(videoFormat, mSurface, null, 0)
                    mCodec!!.start()
                    notifyDecode()
                }

                override fun surfaceDestroyed(p0: SurfaceHolder) {
                }

                override fun surfaceCreated(holder: SurfaceHolder) {

                }
            }
            )
            waitDecode()
        }
    }

    private fun initExtractor() {
        mediaExtractor = VideoExtractor().apply {
            setDataSource(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "monkey.mp4"
            )
        }
    }

}