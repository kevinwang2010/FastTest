package com.kewang.fasttest.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import com.kewang.fasttest.listener.OnVerifyResultCallback;
import com.kewang.fasttest.view.FastTestView;
import com.kewwang.fasttest.R;


/**
 * Created by AD on 2016/8/30.
 */
public class Util {

    /**
     * 图形验证码的dialog
     * @param context
     */
    public static Dialog showFastTestDialog(final Activity context, OnVerifyResultCallback callback){
        final Dialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        alertDialog.show();
        alertDialog.setContentView(R.layout.layout_fast_test);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(false);
        FastTestView ftView = (FastTestView) alertDialog.findViewById(R.id.ftView);
        ftView.setOnVerifyResultCallback(callback);
        return alertDialog;
    }
}
