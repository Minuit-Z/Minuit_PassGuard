package objects;

import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2017/3/12 0012.
 */

public class User extends BmobObject{

    private String username;
    private String userpass;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpass() {
        return userpass;
    }

    public void setUserpass(String userpass) {
        this.userpass = userpass;
    }
}
