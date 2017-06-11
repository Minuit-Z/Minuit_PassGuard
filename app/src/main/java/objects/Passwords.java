package objects;

import utils.PinYinUtil;
import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2017/3/12 0012.
 */

public class Passwords extends BmobObject implements Comparable<Passwords>{

    private String username;  //用户名
    private String AccountDesc; //密码描述
    private String AccountPass; //密码密文
    private String AccountName;  //账户名

    private String pinyin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountDesc() {
        return AccountDesc;
    }

    public void setAccountDesc(String accountDesc) {
        AccountDesc = accountDesc;
    }

    public String getAccountPass() {
        return AccountPass;
    }

    public void setAccountPass(String accountPass) {
        AccountPass = accountPass;
    }

    public String getAccountName() {
        return AccountName;
    }

    public void setAccountName(String accountName) {
        AccountName = accountName;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public Passwords(String desc){
        this.AccountDesc=desc;
        setPinyin(PinYinUtil.getPinyin(desc));
    }

    public Passwords(){

    }

    @Override
    public int compareTo(Passwords o) {
        String anotherPinyin=PinYinUtil.getPinyin(o.getAccountDesc());
        return getPinyin().compareTo(anotherPinyin);
    }
}
