package com.ice.bonds;

import com.ice.bonds.helper.ISINHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ISIN Helper Tests")
class ISINHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(ISINHelperTest.class);

    private ISINHelper isinHelper;

    @BeforeEach
    void setUp() {
        isinHelper = new ISINHelper();
    }

    @Nested
    @DisplayName("isValidISIN Tests")
    class IsValidISINTests {

        @ParameterizedTest
        @DisplayName("Should validate known valid ISINs")
        @ValueSource(strings = {
            "US0378331005",  // Apple Inc.
            "US5949181045",  // Microsoft Corporation
            "US0231351067",  // Amazon.com Inc.
            "GB0002634946",  // BAE Systems
            "DE0007164600",  // SAP SE
            "FR0000120578",  // Sanofi
            "JP3633400001",  // Toyota Motor
            "CH0012005267"   // Novartis AG
        })
        void testValidISINs(String isin) {
            logger.info("Testing valid ISIN: {}", isin);
            assertTrue(isinHelper.isValidISIN(isin), "ISIN should be valid: " + isin);
        }

        @ParameterizedTest
        @DisplayName("Should reject ISINs with invalid checksums")
        @ValueSource(strings = {
            "US0378331006",  // Apple Inc. with wrong check digit
            "US5949181046",  // Microsoft with wrong check digit
            "US0231351068",  // Amazon with wrong check digit
            "GB0002634947",  // BAE Systems with wrong check digit
            "DE0007164601"   // SAP SE with wrong check digit
        })
        void testInvalidChecksumISINs(String isin) {
            logger.info("Testing ISIN with invalid checksum: {}", isin);
            assertFalse(isinHelper.isValidISIN(isin), "ISIN should be invalid due to checksum: " + isin);
        }

        @ParameterizedTest
        @DisplayName("Should reject ISINs with invalid format")
        @ValueSource(strings = {
            "12345678901A",  // Numbers in country code
            "1S0378331005",  // Number in first position
            "U10378331005",  // Number in second position
            "USABCDEFGHI@",  // Special character in check digit
            "US037833100",   // Too short (11 chars)
            "US03783310055"  // Too long (13 chars)
        })
        void testInvalidFormatISINs(String isin) {
            logger.info("Testing ISIN with invalid format: {}", isin);
            assertFalse(isinHelper.isValidISIN(isin), "ISIN should be invalid due to format: " + isin);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject null and empty ISINs")
        void testNullAndEmptyISINs(String isin) {
            logger.info("Testing null or empty ISIN");
            assertFalse(isinHelper.isValidISIN(isin), "Null or empty ISIN should be invalid");
        }

        @Test
        @DisplayName("Should handle lowercase ISINs (case insensitive)")
        void testLowercaseISIN() {
            String lowercase = "us0378331005";
            String uppercase = "US0378331005";

            logger.info("Testing lowercase ISIN: {}", lowercase);
            assertEquals(isinHelper.isValidISIN(uppercase), isinHelper.isValidISIN(lowercase),
                "Lowercase ISIN should be treated the same as uppercase");
        }

        @Test
        @DisplayName("Should reject ISIN with special characters in NSIN")
        void testSpecialCharactersInNSIN() {
            String isin = "US037833!005";
            logger.info("Testing ISIN with special character in NSIN: {}", isin);
            assertFalse(isinHelper.isValidISIN(isin), "ISIN with special characters should be invalid");
        }
    }

    @Nested
    @DisplayName("isValidCountryCode Tests")
    class IsValidCountryCodeTests {

        @ParameterizedTest
        @DisplayName("Should validate valid country codes")
        @ValueSource(strings = {"US", "GB", "DE", "FR", "JP", "CH", "AU", "CA"})
        void testValidCountryCodes(String countryCode) {
            logger.info("Testing valid country code: {}", countryCode);
            assertTrue(isinHelper.isValidCountryCode(countryCode),
                "Country code should be valid: " + countryCode);
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid country codes")
        @ValueSource(strings = {"12", "1A", "A1", "U", "USA", ""})
        void testInvalidCountryCodes(String countryCode) {
            logger.info("Testing invalid country code: {}", countryCode);
            assertFalse(isinHelper.isValidCountryCode(countryCode),
                "Country code should be invalid: " + countryCode);
        }

        @Test
        @DisplayName("Should reject null country code")
        void testNullCountryCode() {
            logger.info("Testing null country code");
            assertFalse(isinHelper.isValidCountryCode(null), "Null country code should be invalid");
        }
    }

    @Nested
    @DisplayName("isValidNSIN Tests")
    class IsValidNSINTests {

        @ParameterizedTest
        @DisplayName("Should validate valid NSINs")
        @ValueSource(strings = {"037833100", "594918104", "ABCDEFGHI", "ABC123DEF", "000000000", "999999999"})
        void testValidNSINs(String nsin) {
            logger.info("Testing valid NSIN: {}", nsin);
            assertTrue(isinHelper.isValidNSIN(nsin), "NSIN should be valid: " + nsin);
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid NSINs")
        @ValueSource(strings = {"03783310", "0378331000", "ABC!DEFGH", "ABC DEF12", ""})
        void testInvalidNSINs(String nsin) {
            logger.info("Testing invalid NSIN: {}", nsin);
            assertFalse(isinHelper.isValidNSIN(nsin), "NSIN should be invalid: " + nsin);
        }

        @Test
        @DisplayName("Should reject null NSIN")
        void testNullNSIN() {
            logger.info("Testing null NSIN");
            assertFalse(isinHelper.isValidNSIN(null), "Null NSIN should be invalid");
        }
    }

    @Nested
    @DisplayName("convertToDigits Tests")
    class ConvertToDigitsTests {

        @ParameterizedTest
        @DisplayName("Should convert letters to correct digit values")
        @CsvSource({
            "A, 10",
            "B, 11",
            "Z, 35",
            "a, 10",
            "z, 35"
        })
        void testLetterConversion(String letter, String expected) {
            logger.info("Testing letter conversion: {} -> {}", letter, expected);
            assertEquals(expected, isinHelper.convertToDigits(letter),
                "Letter " + letter + " should convert to " + expected);
        }

        @ParameterizedTest
        @DisplayName("Should keep digits unchanged")
        @ValueSource(strings = {"0", "1", "5", "9"})
        void testDigitConversion(String digit) {
            logger.info("Testing digit unchanged: {}", digit);
            assertEquals(digit, isinHelper.convertToDigits(digit),
                "Digit should remain unchanged: " + digit);
        }

        @Test
        @DisplayName("Should convert US country code correctly")
        void testUSCountryCode() {
            // U = 30, S = 28
            String result = isinHelper.convertToDigits("US");
            logger.info("US converted to: {}", result);
            assertEquals("3028", result, "US should convert to 3028");
        }

        @Test
        @DisplayName("Should convert full ISIN correctly")
        void testFullISINConversion() {
            String isin = "US0378331005";
            String expected = "30280378331005";
            String result = isinHelper.convertToDigits(isin);
            logger.info("Full ISIN {} converted to: {}", isin, result);
            assertEquals(expected, result, "Full ISIN conversion should match expected");
        }
    }

    @Nested
    @DisplayName("calculateLuhnCheckDigit Tests")
    class CalculateLuhnCheckDigitTests {

        @Test
        @DisplayName("Should return 0 for valid Luhn sequence")
        void testValidLuhnSequence() {
            // Known valid Luhn sequence
            String validSequence = "79927398713";
            int result = isinHelper.calculateLuhnCheckDigit(validSequence);
            logger.info("Luhn check for {}: {}", validSequence, result);
            assertEquals(0, result, "Valid Luhn sequence should return 0");
        }

        @Test
        @DisplayName("Should return non-zero for invalid Luhn sequence")
        void testInvalidLuhnSequence() {
            String invalidSequence = "79927398710";
            int result = isinHelper.calculateLuhnCheckDigit(invalidSequence);
            logger.info("Luhn check for {}: {}", invalidSequence, result);
            assertNotEquals(0, result, "Invalid Luhn sequence should not return 0");
        }
    }

    @Nested
    @DisplayName("validateChecksum Tests")
    class ValidateChecksumTests {

        @ParameterizedTest
        @DisplayName("Should validate correct checksums for real ISINs")
        @ValueSource(strings = {
            "US0378331005",
            "US5949181045",
            "GB0002634946",
            "DE0007164600"
        })
        void testValidChecksums(String isin) {
            logger.info("Testing checksum validation for: {}", isin);
            assertTrue(isinHelper.validateChecksum(isin), "Checksum should be valid for " + isin);
        }

        @ParameterizedTest
        @DisplayName("Should reject incorrect checksums")
        @ValueSource(strings = {
            "US0378331001",
            "US0378331002",
            "US0378331003",
            "US0378331009"
        })
        void testInvalidChecksums(String isin) {
            logger.info("Testing invalid checksum for: {}", isin);
            assertFalse(isinHelper.validateChecksum(isin), "Checksum should be invalid for " + isin);
        }
    }

    @Nested
    @DisplayName("calculateCheckDigit Tests")
    class CalculateCheckDigitTests {

        @ParameterizedTest
        @DisplayName("Should calculate correct check digits for known ISINs")
        @CsvSource({
            "US037833100, 5",   // Apple Inc.
            "US594918104, 5",   // Microsoft
            "GB000263494, 6",   // BAE Systems
            "DE000716460, 0"    // SAP SE
        })
        void testCalculateCheckDigit(String isinWithoutCheckDigit, int expectedCheckDigit) {
            logger.info("Calculating check digit for: {}", isinWithoutCheckDigit);
            int calculatedCheckDigit = isinHelper.calculateCheckDigit(isinWithoutCheckDigit);
            logger.info("Calculated check digit: {}, expected: {}", calculatedCheckDigit, expectedCheckDigit);
            assertEquals(expectedCheckDigit, calculatedCheckDigit,
                "Check digit should be " + expectedCheckDigit + " for " + isinWithoutCheckDigit);
        }

        @Test
        @DisplayName("Should throw exception for null input")
        void testNullInput() {
            logger.info("Testing null input for calculateCheckDigit");
            assertThrows(IllegalArgumentException.class, () -> isinHelper.calculateCheckDigit(null),
                "Should throw exception for null input");
        }

        @ParameterizedTest
        @DisplayName("Should throw exception for invalid length")
        @ValueSource(strings = {"US03783310", "US0378331005", ""})
        void testInvalidLength(String input) {
            logger.info("Testing invalid length input: {}", input);
            assertThrows(IllegalArgumentException.class, () -> isinHelper.calculateCheckDigit(input),
                "Should throw exception for invalid length: " + input);
        }
    }

    @Nested
    @DisplayName("generateISIN Tests")
    class GenerateISINTests {

        @ParameterizedTest
        @DisplayName("Should generate complete ISINs with correct check digits")
        @CsvSource({
            "US037833100, US0378331005",   // Apple Inc.
            "US594918104, US5949181045",   // Microsoft
            "GB000263494, GB0002634946",   // BAE Systems
            "DE000716460, DE0007164600"    // SAP SE
        })
        void testGenerateISIN(String input, String expectedISIN) {
            logger.info("Generating ISIN from: {}", input);
            String generatedISIN = isinHelper.generateISIN(input);
            logger.info("Generated: {}, expected: {}", generatedISIN, expectedISIN);
            assertEquals(expectedISIN, generatedISIN, "Generated ISIN should match expected");
        }

        @Test
        @DisplayName("Generated ISIN should pass validation")
        void testGeneratedISINIsValid() {
            String input = "US037833100";
            String generatedISIN = isinHelper.generateISIN(input);
            logger.info("Generated ISIN: {}", generatedISIN);
            assertTrue(isinHelper.isValidISIN(generatedISIN), "Generated ISIN should be valid");
        }

        @Test
        @DisplayName("Should throw exception for null input")
        void testNullInput() {
            logger.info("Testing null input for generateISIN");
            assertThrows(IllegalArgumentException.class, () -> isinHelper.generateISIN(null),
                "Should throw exception for null input");
        }

        @ParameterizedTest
        @DisplayName("Should throw exception for invalid length")
        @ValueSource(strings = {"US03783310", "US0378331005", ""})
        void testInvalidLength(String input) {
            logger.info("Testing invalid length input: {}", input);
            assertThrows(IllegalArgumentException.class, () -> isinHelper.generateISIN(input),
                "Should throw exception for invalid length: " + input);
        }

        @Test
        @DisplayName("Should handle lowercase input")
        void testLowercaseInput() {
            String lowercase = "us037833100";
            String uppercase = "US037833100";

            String generatedFromLower = isinHelper.generateISIN(lowercase);
            String generatedFromUpper = isinHelper.generateISIN(uppercase);

            logger.info("From lowercase: {}, from uppercase: {}", generatedFromLower, generatedFromUpper);
            assertEquals(generatedFromUpper, generatedFromLower,
                "Generated ISIN should be the same regardless of input case");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Round trip: generate and validate ISIN")
        void testGenerateAndValidate() {
            String[] testInputs = {"US037833100", "GB000263494", "DE000716460", "FR000012057"};

            for (String input : testInputs) {
                String generated = isinHelper.generateISIN(input);
                logger.info("Generated ISIN from {}: {}", input, generated);

                assertTrue(isinHelper.isValidISIN(generated),
                    "Generated ISIN should be valid: " + generated);
                assertEquals(12, generated.length(), "ISIN should be 12 characters");
            }
        }

        @Test
        @DisplayName("Check digit extraction and recalculation")
        void testCheckDigitRecalculation() {
            String validISIN = "US0378331005";
            String isinWithoutCheck = validISIN.substring(0, 11);

            int extractedCheckDigit = Character.getNumericValue(validISIN.charAt(11));
            int calculatedCheckDigit = isinHelper.calculateCheckDigit(isinWithoutCheck);

            logger.info("Extracted check digit: {}, calculated: {}", extractedCheckDigit, calculatedCheckDigit);
            assertEquals(extractedCheckDigit, calculatedCheckDigit,
                "Extracted and calculated check digits should match");
        }

        @Test
        @DisplayName("Multiple country code validation")
        void testMultipleCountryCodes() {
            String[] validISINs = {
                "US0378331005",  // United States
                "GB0002634946",  // United Kingdom
                "DE0007164600",  // Germany
                "FR0000120578",  // France
                "JP3633400001",  // Japan
                "CH0012005267"   // Switzerland
            };

            for (String isin : validISINs) {
                String countryCode = isin.substring(0, 2);
                logger.info("Testing country code {} from ISIN {}", countryCode, isin);

                assertTrue(isinHelper.isValidCountryCode(countryCode),
                    "Country code should be valid: " + countryCode);
                assertTrue(isinHelper.isValidISIN(isin),
                    "Full ISIN should be valid: " + isin);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle ISIN with all zeros in NSIN")
        void testAllZerosNSIN() {
            String isin = isinHelper.generateISIN("US000000000");
            logger.info("Generated ISIN with all zeros NSIN: {}", isin);
            assertTrue(isinHelper.isValidISIN(isin), "ISIN with all zeros should be valid");
        }

        @Test
        @DisplayName("Should handle ISIN with all letters in NSIN")
        void testAllLettersNSIN() {
            String isin = isinHelper.generateISIN("USABCDEFGHI");
            logger.info("Generated ISIN with all letters NSIN: {}", isin);
            assertTrue(isinHelper.isValidISIN(isin), "ISIN with all letters should be valid");
        }

        @Test
        @DisplayName("Should handle ISIN with mixed case")
        void testMixedCaseISIN() {
            String mixedCase = "Us0378331005";
            logger.info("Testing mixed case ISIN: {}", mixedCase);
            assertTrue(isinHelper.isValidISIN(mixedCase), "Mixed case ISIN should be valid");
        }

        @Test
        @DisplayName("Check digit range should be 0-9")
        void testCheckDigitRange() {
            // Test various inputs to ensure check digit is always 0-9
            String[] inputs = {"US037833100", "US123456789", "GB000000000", "DEABCDEFGHI"};

            for (String input : inputs) {
                int checkDigit = isinHelper.calculateCheckDigit(input);
                logger.info("Check digit for {}: {}", input, checkDigit);
                assertTrue(checkDigit >= 0 && checkDigit <= 9,
                    "Check digit should be between 0 and 9, got: " + checkDigit);
            }
        }

        @ParameterizedTest
        @DisplayName("Should reject strings with whitespace")
        @ValueSource(strings = {" US0378331005", "US0378331005 ", "US 378331005", "  "})
        void testWhitespaceHandling(String input) {
            logger.info("Testing ISIN with whitespace: '{}'", input);
            assertFalse(isinHelper.isValidISIN(input), "ISIN with whitespace should be invalid");
        }
    }
}

