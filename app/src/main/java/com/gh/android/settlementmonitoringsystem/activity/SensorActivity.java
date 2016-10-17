package com.gh.android.settlementmonitoringsystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.gh.android.settlementmonitoringsystem.MyApplication;
import com.gh.android.settlementmonitoringsystem.R;
import com.gh.android.settlementmonitoringsystem.model.Device;
import com.gh.android.settlementmonitoringsystem.model.Sensor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SensorActivity extends AppCompatActivity {

    private static final String TAG = "SensorActivity";
    private static final String ApiKey = "=bpjjea4wgLMd2xKVti6=DTw0mI=";
    private static final int SHOW_RESPONSE = 0;
    private static final int SHOW_RESPONSE_1 = 1;

    private TextView mTextView;
    private TextView mTextViewReference;
    private TextView mTextViewCollect;
    private Button mButtonRefreshData;
    private RecyclerView mRecyclerViewBuilding;
    private MyAdapter mAdapter;

    private String device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sensor);

        mTextView = (TextView) findViewById(R.id.data_text_view);
        Intent intent = getIntent();
        Device device1 = (Device) intent.getSerializableExtra("data");
        String title = device1.getTitle();
        String id = device1.getId();
        device = id;
        mTextView.setText(title + "  数据");

        MyApplication app = (MyApplication) getApplication();
        String dateRef = app.getRefDate();
        mTextViewReference = (TextView) findViewById(R.id.text_view_reference);
        mTextViewReference.setText(dateRef);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        Date mDate = new Date();
        String str = sdf.format(mDate);
        mTextViewCollect = (TextView) findViewById(R.id.text_view_collect);
        mTextViewCollect.setText(str);
        app.setColDate(str);

        //网络请求
        sendRequest();

        mButtonRefreshData = (Button) findViewById(R.id.button_reference_data);
        mButtonRefreshData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date mDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                String str = sdf.format(mDate);
                mTextViewCollect.setText(str);

                // 网络请求
                sendRequest();
            }
        });

        mRecyclerViewBuilding = (RecyclerView) findViewById(R.id.building_recycler_view);
        mRecyclerViewBuilding.setLayoutManager(new LinearLayoutManager(this));
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Sensor tag);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private OnRecyclerViewItemClickListener mOnItemClickListener = null;

        private List<Sensor> mDatas1;
        private List<Sensor> mDatas2;

        public MyAdapter(List<Sensor> datas1, List<Sensor> datas2) {
            mDatas1 = datas1;
            mDatas2 = datas2;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_sensor, parent, false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, (Sensor) v.getTag());
                    }
                }
            });

            MyViewHolder vh = new MyViewHolder(view);
            return vh;
        }


        @Override
        public void onBindViewHolder(MyViewHolder holder, int position)
        {
            Sensor sensor1 = mDatas1.get(position);
            Sensor sensor2 = mDatas2.get(position);
            String id = sensor2.getId();
            double value = sensor1.getCurrentValue();

            holder.mTextView.setText(id);
            holder.mText1.setText(value + "mm");
            holder.mText2.setText(value + "mm");
            holder.itemView.setTag(sensor1);
        }

        @Override
        public int getItemCount() {
            return mDatas2.size();
        }

        public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView mTextView;
            private TextView mText1;
            private TextView mText2;

            public MyViewHolder(View view) {
                super(view);
                mTextView = (TextView) view.findViewById(R.id.text_item);
                mText1 = (TextView) view.findViewById(R.id.text1);
                mText2 = (TextView) view.findViewById(R.id.text2);
            }
        }

    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn;
                try {
                    URL url = new URL("http://api.heclouds.com/devices/" + device + "/datastreams");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15 * 1000);
                    conn.setRequestProperty("api-key", ApiKey);
                    if (conn.getResponseCode() == 200) {  //返回码是200，网络正常
                        InputStream in = conn.getInputStream();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        String response1;
                        int len;
                        byte buffer[] = new byte[1024];
                        while ((len = in.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        in.close();
                        os.close();
                        response1 = os.toString();

                        //解析
                        parseJSONObject(response1);

                    }else {
                        //返回码不是200，网络异常
                    }
                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendRequest(final String id, final String start) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn;
                try {
                    URL url = new URL("http://api.heclouds.com/devices/" + device + "/datapoints"
                            + "?datastream_id=" + id + "&start=" + start);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15 * 1000);
                    conn.setRequestProperty("api-key", ApiKey);
                    if (conn.getResponseCode() == 200) {  //返回码是200，网络正常
                        InputStream in = conn.getInputStream();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        int len;
                        byte buffer[] = new byte[1024];
                        while ((len = in.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        in.close();
                        os.close();
                        String response1 = os.toString();
//                        Log.i(TAG, response1);

                        JSONObject jsonObject1 = new JSONObject(response1);
                        String response2 = jsonObject1.getJSONObject("data").getString("datastreams");
                        Integer count = jsonObject1.getJSONObject("data").getInt("count");
                        int count1  = count.intValue();
//                        Log.i(TAG, response2);

                        ArrayList<Sensor> list = new ArrayList<>();

                        JSONArray jsonArray1 = new JSONArray(response2);
                        for (int i = 0; i < jsonArray1.length(); i++) {
                            JSONObject jsonObject2 = jsonArray1.getJSONObject(i);
                            String response3 = jsonObject2.getString("datapoints");
//                            Log.i(TAG, response3);

                            JSONArray jsonArray2 = new JSONArray(response3);
                            for (int j = 0; j < jsonArray2.length(); j++) {
                                JSONObject jsonObject3 = jsonArray2.getJSONObject(j);
                                String time = jsonObject3.getString("at");
                                String time1 = time.substring(0, 16);
                                Double value = jsonObject3.getDouble("value");
                                double value1 = value.doubleValue();

                                Sensor sensor = new Sensor();
                                sensor.setCurrentValue(value1);
                                sensor.setUpdateAt(time1);
                                list.add(sensor);
                            }
                        }

                        Message message = new Message();
                        message.what = SHOW_RESPONSE_1;
                        message.arg1 = count1;
                        message.obj = list;
                        mHandler.sendMessage(message);
                    }else {
                        //返回码不是200，网络异常
                    }
                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONObject(String response1) {
        String response;
        ArrayList<Sensor> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response1);
            response = jsonObject.getString("data");
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String id  = jsonObject1.getString("id");
                String time = jsonObject1.getString("update_at");
                Double value = jsonObject1.getDouble("current_value");
                double value1 = value.doubleValue();
                String time1 = jsonObject1.getString("create_time");

                Sensor sensor = new Sensor();
                sensor.setId(id);
                sensor.setUpdateAt(time);
                sensor.setCurrentValue(value1);
                sensor.setCreateTime(time1);

                list.add(sensor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Message message = new Message();
        message.what = SHOW_RESPONSE;
        message.obj = list;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    ArrayList<Sensor> list = (ArrayList<Sensor>) msg.obj;
                    ArrayList<Sensor> list1 = new ArrayList<>();
                    ArrayList<Sensor> list2 = new ArrayList<>();

                    for (int i = 0; i < list.size(); i++) {
                        String time = list.get(i).getCreateTime();
                        String time1 = time.substring(0, 16);
                        String time2 = time1.replaceAll(" ", "T");
//                        Log.i(TAG, time2);
                    }

                    //判断是否包含汉字
                    for (int i = 0; i < list.size(); i++) {
                        Sensor sensor = list.get(i);
                        String id = sensor.getId();
                        if (id.getBytes().length == id.length()) {
                            list1.add(sensor);
                        } else {
                            list2.add(sensor);
                        }
                    }

                    mAdapter = new MyAdapter(list1, list2);
                    mRecyclerViewBuilding.setAdapter(mAdapter);
                    mAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, Sensor data) {
                            Intent intent = new Intent(SensorActivity.this, LineDrawActivity.class);
                            intent.putExtra("data", data);
                            intent.putExtra("device", device);
                            startActivity(intent);
                        }
                    });
            }
        }
    };

}
