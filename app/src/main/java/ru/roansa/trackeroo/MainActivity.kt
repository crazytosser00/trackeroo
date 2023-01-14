package ru.roansa.trackeroo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import ru.roansa.trackeroo_core.logging.Logger
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Logger.clearLogs()

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