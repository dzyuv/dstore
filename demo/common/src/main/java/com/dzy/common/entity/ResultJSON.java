package com.dzy.common.entity;

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

    public static ResultJSON success() {
        return success(null);
    }

    public static ResultJSON error(int code, String msg) {
        ResultJSON r = new ResultJSON();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public static ResultJSON error(String msg) {
        return error(500, msg);
    }

    public boolean isSuccess() {
        return this.code == 200;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
