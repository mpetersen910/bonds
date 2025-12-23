package com.ice.bonds.helper;

import org.springframework.stereotype.Component;

/**
 * Helper class for validating ISIN (International Securities Identification Number).
 *
 * An ISIN consists of:
 * - 2-letter country code (ISO 3166-1 alpha-2)
 * - 9-character alphanumeric NSIN (National Securities Identifying Number)
 * - 1 check digit (calculated using Luhn algorithm on converted digits)
 *
 * Total length: 12 characters
 */
@Component
public class ISINHelper {

    /**
     * Validates an ISIN number including format and checksum.
     *
     * @param isin The ISIN to validate
     * @return true if the ISIN is valid, false otherwise
     */
    public boolean isValidISIN(String isin) {
        if (isin == null || isin.length() != 12) {
            return false;
        }

        String upperIsin = isin.toUpperCase();

        // Check format: first 2 characters must be letters (country code)
        if (!isValidCountryCode(upperIsin.substring(0, 2))) {
            return false;
        }

        // Characters 3-11 must be alphanumeric (NSIN)
        if (!isValidNSIN(upperIsin.substring(2, 11))) {
            return false;
        }

        // Last character must be a digit (check digit)
        if (!Character.isDigit(upperIsin.charAt(11))) {
            return false;
        }

        // Validate checksum
        return validateChecksum(upperIsin);
    }

    /**
     * Validates the country code (first 2 characters).
     * Must be uppercase letters only.
     *
     * @param countryCode The 2-character country code
     * @return true if valid, false otherwise
     */
    public boolean isValidCountryCode(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) {
            return false;
        }
        return Character.isLetter(countryCode.charAt(0)) &&
               Character.isLetter(countryCode.charAt(1));
    }

    /**
     * Validates the NSIN (National Securities Identifying Number).
     * Must be 9 alphanumeric characters.
     *
     * @param nsin The 9-character NSIN
     * @return true if valid, false otherwise
     */
    public boolean isValidNSIN(String nsin) {
        if (nsin == null || nsin.length() != 9) {
            return false;
        }
        for (char c : nsin.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates the checksum of an ISIN using the Luhn algorithm.
     *
     * The algorithm:
     * 1. Convert letters to numbers (A=10, B=11, ..., Z=35)
     * 2. Apply Luhn algorithm to the resulting digit string
     *
     * @param isin The 12-character ISIN (uppercase)
     * @return true if checksum is valid, false otherwise
     */
    public boolean validateChecksum(String isin) {
        String digits = convertToDigits(isin);
        return calculateLuhnCheckDigit(digits) == 0;
    }

    /**
     * Calculates the expected check digit for an ISIN (without the check digit).
     *
     * @param isinWithoutCheckDigit The first 11 characters of an ISIN
     * @return The check digit (0-9)
     */
    public int calculateCheckDigit(String isinWithoutCheckDigit) {
        if (isinWithoutCheckDigit == null || isinWithoutCheckDigit.length() != 11) {
            throw new IllegalArgumentException("ISIN without check digit must be 11 characters");
        }

        String digits = convertToDigits(isinWithoutCheckDigit.toUpperCase() + "0");
        int remainder = calculateLuhnCheckDigit(digits);
        return (10 - remainder) % 10;
    }

    /**
     * Converts an ISIN string to a digit string.
     * Letters are converted to numbers: A=10, B=11, ..., Z=35
     * Digits remain unchanged.
     *
     * @param isin The ISIN string (or partial ISIN)
     * @return A string containing only digits
     */
    public String convertToDigits(String isin) {
        StringBuilder digits = new StringBuilder();
        for (char c : isin.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (Character.isLetter(c)) {
                // A=10, B=11, ..., Z=35
                int value = Character.toUpperCase(c) - 'A' + 10;
                digits.append(value);
            }
        }
        return digits.toString();
    }

    /**
     * Applies the Luhn (Mod-10) algorithm to a digit string for ISIN validation.
     *
     * The Luhn algorithm for ISIN:
     * 1. Starting from the rightmost digit (check digit at position 1), process each digit
     * 2. Double every second digit starting from position 2 (positions 2, 4, 6, 8... from right)
     * 3. If doubling results in a number > 9, subtract 9 (equivalent to adding the digits)
     * 4. Sum all resulting values
     * 5. The ISIN is valid if sum % 10 == 0
     *
     * Note: Position 1 (the check digit) is NOT doubled. This is consistent with the standard
     * Luhn algorithm where you "double every second digit from the right, starting with the
     * digit immediately to the left of the check digit."
     *
     * @param digits A string containing only digits (after letter-to-number conversion)
     * @return The Luhn checksum remainder (0 if the ISIN is valid)
     */
    public int calculateLuhnCheckDigit(String digits) {
        int sum = 0;
        // For ISIN: double every second digit starting from the second-to-last
        // Position 1 from right (check digit) = not doubled
        // Position 2 from right = doubled
        // Position 3 from right = not doubled
        // etc.

        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(digits.charAt(i));

            // Position from right (1-based): length - i
            int positionFromRight = digits.length() - i;

            // Double every second digit (positions 2, 4, 6, etc. from the right)
            if (positionFromRight % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
        }

        return sum % 10;
    }

    /**
     * Generates a complete ISIN by calculating and appending the check digit.
     *
     * @param isinWithoutCheckDigit The first 11 characters of an ISIN
     * @return The complete 12-character ISIN
     */
    public String generateISIN(String isinWithoutCheckDigit) {
        if (isinWithoutCheckDigit == null || isinWithoutCheckDigit.length() != 11) {
            throw new IllegalArgumentException("ISIN without check digit must be 11 characters");
        }

        String upperIsin = isinWithoutCheckDigit.toUpperCase();
        int checkDigit = calculateCheckDigit(upperIsin);
        return upperIsin + checkDigit;
    }
}
