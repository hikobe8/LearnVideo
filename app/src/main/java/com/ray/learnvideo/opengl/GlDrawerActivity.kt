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

    private lateinit var drawers: ArrayList<IDrawer>
    private var players: MutableList<SimpleVideoPlayer> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_drawer)
        glSV.setEGLContextClientVersion(2)
        drawers = createDrawer()
        glSV.setRenderer(SimpleRender().apply {
            addDrawers(drawers)
        })
    }

    private fun createDrawer(): ArrayList<IDrawer> {
        return when (intent.getIntExtra("type", TYPE_TRIANGLE)) {
            TYPE_TRIANGLE -> {
                arrayListOf(TriangleDrawer())
            }
            TYPE_SQUARE -> {
                arrayListOf(TriangleDrawer())
            }
            TYPE_IMAGE -> {
                val bmp = BitmapFactory.decodeResource(resources, R.drawable.android)
                arrayListOf(BitmapDrawer(bmp))
            }
            TYPE_VIDEO -> {
                arrayListOf(createVideoDrawer(Environment.getExternalStorageDirectory().absolutePath + File.separator + "monkey.mp4"))

            }
            TYPE_VIDEO_MULTIPLE -> {
                val first =
                    createVideoDrawer(Environment.getExternalStorageDirectory().absolutePath + File.separator + "monkey.mp4")
                val second =
                    createVideoDrawer(
                        Environment.getExternalStorageDirectory().absolutePath + File.separator + "dragon.mp4",
                        0.3f
                    )
                arrayListOf(first, second)
            }
            else -> arrayListOf(TriangleDrawer())
        }
    }

    private fun createVideoDrawer(path: String, alpha: Float = 1f) = VideoExtractor().run {
        setDataSource(path)
        val trackFormat = getTrackFormat()
        val width = trackFormat!!.getInteger(MediaFormat.KEY_WIDTH)
        val height = trackFormat.getInteger(MediaFormat.KEY_HEIGHT)
        VideoDrawer { surfaceTexture ->
            SimpleVideoPlayer(
                path
            ).apply {
                prepare(surfaceTexture)
                start()
                players.add(this)
            }
        }.apply {
            setVideoSize(width, height)
            setVideoAlpha(alpha)
        }
    }

    override fun onDestroy() {
        for (drawer in drawers) {
            drawer.release()
        }
        for (player in players) {
            player.release()
        }
        super.onDestroy()
    }

}
