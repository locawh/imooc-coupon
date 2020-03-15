package com.leco;

import lombok.Data;

import java.util.Date;

/**
 * description
 *
 * @author wangheng
 * date 2020-03-11 23:20
 * version 1.0
 */
@Data
public class Staff {
    private String name;
    private Integer age;
    private String sex;
    private Date birthday;

    @Override
    public String toString() {
        return "Staff{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}
