package com.ray.learnvideo.decoder

import android.media.*
import android.util.Log
import com.ray.learnvideo.RayMediaExtractor
import java.nio.ByteBuffer

/**
 * Author : Ray
 * Time : 2020/9/4 6:24 PM
 * Description :
 */
/*
    MediaCodec codec = MediaCodec.createByCodecName(name);
    codec.configure(format, …);
    MediaFormat outputFormat = codec.getOutputFormat(); // option B
    codec.start();
    for (;;) {
        int inputBufferId = codec.dequeueInputBuffer (timeoutUs);
        if (inputBufferId >= 0) {
            ByteBuffer inputBuffer = codec.getInputBuffer (…);
            // fill inputBuffer with valid data
            …
            codec.queueInputBuffer(inputBufferId, …);
        }
        int outputBufferId = codec.dequeueOutputBuffer (…);
        if (outputBufferId >= 0) {
            ByteBuffer outputBuffer = codec.getOutputBuffer (outputBufferId);
            MediaFormat bufferFormat = codec.getOutputFormat (outputBufferId); // option A
            // bufferFormat is identical to outputFormat
            // outputBuffer is ready to be processed or rendered.
            …
            codec.releaseOutputBuffer(outputBufferId, …);
        } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // Subsequent data will conform to new format.
            // Can ignore if using getOutputFormat(outputBufferId)
            outputFormat = codec.getOutputFormat(); // option B
        }
    }
    codec.stop();
    codec.release();
*/
abstract class BaseDecoder(var path: String) : Runnable {

    private var mIsRunning = true

    private lateinit var mediaExtractor: RayMediaExtractor

    private var mCodec: MediaCodec? = null

    private val mBufferInfo = MediaCodec.BufferInfo()

    abstract fun getExtractor(): RayMediaExtractor

    override fun run() {
        initExtractor()
        initCodec()
        Log.e("解码器", "开始解码")
        while (mIsRunning) {
            val inputBufferId = mCodec!!.dequeueInputBuffer(1000)
            //填充数据，原数据喂给解码器
            if (inputBufferId >= 0) {
                val buffer = mCodec!!.getInputBuffer(inputBufferId)
                //填充数据给解码器
                val sampleSize = mediaExtractor.readBuffer(buffer!!)
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
                        mediaExtractor.getCurrentTimestamp(),
                        0
                    )
                }

            }
            //从解码器中读取已解码的数据
            val outputBufferId = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000)
            if (outputBufferId >= 0) {
                val outputBuffer = mCodec!!.getOutputBuffer(outputBufferId)
                render(outputBuffer, mBufferInfo)
                mCodec!!.releaseOutputBuffer(outputBufferId, true)
            }
            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                mIsRunning = false
            }
        }
        Log.e("解码器", "解码结束")
        mCodec!!.release()
        mediaExtractor.stop()
    }

    abstract fun render(buffer: ByteBuffer?, bufferInfo: MediaCodec.BufferInfo)

    private fun initCodec() {
        val audioFormat = mediaExtractor.getTrackFormat()
        if (audioFormat != null) {
            val mime = audioFormat.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(mime!!)
            configureCodec(mCodec!!, audioFormat)
            initRender(audioFormat)
        }
    }

    abstract fun configureCodec(codec: MediaCodec, mediaFormat: MediaFormat?)

    abstract fun initRender(audioFormat: MediaFormat)

    private fun initExtractor() {
        mediaExtractor = getExtractor()
    }

}