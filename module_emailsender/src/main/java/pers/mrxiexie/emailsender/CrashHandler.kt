package pers.mrxiexie.emailsender

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Looper
import android.view.WindowManager
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer

@SuppressLint("StaticFieldLeak")
/**
 * 异常处理
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    private var tip: String = "emmmmmm... 系统出了点故障"

    private val msg: String = "额，不对，我应该是说，系统正在维护中..."

    private lateinit var impl: IHandlerException

    private lateinit var context: Context

    /**
     * 系统默认的处理类
     */
    private lateinit var defaultHandler: Thread.UncaughtExceptionHandler

    override fun uncaughtException(t: Thread?, e: Throwable?) {

        val handlerExceptionRunnable = Runnable {
            /**
             * 没有处理则让系统默认的处理器处理异常
             */
            if (!handlerException(e)) {
                defaultHandler.uncaughtException(t, e)
            } else {
                Looper.prepare()
                val dialog = AlertDialog.Builder(context)
                        .setTitle(tip)
                        .setMessage(msg)
                        .setPositiveButton("离开App") { _, _ ->
                            android.os.Process.killProcess(android.os.Process.myPid())
                            System.exit(1)
                        }
                        .create()
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                dialog.show()
                Looper.loop()
            }
        }

        Thread(handlerExceptionRunnable).start()
    }

    fun init(context: Context) {
        this.context = context
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * tip : 出现异常Toast的内容
     */
    fun init(context: Context, tip: String) {
        CrashHandler.tip = tip
        init(context)
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handlerException(ex: Throwable?): Boolean {

        if (ex == null) {
            return false
        }

        val result = getStringFromThrowable(ex)

        impl.handlerException(result)
        //TODO 处理异常
        return true
    }

    /**
     * 获取错误信息
     */
    private fun getStringFromThrowable(ex: Throwable?): String {

        val info: Writer = StringWriter()

        val printWriter = PrintWriter(info)

        ex?.printStackTrace(printWriter)

        var cause: Throwable? = ex?.cause

        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }

        printWriter.close()

        return info.toString()
    }

    fun setHandlerException(impl: IHandlerException) {
        this.impl = impl
    }

    @FunctionalInterface
    interface IHandlerException {
        fun handlerException(cause: String)
    }

}