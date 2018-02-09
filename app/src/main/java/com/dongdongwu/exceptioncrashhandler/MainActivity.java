package com.dongdongwu.exceptioncrashhandler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dongdongwu.exceptioncrash.ExpectionCrashHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "123===";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取上次的崩溃信息并上传到服务器
        File crashFile = ExpectionCrashHandler.getInstance().getCrashFile();
        //文件存在就上传到服务器
        if (crashFile.exists()) {
            // TODO: 2018/2/9 上传到服务器
            Log.d(TAG, "onCreate: 上传到服务器！");
            Log.d(TAG, "onCreate: -----start-----！");
            try {
//                InputStreamReader fileReader = new InputStreamReader(new FileInputStream(crashFile));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(crashFile), "UTF-8"));
                String ss = "";
                while ((ss = bufferedReader.readLine()) != null) {
                    Log.d(TAG, "内容：" + ss);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onCreate: -----end-----！");

        }



        Log.d(TAG, "onCreate: ---->>>>");
        int i = 2 / 0;
    }
}
