package pers.summer502.j8zyeinkappstore.util;

import java.io.Serial;
import java.io.Serializable;


public class ResponseBodyResult<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -3999803560577989187L;

    public static final int CODE_OK = 0;
    public static final int CODE_ERROR = -1;
    public static final String MSG_SUCCESS = "";

    private int code;
    private String msg;
    private T body;

    public static <E> ResponseBodyResult<E> success(E body) {
        return new ResponseBodyResult<>(CODE_OK, MSG_SUCCESS, body);
    }

    public static <E> ResponseBodyResult<E> error(String msg) {
        return new ResponseBodyResult<>(CODE_ERROR, msg);
    }

    public static <E> ResponseBodyResult<E> result(int code, E body) {
        return new ResponseBodyResult<>(code, "", body);
    }

    public static <E> ResponseBodyResult<E> result(int code, String msg) {
        return new ResponseBodyResult<>(code, msg);
    }

    public static <E> ResponseBodyResult<E> result(int code, String msg, E body) {
        return new ResponseBodyResult<>(code, msg, body);
    }

    private ResponseBodyResult() {

    }

    private ResponseBodyResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private ResponseBodyResult(int code, String msg, T body) {
        this.code = code;
        this.msg = msg;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((msg == null) ? 0 : msg.hashCode());
        result = prime * result + code;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResponseBodyResult<?> that = (ResponseBodyResult<?>) obj;
        if (this.body == null) {
            if (that.body != null) {
                return false;
            }
        } else if (!this.body.equals(that.body)) {
            return false;
        }
        if (msg == null) {
            if (that.msg != null) {
                return false;
            }
        } else if (!msg.equals(that.msg)) {
            return false;
        }
        if (code != that.code) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResponseBodyResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", body=" + body +
                '}';
    }
}