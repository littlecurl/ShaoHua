package cn.edu.heuet.shaohua.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidUtils {
    // 校验账号不能为空且必须是中国大陆手机号（宽松模式匹配）
    public static boolean isPhoneValid(String account) {
        if (account == null) {
            return false;
        }
        // 首位为1, 第二位为3-9, 剩下九位为 0-9, 共11位数字
        String pattern = "^[1]([3-9])[0-9]{9}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(account);
        return m.matches();
    }

    // 校验密码不少于6位
    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // 性别只能填1或2
    public static boolean isGenderValid(String gender) {
        return gender.equals("1") || gender.equals("2");
    }
}
