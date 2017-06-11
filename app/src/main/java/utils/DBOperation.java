package utils;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import objects.Passwords;
import constants.MyApplication;

/**
 * @author 张子扬
 * @description 数据库操作类
 * <p>
 * desc明文
 * pass密文
 * account密文
 */

public class DBOperation {
    private DBHelper helper;
    private Context context;

    public DBOperation(Context context) {
        this.context = context;
        this.helper = new DBHelper(context);
    }

    public static DBOperation getInstance(Context context) {
        return new DBOperation(context);
    }

    /**
     * @param isEncryped 是否被加密过
     * @param desc       明文
     * @param account    明文
     * @param pass       明文
     * @description 插入一条数据进入数据库
     * @author 张子扬
     * create at 2017/3/31 0031 14:45
     */
    public void insert(boolean isEncryped, String desc, String account, String pass) throws Exception {
        SQLiteDatabase base = this.helper.getWritableDatabase();
        String sql = "insert into MyPass (AccountDesc,AccountName,AccountPass) values (?,?,?)";
        //未经加密，要先进行加密操作
        if (!isEncryped) {
            account = AESUtil.encrypt(MyApplication.pass, account);
            pass = AESUtil.encrypt(MyApplication.pass, pass);
        }
        base.execSQL(sql, new String[]{desc, account, pass});
        Log.e("LOG", "insert: "+desc+"   "+account+"    "+pass );
        base.close();
    }

    public void delete(String desc) {
        SQLiteDatabase database = this.helper.getWritableDatabase();
        String sql = "delete from MyPass where AccountDesc=" + desc;
        database.execSQL(sql);
        database.close();
    }


    /**
     * @param paramString 明文desc
     * @return Name明文
     * @description
     * @author 张子扬
     * create at 2017/4/15 0015 22:17
     */
    public String queryNameByDesc(String paramString) {
        SQLiteDatabase localSQLiteDatabase = this.helper.getReadableDatabase();
        Cursor localCursor = localSQLiteDatabase.rawQuery("select * from MyPass where AccountDesc=?", new String[]{paramString});
        boolean bool = localCursor.moveToNext();
        String str = null;
        if (bool)
            Log.e("desc", "queryNameByDesc: " + localCursor.getString(1)); //desc
        Log.e("desc", "queryNameByDesc: " + localCursor.getString(2)); //name
        Log.e("desc", "queryNameByDesc: " + localCursor.getString(3)); //pass
        str = localCursor.getString(2);
        localCursor.close();
        localSQLiteDatabase.close();
        try {
            return AESUtil.decrypt(MyApplication.pass, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param desc 明文
     * @return 明文密码
     * @description
     * @author 张子扬
     * create at 2017/4/15 0015 22:12
     */
    public String queryPassByDesc(String desc) {
        SQLiteDatabase localSQLiteDatabase = this.helper.getReadableDatabase();
        Cursor localCursor = localSQLiteDatabase.rawQuery("select * from MyPass where AccountDesc=?", new String[]{desc});
        boolean bool = localCursor.moveToNext();
        String str = null;
        if (bool)
            Log.e("desc", "queryNameByDesc: " + localCursor.getString(1)); //desc
        Log.e("desc", "queryNameByDesc: " + localCursor.getString(2)); //name
        Log.e("desc", "queryNameByDesc: " + localCursor.getString(3)); //pass
        str = localCursor.getString(3);
        localCursor.close();
        localSQLiteDatabase.close();
        try {
            return AESUtil.decrypt(MyApplication.pass, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param desc    明文
     * @param newName 明文
     * @param newPass 明文
     * @description 更新数据
     * @author 张子扬
     * create at 2017/3/31 0031 15:15
     */
    public void update(String desc, String newName, String newPass) {
        try {
            newName = AESUtil.encrypt(MyApplication.pass, newName);
            newPass = AESUtil.encrypt(MyApplication.pass, newPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SQLiteDatabase localSQLiteDatabase = this.helper.getWritableDatabase();
        localSQLiteDatabase.execSQL("update MyPass set AccountName =? where AccountDesc=?", new String[]{newName, desc});
        localSQLiteDatabase.execSQL("update MyPass set AccountPass =? where AccountDesc=?", new String[]{newPass, desc});
        localSQLiteDatabase.close();
    }

    public ArrayList<String> queryAllDesc() {
        ArrayList localArrayList = new ArrayList();
        SQLiteDatabase localSQLiteDatabase = this.helper.getReadableDatabase();
        Cursor c = localSQLiteDatabase.rawQuery("select AccountDesc from MyPass", null);
        c.moveToFirst();
        while ((!c.isAfterLast()) && (c.getString(0) != null)) {
            localArrayList.add(c.getString(0));
            c.moveToNext();
        }
        c.close();
        localSQLiteDatabase.close();
        return localArrayList;
    }


    /**
     * @return 密文的list
     * @description
     * @author 张子扬
     * create at 2017/4/15 0015 22:18
     */
    public ArrayList<Passwords> queryAll() {
        ArrayList localArrayList = new ArrayList();
        SQLiteDatabase localSQLiteDatabase = this.helper.getReadableDatabase();
        Cursor localCursor = localSQLiteDatabase.rawQuery("select * from MyPass", null);
        localCursor.moveToFirst();
        while (!(localCursor.isAfterLast())) {
            Passwords localPasswords = new Passwords();
            localPasswords.setAccountDesc(localCursor.getString(localCursor.getColumnIndex("AccountDesc")));
            localPasswords.setAccountName(localCursor.getString(localCursor.getColumnIndex("AccountName")));
            localPasswords.setAccountPass(localCursor.getString(localCursor.getColumnIndex("AccountPass")));
            localPasswords.setUsername((String) SPHelper.getInstance(context).getParam("username", ""));
            localArrayList.add(localPasswords);
            localCursor.moveToNext();
        }
        localCursor.close();
        localSQLiteDatabase.close();
        return localArrayList;
    }

    /**
     * @description 清除表中所有数据
     * @author 张子扬
     * create at 2017/4/23 0023 20:06
     */
    public void wipeData() {
        SQLiteDatabase localSQLiteDatabase = this.helper.getWritableDatabase();
        localSQLiteDatabase.execSQL("delete from MyPass");
        localSQLiteDatabase.close();
    }
}
