package com.ice.bonds.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.bonds.dto.BondAnalysisResponse;
import com.ice.bonds.dto.BondDTORequest;
import com.ice.bonds.service.BondService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bonds")
public class BondController {

    private static final Logger logger = LoggerFactory.getLogger(BondController.class);

    private final BondService bondService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for Spring dependency injection.
     * The BondService singleton bean is automatically injected.
     */
    public BondController(BondService bondService, ObjectMapper objectMapper) {
        this.bondService = bondService;
        this.objectMapper = objectMapper;
    }

    /**
     * Analyzes a bond and returns YTM, Macaulay Duration, and Modified Duration.
     * Validates the ISIN before processing.
     *
     * @param bondDTORequest The bond data from JSON request
     * @return BondAnalysisResponse containing YTM (basis points), Macaulay Duration (years), Modified Duration (years)
     * @throws IllegalArgumentException if the ISIN is invalid
     */
    @PostMapping("/analyze")
    public ResponseEntity<BondAnalysisResponse> analyzeBond(@RequestBody BondDTORequest bondDTORequest) {

        logger.info("Received bond analysis request for ISIN: {}", bondDTORequest.getIsin());
        BondAnalysisResponse response = bondService.analyzeBondWithResponse(bondDTORequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes a bond from a JSON string.
     * Accepts JSON serialized into a string and deserializes it.
     *
     * @param jsonString JSON string containing bond data
     * @return BondAnalysisResponse containing YTM, Macaulay Duration, Modified Duration
     * @throws IllegalArgumentException if the JSON is invalid or ISIN is invalid
     */
    @PostMapping("/analyze-from-string")
    public ResponseEntity<BondAnalysisResponse> analyzeBondFromString(@RequestBody String jsonString) {
        logger.info("Received bond analysis request from JSON string");

        BondDTORequest bondDTORequest;
        try {
            // The input is a JSON-encoded string. We need to first deserialize the string value,
            // then parse the resulting JSON object.
            String actualJson = objectMapper.readValue(jsonString, String.class);

            // Validate that the deserialized string is not null or empty
            if (actualJson == null || actualJson.trim().isEmpty()) {
                throw new IllegalArgumentException("JSON string content cannot be null or empty");
            }

            bondDTORequest = objectMapper.readValue(actualJson, BondDTORequest.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON string: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JSON format: " + e.getMessage());
        }

        logger.info("Parsed bond from JSON string for ISIN: {}", bondDTORequest.getIsin());
        BondAnalysisResponse response = bondService.analyzeBondWithResponse(bondDTORequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler for IllegalArgumentException.
     * Returns HTTP 400 Bad Request with the error message.
     *
     * @param ex The exception
     * @return ResponseEntity with error message and 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
