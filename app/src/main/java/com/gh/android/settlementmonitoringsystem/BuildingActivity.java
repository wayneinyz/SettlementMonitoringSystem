package com.gh.android.settlementmonitoringsystem;

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

public class BuildingActivity extends AppCompatActivity {

    private static final String TAG = "BuildingActivity";
    private static final String ApiKey = "=bpjjea4wgLMd2xKVti6=DTw0mI=";
    private static final int SHOW_RESPONSE = 0;

    private TextView mTextView;
    private TextView mTextViewReference;
    private TextView mTextViewCollect;
    private Button mButtonRefreshData;
    private RecyclerView mRecyclerViewBuilding;

    private List<String> mDatas;
    private String device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_building);

        mTextView = (TextView) findViewById(R.id.data_text_view);
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        String title = data.substring(0, 7);
        String id = data.substring(7, 14);
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
        void onItemClick(View view, String tag);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private OnRecyclerViewItemClickListener mOnItemClickListener = null;

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_building, parent, false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, (String) v.getTag());
                    }
                }
            });

            MyViewHolder vh = new MyViewHolder(view);
            return vh;
        }


        @Override
        public void onBindViewHolder(MyViewHolder holder, int position)
        {
            String str = mDatas.get(position);
            String id = str.substring(0, 4);
            String value = str.substring(11, 16);
            holder.mTextView.setText(id);
            holder.mText1.setText(value);
//            holder.mText2.setText(id);
            holder.itemView.setTag(str);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
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
                        String response1, response;
                        int len;
                        byte buffer[] = new byte[1024];
                        while ((len = in.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        in.close();
                        os.close();
                        response1 = os.toString();

                        ArrayList<String> list = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response1);
                        response = jsonObject.getString("data");
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String id  = jsonObject1.getString("id");
                            String time = jsonObject1.getString("update_at");
                            Double value = jsonObject1.getDouble("current_value");
                            String data = id + device + value + time;
                            list.add(data);
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
                    ArrayList<String> list = (ArrayList<String>) msg.obj;
                    mDatas = list;
                    MyAdapter adapter = new MyAdapter();
                    mRecyclerViewBuilding.setAdapter(adapter);
                    adapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, String data) {
                            Intent intent = new Intent(BuildingActivity.this, LineDrawActivity.class);
                            intent.putExtra("data", data);
                            startActivity(intent);
                        }
                    });
            }
        }
    };

}
