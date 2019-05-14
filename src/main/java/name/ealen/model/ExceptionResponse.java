package name.ealen.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by EalenXie on 2019/4/16 15:53.
 */
@Entity
@Table
@Data
public class ExceptionResponse implements Serializable {

    @Transient
    private static final ThreadLocal<ExceptionResponse> EXCEPTION_RESPONSE = new ThreadLocal<>();

    @GeneratedValue
    @Id
    private Integer id;
    private String requestUrl;
    private String requestMethod;
    private String requestBody;
    private String requesterIp;
    private String userAgent;
    private String responseBody;
    private Integer statusCode;
    private String statusText;
    private String statusReasonPhrase;
    private String throwable;
    private Date throwableTime;
    private String message;
    private String localizedMessage;

    @Transient
    private transient Object errorParams;

    public static synchronized ExceptionResponse getCurrentException() {
        ExceptionResponse response = EXCEPTION_RESPONSE.get();
        if (response == null) {
            response = new ExceptionResponse();
            EXCEPTION_RESPONSE.set(response);
        }
        return response;
    }

    public static void removeExceptionResponse() {
        EXCEPTION_RESPONSE.remove();
    }

}
