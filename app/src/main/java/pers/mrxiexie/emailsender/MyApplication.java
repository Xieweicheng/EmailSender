package pers.mrxiexie.emailsender;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

                        Intent intent = new Intent(MainActivity.context, ReStartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainActivity.context.startActivity(intent);
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
