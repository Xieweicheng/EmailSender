package pers.mrxiexie.emailsender;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import org.jetbrains.annotations.NotNull;


/**
 * @author MrXieXie
 * @date 2018/10/27 16:28
 */
public class MyApplication extends Application implements CrashHandler.IHandlerException {

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Crash", "onCreate");
        context = getApplicationContext();

        CrashHandler.INSTANCE.init(this);
        CrashHandler.INSTANCE.setHandlerException(this);
        CrashHandler.INSTANCE.setEmails(new String[]{"442335155@qq.com"});
        CrashHandler.INSTANCE.reSendReport();
    }

    @Override
    public void customizeProcessingException(@NotNull String cause) {
        Log.e("Crash", "customizeProcessingException");
        Log.e("Crash", "customizeProcessingException : " + Thread.currentThread().getName());

        //跳转到App最开始的页面
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Handler", "Handler");
                        Log.e("Handler", Thread.currentThread().getName());

                        AlertDialog alertDialog = new AlertDialog.Builder(context).setPositiveButton("切丁", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(0);
                            }
                        }).create();

                        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        alertDialog.show();
                        Log.e("Handler", Thread.currentThread().getName());
                    }
                });
                Looper.loop();
            }
        }).start();

//        Toast.makeText(context, cause, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean isDisableDefaultProcessing() {
        return false;
    }


}
