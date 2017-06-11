package objects;

import utils.PinYinUtil;

public class PassDesc
        implements Comparable<PassDesc> {
    private String MyPinyin;
    private String name;

    public PassDesc(String paramString) {
        setName(paramString);
        setMyPinyin(paramString);
    }

    public int compareTo(PassDesc paramPassDesc) {
        return getMyPinyin().compareTo(paramPassDesc.getMyPinyin());
    }

    public String getMyPinyin() {
        return this.MyPinyin;
    }

    public String getName() {
        return this.name;
    }

    public void setMyPinyin(String paramString) {
        if (PinYinUtil.getPinyin(paramString).charAt(0) >= 'a' && PinYinUtil.getPinyin(paramString).charAt(0) <= 'z') {
            this.MyPinyin = (char) (PinYinUtil.getPinyin(paramString).charAt(0) - 32) + "";
        } else {
            this.MyPinyin = PinYinUtil.getPinyin(paramString).charAt(0) + "";
        }
    }

    public void setName(String paramString) {
        this.name = paramString;
    }
}