package utils;

import java.util.Random;

/**
 * Created by Administrator on 2017/3/31 0031.
 */

public class RandomPassUtil {

    /**
     * 获取随机密码
     *
     * @param type  密码类型 1仅数字；2仅字母;3数字+字母;4所有字符
     * @param lenth 密码长度
     * @return 未加密的密码
     */
    public String getRandomPass(int type, int lenth) {
        StringBuilder buidler = new StringBuilder();
        String pool ;
        switch (type) {
            case 1:
                pool = "1234567890";
                break;
            case 2:
                pool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;
            case 3:
                pool = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;
            case 0:
                pool = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_@#$%^&*()<>?{}[]";
                break;
            default:
                pool="";
        }
        Random random = new Random();
        for (int i=0;i<lenth;i++){
            buidler.append(pool.charAt(random.nextInt(pool.length())));
        }
        return buidler.toString();
    }
}
