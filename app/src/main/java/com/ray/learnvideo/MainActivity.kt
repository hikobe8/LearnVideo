package com.ray.learnvideo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.ray.learnvideo.decoder.AudioDecoder
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
//        thread {
//            MyMediaExtractor().apply {
////                setDataSource("https://media.w3.org/2010/05/sintel/trailer.mp4")
//                setDataSource(
//                    Environment.getExternalStorageDirectory()
//                        .toString() + File.separator + "test.mp4"
//                )
//                extract()
//            }
//        }
        val executorService = Executors.newFixedThreadPool(2)
        executorService.submit(AudioDecoder())
    }
}
