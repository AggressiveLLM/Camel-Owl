package com.example.taskbackend.controller;

import com.example.taskbackend.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/owl")
public class OwlController {

    private static final Logger logger = LoggerFactory.getLogger(OwlController.class);

    private final RestTemplate restTemplate;

    public OwlController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 接收前端请求，转发到 Python FastAPI 服务 (http://localhost:8001/run)
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runOwl(@Valid @RequestBody Task task) {
        String url = "http://localhost:8001/run";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "title", task.getTitle()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            // 使用 Map.class 接收 FastAPI 返回的 JSON
            ResponseEntity<Map> pythonResp = restTemplate.postForEntity(url, entity, Map.class);

            logger.info("Python service responded with status: {}", pythonResp.getStatusCode());

            if (!pythonResp.getStatusCode().is2xxSuccessful()) {
                logger.warn("Python service non-2xx: {} body: {}", pythonResp.getStatusCode(), pythonResp.getBody());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Python service error: " + pythonResp.getStatusCode()));
            }

            // 返回 FastAPI 的 JSON（包含 stdout/stderr/returncode）
            Map<String, Object> body = pythonResp.getBody();
            return ResponseEntity.ok(body);

        } catch (HttpStatusCodeException ex) {
            logger.error("Python service returned error status: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", ex.getResponseBodyAsString()));
        } catch (ResourceAccessException ex) {
            logger.error("Failed to access Python service", ex);
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of("error", "Python service unreachable"));
        } catch (Exception ex) {
            logger.error("Unexpected error when calling Python service", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }
}
