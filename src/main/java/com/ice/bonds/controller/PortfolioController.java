package com.ice.bonds.controller;

import com.ice.bonds.dto.BondDTO;
import com.ice.bonds.dto.BondInPortfolioAnalysisResponse;
import com.ice.bonds.dto.PortfolioAnalysisResponse;
import com.ice.bonds.model.Bond;
import com.ice.bonds.model.Portfolio;
import com.ice.bonds.service.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Analyzes a portfolio of bonds and returns weighted durations and total value.
     *
     * @param bondDTOs List of bond data from JSON request
     * @return PortfolioAnalysisResponse containing portfolio analysis results
     */
    @PostMapping("/analyze")
    public ResponseEntity<PortfolioAnalysisResponse> analyzePortfolio(@RequestBody List<BondDTO> bondDTOs) {
        logger.info("Received portfolio analysis request with {} bonds", bondDTOs.size());

        // Create a new portfolio with a default account ID
        Portfolio portfolio = new Portfolio("default-account");

        // Add bonds to the portfolio and analyze
        portfolio = portfolioService.addBondsToPortfolio(portfolio, bondDTOs);

        // Map the portfolio to the response DTO
        PortfolioAnalysisResponse response = mapToPortfolioAnalysisResponse(portfolio);

        return ResponseEntity.ok(response);
    }

    /**
     * Maps a Portfolio model to a PortfolioAnalysisResponse DTO.
     *
     * @param portfolio The portfolio to map
     * @return The mapped PortfolioAnalysisResponse
     */
    private PortfolioAnalysisResponse mapToPortfolioAnalysisResponse(Portfolio portfolio) {
        List<BondInPortfolioAnalysisResponse> bondResponses = portfolio.getBonds().stream()
                .map(this::mapToBondInPortfolioAnalysisResponse)
                .collect(Collectors.toList());

        return new PortfolioAnalysisResponse(
                portfolio.getId(),
                portfolio.getAccountId(),
                bondResponses,
                portfolio.getWeightedMacaulayDuration(),
                portfolio.getWeightedModifiedDuration(),
                portfolio.getTotalPortfolioValue()
        );
    }

    /**
     * Maps a Bond model to a BondInPortfolioAnalysisResponse DTO.
     *
     * @param bond The bond to map
     * @return The mapped BondInPortfolioAnalysisResponse
     */
    private BondInPortfolioAnalysisResponse mapToBondInPortfolioAnalysisResponse(Bond bond) {
        return new BondInPortfolioAnalysisResponse(
                bond.getISIN(),
                bond.getYieldToMaturity(),
                bond.getMacaulayDuration(),
                bond.getModifiedDuration(),
                bond.getMaturityDate(),
                bond.getIssueDate(),
                bond.getCouponRate(),
                bond.getFaceValue(),
                bond.getMarketValue(),
                bond.getPaymentTerm(),
                bond.getQuantity(),
                bond.getBondWeightInPortfolio()
        );
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

