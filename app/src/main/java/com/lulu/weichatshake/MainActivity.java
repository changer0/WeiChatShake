package com.lulu.weichatshake;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int START_SHAKE = 0x1;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Vibrator mVibrator;

    //记录摇动状态
    private boolean isShake = false;

    private ImageView mShakeTopImg;
    private ImageView mShakeBottomImg;
    private LinearLayout mTopLayout;
    private LinearLayout mBottomLayout;
    private ImageView mTopLine;
    private ImageView mBottomLine;

    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化View
        initView();
        mHandler = new MyHandler(this);

        //获取 SensorManager 负责管理传感器
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        if (mSensorManager != null) {
            //获取加速度传感器
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
        //获取Vibrator震动服务
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    private void initView() {
        mShakeTopImg = (ImageView) findViewById(R.id.main_shake_top);
        mShakeBottomImg = (ImageView) findViewById(R.id.main_shake_bottom);
        mTopLayout = (LinearLayout) findViewById(R.id.main_linear_top);
        mBottomLayout = ((LinearLayout) findViewById(R.id.main_linear_bottom));
        mTopLine = (ImageView) findViewById(R.id.main_shake_top_line);
        mBottomLine = (ImageView) findViewById(R.id.main_shake_bottom_line);

        //默认
        mTopLine.setVisibility(View.GONE);
        mBottomLine.setVisibility(View.GONE);


    }

    @Override
    protected void onDestroy() {
        //不要忘记在Activity销毁时注销 mAccelerometerSensor
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onDestroy();
    }

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
                            //开始震动和展示动画效果
                            mHandler.obtainMessage(START_SHAKE).sendToTarget();
                            Thread.sleep(500);
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
                    mActivity.mTopLine.setVisibility(View.VISIBLE);
                    mActivity.mBottomLine.setVisibility(View.VISIBLE);
                    mActivity.startAnimation(false);//参数含义: 是
                    break;
            }
        }
    }

    /**
     * 开启 摇一摇动画
     * @param isBack 是否是返回初识状态
     */
    private void startAnimation(boolean isBack) {

    }


}
