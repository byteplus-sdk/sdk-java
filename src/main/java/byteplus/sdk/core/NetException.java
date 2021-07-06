package byteplus.sdk.core;

// throw when net timeout
public class NetException extends Exception {
    public NetException(String message) {
        super(message);
    }
}
