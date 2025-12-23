package com.ice.bonds.service;

import com.ice.bonds.dto.BondAnalysisResponse;
import com.ice.bonds.dto.BondDTORequest;
import com.ice.bonds.helper.DurationHelper;
import com.ice.bonds.helper.ISINHelper;
import com.ice.bonds.helper.YTMHelper;
import com.ice.bonds.model.Bond;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Service class for bond-related operations.
 * Provides reusable functions for bond analysis, validation, and conversion.
 *
 * This is a Spring-managed singleton bean that can be injected into multiple controllers.
 */
@Service
public class BondService {

    private final YTMHelper ytmHelper;
    private final DurationHelper durationHelper;
    private final ISINHelper isinHelper;

    /**
     * Constructor for Spring dependency injection.
     * All helper beans are automatically injected by Spring.
     */
    public BondService(YTMHelper ytmHelper,
                       DurationHelper durationHelper, ISINHelper isinHelper) {
        this.ytmHelper = ytmHelper;
        this.durationHelper = durationHelper;
        this.isinHelper = isinHelper;
    }

    /**
     * Analyzes a bond and returns YTM, Macaulay Duration, and Modified Duration.
     * Validates the ISIN before processing.
     *
     * @param bondDTORequest The bond data
     * @return BondAnalysisResponse containing analysis results
     * @throws IllegalArgumentException if the ISIN is invalid
     */
    public BondAnalysisResponse analyzeBondWithResponse(BondDTORequest bondDTORequest) {

        Bond bond = analyzeBond(bondDTORequest, LocalDate.now());

        return new BondAnalysisResponse(
                bondDTORequest.getIsin(),
                bond.getYieldToMaturity(),
                bond.getMacaulayDuration(),
                bond.getModifiedDuration(),
                bond.getMaturityDate(),
                bond.getIssueDate(),
                bond.getCouponRate(),
                bond.getFaceValue(),
                bond.getMarketValue(),
                bond.getPaymentTerm()
        );
    }

    /**
     * Analyzes a bond as of a specific date.
     * Validates the ISIN before processing.
     *
     * @param bondDTORequest The bond data
     * @param currentDate The date to use for analysis (settlement date)
     * @return Bond containing analysis results
     * @throws IllegalArgumentException if the ISIN is invalid
     */
    public Bond analyzeBond(BondDTORequest bondDTORequest, LocalDate currentDate) {

        // Convert DTO to Bond model
        Bond bond = validateAndConvertToBond(bondDTORequest);

        // Calculate YTM (returns in basis points)
        double ytm = ytmHelper.calculateYTM(currentDate, bond);

        bond.setYieldToMaturity(ytm);

        // Calculate Macaulay Duration (returns in years)
        double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, ytm);

        bond.setMacaulayDuration(macaulayDuration);

        // Calculate Modified Duration (returns in years)
        double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytm, bond.getPaymentTerm());

        bond.setModifiedDuration(modifiedDuration);

        return bond;
    }

    /**
     * Validates and converts a BondDTORequest to a Bond model.
     * Validates the ISIN before conversion.
     *
     * @param dto The bond DTO
     * @return The converted Bond model
     * @throws IllegalArgumentException if the ISIN is invalid
     */
    public Bond validateAndConvertToBond(BondDTORequest dto) {
        validateISIN(dto.getIsin());
        validateDate(dto.getIssueDate(), "issueDate");
        validateDate(dto.getMaturityDate(), "maturityDate");
        int faceValue = validateValue(dto.getFaceValue(), "faceValue");
        int marketValue = validateValue(dto.getMarketValue(), "marketValue");
        int couponRate = validateCouponRate(dto.getCouponRate());
        int quantity = validateQuantity(dto.getQuantity());
        return convertToBond(dto, faceValue, marketValue, couponRate, quantity);
    }

    /**
     * Converts BondDTORequest to Bond model without validation.
     * Use validateAndConvertToBond() if you need ISIN validation.
     *
     * @param dto The bond DTO
     * @param faceValue The validated face value in cents
     * @param marketValue The validated market value in cents
     * @param couponRate The validated coupon rate in basis points
     * @param quantity The validated quantity
     * @return The converted Bond model
     */
    public Bond convertToBond(BondDTORequest dto, int faceValue, int marketValue, int couponRate, int quantity) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        LocalDate maturityDate = LocalDate.parse(dto.getMaturityDate(), formatter);
        LocalDate issueDate = LocalDate.parse(dto.getIssueDate(), formatter);


        return new Bond(
                dto.getIsin(),
                maturityDate,
                issueDate,
                couponRate,
                faceValue,
                marketValue,
                dto.getPaymentTerm(),
                quantity
        );
    }

    /**
     * Validates an ISIN number.
     *
     * @param isin The ISIN to validate
     * @throws IllegalArgumentException if the ISIN is invalid
     */
    public void validateISIN(String isin) {
        if (!isinHelper.isValidISIN(isin)) {
            throw new IllegalArgumentException("Invalid ISIN: " + isin);
        }
    }

    /**
     * Validates a date string is in YYYY-MM-DD format.
     *
     * @param date The date string to validate
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if the date is invalid or not in YYYY-MM-DD format
     */
    public void validateDate(String date, String fieldName) {
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": date cannot be null or empty");
        }
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + date + ". Date must be in YYYY-MM-DD format");
        }
    }

    /**
     * Validates a value is in cents (non-negative integer).
     * Rejects floats (values with decimal points) and values with commas.
     *
     * @param value The value string in cents to validate
     * @param fieldName The name of the field for error messages
     * @return The parsed integer value
     * @throws IllegalArgumentException if the value is null, empty, contains decimals/commas, or is negative
     */
    public int validateValue(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": value cannot be null or empty");
        }
        if (value.contains(".")) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value + ". Value must be a whole number without decimals");
        }
        if (value.contains(",")) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value + ". Value must not contain commas");
        }
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue < 0) {
                throw new IllegalArgumentException("Invalid " + fieldName + ": " + value + ". Value in cents must be non-negative");
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value + ". Value must be a valid integer");
        }
    }

    /**
     * Validates a coupon rate is in basis points (non-negative integer).
     * Rejects floats (values with decimal points) and values with commas.
     *
     * @param couponRate The coupon rate string in basis points to validate
     * @return The parsed integer coupon rate
     * @throws IllegalArgumentException if the coupon rate is null, empty, contains decimals/commas, or is negative
     */
    public int validateCouponRate(String couponRate) {
        if (couponRate == null || couponRate.isEmpty()) {
            throw new IllegalArgumentException("Invalid couponRate: value cannot be null or empty");
        }
        if (couponRate.contains(".")) {
            throw new IllegalArgumentException("Invalid couponRate: " + couponRate + ". Coupon rate must be a whole number without decimals");
        }
        if (couponRate.contains(",")) {
            throw new IllegalArgumentException("Invalid couponRate: " + couponRate + ". Coupon rate must not contain commas");
        }
        try {
            int parsedRate = Integer.parseInt(couponRate);
            if (parsedRate < 0) {
                throw new IllegalArgumentException("Invalid couponRate: " + couponRate + ". Coupon rate in basis points must be non-negative");
            }
            return parsedRate;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid couponRate: " + couponRate + ". Coupon rate must be a valid integer");
        }
    }

    /**
     * Validates a quantity is a positive integer.
     * Rejects floats (values with decimal points) and values with commas.
     *
     * @param quantity The quantity string to validate
     * @return The parsed integer quantity
     * @throws IllegalArgumentException if the quantity is null, empty, contains decimals/commas, or is not positive
     */
    public int validateQuantity(String quantity) {
        if (quantity == null || quantity.isEmpty()) {
            throw new IllegalArgumentException("Invalid quantity: value cannot be null or empty");
        }
        if (quantity.contains(".")) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity + ". Quantity must be a whole number without decimals");
        }
        if (quantity.contains(",")) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity + ". Quantity must not contain commas");
        }
        try {
            int parsedQuantity = Integer.parseInt(quantity);
            if (parsedQuantity <= 0) {
                throw new IllegalArgumentException("Invalid quantity: " + quantity + ". Quantity must be a positive integer");
            }
            return parsedQuantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity + ". Quantity must be a valid integer");
        }
    }
}
