package com.ray.learnvideo.opengl

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
    }

    private fun createDrawer(): IDrawer {
        return when (intent.getIntExtra("type", TYPE_TRIANGLE)) {
            TYPE_SQUARE -> {
                TriangleDrawer()
            }
            TYPE_SQUARE -> {
                TriangleDrawer()
            }
            else -> {
                TriangleDrawer()
            }
        }
    }

    override fun onDestroy() {
        drawer.release()
        super.onDestroy()
    }

}
