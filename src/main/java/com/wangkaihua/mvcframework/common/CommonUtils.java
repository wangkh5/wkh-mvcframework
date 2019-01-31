package com.wangkaihua.mvcframework.common;

/**
 * @desciption: TODO
 * @author: wangkaihua
 * @date: 2019/1/30 19:05
 */
public class CommonUtils {
    private CommonUtils(){

    }

    /**
     * 把小写的首字母变成大写
     * @param word
     * @return
     */
    public static String toLowerFirstCase(String word) {
        char[] chars = word.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
