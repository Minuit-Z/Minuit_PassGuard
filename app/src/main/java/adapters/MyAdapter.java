package adapters;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import constants.MyApplication;
import objects.PassDesc;
import objects.Passwords;
import utils.AESUtil;
import utils.DBOperation;
import ziye.minuit_passguard.R;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/3/12 0012.
 */

public class MyAdapter extends BaseAdapter {

    private Context context;
    private List<PassDesc> lists;
    private AlertDialog.Builder builder;


    public MyAdapter(Context context, List<PassDesc> lists) {
        this.context = context;
        this.lists = lists;
        builder = new AlertDialog.Builder(context);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_4_myadapter, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String currentWord = lists.get(position).getMyPinyin();
        Log.e(TAG, "getView: " + currentWord);
        //获取上一个字母的首字母
        if (position > 0) {
            String lastWord = lists.get(position - 1).getMyPinyin();
            //当前首字母和上一个字母比较
            if (currentWord.equals(lastWord)) {
                //首字母相同，隐藏当前item的TextView
                holder.tv_first.setVisibility(View.GONE);
            } else {
                //不一样，显示首字母
                holder.tv_first.setVisibility(View.VISIBLE);
                holder.tv_first.setText(currentWord);
            }
        } else {
            holder.tv_first.setVisibility(View.VISIBLE);
            holder.tv_first.setText(currentWord);
        }
        PassDesc p = lists.get(position);
        holder.tv_desc.setText(p.getName());

        holder.tv_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //单击事件
                showClickDialog(((TextView) v).getText().toString().trim());
            }
        });

        holder.tv_desc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongClickDialog(((TextView) v).getText().toString().trim());
                return true;
            }
        });
        return convertView;
    }


    /**
     * @description 长按事件的对话框，操作
     * @author 张子扬
     * create at 2017/4/23 0023 16:55
     */
    private void showLongClickDialog(final String desc) {
        builder.setTitle("操作");
        builder.setItems(new String[]{"修改", "删除"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //修改
                        showAlterDialog(desc);
                        break;
                    case 1:
                        //删除
                        showDeleteDialog(desc);
                        break;
                }
            }
        }).show();

    }


    /**
     * @description 修改数据
     * @author 张子扬
     * create at 2017/4/27 0027 14:14
     */
    private void showAlterDialog(String desc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("修改密码");
        View v = View.inflate(context, R.layout.dialog_alter, null);
        builder.setView(v);

        EditText et_name = (EditText) v.findViewById(R.id.et_alter_name);
        EditText et_pass = (EditText) v.findViewById(R.id.et_alter_pass);
        String name = et_name.getText().toString();
        String pass = et_pass.getText().toString();

        builder.setPositiveButton("本地修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alterPass(desc, 1, name, pass);
            }
        });

        builder.setNegativeButton("网络修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alterPass(desc, 2, name, pass);

            }
        });

        builder.show();
    }


    /**
     * @description 修改密码
     * @author 张子扬
     * create at 2017/4/27 0027 14:35
     */
    private void alterPass(String desc, int i, String name, String pass) {
        switch (i) {
            case 1:
                DBOperation.getInstance(context).update(desc, name, pass);
            case 2:
                Passwords p = new Passwords();
                p.setAccountDesc(desc);
                p.setAccountName(name);
                p.setAccountPass(pass);

                BmobQuery<Passwords> query = new BmobQuery<>();
                query.addWhereEqualTo("AccountPass", desc);
                query.findObjects(new FindListener<Passwords>() {
                    @Override
                    public void done(List<Passwords> list, BmobException e) {
                        if (e == null) {
                            try {
                                list.get(0).setAccountName(AESUtil.encrypt(MyApplication.pass, name));
                                list.get(0).setAccountDesc(desc);
                                list.get(0).setAccountPass(AESUtil.encrypt(MyApplication.pass, pass));

                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            list.get(0).update(list.get(0).getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e==null){
                                        Toast.makeText(context, "修改完成", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
                notifyDataSetChanged();
                break;
        }
    }


    /**
     * @description 删除时的提示对话框
     * @author 张子扬
     * create at 2017/4/23 0023 17:55
     */
    private void showDeleteDialog(final String desc) {
        builder.setTitle("确认要删除么");
        builder.setMessage("该删除操作会将本地数据和网络数据同时删除,数据物价,请谨慎操作");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //删除本地数据库的操作
                DBOperation.getInstance(context).delete(desc);
                //删除网络数据
                BmobQuery<Passwords> query = new BmobQuery<Passwords>();
                query.addWhereEqualTo("AccountDesc", desc);
                query.findObjects(new FindListener<Passwords>() {
                    @Override
                    public void done(final List<Passwords> list, BmobException e) {
                        if (e == null && list != null) {
                            final Passwords passwords = list.get(0);
                            passwords.delete(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        list.remove(passwords);
                                        notifyDataSetChanged();
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).show();

    }


    /**
     * @description 弹出单击事件时的对话框，复制
     * @author 张子扬
     * create at 2017/4/23 0023 16:08
     */
    private void showClickDialog(final String paramString) {
        builder.setItems(new String[]{"仅账号", "仅密码", "账号+密码"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result = "";
                switch (which) {
                    case 0:
                        //仅账号
                        result = DBOperation.getInstance(context).queryNameByDesc(paramString);
                        break;
                    case 1:
                        //仅密码
                        result = DBOperation.getInstance(context).queryPassByDesc(paramString);
                        break;
                    case 2:
                        //账号+密码
                        String account = DBOperation.getInstance(context).queryNameByDesc(paramString);
                        String pass = DBOperation.getInstance(context).queryPassByDesc(paramString);
                        result = account + "  " + pass;
                        break;
                }
                if (!"".equals(result)) {
                    //已被赋值
                    ClipboardManager localClipboardManager = (ClipboardManager) context.getSystemService(Service.CLIPBOARD_SERVICE);
                    localClipboardManager.setText(result);
                } else {
                    //TODO 数据异常
                    Log.e(TAG, "onClick: " + "异常");
                }
            }
        });
        builder.setTitle("复制  " + paramString + "  的内容：");
        builder.show();
    }

    static class ViewHolder {
        TextView tv_first, tv_desc;

        public ViewHolder(View convertView) {
            tv_first = (TextView) convertView.findViewById(R.id.tv_first_words_item);
            tv_desc = (TextView) convertView.findViewById(R.id.tv_desc_item);
        }
    }
}
