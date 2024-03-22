package ru.roansa.trackeroo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import ru.roansa.trackeroo_core.logging.Logger

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvAddLog = findViewById<AppCompatTextView>(R.id.tvAddLog)
        tvAddLog.setOnClickListener {
            for (i in 1..2) {
                Logger.d("Test", "test string $i Lorem ipsum dolor sit amet")
            }
        }

        val tvPublish = findViewById<AppCompatTextView>(R.id.tvPublish)
        tvPublish.setOnClickListener {
            Logger.publish()
        }

        val tvThrowException = findViewById<AppCompatTextView>(R.id.tvThrowException)
        tvThrowException.setOnClickListener {
            var a: Int? = null
            var x = 6
            x = x / a!!
        }

    }


}