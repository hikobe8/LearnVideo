package com.ray.learnvideo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ray.learnvideo.decoder.AudioDecoder
import com.ray.learnvideo.decoder.VideoDecoder
import com.ray.learnvideo.repack.VideoRepack
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executors


class SimplePlayerActivity : AppCompatActivity() {

    private val path =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "monkey.mp4"

    private val simpleVideoPlayer = SimpleVideoPlayer(path)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
        VideoExtractor().apply {
            setDataSource(
                path
            )
            val trackFormat = getTrackFormat()
            val width = trackFormat!!.getInteger(MediaFormat.KEY_WIDTH)
            val height = trackFormat.getInteger(MediaFormat.KEY_HEIGHT)
            val aspectRatio = height.toFloat() / width
            surfaceView.layoutParams = surfaceView.layoutParams.apply {
                this.height = (resources.displayMetrics.widthPixels * aspectRatio).toInt()
            }
            simpleVideoPlayer.prepare(surfaceView)
        }

    }

    fun play(view: View) {
        view as Button
        if (simpleVideoPlayer.isPlaying()) {
            simpleVideoPlayer.stop()
            view.text = "播放"
        } else {
            simpleVideoPlayer.start()
            view.text = "暂停"
        }
    }

    fun repack(view: View) {
        VideoRepack(path).start()
    }

    override fun onDestroy() {
        simpleVideoPlayer.release()
        super.onDestroy()
    }

}

class SimpleVideoPlayer(private val path: String) {

    companion object {
        const val STATE_IDLE = 0
        const val STATE_PLAYING = 1
    }

    private var videoDecoder: VideoDecoder? = null
    private var audioDecoder: AudioDecoder? = null
    private var executorService = Executors.newFixedThreadPool(2)

    private var mState = STATE_IDLE

    private var mPrepared = false

    private lateinit var mSurface: Surface

    fun prepare(surfaceView: SurfaceView) {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mPrepared = true
                mSurface = holder!!.surface
                if (mState == STATE_PLAYING) {
                    start()
                }
            }
        })
    }

    fun prepare(surfaceTexture: SurfaceTexture) {
        mPrepared = true
        mSurface = Surface(surfaceTexture)
    }

    fun start() {
        if (mState != STATE_IDLE) {
            return
        }
        mState = STATE_PLAYING
        if (mPrepared) {
            if (audioDecoder == null) {
                executorService.submit(AudioDecoder(path).apply {
                    audioDecoder = this
                })
            } else {
                audioDecoder?.resume()
            }
            if (videoDecoder == null) {
                executorService.submit(
                    VideoDecoder(
                        mSurface,
                        path
                    ).apply {
                        videoDecoder = this
                    }
                )
            } else {
                videoDecoder?.resume()
            }
        }
    }

    fun stop() {
        if (mState != STATE_PLAYING) {
            return
        }
        mState = STATE_IDLE
        audioDecoder?.pause()
        videoDecoder?.pause()
    }

    fun isPlaying() = mState == STATE_PLAYING

    fun release(){
        audioDecoder?.release()
        videoDecoder?.release()
    }

}

