package com.ray.learnvideo.decoder

import android.media.*
import android.os.Environment
import android.util.Log
import com.ray.learnvideo.MyMediaExtractor
import java.io.File

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
class AudioDecoder : Runnable {

    private var mIsRunning = true

    private var mediaExtractor: MyMediaExtractor? = null

    private var mCodec: MediaCodec? = null

    private val mBufferInfo = MediaCodec.BufferInfo()

    private var mAudioTrack: AudioTrack? = null

    private var mAudioOutTempBuffer = ShortArray(0)

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
                val outputBuffer = mCodec!!.getOutputBuffer(outputBufferId)
                if (mAudioOutTempBuffer.size < mBufferInfo.size / 2) {
                    mAudioOutTempBuffer = ShortArray(mBufferInfo.size / 2)
                }
                outputBuffer?.apply {
                    position(0)
                    asShortBuffer().get(mAudioOutTempBuffer, 0, mBufferInfo.size / 2)
                    mAudioTrack!!.write(mAudioOutTempBuffer, 0, mBufferInfo.size / 2)
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
        val audioFormat = mediaExtractor?.getAudioFormat()
        if (audioFormat != null) {
            val mime = audioFormat.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(mime!!)
            mCodec!!.configure(audioFormat, null, null, 0)
            mCodec!!.start()

            //初始化播放器 AudioTrack
            val sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val pcmEncodeBit = if (audioFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                audioFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
            } else {
                AudioFormat.ENCODING_PCM_16BIT
            }
            val channel = if (channelCount == 1) {
                AudioFormat.CHANNEL_OUT_MONO
            } else {
                AudioFormat.CHANNEL_OUT_STEREO
            }
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, pcmEncodeBit)
            mAudioOutTempBuffer = ShortArray(minBufferSize / 2)
            mAudioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channel,
                pcmEncodeBit,
                minBufferSize,
                AudioTrack.MODE_STREAM
            )
            mAudioTrack!!.play()
        }
    }

    private fun initExtractor() {
        mediaExtractor = MyMediaExtractor().apply {
            setDataSource(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "test.mp4"
            )
        }
    }

}