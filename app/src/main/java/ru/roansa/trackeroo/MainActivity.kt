package ru.roansa.trackeroo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.OnScrollChangeListener
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import ru.roansa.trackeroo.R
import ru.roansa.trackeroo_core.logging.Logger
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvAddLog = findViewById<AppCompatTextView>(R.id.tvAddLog)
        tvAddLog.setOnClickListener {
            Timber.d("Test text")
        }

        val tvPublish = findViewById<AppCompatTextView>(R.id.tvPublish)
        tvPublish.setOnClickListener {
            Logger.publish()
        }
    }


}