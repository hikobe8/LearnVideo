package com.ray.learnvideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ray.learnvideo.opengl.OpenGLActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    fun simple(view: View) {
        startActivity(Intent(this, SimplePlayerActivity::class.java))
    }

    fun opengl(view: View) {
        startActivity(Intent(this, OpenGLActivity::class.java))
    }
}
