package pers.mrxiexie.emailsender;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * @author MrXieXie
 * @date 2018-10-29 0:40
 */
public class ReStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("llllllllllllllllllllllllllllllllll");
        setContentView(textView);
    }
}