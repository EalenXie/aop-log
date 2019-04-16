package name.ealen.config;

import com.alibaba.fastjson.JSON;
import name.ealen.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by EalenXie on 2018/11/8 16:25.
 * 全局异常、错误返回处理
 */
@ControllerAdvice
public class ControllerExceptionListener {

    private final Logger log = LoggerFactory.getLogger(ControllerExceptionListener.class);

    private final static String REQUESTER_IP = "RequesterIp";
    private final static String ERROR_PARAMS = "errorParams";
    private final static String RESPONSE_BODY = "responseBody";
    private final static String STATUS_CODE = "statusCode";
    private final static String STATUS_TEXT = "statusText";
    private final static String STATUS_REASON_PHRASE = "statusReasonPhrase";
    private final static String THROWABLE = "throwable";
    private final static String THROWABLE_TIME = "throwableTime";
    private final static String MESSAGE = "message";
    private final static String LOCALIZED_MESSAGE = "localizedMessage";


    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity Throwable(Throwable throwable, HttpServletRequest request) {
        Map<String, Object> result = getThrowable(throwable);
        if (request != null) {
            Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            result.put(REQUESTER_IP, CommonUtil.getIpAddress(request));
            result.put(HttpHeaders.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT));
            if (statusCode != null) {
                new ResponseEntity<>(JSON.toJSON(result).toString(), HttpStatus.valueOf(statusCode));
            }
        }
        if (throwable instanceof MethodArgumentNotValidException) {
            List<FieldError> fieldErrors = ((MethodArgumentNotValidException) throwable).getBindingResult().getFieldErrors();
            Map<String, String> params = new HashMap<>();
            for (FieldError error : fieldErrors) {
                params.put(error.getField(), error.getDefaultMessage());
            }
            result.put(ERROR_PARAMS, JSON.toJSON(params));
            return new ResponseEntity<>(JSON.toJSON(result), HttpStatus.BAD_REQUEST);
        }
        if (throwable instanceof HttpServerErrorException) {
            HttpServerErrorException serverError = (HttpServerErrorException) throwable;
            HttpStatus status = serverError.getStatusCode();
            result.put(RESPONSE_BODY, "" + serverError.getResponseBodyAsString());
            result.put(STATUS_CODE, "" + status.toString());
            result.put(STATUS_TEXT, "" + serverError.getStatusText());
            result.put(STATUS_REASON_PHRASE, "" + status.getReasonPhrase());
            return new ResponseEntity<>(JSON.toJSON(result).toString(), status);
        }
        if (throwable instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) throwable;
            HttpStatus status = clientError.getStatusCode();
            result.put(RESPONSE_BODY, "" + clientError.getResponseBodyAsString());
            result.put(STATUS_CODE, "" + clientError.getStatusCode().toString());
            result.put(STATUS_TEXT, "" + clientError.getStatusText());
            result.put(STATUS_REASON_PHRASE, "" + status.getReasonPhrase());
            return new ResponseEntity<>(JSON.toJSON(result).toString(), status);
        }
        return new ResponseEntity<>(JSON.toJSON(result).toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 公共异常信息
     */
    private Map<String, Object> getThrowable(Throwable throwable) {
        Map<String, Object> result = new HashMap<>();
        result.put(THROWABLE, "" + throwable);
        result.put(THROWABLE_TIME, "" + CommonUtil.getCurrentDateTime());
        result.put(MESSAGE, "" + throwable.getMessage());
        result.put(LOCALIZED_MESSAGE, "" + throwable.getLocalizedMessage());
        log.error("Exception : {}", JSON.toJSON(result));
        throwable.printStackTrace();
        return result;
    }
}
