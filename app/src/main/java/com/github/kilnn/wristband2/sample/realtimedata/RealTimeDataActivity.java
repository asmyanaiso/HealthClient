package com.github.kilnn.wristband2.sample.realtimedata;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.HealthyDataResult;
import com.htsmart.wristband2.bean.WristbandConfig;
import com.htsmart.wristband2.bean.WristbandVersion;
import com.htsmart.wristband2.bean.data.EcgData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import android.app.Service;

/**
 * Created by Kilnn on 16-10-26.
 * <p>
 * en:Because the hardware may be different, so you should request and check the WristbandVersion
 * before using real time testing.
 * </p>
 * <p>
 * zh-rCN:因为不同的手环可能硬件模块不一样，所以在开始实时测量之前，请先获取并检测WristbandVersion。
 * </p>
 */
public class RealTimeDataActivity extends BaseActivity{

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private WristbandConfig mWristbandConfig;

    private TextView mTvHeartRate;
    private TextView mTvOxygen;
    private TextView mTvBloodPressure;
    private TextView mTvRespiratoryRate;
    private TextView mTvBodyTemperature;
    private CheckBox mCbHeartRate;
    private CheckBox mCbOxygen;
    private CheckBox mCbBloodPressure;
    private CheckBox mCbRespiratoryRate;
    private Button mBtnTestHealthy;

    private TextView mTvEcgSample;
    private TextView mTvEcgValue;
    private Button mBtnTestEcg;

    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_data);
//
//        database = FirebaseDatabase.getInstance();
//
//        mWristbandConfig = mWristbandManager.getWristbandConfig();
//        initView();
        Intent intent = new Intent(this, DataTransferService.class);
        startService(intent);
    }

    private void initView() {
        mTvHeartRate = findViewById(R.id.tv_heart_rate);
        mTvOxygen = findViewById(R.id.tv_oxygen);
        mTvBloodPressure = findViewById(R.id.tv_blood_pressure);
        //mTvRespiratoryRate = findViewById(R.id.tv_respiratory_rate);
        mTvBodyTemperature = findViewById(R.id.tv_body_temperature);

        /*mCbHeartRate = findViewById(R.id.cb_heart_rate);
        mCbOxygen = findViewById(R.id.cb_oxygen);
        mCbBloodPressure = findViewById(R.id.cb_blood_pressure);
        mCbRespiratoryRate = findViewById(R.id.cb_respiratory_rate);*/

        mBtnTestHealthy = findViewById(R.id.btn_test_healthy);
        mBtnTestHealthy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleHealthyTesting();
            }
        });

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
                toast(R.string.device_disconnected);
                return;
            } else if (mWristbandManager.isSyncingData()) {
                toast(R.string.device_sync_data_busy);
                return;
            } else if (mWristbandConfig == null) {
                toast("mWristbandConfig=null");
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
                toast("healthyType=0");
                return;
            }
            //开始测量
            mTestingHealthyDisposable = mWristbandManager
                    .openHealthyRealTimeData(healthyType)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {
                            mBtnTestHealthy.setText(R.string.real_time_data_stop);
                        }
                    })
                    .doOnTerminate(new Action() {
                        @Override
                        public void run() throws Exception {
                            mBtnTestHealthy.setText(R.string.real_time_data_start);
                        }
                    })
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {
                            mBtnTestHealthy.setText(R.string.real_time_data_start);
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
                            mTvHeartRate.setText("Heart Rate : "+heartRate);
                            mTvOxygen.setText("Blood Oxygen Saturation : "+bloodOxygen);
                            mTvBloodPressure.setText("Blood Pressure : "+diastolicBP+" "+systolicBP);
                            //mTvRespiratoryRate.setText("Respiratory Rate : "+bodyTemp);
                            mTvBodyTemperature.setText("Body Temperature : "+bodyTemp);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.w("RealTimeData", "RealTimeData", throwable);
                        }
                    });
        }
    }

    /*
    private Disposable mTestingEcgDisposable;
    private EcgData mEcgData;

    private void toggleEcgTesting() {
        if (mTestingEcgDisposable != null && !mTestingEcgDisposable.isDisposed()) {
            //结束测量
            mTestingEcgDisposable.dispose();
        } else {
            if (!mWristbandManager.isConnected()) {
                toast(R.string.device_disconnected);
                return;
            } else if (mWristbandManager.isSyncingData()) {
                toast(R.string.device_sync_data_busy);
                return;
            } else if (mWristbandConfig == null) {
                toast("mWristbandConfig=null");
                return;
            } else if (!mWristbandConfig.getWristbandVersion().isEcgEnabled()) {
                toast("isEcgEnabled=false");
                return;
            }
            mEcgData = null;
            //开始测量
            mTestingEcgDisposable = mWristbandManager
                    .openEcgRealTimeData()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {
                            mBtnTestEcg.setText(R.string.real_time_data_stop);
                        }
                    })
                    .doOnTerminate(new Action() {
                        @Override
                        public void run() throws Exception {
                            mBtnTestEcg.setText(R.string.real_time_data_start);
                        }
                    })
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {
                            mBtnTestEcg.setText(R.string.real_time_data_start);
                        }
                    })
                    .subscribe(new Consumer<int[]>() {
                        @Override
                        public void accept(int[] ints) throws Exception {
                            if (mEcgData == null) {//This is the first packet
                                mEcgData = new EcgData();
                                mEcgData.setItems(new ArrayList<Integer>(1000));
                                if (ints.length == 1) {//Sample packet
                                    mEcgData.setSample(ints[0]);
                                    mTvEcgSample.setText(getString(R.string.ecg_sample, mEcgData.getSample()));
                                } else {//Error packet, may be lost the sample packet.
                                    mEcgData.setSample(EcgData.DEFAULT_SAMPLE);//Set a default sample
                                    mEcgData.getItems().addAll(intsAsList(ints));//Add this ecg data

                                    mTvEcgSample.setText(getString(R.string.ecg_sample, mEcgData.getSample()));
                                    mTvEcgValue.setText(getString(R.string.ecg_value, Arrays.toString(ints)));
                                }
                            } else {
                                mEcgData.getItems().addAll(intsAsList(ints));//Add this ecg data
                                mTvEcgValue.setText(getString(R.string.ecg_value, Arrays.toString(ints)));
                            }

                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.w("RealTimeData", "RealTimeData", throwable);
                        }
                    });
        }
    }*/

    private static List<Integer> intsAsList(int[] values) {
        List<Integer> list = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
        }
        return list;
    }
}
