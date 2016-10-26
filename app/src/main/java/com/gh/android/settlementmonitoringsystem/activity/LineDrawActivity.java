package com.gh.android.settlementmonitoringsystem.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gh.android.settlementmonitoringsystem.MyApplication;
import com.gh.android.settlementmonitoringsystem.R;
import com.gh.android.settlementmonitoringsystem.model.Sensor;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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

public class LineDrawActivity extends AppCompatActivity {

    private static final String TAG = "LineDrawActivity";
    private static final String ApiKey = "=bpjjea4wgLMd2xKVti6=DTw0mI=";
    private static final int SHOW_RESPONSE = 0;

    private Button mButtonRefTime;
    private Button mButtonColTime;
    private Button mButtonRefresh;
    private TextView mTextView;
    private LineChart mLineChart;
    private TextView mTextView1;
    private TextView mTextView2;

    private String hour = "";
    private String minute = "";

    private String device;
    private String id;
    private String start, end;

    ArrayList<Float> values = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_line_draw);

        Intent intent = getIntent();
        Sensor sensor = (Sensor) intent.getSerializableExtra("data");
        id = sensor.getId();
        device = intent.getStringExtra("device");
        double value = sensor.getCurrentValue();
        String time = sensor.getUpdateAt();
        float settleValue = sensor.getSettleValue();

        mButtonRefTime = (Button) findViewById(R.id.button_ref_time);
        MyApplication app = (MyApplication) getApplication();
        String dateRef = app.getRefDate();
        mButtonRefTime.setText(dateRef);
        mButtonRefTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog("参考时间设置", mButtonRefTime);
            }
        });
        //参考时间点
        start = timeTrans(dateRef);

        mButtonColTime = (Button) findViewById(R.id.button_col_time);
        String dateCol = app.getColDate();
        mButtonColTime.setText(dateCol);
        mButtonColTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog("采集时间设置", mButtonColTime);
            }
        });
        //采集时间点
        end = timeTrans(dateCol);

        // 显示格式化日期
        Date date;
        String time1 = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time);
            time1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTextView = (TextView) findViewById(R.id.text_refresh);
        mTextView.setText(time1);

        //观测值
        mTextView1 = (TextView) findViewById(R.id.textview1);
        mTextView1.setText(value + "mm");

        //沉降值
        mTextView2 = (TextView) findViewById(R.id.textview2);
        mTextView2.setText(settleValue + "mm");

        mLineChart = (LineChart) findViewById(R.id.line_chart);

        //刷新网络请求
        mButtonRefresh = (Button) findViewById(R.id.button_refresh);
        mButtonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 网络请求
                sendRequest(start, end);
            }
        });

        // 网络请求
        sendRequest(start, end);
    }

    private String timeTrans(String str) {
        String s1 = str.replace('年', '-');
        String s2 = s1.replace('月', '-');
        String s3 = s2.replace('日', 'T');
        String s4 = s3.replaceAll(" ", "");
        return s4;
    }

    private void setChart(int count, ArrayList<String> times, ArrayList<Float> values) {
        mLineChart.setDrawBorders(false);  //是否在折线图上添加边框

        // no description text
        mLineChart.setDescription("沉降监测折线图");// 数据描述
        // 如果没有数据的时候，会显示这个，类似listview的emtpyview
        mLineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable / disable grid background
        mLineChart.setDrawGridBackground(false); // 是否显示表格颜色
        mLineChart.setGridBackgroundColor(Color.WHITE & 0x70FFFFFF); // 表格的的颜色，在这里是是给颜色设置一个透明度

        // enable touch gestures
        mLineChart.setTouchEnabled(true); // 设置是否可以触摸

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);// 是否可以拖拽
        mLineChart.setScaleEnabled(true);// 是否可以缩放

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);

//        mLineChart.setBackgroundColor(Color.rgb(114, 188, 223));// 设置背景

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // add data
        setData(count, times, values);

        mLineChart.animateX(0); // 立即执行的动画,x轴

        // get the legend (only possible after setting data)
        Legend mLegend = mLineChart.getLegend(); // 设置比例图标示，就是那个一组y的value的

        // modify the legend ...
        mLegend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        mLegend.setForm(Legend.LegendForm.CIRCLE);// 样式
        mLegend.setFormSize(6f);// 字体
        mLegend.setTextColor(Color.rgb(239, 154, 72));// 颜色
    }

    private void setData(int count, ArrayList<String> times, ArrayList<Float> values) {
        // x轴的数据
        ArrayList<String> x = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            x.add(times.get(i));
        }

        // y轴的数据
        ArrayList<Entry> y = new ArrayList<>();
        for (int i = 0; i < count; i++) {
//            Log.i(TAG, values.get(i) + "a");
            Entry entry = new Entry(values.get(i), i);
            y.add(entry);
        }

        // y轴的数据集
        LineDataSet set = new LineDataSet(y, id);
        set.setDrawValues(true);// 是否在点上绘制Value
        set.setLineWidth(2.0f);
        set.setColor(Color.rgb(239, 154, 72));// 绿色
        set.setCircleColor(Color.rgb(50,205,50));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(10f);
        set.setHighlightEnabled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        LineData data = new LineData(x, dataSets);
        mLineChart.setData(data);
    }

    private void showTimeDialog(String title, final Button button)
    {
        View view = getLayoutInflater().inflate(R.layout.dialog_activity_line_draw, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker_1);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker_1);
        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setIs24HourView(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setTime(datePicker, timePicker, button);
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setTime(DatePicker datePicker, TimePicker timePicker, Button button)
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
        button.setText(timeString);
        if (button == mButtonRefTime) {
            start = timeTrans(timeString);
        } else {
            end = timeTrans(timeString);
        }
    }

    private void sendRequest(final String start, final String end) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn;
                try {
                    URL url = new URL("http://api.heclouds.com/devices/" + device + "/datapoints"
                                    + "?datastream_id=" + id + "&start=" + start + "&end=" + end);
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

    private void parseJSONObject(String response1) {
        ArrayList<Sensor> list = new ArrayList<>();
        int count1 = 0;
        try {
            JSONObject jsonObject1 = new JSONObject(response1);
            String response2 = jsonObject1.getJSONObject("data").getString("datastreams");
            Integer count = jsonObject1.getJSONObject("data").getInt("count");
            count1  = count.intValue();

            JSONArray jsonArray1 = new JSONArray(response2);
            for (int i = 0; i < jsonArray1.length(); i++) {
                JSONObject jsonObject2 = jsonArray1.getJSONObject(i);
                String response3 = jsonObject2.getString("datapoints");

                JSONArray jsonArray2 = new JSONArray(response3);
                for (int j = 0; j < jsonArray2.length(); j++) {
                    JSONObject jsonObject3 = jsonArray2.getJSONObject(j);
                    String time = jsonObject3.getString("at");
                    String time1 = time.substring(0, 16);
                    Double value = jsonObject3.getDouble("value");

                    Sensor sensor = new Sensor();
                    sensor.setCurrentValue(value);
                    sensor.setUpdateAt(time1);
                    list.add(sensor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Message message = new Message();
        message.what = SHOW_RESPONSE;
        message.arg1 = count1;
        message.obj = list;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    ArrayList<Sensor> list = (ArrayList<Sensor>) msg.obj;
                    int count = msg.arg1;

                    for (int i = 0; i < list.size(); i++) {
                        double value = list.get(i).getCurrentValue();
                        Float value1 = new Float(value);
                        values.add(value1);

                        String time = list.get(i).getUpdateAt();
                        times.add(time);
                    }

                    setChart(count, times, values);
            }
        }
    };

}
