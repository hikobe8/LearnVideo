package com.ray.learnvideo

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.ray.learnvideo.decoder.AudioDecoder
import com.ray.learnvideo.decoder.VideoDecoder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    val path =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "monkey.mp4"

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
            val executorService = Executors.newFixedThreadPool(2)
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
                    executorService.submit(AudioDecoder(path))
                    executorService.submit(
                        VideoDecoder(
                            holder!!.surface,
                            path
                        )
                    )
                }
            })
        }

    }
}
