package com.dzy.orderconsumer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultJSON {
    private int code;
    private String msg;
    private Object data;

    public static ResultJSON success(Object data) {
        ResultJSON r = new ResultJSON();
        r.code = 200;
        r.msg = "成功";
        r.data = data;
        return r;
    }

    public static ResultJSON error(int code, String msg) {
        ResultJSON r = new ResultJSON();
        r.code = code;
        r.msg = msg;
        return r;
    }
}
