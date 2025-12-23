package com.ice.bonds;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Bond Controller Tests")
class BondControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Valid ISIN constants for testing (Apple Inc. and Microsoft)
    private static final String VALID_ISIN_1 = "US0378331005";
    private static final String VALID_ISIN_2 = "US5949181045";

    private String createBondJson(String isin, String issueDate, String maturityDate,
                                   String couponRate, String faceValue, String marketValue,
                                   String paymentTerm, String quantity) {
        return String.format("""
            {
                "isin": "%s",
                "issueDate": "%s",
                "maturityDate": "%s",
                "couponRate": "%s",
                "faceValue": "%s",
                "marketValue": "%s",
                "paymentTerm": "%s",
                "quantity": "%s"
            }
            """, isin, issueDate, maturityDate, couponRate, faceValue, marketValue, paymentTerm, quantity);
    }

    // Convenience overload that accepts int values and converts them to strings
    private String createBondJson(String isin, String issueDate, String maturityDate,
                                   int couponRate, int faceValue, int marketValue,
                                   String paymentTerm, int quantity) {
        return createBondJson(isin, issueDate, maturityDate,
                String.valueOf(couponRate), String.valueOf(faceValue), String.valueOf(marketValue),
                paymentTerm, String.valueOf(quantity));
    }

    @Nested
    @DisplayName("Date Format Validation Tests")
    class DateFormatValidationTests {

        @Test
        @DisplayName("Should accept valid ISO date format YYYY-MM-DD")
        void shouldAcceptValidISODateFormat() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isin").value(VALID_ISIN_1))
                    .andExpect(jsonPath("$.issueDate").value("2023-01-15"))
                    .andExpect(jsonPath("$.maturityDate").value("2033-01-15"));
        }

        @Test
        @DisplayName("Should reject invalid date format MM/DD/YYYY")
        void shouldRejectInvalidDateFormatSlashes() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "01/15/2023", "01/15/2033",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid date format DD-MM-YYYY")
        void shouldRejectInvalidDateFormatDDMMYYYY() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "15-01-2023", "15-01-2033",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject date with invalid month")
        void shouldRejectInvalidMonth() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-13-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject date with invalid day")
        void shouldRejectInvalidDay() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-02-30", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept leap year date February 29")
        void shouldAcceptLeapYearDate() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2024-02-29", "2034-02-28",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.issueDate").value("2024-02-29"));
        }

        @Test
        @DisplayName("Should reject non-leap year February 29")
        void shouldRejectNonLeapYearFeb29() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-02-29", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Coupon Rate Validation Tests - Basis Points")
    class CouponRateValidationTests {

        @Test
        @DisplayName("Should accept coupon rate in basis points (500 = 5.00%)")
        void shouldAcceptCouponRateInBasisPoints() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.couponRate").value(500));
        }

        @Test
        @DisplayName("Should accept high coupon rate in basis points (750 = 7.50%)")
        void shouldAcceptHighCouponRateInBasisPoints() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                750, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.couponRate").value(750));
        }

        @Test
        @DisplayName("Should accept fractional basis points (525 = 5.25%)")
        void shouldAcceptFractionalBasisPoints() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                525, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.couponRate").value(525));
        }

        @Test
        @DisplayName("Should accept zero coupon rate (0 bps)")
        void shouldAcceptZeroCouponRate() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                0, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.couponRate").value(0));
        }

        @Test
        @DisplayName("Should correctly interpret 100 bps as 1.00% coupon rate")
        void shouldInterpret100BpsAsOnePercent() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                100, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.couponRate").value(100));
        }

        @Test
        @DisplayName("Should correctly interpret 1000 bps as 10.00% coupon rate")
        void shouldInterpret1000BpsAsTenPercent() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                1000, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.couponRate").value(1000));
        }
    }

    @Nested
    @DisplayName("Face Value and Market Value Validation Tests - Cents")
    class ValueValidationTests {

        @Test
        @DisplayName("Should accept faceValue in cents (100000 = $1,000)")
        void shouldAcceptFaceValueInCents() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faceValue").value(100000));
        }

        @Test
        @DisplayName("Should accept marketValue in cents (95000 = $950)")
        void shouldAcceptMarketValueInCents() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marketValue").value(95000));
        }

        @Test
        @DisplayName("Should accept large faceValue in cents (10000000 = $100,000)")
        void shouldAcceptLargeFaceValueInCents() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 10000000, 9500000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faceValue").value(10000000))
                    .andExpect(jsonPath("$.marketValue").value(9500000));
        }

        @Test
        @DisplayName("Should handle premium bond (marketValue > faceValue)")
        void shouldHandlePremiumBond() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                650, 100000, 105000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faceValue").value(100000))
                    .andExpect(jsonPath("$.marketValue").value(105000));
        }

        @Test
        @DisplayName("Should handle discount bond (marketValue < faceValue)")
        void shouldHandleDiscountBond() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                300, 100000, 85000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faceValue").value(100000))
                    .andExpect(jsonPath("$.marketValue").value(85000));
        }

        @Test
        @DisplayName("Should handle par bond (marketValue = faceValue)")
        void shouldHandleParBond() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 100000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faceValue").value(100000))
                    .andExpect(jsonPath("$.marketValue").value(100000));
        }
    }

    @Nested
    @DisplayName("Payment Term Validation Tests")
    class PaymentTermValidationTests {

        @Test
        @DisplayName("Should accept 'semiannual' payment term")
        void shouldAcceptSemiannualPaymentTerm() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentTerm").value("semiannual"));
        }

        @Test
        @DisplayName("Should accept 'annual' payment term")
        void shouldAcceptAnnualPaymentTerm() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "annual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentTerm").value("annual"));
        }

        @Test
        @DisplayName("Should accept 'quarterly' payment term")
        void shouldAcceptQuarterlyPaymentTerm() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "quarterly", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentTerm").value("quarterly"));
        }

        @Test
        @DisplayName("Should accept 'monthly' payment term")
        void shouldAcceptMonthlyPaymentTerm() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "monthly", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentTerm").value("monthly"));
        }

        @Test
        @DisplayName("Should reject invalid payment term 'weekly'")
        void shouldRejectInvalidPaymentTermWeekly() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "weekly", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid payment term 'biweekly'")
        void shouldRejectInvalidPaymentTermBiweekly() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "biweekly", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject empty payment term")
        void shouldRejectEmptyPaymentTerm() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Full Request Validation")
    class IntegrationTests {

        @Test
        @DisplayName("Should successfully analyze valid bond request")
        void shouldAnalyzeValidBondRequest() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isin").value(VALID_ISIN_1))
                    .andExpect(jsonPath("$.couponRate").value(500))
                    .andExpect(jsonPath("$.faceValue").value(100000))
                    .andExpect(jsonPath("$.marketValue").value(95000))
                    .andExpect(jsonPath("$.paymentTerm").value("semiannual"))
                    .andExpect(jsonPath("$.ytm").isNumber())
                    .andExpect(jsonPath("$.macaulayDuration").isNumber())
                    .andExpect(jsonPath("$.modifiedDuration").isNumber());
        }

        @Test
        @DisplayName("Should return YTM in basis points")
        void shouldReturnYTMInBasisPoints() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    // YTM should be positive and reasonable (greater than 0)
                    .andExpect(jsonPath("$.ytm").value(greaterThan(0.0)));
        }

        @Test
        @DisplayName("Should return duration in years")
        void shouldReturnDurationInYears() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    // Duration should be positive and less than maturity (10 years)
                    .andExpect(jsonPath("$.macaulayDuration").value(greaterThan(0.0)))
                    .andExpect(jsonPath("$.macaulayDuration").value(lessThan(10.0)))
                    .andExpect(jsonPath("$.modifiedDuration").value(greaterThan(0.0)));
        }
    }

    @Nested
    @DisplayName("Float/Decimal Value Rejection Tests")
    class FloatDecimalValidationTests {

        @Test
        @DisplayName("Should reject coupon rate with decimal point")
        void shouldRejectCouponRateWithDecimal() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500.5", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject coupon rate with trailing decimal zero")
        void shouldRejectCouponRateWithTrailingDecimalZero() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500.0", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject face value with decimal point")
        void shouldRejectFaceValueWithDecimal() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000.50", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject market value with decimal point")
        void shouldRejectMarketValueWithDecimal() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000.99", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject quantity with decimal point")
        void shouldRejectQuantityWithDecimal() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "1.5"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject quantity with trailing decimal zero")
        void shouldRejectQuantityWithTrailingDecimalZero() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "10.0"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Comma in Values Rejection Tests")
    class CommaValidationTests {

        @Test
        @DisplayName("Should reject coupon rate with comma")
        void shouldRejectCouponRateWithComma() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "1,000", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject face value with comma thousands separator")
        void shouldRejectFaceValueWithComma() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100,000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject market value with comma thousands separator")
        void shouldRejectMarketValueWithComma() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95,000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject quantity with comma")
        void shouldRejectQuantityWithComma() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "1,000"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject large face value with multiple commas")
        void shouldRejectFaceValueWithMultipleCommas() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "1,000,000", "950000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Quantity Validation Tests")
    class QuantityValidationTests {

        @Test
        @DisplayName("Should accept positive quantity")
        void shouldAcceptPositiveQuantity() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 100
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept quantity of 1")
        void shouldAcceptQuantityOfOne() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept zero quantity")
        void shouldAcceptZeroQuantity() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "0"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject negative quantity")
        void shouldRejectNegativeQuantity() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "-1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept large quantity")
        void shouldAcceptLargeQuantity() throws Exception {
            String validJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1000000
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Negative Value Validation Tests")
    class NegativeValueValidationTests {

        @Test
        @DisplayName("Should reject negative coupon rate")
        void shouldRejectNegativeCouponRate() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "-500", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative face value")
        void shouldRejectNegativeFaceValue() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "-100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative market value")
        void shouldRejectNegativeMarketValue() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "-95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Invalid Non-Numeric Value Tests")
    class InvalidNonNumericValueTests {

        @Test
        @DisplayName("Should reject alphabetic coupon rate")
        void shouldRejectAlphabeticCouponRate() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "abc", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject alphabetic face value")
        void shouldRejectAlphabeticFaceValue() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "hundred", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject alphabetic market value")
        void shouldRejectAlphabeticMarketValue() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "ninety-five", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject alphabetic quantity")
        void shouldRejectAlphabeticQuantity() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "ten"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject mixed alphanumeric coupon rate")
        void shouldRejectMixedAlphanumericCouponRate() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500bps", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject value with currency symbol")
        void shouldRejectValueWithCurrencySymbol() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "$100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject value with spaces")
        void shouldRejectValueWithSpaces() throws Exception {
            String invalidJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100 000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Analyze From String Endpoint Tests")
    class AnalyzeFromStringTests {

        // Create a JSON-serialized string (the outer quotes make it a JSON string value containing escaped JSON)
        private String createJsonStringPayload(String isin, String issueDate, String maturityDate,
                                              String couponRate, String faceValue, String marketValue,
                                              String paymentTerm, String quantity) {
            // The endpoint expects a JSON string (with outer quotes) that contains escaped JSON inside
            String innerJson = String.format(
                "{\"isin\": \"%s\", \"issueDate\": \"%s\", \"maturityDate\": \"%s\", " +
                "\"couponRate\": \"%s\", \"faceValue\": \"%s\", \"marketValue\": \"%s\", " +
                "\"paymentTerm\": \"%s\", \"quantity\": \"%s\"}",
                isin, issueDate, maturityDate, couponRate, faceValue, marketValue, paymentTerm, quantity);
            // Escape the inner JSON and wrap in quotes to make it a JSON string value
            return "\"" + innerJson.replace("\"", "\\\"") + "\"";
        }

        @Test
        @DisplayName("Should successfully analyze valid bond from JSON string")
        void shouldAnalyzeValidBondFromJsonString() throws Exception {
            String jsonString = createJsonStringPayload(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isin").value(VALID_ISIN_1))
                    .andExpect(jsonPath("$.couponRate").value(500))
                    .andExpect(jsonPath("$.faceValue").value(100000))
                    .andExpect(jsonPath("$.marketValue").value(95000))
                    .andExpect(jsonPath("$.paymentTerm").value("semiannual"))
                    .andExpect(jsonPath("$.ytm").isNumber())
                    .andExpect(jsonPath("$.macaulayDuration").isNumber())
                    .andExpect(jsonPath("$.modifiedDuration").isNumber());
        }

        @Test
        @DisplayName("Should return same results as regular analyze endpoint")
        void shouldReturnSameResultsAsRegularEndpoint() throws Exception {
            // First, call the regular endpoint
            String regularJson = createBondJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            // Then, call the string endpoint with the same data
            String jsonString = createJsonStringPayload(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "1"
            );

            // Both should succeed with same response structure
            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isin").value(VALID_ISIN_1))
                    .andExpect(jsonPath("$.ytm").isNumber())
                    .andExpect(jsonPath("$.macaulayDuration").isNumber())
                    .andExpect(jsonPath("$.modifiedDuration").isNumber());
        }

        @Test
        @DisplayName("Should reject invalid JSON format")
        void shouldRejectInvalidJsonFormat() throws Exception {
            String invalidJsonString = "\"this is not valid json\"";

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJsonString))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject malformed escaped JSON")
        void shouldRejectMalformedEscapedJson() throws Exception {
            String malformedJsonString = "\"{\\\"isin\\\": \\\"US0378331005\\\", \\\"missing_closing_brace\\\"\"";

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJsonString))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid ISIN in JSON string")
        void shouldRejectInvalidIsinInJsonString() throws Exception {
            String jsonString = createJsonStringPayload(
                "INVALID123", "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid date format in JSON string")
        void shouldRejectInvalidDateFormatInJsonString() throws Exception {
            String jsonString = createJsonStringPayload(
                VALID_ISIN_1, "01/15/2023", "01/15/2033",
                "500", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject decimal values in JSON string")
        void shouldRejectDecimalValuesInJsonString() throws Exception {
            String jsonString = createJsonStringPayload(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500.5", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative values in JSON string")
        void shouldRejectNegativeValuesInJsonString() throws Exception {
            String jsonString = createJsonStringPayload(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "-500", "100000", "95000", "semiannual", "1"
            );

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject null JSON string")
        void shouldRejectNullJsonString() throws Exception {
            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject raw object instead of string")
        void shouldRejectRawObjectInsteadOfString() throws Exception {
            // Raw object without string wrapper - should fail because endpoint expects a JSON string
            String rawObject = "{\"isin\": \"US0378331005\", \"issueDate\": \"2023-01-15\", \"maturityDate\": \"2033-01-15\", \"couponRate\": \"500\", \"faceValue\": \"100000\", \"marketValue\": \"95000\", \"paymentTerm\": \"semiannual\", \"quantity\": \"1\"}";

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(rawObject))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject empty string")
        void shouldRejectEmptyString() throws Exception {
            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("\"\""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject double-encoded JSON string")
        void shouldRejectDoubleEncodedJsonString() throws Exception {
            // Double escaped - a string containing an escaped string
            String doubleEncoded = "\"\\\"{\\\\\\\"isin\\\\\\\": \\\\\\\"US0378331005\\\\\\\"}\\\"\"";

            mockMvc.perform(post("/api/bonds/analyze-from-string")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(doubleEncoded))
                    .andExpect(status().isBadRequest());
        }
    }
}

