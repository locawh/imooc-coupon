package com.leco;

import com.alibaba.fastjson.JSON;

/**
 * description
 *
 * @author wangheng
 * date 2020-03-11 23:19
 * version 1.0
 */
public class JsonTest {
    public static void main(String[] args) {
        String jsonString = "{name:'Antony',age:'12',sex:'male',telephone:'88888'}";
        Staff staff = JSON.parseObject(jsonString, Staff.class);
        System.out.println(staff.toString());

        String st = JSON.toJSONString(staff);
        System.out.println(st);

    }
}
