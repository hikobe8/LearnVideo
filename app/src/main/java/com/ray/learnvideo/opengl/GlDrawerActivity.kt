package com.ray.learnvideo.opengl

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ray.learnvideo.R
import kotlinx.android.synthetic.main.activity_gl_drawer.*

class GlDrawerActivity : AppCompatActivity() {

    private lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_drawer)
        glSV.setEGLContextClientVersion(2)
        drawer = createDrawer()
        glSV.setRenderer(SimpleRender(drawer))
        glSV.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    private fun createDrawer(): IDrawer {
        return when (intent.getIntExtra("type", TYPE_TRIANGLE)) {
            TYPE_TRIANGLE -> {
                TriangleDrawer()
            }
            TYPE_SQUARE -> {
                TriangleDrawer()
            }
            else -> {
                val bmp = BitmapFactory.decodeResource(resources, R.drawable.android)
                BitmapDrawer(bmp)
            }
        }
    }

    override fun onDestroy() {
        drawer.release()
        super.onDestroy()
    }

}
