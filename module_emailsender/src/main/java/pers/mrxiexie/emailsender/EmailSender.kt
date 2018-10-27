package pers.mrxiexie.emailsender

import android.content.Context
import android.text.TextUtils
import java.io.File
import java.util.*
import javax.activation.CommandMap
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.activation.MailcapCommandMap
import javax.mail.*
import javax.mail.internet.*


/**
 * 发送邮件
 */
object EmailSender {

    /**
     * 创建参数配置, 用于连接邮件服务器的参数配置
     */
    private lateinit var properties: Properties

    /**
     * 根据配置创建会话对象, 用于和邮件服务器交互
     */
    private lateinit var session: Session

    /**
     * 创建一封邮件
     */
    private lateinit var message: MimeMessage

    /**
     * 发送邮件
     *
     * @param emailParams 封装邮件参数
     */
    fun send(emailParams: EmailParams): Boolean {

        val mm = MimeMultipart()
        val message = getMessage(emailParams)
        val content = emailParams.content
        val files = emailParams.files

        if (!TextUtils.isEmpty(content)) {
            val contentPart = MimeBodyPart()
            contentPart.setText(content)
            mm.addBodyPart(contentPart)
        }

        if (!files.isEmpty()) {
            val filesPart = MimeBodyPart()
            files.forEach {
                val dh = DataHandler(FileDataSource(it))
                filesPart.dataHandler = dh
                filesPart.fileName = MimeUtility.encodeText(dh.name)
                mm.addBodyPart(filesPart)
            }
        }

        message.setContent(mm)

        try {
            Transport.send(EmailSender.message)
        } catch (e: MessagingException) {
            //发送失败
            return false
        }
        return true
    }

    private fun getMessage(emailParams: EmailParams): Message {
        val internetAddressTo = arrayOfNulls<InternetAddress>(emailParams.toArray.size)
        emailParams.toArray.forEachIndexed { index, s ->
            internetAddressTo[index] = InternetAddress(s)
        }

        message = MimeMessage(getSession(emailParams))

        message.run {

            /**
             * 发件人
             */
            setFrom(InternetAddress(emailParams.from))

            /**
             * RecipientType.TO 收件人
             * RecipientType.CC 抄送人
             * RecipientType.BCC 密送人
             */
            setRecipients(Message.RecipientType.TO, internetAddressTo)

            /**
             * 设置主题与编码格式
             */
            setSubject(emailParams.subject, "UTF-8")

            /**
             * 发送时间
             */
            sentDate = emailParams.sentTime

            /**
             * 保存前面的设置
             */
            //saveChanges()
        }

        val mc = CommandMap.getDefaultCommandMap() as MailcapCommandMap
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
        CommandMap.setDefaultCommandMap(mc)

        return message
    }

    private fun getSession(emailParams: EmailParams): Session {

        val properties = Properties()

        properties.run {
            put("mail.smtp.host", emailParams.host)
            put("mail.transport.protocol", "smtp")
            put("mail.smtp.auth", emailParams.needAuth)
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.port", emailParams.port)
            put("mail.smtp.socketFactory.port", emailParams.port)
        }

        if (emailParams.needAuth.toBoolean()) {
            session = Session.getDefaultInstance(properties, EmailAuthenticator(emailParams.from, emailParams.pwd))
        } else {
            session = Session.getInstance(properties)
        }

        return session
    }

    /**
     * 身份认证
     */
    class EmailAuthenticator(val from: String, val pwd: String) : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(from, pwd)
        }
    }

    /**
     * Email参数
     *
     * @param subject : 邮件主题
     * @param toArray : 接收者列表
     */
    class EmailParams(
            var context: Context,
            var subject: String,
            var toArray: Array<String> =
                    context.resources.getStringArray(R.array.array_email_to)) {


        /**
         * 邮件内容
         */
        var content: String? = null

        /**
         * 附件列表
         */
        var files: Array<File> = emptyArray()

        /**
         * 发送者
         */
        lateinit var from: String

        /**
         * 发送者密码
         */
        lateinit var pwd: String

        /**
         * 是否需要验证
         */
        lateinit var needAuth: String

        /**
         * 发送时间
         */
        lateinit var sentTime: Date

        /**
         * 服务器
         */
        lateinit var host: String

        /**
         * 服务器端口
         */
        lateinit var port: String

        init {
            context.resources.run {
                from = getString(R.string.email_from)
                pwd = getString(R.string.email_pwd)
                needAuth = getString(R.string.email_auth)
                host = getString(R.string.email_host)
                port = getString(R.string.email_port)
                sentTime = Date()
            }
        }

        override fun toString(): String {
            return "EmailParams(context=$context, subject='$subject', toArray=${Arrays.toString(toArray)}, content=$content, from='$from', pwd='$pwd', needAuth='$needAuth', sentTime=$sentTime, host='$host', port='$port')"
        }
    }
}