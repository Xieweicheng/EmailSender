package pers.mrxiexie.emailsender

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.FutureTask

@SuppressLint("StaticFieldLeak")
/**
 * 异常处理
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    private const val TAG = "CrashHandler"

    private var tip: String = "emmmmmm... 系统出了点故障"

    private val msg: String = "额，不对，我应该是说，系统正在维护中..."

    private var impl: IHandlerException? = null

    private lateinit var context: Context

    /**
     * 邮箱列表
     */
    lateinit var emails: Array<String>

    /**
     * 异常日志发送邮件失败时，保存的文件路径
     */
    lateinit var reportPath: String

    /**
     * 系统默认的处理类
     */
    private lateinit var defaultHandler: Thread.UncaughtExceptionHandler

    @SuppressLint("MissingPermission")
    override fun uncaughtException(t: Thread?, e: Throwable?) {

        /**
         * 没有处理则让系统默认的处理器处理异常
         */
        Log.e("Crash", "t : $t e : $e")
        val result = getStringFromThrowable(e)
        if (impl == null) {
            defaultProcessingException(result)
            Thread.sleep(1000)
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0)
        } else {
            if (!impl!!.isDisableDefaultProcessing()) {
                defaultProcessingException(result)
            }
            customizeProcessingException(impl!!, result)
        }
    }

    fun init(context: Context) {
        this.context = context
        reportPath =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + CrashHandler.context.resources.getString(R.string.report_path)
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
     * 自定义异常处理，收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun customizeProcessingException(impl: IHandlerException, result: String) {
        impl.customizeProcessingException(result)
    }

    /**
     * 默认异常处理
     */
    private fun defaultProcessingException(result: String) {
        val params = EmailSender.EmailParams(
                context,
                catchMessage(),
                emails)
        params.content = result
        //发送失败则把错误信息写到SD卡上，当有网络的时候重新发送。

        val futureTask = FutureTask<Boolean> {
            Log.e("EmailSender", "start")
            if (!EmailSender.send(params)) {
                Log.e(TAG, "发送邮件失败，把异常写入${reportPath}中")
                writeReport(result)
            } else {
                Log.e(TAG, "发送邮箱成功")
            }
            true
        }

        Thread(futureTask).start()
        futureTask.get()
    }

    /**
     * 若发送邮件失败，则把错误信息写在sd卡中
     */
    private fun writeReport(cause: String) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val dir = File(reportPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val report = File(reportPath + File.separator + catchMessage())
            report.writeText(cause)
        }
    }

    /**
     * 发送失败的邮件，重新发送
     */
    fun reSendReport() {
        Thread {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                val file = File(reportPath)
                if (!file.exists() || file.listFiles() == null || file.listFiles().isEmpty()) {
                    return@Thread
                }
                val reportFiles = file.listFiles()
                val emailParams = EmailSender.EmailParams(
                        context,
                        "reSendReport",
                        emails)
                emailParams.files = reportFiles
                if (EmailSender.send(emailParams)) {
                    reportFiles.forEach { it.delete() }
                } else {
                    Log.e(TAG, "重新发送邮件失败")
                }
            }
        }.start()
    }

    private fun catchMessage(): String {

        var version_code = 0
        var version_name: String? = null
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
            version_code = info.versionCode
            version_name = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH-mm-SS", Locale.CHINA)

        return "版本号：" + version_code +
                " 版本名：" + version_name +
                " SDK版本：" + Build.VERSION.SDK_INT +
                " 版本：" + Build.VERSION.RELEASE +
                " 设备：" + Build.DEVICE +
                " 当前时间：" + simpleDateFormat.format(Date())
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

        /**
         * 自定义异常处理
         */
        fun customizeProcessingException(cause: String)

        /**
         * 是否禁止默认异常处理（若不禁止，则先使用默认处理再使用自定义处理）
         */
        fun isDisableDefaultProcessing(): Boolean
    }
}