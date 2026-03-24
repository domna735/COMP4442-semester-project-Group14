package hk.polyu.comp4442.cloudcompute.controller;

import hk.polyu.comp4442.cloudcompute.dto.CalculateRequest;
import hk.polyu.comp4442.cloudcompute.dto.CalculateResponse;
import hk.polyu.comp4442.cloudcompute.service.ComputeService;
import jakarta.validation.Valid;
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
    public ResponseEntity<CalculateResponse> calculate(@Valid @RequestBody CalculateRequest request) {
        CalculateResponse response = computeService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
