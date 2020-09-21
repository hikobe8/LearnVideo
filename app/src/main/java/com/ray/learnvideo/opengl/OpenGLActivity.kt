package com.ray.learnvideo.opengl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ray.learnvideo.R

class OpenGLActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opengl)
    }

    fun triangle(view: View) {
        startActivity(Intent(this, GlDrawerActivity::class.java).apply {
            putExtra("type", TYPE_TRIANGLE)
        })
    }

    fun bitmap(view: View) {
        startActivity(Intent(this, GlDrawerActivity::class.java).apply {
            putExtra("type", TYPE_IMAGE)
        })
    }

    fun video(view: View) {
        startActivity(Intent(this, GlDrawerActivity::class.java).apply {
            putExtra("type", TYPE_VIDEO)
        })
    }

    fun videoMulti(view: View) {
        startActivity(Intent(this, GlDrawerActivity::class.java).apply {
            putExtra("type", TYPE_VIDEO_MULTIPLE)
        })
    }
}
