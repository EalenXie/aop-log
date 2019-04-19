package name.ealen.service;

import name.ealen.model.ExceptionResponse;
import name.ealen.repository.ExceptionResponseRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by EalenXie on 2019/4/17 14:45.
 */
@Service
public class ExceptionResponseService {


    @Resource
    private ExceptionResponseRepository exceptionResponseRepository;


    @Async
    public void asyncAddExceptionResponse(ExceptionResponse exceptionResponse) {
        exceptionResponseRepository.save(exceptionResponse);
    }

}
