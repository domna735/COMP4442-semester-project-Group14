package hk.polyu.comp4442.cloudcompute.controller;

import hk.polyu.comp4442.cloudcompute.dto.CalculateRequest;
import hk.polyu.comp4442.cloudcompute.dto.CalculateResponse;
import hk.polyu.comp4442.cloudcompute.service.ComputeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/compute")
public class ComputeController {

    private final ComputeService computeService;

    public ComputeController(ComputeService computeService) {
        this.computeService = computeService;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("message", "Compute service is running");
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@Valid @RequestBody CalculateRequest request) {
        try {
            CalculateResponse response = computeService.calculate(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
