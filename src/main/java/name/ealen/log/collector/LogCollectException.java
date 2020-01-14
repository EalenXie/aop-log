package name.ealen.log.collector;

/**
 * @author EalenXie Created on 2020/1/7 9:13.
 * 日志收集异常
 */
public class LogCollectException extends Exception {

    private static final long serialVersionUID = 4150111145052485399L;

    public LogCollectException() {
        super();
    }

    public LogCollectException(String message) {
        super(message);
    }

    public LogCollectException(Throwable cause) {
        super(cause);
    }

    public LogCollectException(String message, Throwable cause) {
        super(message, cause);
    }

    protected LogCollectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
