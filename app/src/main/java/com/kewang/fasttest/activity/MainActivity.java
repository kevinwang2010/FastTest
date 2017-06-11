package com.kewang.fasttest.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kewang.fasttest.listener.OnVerifyResultCallback;
import com.kewang.fasttest.utils.Util;
import com.kewwang.fasttest.R;

public class MainActivity extends AppCompatActivity {

    private Dialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.tvFastTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = Util.showFastTestDialog(MainActivity.this, new OnVerifyResultCallback() {
                    @Override
                    public void onSuccess() {
                        mDialog.dismiss();
                    }

                    @Override
                    public void onFail() {

                    }
                });
            }
        });

    }
}
