package com.github.proxy;

import com.github.AppNameHelper;
import com.github.CollectorExecutor;
import com.github.DataExtractor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * @author EalenXie create on 2020/11/5 19:34
 * 代理了restTemplate主流的请求方法 支持对restTemplate 请求的收集
 */
public class RestTemplateProxy implements ApplicationContextAware {


    private final RestTemplate restTemplate;
    private final UriTemplateHandler uriTemplateHandler = initUriTemplateHandler();
    private final RestTemplateCollector restTemplateCollector;
    private String appName;
    private CollectorExecutor collectorExecutor;

    @Override
    public void setApplicationContext(@Autowired ApplicationContext applicationContext) throws BeansException {
        this.appName = AppNameHelper.getAppNameByApplicationContext(applicationContext);
    }

    public void setCollectorExecutor(@Autowired CollectorExecutor collectorExecutor) {
        this.collectorExecutor = collectorExecutor;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    private String getAppName() {
        return this.appName;
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    public RestTemplateProxy(ClientHttpRequestFactory factory, RestTemplateCollector restTemplateCollector) {
        this(new RestTemplate(factory), restTemplateCollector);
    }

    public RestTemplateProxy(RestTemplate restTemplate, RestTemplateCollector restTemplateCollector) {
        this.restTemplate = restTemplate;
        this.restTemplateCollector = restTemplateCollector;
    }

    private static DefaultUriBuilderFactory initUriTemplateHandler() {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        return uriFactory;
    }

    public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return getForEntity(url, responseType, uriVariables).getBody();
    }

    public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
        return getForEntity(url, responseType, uriVariables).getBody();
    }

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.GET, null, responseType, uriVariables);
    }

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.GET, null, responseType, uriVariables);
    }

    public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return postForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return postForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.POST, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.POST, httpEntity(request), responseType, uriVariables);
    }

    public <T> T putForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return putForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> T putForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return putForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.PUT, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.PUT, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> deleteForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.DELETE, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        return exchange(uriTemplateHandler.expand(url, uriVariables), method, requestEntity, responseType);
    }

    public void delete(String url, Object... uriVariables) {
        exchange(uriTemplateHandler.expand(url, uriVariables), HttpMethod.DELETE, null, Void.class);
    }

    public void delete(String url, Map<String, ?> uriVariables) {
        exchange(uriTemplateHandler.expand(url, uriVariables), HttpMethod.DELETE, null, Void.class);
    }

    public void delete(URI url) {
        exchange(url, HttpMethod.DELETE, null, Void.class);
    }

    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
        return exchange(url, method, requestEntity, responseType, restTemplateCollector);
    }

    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, RestTemplateCollector collector) {
        if (collector == null) {
            return restTemplate.exchange(url, method, requestEntity, responseType);
        }
        ResponseEntity<T> responseEntity;
        long startTime = System.currentTimeMillis();
        boolean success = false;
        Object req = null;
        Object resp = null;
        String desc = null;
        try {
            // 请求参数解析
            if (requestEntity != null) {
                req = extractData(requestEntity.getBody(), requestEntity.getHeaders().getContentType());
            }
            responseEntity = restTemplate.exchange(url, method, requestEntity, responseType);
            // 请求响应解析
            resp = extractData(responseEntity.getBody(), responseEntity.getHeaders().getContentType());
            // 请求标识
            success = true;
            desc = "SUCCESS";
        } catch (RestClientResponseException e) {
            desc = e.getMessage();
            resp = e.getResponseBodyAsString();
            throw e;
        } catch (Exception e) {
            desc = e.getMessage();
            throw e;
        } finally {
            ReqInfo info = new ReqInfo();
            info.setAppName(getAppName());
            info.setHost(url.getHost());
            info.setPort(url.getPort());
            info.setUrl(url.toString());
            info.setMethod(method.name());
            info.setReq(req);
            info.setLogDate(new Date());
            info.setCostTime(System.currentTimeMillis() - startTime);
            info.setResp(resp);
            info.setSuccess(success);
            info.setUrlParam(url.getQuery());
            info.setDesc(desc);
            if (collectorExecutor != null) {
                collectorExecutor.asyncExecute(collector, info);
            } else {
                collector.collect(info);
            }
        }
        return responseEntity;
    }

    public Object extractData(Object body, MediaType mediaType) {
        if (body != null && MediaType.APPLICATION_XML == mediaType) {
            return DataExtractor.xmlArgs(body);
        }
        return body;
    }

    @SuppressWarnings(value = "unchecked")
    public <T> HttpEntity<T> httpEntity(T requestBody) {
        if (requestBody instanceof HttpEntity) {
            return (HttpEntity<T>) requestBody;
        } else if (requestBody != null) {
            return new HttpEntity<>(requestBody);
        } else {
            return new HttpEntity<>(null, null);
        }
    }
}
