package com.gh.android.settlementmonitoringsystem;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CdkeyActivity extends AppCompatActivity {

    private static final int SHOW_RESPONSE = 0;

    private static final String TAG = "CdkeyActivity";

    private static final String DeviceID = "3420157";
    private static final String ApiKey = "=bpjjea4wgLMd2xKVti6=DTw0mI=";
    private static final String DataStream = "CDKEY";

    private EditText mEditText;
    private Button mButtonConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cdkey);

        mEditText = (EditText) findViewById(R.id.edit_text);

        mButtonConfirm = (Button) findViewById(R.id.button_confirm);
        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://api.heclouds.com/devices/" + DeviceID + "/datastreams/" + DataStream);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15 * 1000);
                    conn.setRequestProperty("api-key", ApiKey);
                    if (conn.getResponseCode() == 200) {  //返回码是200，网络正常
                        InputStream in = conn.getInputStream();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        String response1;
                        long response;
                        int len = 0;
                        byte buffer[] = new byte[1024];
                        while ((len = in.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        in.close();
                        os.close();
                        response1 = os.toString();
                        Log.i(TAG, response1);
                        JSONObject jsonObject = new JSONObject(os.toString());
                        response = jsonObject.getJSONObject("data").getLong("current_value");
                        Log.i(TAG, "" + response);
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = response;
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
                    String input = mEditText.getText().toString();
                    if (input.equals("")) {
                        Toast.makeText(CdkeyActivity.this, "请输入有效CDKEY", Toast.LENGTH_SHORT).show();
                    } else if (input.equals("" + msg.obj)) {
                        Toast.makeText(CdkeyActivity.this, "校验成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CdkeyActivity.this, "校验失败！", Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };
}
