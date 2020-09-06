package com.ray.learnvideo

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Author : Ray
 * Time : 2020/9/6 8:22 PM
 * Description :
 */
abstract class RayMediaExtractor {

    private val mExtractor = MediaExtractor()

    private var mTrack = -1

    private var mSampleTime = -1L

    fun setDataSource(url: String) {
        mExtractor.setDataSource(url)
    }

    fun getTrackFormat(): MediaFormat? {
        val trackCount = mExtractor.trackCount
        for (i in 0 until trackCount) {
            val trackFormat = mExtractor.getTrackFormat(i)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith(getDataType())!!) {
                mTrack = i
                break
            }
        }
        if (mTrack >= 0) {
            return mExtractor.getTrackFormat(mTrack)
        }
        return null
    }

    fun readBuffer(buffer: ByteBuffer): Int {
        buffer.clear()
        mExtractor.selectTrack(mTrack)
        val sampleSize = mExtractor.readSampleData(buffer, 0)
        mSampleTime = mExtractor.sampleTime
        mExtractor.advance()
        return sampleSize
    }

    fun stop() {
        mExtractor.release()
    }

    fun getCurrentTimestamp() = mSampleTime

    abstract fun getDataType(): String

}