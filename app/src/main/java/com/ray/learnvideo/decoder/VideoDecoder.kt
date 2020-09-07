package com.ray.learnvideo.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.ray.learnvideo.RayMediaExtractor
import com.ray.learnvideo.VideoExtractor
import java.nio.ByteBuffer

/**
 * Author : Ray
 * Time : 2020/9/4 6:24 PM
 * Description :
 */

class VideoDecoder(private var mSurface: Surface, path: String) : BaseDecoder(path) {

    override fun getExtractor(): RayMediaExtractor {
        return VideoExtractor().apply {
            setDataSource(path)
        }
    }

    private var mStartDecodeTime = -1L //第一次解码数据完成的时间

    private var mIsFirst = true

    override fun render(buffer: ByteBuffer?, bufferInfo: MediaCodec.BufferInfo) {
        syncVideo(bufferInfo)
    }

    /**
     * 计算线程睡眠时间，同步音频，视频播放
     */
    private fun syncVideo(bufferInfo: MediaCodec.BufferInfo) {
        val currentTime = bufferInfo.presentationTimeUs / 1000
        if (mIsFirst) {
            mStartDecodeTime = System.currentTimeMillis() - currentTime
            mIsFirst = false
        }
        val passTime = System.currentTimeMillis() - mStartDecodeTime
        if (passTime < currentTime) {
            Thread.sleep(currentTime - passTime)
        }
    }

    override fun configureCodec(codec: MediaCodec, mediaFormat: MediaFormat?) {
        codec.configure(mediaFormat, mSurface, null, 0)
        codec.start()
    }

    override fun initRender(audioFormat: MediaFormat) {}

    override fun resume() {
        super.resume()
        mIsFirst = true
    }

}