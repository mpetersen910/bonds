package com.ice.bonds.controller;

import com.ice.bonds.dto.BondAnalysisResponse;
import com.ice.bonds.dto.BondDTO;
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

    /**
     * Constructor for Spring dependency injection.
     * The BondService singleton bean is automatically injected.
     */
    public BondController(BondService bondService) {
        this.bondService = bondService;
    }

    /**
     * Analyzes a bond and returns YTM, Macaulay Duration, and Modified Duration.
     * Validates the ISIN before processing.
     *
     * @param bondDTO The bond data from JSON request
     * @return BondAnalysisResponse containing YTM (basis points), Macaulay Duration (years), Modified Duration (years)
     * @throws IllegalArgumentException if the ISIN is invalid
     */
    @PostMapping("/analyze")
    public ResponseEntity<BondAnalysisResponse> analyzeBond(@RequestBody BondDTO bondDTO) {

        logger.info("Received bond analysis request for ISIN: {}", bondDTO.getIsin());
        BondAnalysisResponse response = bondService.analyzeBondWithResponse(bondDTO);
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
