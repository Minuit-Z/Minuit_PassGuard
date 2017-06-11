package ziye.minuit_passguard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import constants.Constants;
import fragments.HomeFragment;
import fragments.SettingsFragment;
import objects.Passwords;
import utils.DBOperation;
import utils.SPHelper;


public class MainActivity extends AppCompatActivity {
    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    FragmentManager fragmentManager;
    NavigationView navigationView;
    FrameLayout frameLayout;
    ProgressBar pb;
    //用来更新对话框的handler
    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {


        }
    };
    private final String TAG = "LOG";

    private String MyPass; //使用密码过程中的key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyPass = getIntent().getStringExtra("key");
        fragmentManager = getSupportFragmentManager();

        setupView();
        if (savedInstanceState == null) showHome();
    }

    private void setupView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frameLayout = (FrameLayout) findViewById(R.id.content_frame);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });
    }

    private void showHome() {
        selectDrawerItem(navigationView.getMenu().getItem(0));
//        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void selectDrawerItem(MenuItem menuItem) {
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.drawer_home:
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.menu_add);
                fragmentClass = HomeFragment.class;
                break;
            case R.id.drawer_settings:
                fragmentClass = SettingsFragment.class;
                break;
            //数据同步对话框
            case R.id.drawer_favorites:
                showSyncDialog();
            default:
                fragmentClass = HomeFragment.class;
                break;
        }

        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }


    /**
     * @description 数据同步操作
     * @author 张子扬
     * create at 2017/4/23 0023 19:45
     */
    private void showSyncDialog() {
        View v = View.inflate(this, R.layout.dialog_sync, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("数据同步");
        builder.setView(v);
        AlertDialog dialog = builder.show();

        v.findViewById(R.id.img_upload).setOnClickListener((View) -> {
            //上传
            dialog.dismiss();
            doUpLoad();

        });

        v.findViewById(R.id.img_download).setOnClickListener((View) -> {
            //下载
            dialog.dismiss();
            doDownLoad();
        });
    }


    /**
     * @description 上传本地数据到云端
     * @author 张子扬
     * create at 2017/4/25 0025 14:07
     */
    private void doUpLoad() {
        //取出所有数据
        List lists = DBOperation.getInstance(this).queryAll();
        for (Object p:lists){
            ((Passwords)p).setUsername((String) SPHelper.getInstance(MainActivity.this).getParam(Constants.UserName,""));
        }
        //准备对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("上传中");
        View v = View.inflate(this, R.layout.dialog_sync_progress, null);
        builder.setView(v);
        AlertDialog dialog = builder.show();

        new BmobBatch().insertBatch(lists).doBatch(new QueryListListener<BatchResult>() {
            @Override
            public void done(List<BatchResult> list, BmobException e) {
                if (e == null) {
                    for (int i = 0; i < list.size(); i++) {
                        BatchResult result = list.get(i);
                        BmobException ex = result.getError();
                        float j = (i + 1f) / (list.size() + 1f);

                        if (j > 0.8) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "上传完成", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }


    /**
     * @description 从云端下载数据
     * @author 张子扬
     * create at 2017/4/23 0023 19:56
     */
    private void doDownLoad() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("下载中");
        View v = View.inflate(this, R.layout.dialog_sync_progress, null);
        builder.setView(v);
        AlertDialog dialog = builder.show();

        BmobQuery<Passwords> query = new BmobQuery<>();
        query.addWhereEqualTo("username", SPHelper.getInstance(this).getParam(Constants.UserName, ""));
        query.findObjects(new FindListener<Passwords>() {
            @Override
            public void done(List<Passwords> list, BmobException e) {
                if (e == null) {
                    //已经查询到数据，可以开始填充数据库
                    DBOperation.getInstance(MainActivity.this).wipeData();
                    for (Passwords p : list) {
                        //此时的数据从网络端直接获取，已经为加密后的数据
                        try {
                            DBOperation.getInstance(MainActivity.this).insert(true, p.getAccountDesc(), p.getAccountName(), p.getAccountPass());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "同步完成", Toast.LENGTH_SHORT).show();
                    showHome();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_addpass:
                //新增密码
                Intent i = new Intent(this, AddPassActivity.class);
                startActivity(i);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        drawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent i=new Intent(MainActivity.this,LockActivity.class);
        i.putExtra("from","MainActivity");
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder buidler=new AlertDialog.Builder(this);
        buidler.setTitle("提示");
        buidler.setMessage("确定要退出么");
        buidler.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        buidler.setNegativeButton("取消",null);
        buidler.show();
    }
}
