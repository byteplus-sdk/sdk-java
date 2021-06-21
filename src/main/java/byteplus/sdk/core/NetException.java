package byteplus.sdk.core;

public class NetException extends Exception {
    public NetException(int code) {
        super(String.format("Http response code not '200', but '%d'", code));
    }

    public NetException(String message) {
        super(message);
    }
}
