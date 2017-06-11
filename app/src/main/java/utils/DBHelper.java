package utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import constants.Constants;

/**
 * Created by Administrator on 2017/3/31 0031.
 */

public class DBHelper extends SQLiteOpenHelper{

    private static String name= Constants.DB_NAME;
    private static int version=Constants.DB_VERSION;

    public DBHelper(Context context){
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table "+name+" (id integer primary key autoincrement," +
                "AccountDesc varchar(64)," + //账户描述
                "AccountName varchar(64)," + //账户名
                "AccountPass varchar(64))";  //账户密码
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
