package ziye.minuit_passguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import constants.MyApplication;
import utils.AESUtil;
import utils.MD5Util;

public class LockActivity extends AppCompatActivity {


    private TextView tv_check;
    private EditText et_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        intiView();

        tv_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = et_check.getText().toString();
                char[] chars = new char[0];
                try {
                    chars = AESUtil.encrypt("123", msg).toCharArray();

                    StringBuilder builder = new StringBuilder();
                    for (int s = 0; s < chars.length; s = s + 3)
                        builder.append(chars[s]);

                    if (MyApplication.pass.equals(builder.toString())) {
                        //验证完成

                        tv_check.setText("UnLocked");
                        String from = getIntent().getStringExtra("from");
                        Intent intent = new Intent();

                        intent.setClassName(LockActivity.this, getPackageName() + "." + from);
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void intiView() {
        tv_check = (TextView) findViewById(R.id.tv_check);
        et_check = (EditText) findViewById(R.id.et_check);
    }

    @Override
    public void onBackPressed() {
    }
}
