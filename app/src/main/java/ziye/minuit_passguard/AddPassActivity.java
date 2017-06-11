package ziye.minuit_passguard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.BoolRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import constants.Constants;
import constants.MyApplication;
import objects.Passwords;
import utils.AESUtil;
import utils.DBOperation;
import utils.RandomPassUtil;
import utils.SPHelper;

public class AddPassActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText et_desc, et_name, et_pass;
    private ImageView img_autoAdd;
    private Button btn_commit;
    private NumberPicker picker_type, picker_length;
    private Button btn_getpass;
    private ImageView img_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pass);

        initView();

        btn_commit.setOnClickListener((View) -> {
            if (checkInput()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提交方式");
                builder.setItems(new String[]{"仅本地提交", "同步到网络"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1:
                                Passwords p = new Passwords();
                                p.setUsername((String) SPHelper.getInstance(AddPassActivity.this).getParam(Constants.UserName, ""));
                                try {
                                    p.setAccountDesc(et_desc.getText().toString().trim());
                                    p.setAccountName(AESUtil.encrypt(MyApplication.pass, et_name.getText().toString().trim()));
                                    p.setAccountPass(AESUtil.encrypt(MyApplication.pass, et_pass.getText().toString().trim()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                p.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        if (e == null) {
                                            Toast.makeText(AddPassActivity.this, "网络同步完成", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            case 0:
                                try {
                                    DBOperation.getInstance(AddPassActivity.this).insert(false, et_desc.getText().toString().trim(),
                                            et_name.getText().toString().trim(), et_pass.getText().toString().trim());
                                    Toast.makeText(AddPassActivity.this, "本地提交完成", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                });
                builder.show();
            }
        });

        img_autoAdd.setOnClickListener((View) -> {
            showAutoPassDialog();
        });

        et_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()!=0){
                    img_edit.setVisibility(View.VISIBLE);
                }else {
                    img_edit.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        img_edit.setOnClickListener((View)->{
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            View v=View.inflate(this,R.layout.dialog_pass_edit,null);
            EditText et_old= (EditText) v.findViewById(R.id.et_old);
            EditText et_new= (EditText) v.findViewById(R.id.et_new);

            builder.setTitle("二次加密");
            builder.setView(v);
            builder.setPositiveButton("替换", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!(et_new.getText().toString().equals("")&&et_old.getText().toString().equals(""))){
                        et_pass.setText(et_pass.getText().toString().replace(et_old.getText().toString().trim(),et_new.getText().toString().trim()));
                    }
                }
            });

            builder.show();
        });
    }


    /**
     * @description 自动生成密码的对话框
     * @author 张子扬
     * create at 2017/4/26 0026 10:59
     */
    private void showAutoPassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("自动生成密码");
        View v = View.inflate(this, R.layout.dialog_auto_pass, null);
        builder.setView(v);
        AlertDialog dialog = builder.show();

        picker_type = (NumberPicker) v.findViewById(R.id.picker_type);
        picker_length = (NumberPicker) v.findViewById(R.id.picker_length);
        btn_getpass = (Button) v.findViewById(R.id.btn_getpass);

        picker_type.setDisplayedValues(new String[]{"数字+字符+字母", "仅数字", "仅字母", "字母+数字"});
        picker_type.setMinValue(0);
        picker_type.setMaxValue(3);
        picker_type.setValue(2);

        picker_length.setMaxValue(20);
        picker_length.setMinValue(1);
        picker_length.setValue(6);

        btn_getpass.setOnClickListener((View) -> {
            Toast.makeText(this, "密码类型：" + picker_type.getValue() + "  密码长度" + picker_length.getValue(), Toast.LENGTH_SHORT).show();
            et_pass.setText(new RandomPassUtil().getRandomPass(picker_type.getValue(), picker_length.getValue()));
            dialog.dismiss();
        });
    }


    /**
     * @description 表单完整性检查
     * @author 张子扬
     * create at 2017/4/26 0026 10:24
     */
    private Boolean checkInput() {

        if ("".equals(et_desc.getText().toString())) {
            et_desc.setError("必填项");
            et_desc.requestFocus();
            return false;
        }
        if ("".equals(et_name.getText().toString())) {
            et_name.setError("必填项");
            et_name.requestFocus();
            return false;
        }
        if ("".equals(et_pass.getText().toString())) {
            et_pass.setError("必填项");
            et_pass.requestFocus();
            return false;
        }
        return true;
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        et_desc = (EditText) findViewById(R.id.et_desc);
        et_name = (EditText) findViewById(R.id.et_name);
        et_pass = (EditText) findViewById(R.id.et_pass);
        img_autoAdd = (ImageView) findViewById(R.id.img_auto);
        btn_commit = (Button) findViewById(R.id.btn_commit);
        img_edit= (ImageView) findViewById(R.id.img_edit);

        setSupportActionBar(toolbar);
        toolbar.setTitle("新增密码");
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent i = new Intent(AddPassActivity.this, LockActivity.class);
        i.putExtra("from", "AddPassActivity");
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
