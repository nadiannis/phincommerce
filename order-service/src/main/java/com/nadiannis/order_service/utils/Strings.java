package com.nadiannis.order_service.utils;

public class Strings {

    public static String camelToSnake(String str) {
        String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        return str.replaceAll(regex, replacement).toLowerCase();
    }

}
