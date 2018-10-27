package pers.mrxiexie.emailsender

import android.content.Context
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

    fun sendcomplexMail(emailParams: EmailParams, file: File, file1: File): Boolean {

        val internetAddressTo = arrayOfNulls<InternetAddress>(emailParams.toArray.size)
        emailParams.toArray.forEachIndexed { index, s ->
            internetAddressTo[index] = InternetAddress(s)
        }

        message = MimeMessage(getSession(emailParams))

       /* val mimeBodyPart = MimeBodyPart()
        val dataHandler = DataHandler(FileDataSource(file))
        mimeBodyPart.dataHandler = dataHandler

        val mimeMultipart = MimeMultipart()
        mimeMultipart.addBodyPart(mimeBodyPart)

        message.setContent(mimeMultipart)*/



        // 5. 创建图片“节点”
        val image =  MimeBodyPart();
        val dh =  DataHandler( FileDataSource(file1)); // 读取本地文件
        image.setDataHandler(dh);                   // 将图片数据添加到“节点”
        image.setContentID("image_fairy_tail");     // 为“节点”设置一个唯一编号（在文本“节点”将引用该ID）

        // 6. 创建文本“节点”
        val text =  MimeBodyPart();
        //    这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
        text.setContent("这是一张图片<br/><img src='cid:image_fairy_tail'/>", "text/html;charset=UTF-8");

        // 7. （文本+图片）设置 文本 和 图片 “节点”的关系（将 文本 和 图片 “节点”合成一个混合“节点”）
        val mm_text_image =  MimeMultipart();
        mm_text_image.addBodyPart(text);
        mm_text_image.addBodyPart(image);
        mm_text_image.setSubType("related");    // 关联关系

        // 8. 将 文本+图片 的混合“节点”封装成一个普通“节点”
        //    最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
        //    上面的 mm_text_image 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
        val text_image =  MimeBodyPart();
        text_image.setContent(mm_text_image);

        // 9. 创建附件“节点”
        val attachment =  MimeBodyPart();
        val dh2 =  DataHandler( FileDataSource(file));  // 读取本地文件
        attachment.setDataHandler(dh2);                                             // 将附件数据添加到“节点”
        attachment.setFileName(MimeUtility.encodeText(dh2.getName()));              // 设置附件的文件名（需要编码）

        // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合“节点” / Multipart ）
        val mm =  MimeMultipart();
//        mm.addBodyPart(text_image);
        mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
//        mm.setSubType("mixed");         // 混合关系

        // 11. 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
        message.setContent(mm);


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
             * 邮件正文可以使用HTML
             */
//            setText(emailParams.content)

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

        try {
            Transport.send(message)
        } catch (e: MessagingException) {
            //发送失败
            return false
        }
        return true
    }

    /**
     * 发送邮件
     *
     * @param emailParams 邮件参数
     * @return 发送失败为false ，成功则为true
     */
    fun sendSimpleTextMail(emailParams: EmailParams): Boolean {

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
             * 邮件正文可以使用HTML
             */
            setText(emailParams.content)

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

        try {
            Transport.send(message)
        } catch (e: MessagingException) {
            //发送失败
            return false
        }
        return true
    }

    fun getSession(emailParams: EmailParams): Session {

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
     * @param content : 邮件内容
     * @param toArray : 接收者列表
     */
    class EmailParams(
            var context: Context,
            var subject: String,
            var content: String,
            var toArray: Array<String> =
                    context.resources.getStringArray(R.array.array_email_to)) {
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
            return "EmailParams(context=$context, subject='$subject', content='$content', toArray=${Arrays.toString(toArray)}, from='$from', pwd='$pwd', needAuth='$needAuth', sentTime=$sentTime, host='$host', port='$port')"
        }
    }
}