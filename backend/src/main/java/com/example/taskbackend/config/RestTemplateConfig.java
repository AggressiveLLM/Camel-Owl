// RestTemplateConfig.java
package com.example.taskbackend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/*
* 这个 Bean 的作用就是在 Spring 容器里注册一个配置好的 HTTP 客户端（RestTemplate），
* 可以在全项目中安全、统一地调用外部 HTTP 服务（比如 Python FastAPI 的 OWL 接口）。
* */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 使用 SimpleClientHttpRequestFactory 设置超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000); // 连接超时 5s
        factory.setReadTimeout(120_000); // 读取超时 120s
        return new RestTemplate(factory);
    }
}
