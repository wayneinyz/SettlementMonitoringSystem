package com.gh.android.settlementmonitoringsystem.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gh.android.settlementmonitoringsystem.MyApplication;
import com.gh.android.settlementmonitoringsystem.R;
import com.gh.android.settlementmonitoringsystem.model.Device;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SHOW_RESPONSE = 0;
    private static final String ApiKey = "=bpjjea4wgLMd2xKVti6=DTw0mI=";

    private Button mButtonFreshList;
    private RecyclerView mRecyclerView;
    private Button mButtonReferenceTime;

    Date date;
    private String hour = "";
    private String minute = "";

    private MyApplication app;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_to_cdkey:
                        Intent intent = new Intent(MainActivity.this, CdkeyActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mButtonFreshList = (Button) findViewById(R.id.button_refresh_list);
        mButtonFreshList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 网络请求
                sendRequest();
            }
        });

        mButtonReferenceTime = (Button) findViewById(R.id.button_reference_time);
        Date mDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        date = MyApplication.getNextDay(mDate);
        String str = sdf.format(date);
        mButtonReferenceTime.setText(str);
        mButtonReferenceTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog();
            }
        });

        app = (MyApplication) getApplication();
        app.setRefDate((String) mButtonReferenceTime.getText());

        // 网络请求
        sendRequest();
    }

    private void showTimeDialog()
    {
        View view = getLayoutInflater().inflate(R.layout.dialog_activity_main, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setIs24HourView(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("参考时间设置");
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setTime(datePicker,timePicker);
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setTime(DatePicker datePicker, TimePicker timePicker)
    {
        String year = datePicker.getYear() + "";
        String month = datePicker.getMonth() + 1 + "";
        String day = datePicker.getDayOfMonth() + "";
        hour = timePicker.getCurrentHour() + "";
        minute = timePicker.getCurrentMinute() + "";
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfHour) {
                hour = hourOfDay + "";
                minute = minuteOfHour + "";
            }
        });

        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 1) {
            minute = "0" + minute;
        }

        String timeString = year + "年" + month + "月" + day + "日 " + hour+ ":" + minute;
        mButtonReferenceTime.setText(timeString);
        app.setRefDate(timeString);
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Device tag);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private OnRecyclerViewItemClickListener mOnItemClickListener = null;

        private List<Device> mDatas;

        public MyAdapter(List<Device> datas) {
            mDatas = datas;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_main, parent, false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, (Device) v.getTag());
                    }
                }
            });

            MyViewHolder vh = new MyViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position)
        {
            Device device = mDatas.get(position);
            String title = device.getTitle();

            holder.textView.setText(title);
            holder.itemView.setTag(device);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        public void setDevices(List<Device> devices) {
            mDatas = devices;
        }

        public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public MyViewHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.id_num);
            }
        }
    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn;
                try {
                    URL url = new URL("http://api.heclouds.com/devices");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15 * 1000);
                    conn.setRequestProperty("api-key", ApiKey);
                    if (conn.getResponseCode() == 200) {  //返回码是200，网络正常
                        InputStream in = conn.getInputStream();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        String response1, response;
                        int len;
                        byte buffer[] = new byte[1024];
                        while ((len = in.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        in.close();
                        os.close();
                        response1 = os.toString();
//                        Log.i(TAG, response1);

                        ArrayList<Device> list = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response1);
                        response = jsonObject.getJSONObject("data").getString("devices");
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String title = jsonObject1.getString("title");
                            String id = jsonObject1.getString("id");

                            Device device = new Device();
                            device.setId(id);
                            device.setTitle(title);

                            if (!title.equals("CDKEY")) {
                                list.add(device);
                            }
                        }
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    ArrayList<Device> list = (ArrayList<Device>) msg.obj;
                    mAdapter = new MyAdapter(list);
                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, Device data) {
                            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
                            intent.putExtra("data", data);
                            startActivity(intent);
                        }
                    });
            }
        }
    };

}
