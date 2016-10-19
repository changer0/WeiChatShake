---
title: Android 微信摇一摇功能实现
date: 2016-10-19 23:22:47
tags: Android原创
---

## 开发之前

> ...感言

### 开发环境
- Android Studio 2.2.1
- JDK1.7 
- API 24
- Gradle 2.2.1

### 相关知识点

- 加速度传感器
- 补间动画 
- 手机震动 (Vibrator)
- 较短 声音/音效 的播放 (SoundPool)


## 开始开发


### 案例预览


![weichat.gif](http://upload-images.jianshu.io/upload_images/3118842-9f58c186f3954fa1.gif?imageMogr2/auto-orient/strip)

### 案例分析

> 我们接下来分析一下这个案例, 当用户晃动手机时, 会触发加速传感器, 此时加速传感器会调用相应接口供我们使用, 此时我们可以做一些相应的动画效果, 震动效果和声音效果. 大致思路就是这样. 具体功能点: 

- 用户晃动后两张图片分开, 显示后面图片
- 晃动后伴随震动效果, 声音效果

> 根据以上的简单分析, 我们就知道该怎么做了, Just now

### 先搭建布局


> 布局没啥可说的, 大家直接看代码吧

``` xml

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ff222222"
    android:orientation="vertical"
    tools:context="com.lulu.weichatshake.MainActivity">
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!--摇一摇中心图片-->
        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:src="@mipmap/weichat_icon"/>
        <LinearLayout
            android:gravity="center"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_alignParentTop="true"
            android:layout_alignParentLeft="true"
            android:layout_alignParentStart="true">
            <!--顶部的横线和图片-->
            <LinearLayout
                android:gravity="center_horizontal|bottom"
                android:id="@+id/main_linear_top"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">
                <ImageView
                    android:src="@mipmap/shake_top"
                    android:id="@+id/main_shake_top"
                    android:layout_width="wrap_content"
                    android:layout_height="100dp"/>
                <ImageView
                    android:background="@mipmap/shake_top_line"
                    android:id="@+id/main_shake_top_line"
                    android:layout_width="match_parent"
                    android:layout_height="5dp"/>
            </LinearLayout>
            <!--底部的横线和图片-->
            <LinearLayout
                android:gravity="center_horizontal|bottom"
                android:id="@+id/main_linear_bottom"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">
                <ImageView
                    android:background="@mipmap/shake_bottom_line"
                    android:id="@+id/main_shake_bottom_line"
                    android:layout_width="match_parent"
                    android:layout_height="5dp"/>
                <ImageView
                    android:src="@mipmap/shake_bottom"
                    android:id="@+id/main_shake_bottom"
                    android:layout_width="wrap_content"
                    android:layout_height="100dp"/>
            </LinearLayout>
        </LinearLayout>
    </RelativeLayout>
</LinearLayout>


```

### 得到加速度传感器的回调接口

> step1: 在onStart() 方法中获取传感器的SensorManager

``` java

@Override
protected void onStart() {
    super.onStart();
    //获取 SensorManager 负责管理传感器
    mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
    if (mSensorManager != null) {
        //获取加速度传感器
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometerSensor != null) {
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }
}

```

> step2: 紧接着我们就要在Pause中注销传感器

``` java

@Override
protected void onPause() {
    // 务必要在pause中注销 mSensorManager
    // 否则会造成界面退出后摇一摇依旧生效的bug
    if (mSensorManager != null) {
        mSensorManager.unregisterListener(this);
    }
    super.onPause();
}

```

> Note: 至于为什么我们要在onStart和onPause中就行SensorManager的注册和注销, 就是因为, 防止在界面退出(包括按Home键)时, 摇一摇依旧生效(代码中有注释)


> step3: 在step1中的注册监听事件方法中, 我们传入了当前Activity对象, 故让其实现回调接口, 得到以下方法

``` java

///////////////////////////////////////////////////////////////////////////
// SensorEventListener回调方法
///////////////////////////////////////////////////////////////////////////
@Override
public void onSensorChanged(SensorEvent event) {
    int type = event.sensor.getType();

    if (type == Sensor.TYPE_ACCELEROMETER) {
        //获取三个方向值
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];

        if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math
                .abs(z) > 17) && !isShake) {
            isShake = true;
            // TODO: 2016/10/19 实现摇动逻辑, 摇动后进行震动
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Log.d(TAG, "onSensorChanged: 摇动");

                        //开始震动 发出提示音 展示动画效果
                        mHandler.obtainMessage(START_SHAKE).sendToTarget();
                        Thread.sleep(500);
                        //再来一次震动提示
                        mHandler.obtainMessage(AGAIN_SHAKE).sendToTarget();
                        Thread.sleep(500);
                        mHandler.obtainMessage(END_SHAKE).sendToTarget();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }
}

@Override
public void onAccuracyChanged(Sensor sensor, int accuracy) {
}

```

> Note: 当用户晃动手机会调用onSensorChanged方法, 可以做一些相应的操作
> 为解决动画和震动延迟, 我们开启了一个子线程来实现. 
> 子线程中会通过发送Handler消息, 先开始动画效果, 并伴随震动和声音
> 先把Handler的实现放一放, 我们再来看一下震动和声音初始化


### 动画, 震动和音效实现

> step 1: 先获取到震动相关的服务,注意要加权限. 至于音效, 我们采用SoundPool来播放, 在这里非常感谢[Vincent](http://www.devdiv.com/Android-SoundPool%E7%B1%BB%E7%9A%84%E4%BD%BF%E7%94%A8-thread-130200-1-1.html) 的贴子, 好初始化SoundPool

**震动权限**

``` xml
    <uses-permission android:name="android.permission.VIBRATE"/>
```

``` java

//初始化SoundPool
mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
mWeiChatAudio = mSoundPool.load(this, R.raw.weichat_audio, 1);

//获取Vibrator震动服务
mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

```

> Note: 大家可能发现SoundPool的构造方法已经过时, 不过不用担心这是Api21之后过时的, 所以也不算太"过时"吧
　

> step2: 接下来我们就要介绍Handler中的实现了, 为避免Activity内存泄漏, 采用了软引用方式


``` java
private static class MyHandler extends Handler {
    private WeakReference<MainActivity> mReference;
    private MainActivity mActivity;
    public MyHandler(MainActivity activity) {
        mReference = new WeakReference<MainActivity>(activity);
        if (mReference != null) {
            mActivity = mReference.get();
        }
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case START_SHAKE:
                //This method requires the caller to hold the permission VIBRATE.
                mActivity.mVibrator.vibrate(300);
                //发出提示音
                mActivity.mSoundPool.play(mActivity.mWeiChatAudio, 1, 1, 0, 0, 1);
                mActivity.mTopLine.setVisibility(View.VISIBLE);
                mActivity.mBottomLine.setVisibility(View.VISIBLE);
                mActivity.startAnimation(false);//参数含义: (不是回来) 也就是说两张图片分散开的动画
                break;
            case AGAIN_SHAKE:
                mActivity.mVibrator.vibrate(300);
                break;
            case END_SHAKE:
                //整体效果结束, 将震动设置为false
                mActivity.isShake = false;
                // 展示上下两种图片回来的效果
                mActivity.startAnimation(true);
                break;
        }
    }
}


```
> Note: 内容不多说了, 代码注释中很详细, 还有一个startAnimation方法
> 我先来说一下它的参数, true表示布局中两张图片从打开到关闭的动画, 反之, false是从关闭到打开状态, 上代码

> step3: startAnimaion方法上的实现
``` java


/**
 * 开启 摇一摇动画
 *
 * @param isBack 是否是返回初识状态
 */
private void startAnimation(boolean isBack) {
    //动画坐标移动的位置的类型是相对自己的
    int type = Animation.RELATIVE_TO_SELF;

    float topFromY;
    float topToY;
    float bottomFromY;
    float bottomToY;
    if (isBack) {
        topFromY = -0.5f;
        topToY = 0;
        bottomFromY = 0.5f;
        bottomToY = 0;
    } else {
        topFromY = 0;
        topToY = -0.5f;
        bottomFromY = 0;
        bottomToY = 0.5f;
    }

    //上面图片的动画效果
    TranslateAnimation topAnim = new TranslateAnimation(
            type, 0, type, 0, type, topFromY, type, topToY
    );
    topAnim.setDuration(200);
    //动画终止时停留在最后一帧~不然会回到没有执行之前的状态
    topAnim.setFillAfter(true);

    //底部的动画效果
    TranslateAnimation bottomAnim = new TranslateAnimation(
            type, 0, type, 0, type, bottomFromY, type, bottomToY
    );
    bottomAnim.setDuration(200);
    bottomAnim.setFillAfter(true);

    //大家一定不要忘记, 当要回来时, 我们中间的两根线需要GONE掉
    if (isBack) {
        bottomAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                //当动画结束后 , 将中间两条线GONE掉, 不让其占位
                mTopLine.setVisibility(View.GONE);
                mBottomLine.setVisibility(View.GONE);
            }
        });
    }
    //设置动画
    mTopLayout.startAnimation(topAnim);
    mBottomLayout.startAnimation(bottomAnim);

}

```

> 至此 核心代码已经介绍完毕 , 但是还有部分小细节不得不提一下


### 细枝末节

> 1. 大家要在初始化View之前将上下两条横线GONE掉, 用GONE是不占位的

``` java

mTopLine.setVisibility(View.GONE);
mBottomLine.setVisibility(View.GONE);
```

> 2.咱们的摇一摇最好是只竖屏 (毕竟我也没见过横屏的摇一摇), 加上下面代码

``` java
//设置只竖屏
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
```


## 完整代码

源码我已经发在了[github](https://github.com/changer0/WeiChatShake)上, 希望大家多多支持!