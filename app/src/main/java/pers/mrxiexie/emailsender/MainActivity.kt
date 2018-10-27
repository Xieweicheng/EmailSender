package pers.mrxiexie.emailsender

import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * @author MrXieXie
 * @date 2018/10/27 15:43
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "TEST"
        setContentView(textView)

        var bool: Boolean
        Thread {
            val emailParams = EmailSender.EmailParams(this, "subject")
            Log.e("MrXieXie", "emailParams : $emailParams")
            val file = File(Environment.getExternalStorageDirectory().absolutePath + "/1.txt")
            if (!file.exists()) {
                file.createNewFile()
            }
            emailParams.files = arrayOf(file, file)
            emailParams.content = "你好"
            bool = EmailSender.send(emailParams)
            runOnUiThread {
                Toast.makeText(this, "bool : $bool", Toast.LENGTH_LONG).show()
            }
        }.start()
    }
}