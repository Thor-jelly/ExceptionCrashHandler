package com.dongdongwu.exceptioncrash;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 类描述：异常捕获单例 <br/>
 * 创建人：吴冬冬<br/>
 * 创建时间：2018/2/8 11:00 <br/>
 */

public class ExpectionCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "123===";

    private static ExpectionCrashHandler mInstance;
    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;
    private String mMobileInfo;

    public static ExpectionCrashHandler getInstance() {
        if (mInstance == null) {
            //解决多并发问题
            synchronized (ExpectionCrashHandler.class) {
                if (mInstance == null) {
                    mInstance = new ExpectionCrashHandler();
                }
            }
        }

        return mInstance;
    }

    /**
     * 主要用来获取应用的一些信息
     */
    private Context mContext;

    public void init(Context context) {
        mContext = context;

        //设置全局异常类为当前this
        Thread.currentThread().setUncaughtExceptionHandler(this);

        //获取系统默认的
        mDefaultUncaughtExceptionHandler = Thread.currentThread().getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //管理全局异常
        Log.e(TAG, "uncaughtException: 走异常方法了！！！");

        //1、写入到本地文件 -> 需要写入 异常内容 和 当前版本信息 手机信息
        //所以需要context这个,这就是context存在的意义

        //1.1崩溃信息
        //1.2应用信息 版本号
        //1.3手机信息
        String crashFileName = saveInfoToSD(e);

        Log.e(TAG, "uncaughtException:-生成文件名--->>> " + crashFileName);

        //2、保存当前文件，等应用再次启动再上传文件。(上传文件，但是不在这里处理)
        cacheCrashFile(crashFileName);

        //让系统默认处理->也就是系统原来怎么处理还怎么处理(比如空指针异常会关闭该应用，如没有下面一行则不会关闭应用直接变成白色，并且没有没有系统提示的错误在哪一行。)
        mDefaultUncaughtExceptionHandler.uncaughtException(t, e);
    }

    /**
     * 缓存日志文件
     */
    private void cacheCrashFile(String crashFileName) {
        SharedPreferences sp = mContext.getSharedPreferences("crash", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("CRASH_FILE_NAME", crashFileName);
        edit.apply();
    }

    /**
     * 获取崩溃文件名称
     * @return
     */
    public File getCrashFile() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("crash", Context.MODE_PRIVATE);
        String crashFileName = sharedPreferences.getString("CRASH_FILE_NAME", "");
        return new File(crashFileName);
    }

    /**
     * 保存获取的 软件信息、设备信息、出错信息 保存到SDcard中
     *
     * @param e
     * @return
     */
    private String saveInfoToSD(Throwable e) {
        String fileName = null;
        StringBuilder sb = new StringBuilder();

        //1手机信息 + 应用信息 ->>>obtainSimpleInfo
        //获取一些简单的信息，软件版本，手机版本型号等存放到hashmap中
        for (Map.Entry<String, String> entry : obtainSimpleInfo(mContext).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key)
                    .append(" = ")
                    .append(value)
                    .append("\n");
        }

        //2异常信息
        //获取系统未捕捉的错误信息
        sb.append(obtaionExceptionInfo(e));

        //这里直接用的手机应用的目录，并没有拿手机Sd卡的目录，因为6.0以上需要动态申请权限
        //判断sd是否被正常挂载
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        //文件在的文件夹位置
        File dir = new File(mContext.getFilesDir() + File.separator + "crash" + File.separator);

        //先删除之前的异常信息
        if (dir.exists()) {
            //删除该目录下的所有子文件
            deleteDir(dir);
        }

        //再重新创建文件夹
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            fileName = dir.toString()
                    + File.separator
                    + getAssignTime("yyyy_MM_dd_HH_mm")
                    + ".txt";
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(sb.toString().getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
//        }
        return fileName;
    }

    /**
     * 日期格式化
     *
     * @param dateFormatStr 提起格式
     */
    private String getAssignTime(String dateFormatStr) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 获取一些简单的信息，软件版本，手机版本型号等存放到hashmap中
     */
    private LinkedHashMap<String, String> obtainSimpleInfo(Context context) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        map.put("MOBLE_INFO", getMobileInfo());//手机信息
        map.put("versionName", packageInfo.versionName);
        map.put("versionCode", packageInfo.versionCode + "");
        map.put("MODEL", Build.MODEL);//手机名
        map.put("RELEASE", Build.VERSION.RELEASE);//系统版本
        map.put("SDK_INT", Build.VERSION.SDK_INT + "");//SDK版本
        map.put("PRODUCT", Build.PRODUCT);//手机制造商
        return map;
    }

    /**
     * 获取手机信息
     * 通过类的反射
     */
    private String getMobileInfo() {
        StringBuffer stringBuffer = new StringBuffer("\n");
        stringBuffer.append("----------------------------");
        stringBuffer.append("\n");
        try {
            //利用反射获取 Build的所有属性
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                //这里为什么可以传空呢？因为它里面所有属性都是静态的
                String value = field.get(null).toString();
                stringBuffer.append(name + " = " + value)
                        .append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stringBuffer.append("----------------------------");
        stringBuffer.append("\n");
        return stringBuffer.toString();
    }

    /**
     * 获取系统未捕捉的错误信息(其实就是把异常变成string返回)
     */
    private String obtaionExceptionInfo(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    /**
     * 递归删除目录下所有文件所有文件
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            //递归删除目录中的子目录
            for (File child : children) {
                child.delete();
            }
        }
        //目录此时为空可以删除
        return true;
    }
}
