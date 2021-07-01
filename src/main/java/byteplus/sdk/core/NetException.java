package byteplus.sdk.core;

public class NetException extends Exception {
    public NetException(int code, String msg) {
        super(String.format("Http response code not '200', but '%d', msg:'%s'", code, msg));
    }

    public NetException(String message) {
        super(message);
    }
}
