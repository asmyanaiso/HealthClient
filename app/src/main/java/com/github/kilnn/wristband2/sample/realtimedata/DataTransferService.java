package com.github.kilnn.wristband2.sample.realtimedata;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.kilnn.wristband2.sample.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.HealthyDataResult;
import com.htsmart.wristband2.bean.WristbandConfig;
import com.htsmart.wristband2.bean.WristbandVersion;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DataTransferService extends Service {

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private WristbandConfig mWristbandConfig;

    FirebaseDatabase database;
    DatabaseReference myRef;

    public DataTransferService() {
        database = FirebaseDatabase.getInstance();
        mWristbandConfig = mWristbandManager.getWristbandConfig();
    }

    private final LocalBinder mBinder = new LocalBinder();
    protected Handler handler;
    protected Toast mToast;

    public class LocalBinder extends Binder {
        public DataTransferService getService() {
            return DataTransferService .this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }


    private void initView() {
//        mTvHeartRate = findViewById(R.id.tv_heart_rate);
//        mTvOxygen = findViewById(R.id.tv_oxygen);
//        mTvBloodPressure = findViewById(R.id.tv_blood_pressure);
        //mTvRespiratoryRate = findViewById(R.id.tv_respiratory_rate);
//        mTvBodyTemperature = findViewById(R.id.tv_body_temperature);

        /*mCbHeartRate = findViewById(R.id.cb_heart_rate);
        mCbOxygen = findViewById(R.id.cb_oxygen);
        mCbBloodPressure = findViewById(R.id.cb_blood_pressure);
        mCbRespiratoryRate = findViewById(R.id.cb_respiratory_rate);*/

//        mBtnTestHealthy = findViewById(R.id.btn_test_healthy);
//        mBtnTestHealthy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggleHealthyTesting();
//            }
//        });

        toggleHealthyTesting();

        if (mWristbandConfig != null) {
            WristbandVersion version = mWristbandConfig.getWristbandVersion();
            /*mCbHeartRate.setVisibility(version.isHeartRateEnabled() ? View.VISIBLE : View.GONE);
            mCbOxygen.setVisibility(version.isOxygenEnabled() ? View.VISIBLE : View.GONE);
            mCbBloodPressure.setVisibility(version.isBloodPressureEnabled() ? View.VISIBLE : View.GONE);
            mCbRespiratoryRate.setVisibility(version.isRespiratoryRateEnabled() ? View.VISIBLE : View.GONE);*/ //version.isRespiratoryRateEnabled()*/
        }


        /*mTvEcgSample = findViewById(R.id.tv_ecg_sample);
        mTvEcgValue = findViewById(R.id.tv_ecg_value);
        mBtnTestEcg = findViewById(R.id.btn_test_ecg);
        mBtnTestEcg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEcgTesting();
            }
        });*/
    }

    private Disposable mTestingHealthyDisposable;

    private void toggleHealthyTesting() {
        if (mTestingHealthyDisposable != null && !mTestingHealthyDisposable.isDisposed()) {
            //结束测量
            mTestingHealthyDisposable.dispose();
        } else {
            if (!mWristbandManager.isConnected()) {
//                toast(R.string.device_disconnected);
                return;
            } else if (mWristbandManager.isSyncingData()) {
//                toast(R.string.device_sync_data_busy);
                return;
            } else if (mWristbandConfig == null) {
//                toast("mWristbandConfig=null");
                return;
            }
            int healthyType = 0;
            /*if (mCbHeartRate.getVisibility() == View.VISIBLE && mCbHeartRate.isChecked()) {
                healthyType |= WristbandManager.HEALTHY_TYPE_HEART_RATE;
                Log.e("RealTimeData", "Add HeartRate Test");
            }
            if (mCbOxygen.getVisibility() == View.VISIBLE && mCbOxygen.isChecked()) {
                healthyType |= WristbandManager.HEALTHY_TYPE_OXYGEN;
                Log.e("RealTimeData", "Add Oxygen Test");
            }
            if (mCbBloodPressure.getVisibility() == View.VISIBLE && mCbBloodPressure.isChecked()) {
                healthyType |= WristbandManager.HEALTHY_TYPE_BLOOD_PRESSURE;
                Log.e("RealTimeData", "Add BloodPressure Test");
            }
            if (mCbRespiratoryRate.getVisibility() == View.VISIBLE && mCbRespiratoryRate.isChecked()) {
                healthyType |= WristbandManager.HEALTHY_TYPE_RESPIRATORY_RATE;
                Log.e("RealTimeData", "Add RespiratoryRate Test");
            }*/

            healthyType |= WristbandManager.HEALTHY_TYPE_HEART_RATE;
            healthyType |= WristbandManager.HEALTHY_TYPE_OXYGEN;
            healthyType |= WristbandManager.HEALTHY_TYPE_BLOOD_PRESSURE;
            healthyType |= WristbandManager.HEALTHY_TYPE_TEMPERATURE;

            if (healthyType == 0) {
//                toast("healthyType=0");
                return;
            }
            //开始测量
            mTestingHealthyDisposable = mWristbandManager
                    .openHealthyRealTimeData(healthyType)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {
//                            mBtnTestHealthy.setText(R.string.real_time_data_stop);
                        }
                    })
                    .doOnTerminate(new Action() {
                        @Override
                        public void run() throws Exception {
//                            mBtnTestHealthy.setText(R.string.real_time_data_start);
                        }
                    })
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {
//                            mBtnTestHealthy.setText(R.string.real_time_data_start);
                        }
                    })
                    .subscribe(new Consumer<HealthyDataResult>() {
                        @Override
                        public void accept(HealthyDataResult result) throws Exception {

                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();

                            myRef = database.getReference("data/room_4xfaednjbubevvk/heart_rate/"+ts);
                            int heartRate = result.getHeartRate();
                            HashMap<String, String> hmp = new HashMap<String, String> ();
                            hmp.put("timestamp", ts);
                            hmp.put("value", String.valueOf(heartRate));
                            myRef.setValue(hmp);
                            hmp.clear();

                            myRef = database.getReference("data/room_4xfaednjbubevvk/blood_pressure_diastolic/"+ts);
                            int diastolicBP = result.getDiastolicPressure();
                            hmp.put("timestamp", ts);
                            hmp.put("value", String.valueOf(diastolicBP));
                            myRef.setValue(hmp);
                            hmp.clear();

                            myRef = database.getReference("data/room_4xfaednjbubevvk/blood_pressure_systolic/"+ts);
                            int systolicBP = result.getSystolicPressure();
                            hmp.put("timestamp", ts);
                            hmp.put("value", String.valueOf(systolicBP));
                            myRef.setValue(hmp);
                            hmp.clear();

                            myRef = database.getReference("data/room_4xfaednjbubevvk/blood_oxygen/"+ts);
                            int bloodOxygen = result.getOxygen();
                            hmp.put("timestamp", ts);
                            hmp.put("value", String.valueOf(bloodOxygen));
                            myRef.setValue(hmp);
                            hmp.clear();

                            myRef = database.getReference("data/room_4xfaednjbubevvk/body_temperature/"+ts);
                            float bodyTemp = result.getTemperatureWrist();
                            hmp.put("timestamp", ts);
                            hmp.put("value", String.valueOf(bodyTemp));
                            myRef.setValue(hmp);
                            hmp.clear();

                            //myRef = database.getReference("Chamber1/body/heart_rate");
                            //myRef.setValue( result.getHeartRate());

                            /*mTvHeartRate.setText(getString(R.string.heart_rate_value, result.getHeartRate()));
                            mTvOxygen.setText(getString(R.string.oxygen_value, result.getOxygen()));
                            mTvBloodPressure.setText(getString(R.string.blood_pressure_value, result.getDiastolicPressure(), result.getSystolicPressure()));
                            mTvRespiratoryRate.setText(getString(R.string.respiratory_rate_value, result.getRespiratoryRate()));*/ //getString(R.string.respiratory_rate_value, result.getRespiratoryRate())
//                            mTvHeartRate.setText("Heart Rate : "+heartRate);
//                            mTvOxygen.setText("Blood Oxygen Saturation : "+bloodOxygen);
//                            mTvBloodPressure.setText("Blood Pressure : "+diastolicBP+" "+systolicBP);
                            //mTvRespiratoryRate.setText("Respiratory Rate : "+bodyTemp);
//                            mTvBodyTemperature.setText("Body Temperature : "+bodyTemp);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.w("RealTimeData", "RealTimeData", throwable);
                        }
                    });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                initView();
                // write your code to post content on server
            }
        });
        return android.app.Service.START_STICKY;
    }

}