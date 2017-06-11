package constants;

import android.app.Application;

import cn.bmob.v3.Bmob;

/**
 * Created by Administrator on 2017/3/12 0012.
 */

public class MyApplication extends Application{

    //AES加密的秘钥
    public static String pass;

    @Override
    public void onCreate() {
        super.onCreate();

        Bmob.initialize(getApplicationContext(),"31f947c54b170a8842e0b7a21f3226a0");
    }
}
