# ExceptionCrashHandler
全局异常类  
[![GitHub release](https://img.shields.io/badge/release-v1.0.0-green.svg)](https://github.com/Thor-jelly/MaxNumEditText/releases)

# 使用方法
1. 在application中初始化
    
    ```
    //设置全局异常捕捉类  
    ExpectionCrashHandler.getInstance().init(this);
    ```

2. 在第一个activity获得异常信息

    ```
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
    ```
    
# 具体请看源码注释已经很齐全了

# 依赖

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
	
dependencies {
	  compile 'com.github.Thor-jelly:ExceptionCrashHandler:v1.0.0'
}	
```