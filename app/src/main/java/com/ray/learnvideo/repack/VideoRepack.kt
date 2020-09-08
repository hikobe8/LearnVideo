package com.ray.learnvideo.repack

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import com.ray.learnvideo.AudioExtractor
import com.ray.learnvideo.VideoExtractor
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread

/**
 * Author : Ray
 * Time : 2020/9/8 3:40 PM
 * Description :
 */
class VideoRepack(private val path: String) {

    private val mAudioExtractor = AudioExtractor().apply { setDataSource(path) }
    private val mVideoExtractor = VideoExtractor().apply { setDataSource(path) }
    private val mMuxer = SimpleMediaMuxer()

    fun start() {
        thread {
            Log.e("VideoRepack", "视频重打包开始")
            val audioFormat = mAudioExtractor.getTrackFormat()
            mMuxer.addAudioTrack(audioFormat!!)
            val videoFormat = mVideoExtractor.getTrackFormat()
            mMuxer.addVideoTrack(videoFormat!!)
            val buffer = ByteBuffer.allocate(800 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            var size = mAudioExtractor.readBuffer(buffer)
            while (size >= 0) {
                bufferInfo.set(
                    0,
                    size,
                    mAudioExtractor.getCurrentTimestamp(),
                    mAudioExtractor.getCurrentSampleFlags()
                )
                mMuxer.writeAudioData(buffer, bufferInfo)
                size = mAudioExtractor.readBuffer(buffer)
            }
            size = mVideoExtractor.readBuffer(buffer)
            while (size >= 0) {
                bufferInfo.set(
                    0,
                    size,
                    mVideoExtractor.getCurrentTimestamp(),
                    mVideoExtractor.getCurrentSampleFlags()
                )
                mMuxer.writeVideoData(buffer, bufferInfo)
                size = mVideoExtractor.readBuffer(buffer)
            }
            mAudioExtractor.stop()
            mVideoExtractor.stop()
            mMuxer.release()
            Log.e("VideoRepack", "视频重打包结束")
        }
    }

}

class SimpleMediaMuxer {

    private val mMediaMuxer: MediaMuxer

    private var mIsAudioAdded = false

    private var mIsVideoAdded = false

    private var mAudioTrack = -1

    private var mVideoTrack = -1

    init {
        val path =
            "${Environment.getExternalStorageDirectory().absolutePath + File.separator}output${System.currentTimeMillis()}.mp4"
        mMediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun addAudioTrack(audioFormat: MediaFormat) {
        mAudioTrack = mMediaMuxer.addTrack(audioFormat)
        mIsAudioAdded = true
        startMuxer()
    }

    fun addVideoTrack(videoFormat: MediaFormat) {
        mVideoTrack = mMediaMuxer.addTrack(videoFormat)
        mIsVideoAdded = true
        startMuxer()
    }

    private fun startMuxer() {
        if (mIsAudioAdded && mIsVideoAdded) {
            mMediaMuxer.start()
        }
    }

    fun writeAudioData(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        mMediaMuxer.writeSampleData(mAudioTrack, buffer, bufferInfo)
    }

    fun writeVideoData(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        mMediaMuxer.writeSampleData(mVideoTrack, buffer, bufferInfo)
    }

    fun release() {
        mIsAudioAdded = false
        mIsVideoAdded = false
        mMediaMuxer.stop()
        mMediaMuxer.release()
    }

}

