package com.northbay.ragchat.controller;

import com.northbay.ragchat.api.HealthApi;
import com.northbay.ragchat.model.HealthCheck200Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

    @Override
    public ResponseEntity<HealthCheck200Response> healthCheck() {
        HealthCheck200Response resp = new HealthCheck200Response();
        resp.setStatus("UP");
        resp.setDatabase("UP");
        return ResponseEntity.ok(resp);
    }
}
