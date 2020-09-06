package com.ray.learnvideo.decoder

import android.media.*
import com.ray.learnvideo.AudioExtractor
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
class AudioDecoder(path: String) : BaseDecoder(path) {

    override fun getExtractor(): RayMediaExtractor {
        return AudioExtractor().apply {
            setDataSource(path)
        }
    }

    private var mAudioTrack: AudioTrack? = null

    private var mAudioOutTempBuffer = ShortArray(0)

    override fun render(buffer: ByteBuffer?, bufferInfo: MediaCodec.BufferInfo) {
        if (mAudioOutTempBuffer.size < bufferInfo.size / 2) {
            mAudioOutTempBuffer = ShortArray(bufferInfo.size / 2)
        }
        buffer?.apply {
            position(0)
            asShortBuffer().get(mAudioOutTempBuffer, 0, bufferInfo.size / 2)
            mAudioTrack!!.write(mAudioOutTempBuffer, 0, bufferInfo.size / 2)
        }
    }

    override fun configureCodec(codec: MediaCodec, mediaFormat: MediaFormat?) {
        codec.configure(mediaFormat, null, null, 0)
        codec.start()
    }

    override fun initRender(audioFormat: MediaFormat) {
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