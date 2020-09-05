package com.ray.learnvideo

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer

/**
 * Author : Ray
 * Time : 2020/9/4 3:29 PM
 * Description :
 */
class AudioExtractor {

    companion object {
        const val TAG = "AudioExtractor"
    }

    private val mExtractor = MediaExtractor()

    private var mAudioTrack = -1

    private var mSampleTime = -1L

    fun setDataSource(url: String) {
        mExtractor.setDataSource(url)
        Log.d(TAG, "data source = $url")
    }

    fun getAudioFormat(): MediaFormat? {
        val trackCount = mExtractor.trackCount
        for (i in 0 until trackCount) {
            val trackFormat = mExtractor.getTrackFormat(i)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/")!!) {
                mAudioTrack = i
                break
            }
        }
        if (mAudioTrack >= 0) {
            return mExtractor.getTrackFormat(mAudioTrack)
        }
        return null
    }

    fun readBuffer(buffer: ByteBuffer): Int {
        buffer.clear()
        val trackCount = mExtractor.trackCount
        for (i in 0 until trackCount) {
            val trackFormat = mExtractor.getTrackFormat(i)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/")!!) {
                mExtractor.selectTrack(i)
                break
            }
        }
        val sampleSize = mExtractor.readSampleData(buffer, 0)
        mSampleTime = mExtractor.sampleTime
        Log.d(TAG, "extract data = $sampleSize bytes")
        mExtractor.advance()
        return sampleSize
    }

    fun stop() {
        mExtractor.release()
    }

    fun getCurrentTimestamp() = mSampleTime

}