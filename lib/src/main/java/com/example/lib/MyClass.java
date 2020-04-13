package com.example.lib;

import java.util.Calendar;
import java.util.TimeZone;

public class MyClass {
    public static void main(String[] args) {
        TimeZone tz = TimeZone.getDefault();
        Calendar c = Calendar.getInstance(tz);
        c.add(Calendar.HOUR, 5);
        c.add(Calendar.MINUTE, 30);
    }
}
