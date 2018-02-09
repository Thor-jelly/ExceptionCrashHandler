package com.dongdongwu.exceptioncrashhandler;

import android.app.Application;

import com.dongdongwu.exceptioncrash.ExpectionCrashHandler;

/**
 * 类描述：//TODO:(这里用一句话描述这个方法的作用)    <br/>
 * 创建人：吴冬冬<br/>
 * 创建时间：2018/2/8 12:16 <br/>
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //设置全局异常捕捉类
        ExpectionCrashHandler.getInstance().init(this);
    }
}
