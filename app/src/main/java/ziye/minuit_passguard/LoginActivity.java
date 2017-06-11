package ziye.minuit_passguard;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import constants.Constants;
import constants.MyApplication;
import objects.User;
import utils.AESUtil;
import utils.DBHelper;
import utils.DBOperation;
import utils.SPHelper;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class LoginActivity extends AppCompatActivity {

    private EditText et_account, et_password;
    private Button btn_login, btn_register;
    private LinearLayout ll_root;
    String pass;
    String name;
    String passByEncrypt;
    String nameByEncrypt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();

        initEvents();
    }

    private void initEvents() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et_account.getText().toString().trim();
                pass = et_password.getText().toString().trim();
                try {
                    //账号密码使用AES
                    passByEncrypt = AESUtil.encrypt("123", pass);
                    nameByEncrypt = AESUtil.encrypt("123", name);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Check4Login(nameByEncrypt, passByEncrypt);
            }
        });
    }

    /**
     * 验证登录
     *
     * @param nameByEncrypt
     * @param passByEncrypt
     */
    private void Check4Login(final String nameByEncrypt, final String passByEncrypt) {
        Snackbar.make(ll_root, "登陆中", Snackbar.LENGTH_SHORT).show();
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("username", nameByEncrypt);
        query.addWhereEqualTo("userpass", passByEncrypt);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    //查询到数据
                    if (list.get(0).getUsername().equals(nameByEncrypt) && list.get(0).getUserpass().equals(passByEncrypt)) {
                        //把用户的名字加入到SP中
                        SPHelper.getInstance(LoginActivity.this).setParam(Constants.UserName, name);
                        //用户的加密后的密码存入SP，用来离线登录
                        SPHelper.getInstance(LoginActivity.this).setParam(Constants.UserPass, passByEncrypt);
                        //把密码明文作为解密的key记录在Application中
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);

                        //生成秘钥
                        char[] array = passByEncrypt.toCharArray();
                        StringBuilder builder = new StringBuilder();
                        for (int s = 0; s < array.length; s = s + 3)
                            builder.append(array[s]);

                        Log.e("LOG ", "done: " + builder.toString());
                        MyApplication.pass = builder.toString();
                        startActivity(i);
                        finish();
                    } else {
                        //无账户
                        et_account.setError("请检查账号密码");
                        et_password.setText("");
                        countAdd();
                    }
                } else {
                    countAdd();
                    et_password.setError("用户信息有误");
                    Log.e("TAG", e.toString() + "adasdasd" + passByEncrypt);
                }
            }
        });
    }

    private void initData() {
        String name = (String) SPHelper.getInstance(this).getParam(Constants.UserName, "");
        if (!name.equals("")) {
            et_account.setText(name);
        }
    }


    private void initView() {
        btn_login = (Button) findViewById(R.id.btn_sign_in);
        btn_register = (Button) findViewById(R.id.register);
        et_account = (EditText) findViewById(R.id.et_account);
        et_password = (EditText) findViewById(R.id.et_password);
        ll_root = (LinearLayout) findViewById(R.id.login_root);
    }

    private void countAdd() {
        int count = (int) SPHelper.getInstance(this).getParam(SPHelper.ERROR_TIME, 0);
        if (count > 3) {
            DBOperation.getInstance(this).wipeData();
        } else {
            SPHelper.getInstance(this).setParam(SPHelper.ERROR_TIME, count + 1);
        }
    }
}
