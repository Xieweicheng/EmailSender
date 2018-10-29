package pers.mrxiexie.emailsender

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast

/**
 * @author MrXieXie
 * @date 2018/10/27 15:43
 */
open class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "TEST"
        context = this
        setContentView(textView)

        Log.e("MainActivity", "onCreate")


        toast(Thread.currentThread().name)

//        val random = Math.random() * 10
//        Log.e("CRASH", "random : " + random)
//        if(random > 5){
//            val i = 1 / 0
//        }else{
//            toast("安全")
//        }

        Thread {
            Thread.sleep(3000)

            val i = 1 / 0
            /* Looper.prepare()
             Handler(Looper.getMainLooper()).post {
                 val dialog = AlertDialog.Builder(this)
                         .setTitle("Title")
                         .setMessage("Msg")
                         .setPositiveButton("离开App") { _, _ ->
                             android.os.Process.killProcess(android.os.Process.myPid())
                             System.exit(1)
                         }
                         .create()
 //                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                 dialog.show()
             }
             Looper.loop()*/

        }.start()
//        val i = 1 / 0
        /* var bool: Boolean
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
         }.start()*/
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e("MainActivity", "ondestroy")
    }
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}