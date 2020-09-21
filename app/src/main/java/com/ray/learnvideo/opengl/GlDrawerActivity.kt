package com.ray.learnvideo.opengl

import android.graphics.BitmapFactory
import android.media.MediaFormat
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.ray.learnvideo.R
import com.ray.learnvideo.SimpleVideoPlayer
import com.ray.learnvideo.VideoExtractor
import kotlinx.android.synthetic.main.activity_gl_drawer.*
import java.io.File

class GlDrawerActivity : AppCompatActivity() {

    private lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_drawer)
        glSV.setEGLContextClientVersion(2)
        drawer = createDrawer()
        glSV.setRenderer(SimpleRender(drawer))
    }

    private fun createDrawer(): IDrawer {
        return when (intent.getIntExtra("type", TYPE_TRIANGLE)) {
            TYPE_TRIANGLE -> {
                TriangleDrawer()
            }
            TYPE_SQUARE -> {
                TriangleDrawer()
            }
            TYPE_IMAGE -> {
                val bmp = BitmapFactory.decodeResource(resources, R.drawable.android)
                BitmapDrawer(bmp)
            }
            TYPE_VIDEO -> {
                VideoExtractor().run {
                    setDataSource(
                        Environment.getExternalStorageDirectory().absolutePath + File.separator + "monkey.mp4"
                    )
                    val trackFormat = getTrackFormat()
                    val width = trackFormat!!.getInteger(MediaFormat.KEY_WIDTH)
                    val height = trackFormat.getInteger(MediaFormat.KEY_HEIGHT)
                    VideoDrawer { surfaceTexture ->
                        SimpleVideoPlayer(
                            Environment.getExternalStorageDirectory().absolutePath + File.separator + "monkey.mp4"
                        ).apply {
                            prepare(surfaceTexture)
                            start()
                        }
                    }.apply {
                        setVideoSize(width, height)
                    }
                }

            }
            else -> TriangleDrawer()
        }
    }

    override fun onDestroy() {
        drawer.release()
        super.onDestroy()
    }

}
