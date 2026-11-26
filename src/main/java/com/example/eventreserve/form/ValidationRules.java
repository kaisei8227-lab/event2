package com.example.eventreserve.form;

/**
 * 入力チェックのルール。
 * static/js/common.js にも同じルールを書いているので、変えるときは両方そろえる。
 */
public final class ValidationRules {

    /** 「名前@ドメイン.トップレベル」の形。ドットのない「a@b」は通さない */
    public static final String EMAIL = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    /** 8〜64文字で、英字と数字を両方ふくむ */
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$";

    /** 全角カタカナ（長音・スペース可） */
    public static final String KANA = "^[ァ-ヶー　 ]+$";

    /** 0から始まる電話番号。ハイフンはあってもなくてもよい */
    public static final String PHONE = "^0\\d{1,4}-?\\d{1,4}-?\\d{3,4}$";

    private ValidationRules() {
    }
}
