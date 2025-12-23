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
@DisplayName("Portfolio Controller Tests")
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Valid ISIN constants for testing (Apple Inc. and Microsoft)
    private static final String VALID_ISIN_1 = "US0378331005";
    private static final String VALID_ISIN_2 = "US5949181045";

    private String createSingleBondPortfolioJson(String isin, String issueDate, String maturityDate,
                                                  String couponRate, String faceValue, String marketValue,
                                                  String paymentTerm, String quantity) {
        return String.format("""
            [
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
            ]
            """, isin, issueDate, maturityDate, couponRate, faceValue, marketValue, paymentTerm, quantity);
    }

    // Convenience overload that accepts int values and converts them to strings
    private String createSingleBondPortfolioJson(String isin, String issueDate, String maturityDate,
                                                  int couponRate, int faceValue, int marketValue,
                                                  String paymentTerm, int quantity) {
        return createSingleBondPortfolioJson(isin, issueDate, maturityDate,
                String.valueOf(couponRate), String.valueOf(faceValue), String.valueOf(marketValue),
                paymentTerm, String.valueOf(quantity));
    }

    private String createTwoBondPortfolioJson(
            String isin1, String issueDate1, String maturityDate1, String couponRate1,
            String faceValue1, String marketValue1, String paymentTerm1, String quantity1,
            String isin2, String issueDate2, String maturityDate2, String couponRate2,
            String faceValue2, String marketValue2, String paymentTerm2, String quantity2) {
        return String.format("""
            [
                {
                    "isin": "%s",
                    "issueDate": "%s",
                    "maturityDate": "%s",
                    "couponRate": "%s",
                    "faceValue": "%s",
                    "marketValue": "%s",
                    "paymentTerm": "%s",
                    "quantity": "%s"
                },
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
            ]
            """, isin1, issueDate1, maturityDate1, couponRate1, faceValue1, marketValue1, paymentTerm1, quantity1,
                 isin2, issueDate2, maturityDate2, couponRate2, faceValue2, marketValue2, paymentTerm2, quantity2);
    }

    // Convenience overload that accepts int values and converts them to strings
    private String createTwoBondPortfolioJson(
            String isin1, String issueDate1, String maturityDate1, int couponRate1,
            int faceValue1, int marketValue1, String paymentTerm1, int quantity1,
            String isin2, String issueDate2, String maturityDate2, int couponRate2,
            int faceValue2, int marketValue2, String paymentTerm2, int quantity2) {
        return createTwoBondPortfolioJson(
                isin1, issueDate1, maturityDate1, String.valueOf(couponRate1),
                String.valueOf(faceValue1), String.valueOf(marketValue1), paymentTerm1, String.valueOf(quantity1),
                isin2, issueDate2, maturityDate2, String.valueOf(couponRate2),
                String.valueOf(faceValue2), String.valueOf(marketValue2), paymentTerm2, String.valueOf(quantity2));
    }

    @Nested
    @DisplayName("Date Format Validation Tests")
    class DateFormatValidationTests {

        @Test
        @DisplayName("Should accept valid ISO date format YYYY-MM-DD for portfolio")
        void shouldAcceptValidISODateFormat() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].issueDate").value("2023-01-15"))
                    .andExpect(jsonPath("$.bonds[0].maturityDate").value("2033-01-15"));
        }

        @Test
        @DisplayName("Should reject invalid date format MM/DD/YYYY in portfolio")
        void shouldRejectInvalidDateFormatSlashes() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "01/15/2023", "01/15/2033",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid date format DD-MM-YYYY in portfolio")
        void shouldRejectInvalidDateFormatDDMMYYYY() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "15-01-2023", "15-01-2033",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject date with invalid month in portfolio")
        void shouldRejectInvalidMonth() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-13-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept leap year date February 29 in portfolio")
        void shouldAcceptLeapYearDate() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2024-02-29", "2034-02-28",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].issueDate").value("2024-02-29"));
        }

        @Test
        @DisplayName("Should reject non-leap year February 29 in portfolio")
        void shouldRejectNonLeapYearFeb29() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-02-29", "2033-01-15",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate dates for all bonds in portfolio")
        void shouldValidateDatesForAllBonds() throws Exception {
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "semiannual", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].issueDate").value("2023-01-15"))
                    .andExpect(jsonPath("$.bonds[0].maturityDate").value("2033-01-15"))
                    .andExpect(jsonPath("$.bonds[1].issueDate").value("2022-06-01"))
                    .andExpect(jsonPath("$.bonds[1].maturityDate").value("2032-06-01"));
        }
    }

    @Nested
    @DisplayName("Coupon Rate Validation Tests - Basis Points")
    class CouponRateValidationTests {

        @Test
        @DisplayName("Should accept coupon rate in basis points (500 = 5.00%)")
        void shouldAcceptCouponRateInBasisPoints() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].couponRate").value(500));
        }

        @Test
        @DisplayName("Should accept different coupon rates for multiple bonds in basis points")
        void shouldAcceptDifferentCouponRatesInBasisPoints() throws Exception {
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "semiannual", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].couponRate").value(500))
                    .andExpect(jsonPath("$.bonds[1].couponRate").value(650));
        }

        @Test
        @DisplayName("Should accept zero coupon rate (0 bps) in portfolio")
        void shouldAcceptZeroCouponRate() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                0, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].couponRate").value(0));
        }

        @Test
        @DisplayName("Should correctly interpret 100 bps as 1.00% coupon rate in portfolio")
        void shouldInterpret100BpsAsOnePercent() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                100, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].couponRate").value(100));
        }

        @Test
        @DisplayName("Should correctly interpret high coupon rates (1000 bps = 10.00%)")
        void shouldInterpretHighCouponRate() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                1000, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].couponRate").value(1000));
        }
    }

    @Nested
    @DisplayName("Face Value and Market Value Validation Tests - Cents")
    class ValueValidationTests {

        @Test
        @DisplayName("Should accept faceValue and marketValue in cents")
        void shouldAcceptValuesInCents() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].faceValue").value(100000))
                    .andExpect(jsonPath("$.bonds[0].marketValue").value(95000));
        }

        @Test
        @DisplayName("Should calculate totalPortfolioValue correctly in cents")
        void shouldCalculateTotalPortfolioValueInCents() throws Exception {
            // Bond 1: marketValue 95000 * quantity 10 = 950000
            // Bond 2: marketValue 105000 * quantity 5 = 525000
            // Total: 1475000 cents = $14,750
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "semiannual", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPortfolioValue").value(1475000));
        }

        @Test
        @DisplayName("Should accept large values in cents")
        void shouldAcceptLargeValuesInCents() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 10000000, 9500000, "semiannual", 100
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].faceValue").value(10000000))
                    .andExpect(jsonPath("$.bonds[0].marketValue").value(9500000));
        }

        @Test
        @DisplayName("Should handle premium bonds (marketValue > faceValue)")
        void shouldHandlePremiumBonds() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                650, 100000, 105000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].faceValue").value(100000))
                    .andExpect(jsonPath("$.bonds[0].marketValue").value(105000));
        }

        @Test
        @DisplayName("Should handle discount bonds (marketValue < faceValue)")
        void shouldHandleDiscountBonds() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                300, 100000, 85000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].faceValue").value(100000))
                    .andExpect(jsonPath("$.bonds[0].marketValue").value(85000));
        }
    }

    @Nested
    @DisplayName("Payment Term Validation Tests")
    class PaymentTermValidationTests {

        @Test
        @DisplayName("Should accept 'semiannual' payment term in portfolio")
        void shouldAcceptSemiannualPaymentTerm() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].paymentTerm").value("semiannual"));
        }

        @Test
        @DisplayName("Should accept 'annual' payment term in portfolio")
        void shouldAcceptAnnualPaymentTerm() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "annual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].paymentTerm").value("annual"));
        }

        @Test
        @DisplayName("Should accept 'quarterly' payment term in portfolio")
        void shouldAcceptQuarterlyPaymentTerm() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "quarterly", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].paymentTerm").value("quarterly"));
        }

        @Test
        @DisplayName("Should accept 'monthly' payment term in portfolio")
        void shouldAcceptMonthlyPaymentTerm() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "monthly", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].paymentTerm").value("monthly"));
        }

        @Test
        @DisplayName("Should accept mixed payment terms in portfolio")
        void shouldAcceptMixedPaymentTerms() throws Exception {
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "quarterly", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].paymentTerm").value("semiannual"))
                    .andExpect(jsonPath("$.bonds[1].paymentTerm").value("quarterly"));
        }

        @Test
        @DisplayName("Should reject invalid payment term 'weekly' in portfolio")
        void shouldRejectInvalidPaymentTermWeekly() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "weekly", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid payment term 'biweekly' in portfolio")
        void shouldRejectInvalidPaymentTermBiweekly() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "biweekly", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject empty payment term in portfolio")
        void shouldRejectEmptyPaymentTerm() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject if any bond has invalid payment term")
        void shouldRejectIfAnyBondHasInvalidPaymentTerm() throws Exception {
            String invalidJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "invalid", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
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
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 10
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].quantity").value(10));
        }

        @Test
        @DisplayName("Should accept large quantity")
        void shouldAcceptLargeQuantity() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1000
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].quantity").value(1000));
        }

        @Test
        @DisplayName("Should accept quantity of 1")
        void shouldAcceptQuantityOfOne() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 1
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].quantity").value(1));
        }
    }

    @Nested
    @DisplayName("Integration Tests - Full Portfolio Request Validation")
    class IntegrationTests {

        @Test
        @DisplayName("Should successfully analyze valid portfolio request")
        void shouldAnalyzeValidPortfolioRequest() throws Exception {
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "semiannual", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.accountId").value("default-account"))
                    .andExpect(jsonPath("$.bonds").isArray())
                    .andExpect(jsonPath("$.bonds.length()").value(2))
                    .andExpect(jsonPath("$.weightedMacaulayDuration").isNumber())
                    .andExpect(jsonPath("$.weightedModifiedDuration").isNumber())
                    .andExpect(jsonPath("$.totalPortfolioValue").isNumber());
        }

        @Test
        @DisplayName("Should return bond weights in portfolio")
        void shouldReturnBondWeights() throws Exception {
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "semiannual", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].bondWeightInPortfolio").isNumber())
                    .andExpect(jsonPath("$.bonds[1].bondWeightInPortfolio").isNumber())
                    // Weights should sum to approximately 1.0
                    .andExpect(jsonPath("$.bonds[0].bondWeightInPortfolio").value(greaterThan(0.0)))
                    .andExpect(jsonPath("$.bonds[0].bondWeightInPortfolio").value(lessThan(1.0)))
                    .andExpect(jsonPath("$.bonds[1].bondWeightInPortfolio").value(greaterThan(0.0)))
                    .andExpect(jsonPath("$.bonds[1].bondWeightInPortfolio").value(lessThan(1.0)));
        }

        @Test
        @DisplayName("Should return YTM in basis points for each bond")
        void shouldReturnYTMInBasisPointsForEachBond() throws Exception {
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "semiannual", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].ytm").isNumber())
                    .andExpect(jsonPath("$.bonds[1].ytm").isNumber());
        }

        @Test
        @DisplayName("Should return duration in years for each bond")
        void shouldReturnDurationInYearsForEachBond() throws Exception {
            String validJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", 500, 100000, 95000, "semiannual", 10,
                VALID_ISIN_2, "2022-06-01", "2032-06-01", 650, 100000, 105000, "semiannual", 5
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds[0].macaulayDuration").isNumber())
                    .andExpect(jsonPath("$.bonds[0].modifiedDuration").isNumber())
                    .andExpect(jsonPath("$.bonds[1].macaulayDuration").isNumber())
                    .andExpect(jsonPath("$.bonds[1].modifiedDuration").isNumber())
                    // Durations should be positive and reasonable
                    .andExpect(jsonPath("$.bonds[0].macaulayDuration").value(greaterThan(0.0)))
                    .andExpect(jsonPath("$.bonds[1].macaulayDuration").value(greaterThan(0.0)));
        }

        @Test
        @DisplayName("Should handle single bond portfolio")
        void shouldHandleSingleBondPortfolio() throws Exception {
            String validJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                500, 100000, 95000, "semiannual", 100
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds.length()").value(1))
                    .andExpect(jsonPath("$.bonds[0].bondWeightInPortfolio").value(1.0));
        }

        @Test
        @DisplayName("Should handle empty portfolio")
        void shouldHandleEmptyPortfolio() throws Exception {
            String emptyJson = "[]";

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(emptyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bonds").isArray())
                    .andExpect(jsonPath("$.bonds.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Float/Decimal Value Rejection Tests")
    class FloatDecimalValidationTests {

        @Test
        @DisplayName("Should reject coupon rate with decimal point in portfolio")
        void shouldRejectCouponRateWithDecimal() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500.5", "100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject coupon rate with trailing decimal zero in portfolio")
        void shouldRejectCouponRateWithTrailingDecimalZero() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500.0", "100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject face value with decimal point in portfolio")
        void shouldRejectFaceValueWithDecimal() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000.50", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject market value with decimal point in portfolio")
        void shouldRejectMarketValueWithDecimal() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000.99", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject quantity with decimal point in portfolio")
        void shouldRejectQuantityWithDecimal() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "10.5"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject if any bond in portfolio has decimal values")
        void shouldRejectIfAnyBondHasDecimalValues() throws Exception {
            String invalidJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", "500", "100000", "95000", "semiannual", "10",
                VALID_ISIN_2, "2022-06-01", "2032-06-01", "650.5", "100000", "105000", "semiannual", "5"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Comma in Values Rejection Tests")
    class CommaValidationTests {

        @Test
        @DisplayName("Should reject coupon rate with comma in portfolio")
        void shouldRejectCouponRateWithComma() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "1,000", "100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject face value with comma thousands separator in portfolio")
        void shouldRejectFaceValueWithComma() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100,000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject market value with comma thousands separator in portfolio")
        void shouldRejectMarketValueWithComma() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95,000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject quantity with comma in portfolio")
        void shouldRejectQuantityWithComma() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "1,000"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject if any bond in portfolio has comma values")
        void shouldRejectIfAnyBondHasCommaValues() throws Exception {
            String invalidJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", "500", "100000", "95000", "semiannual", "10",
                VALID_ISIN_2, "2022-06-01", "2032-06-01", "650", "100,000", "105000", "semiannual", "5"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Negative Value Validation Tests")
    class NegativeValueValidationTests {

        @Test
        @DisplayName("Should reject negative coupon rate in portfolio")
        void shouldRejectNegativeCouponRate() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "-500", "100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative face value in portfolio")
        void shouldRejectNegativeFaceValue() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "-100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative market value in portfolio")
        void shouldRejectNegativeMarketValue() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "-95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative quantity in portfolio")
        void shouldRejectNegativeQuantity() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "-10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject zero quantity in portfolio")
        void shouldRejectZeroQuantity() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "0"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject if any bond in portfolio has negative values")
        void shouldRejectIfAnyBondHasNegativeValues() throws Exception {
            String invalidJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", "500", "100000", "95000", "semiannual", "10",
                VALID_ISIN_2, "2022-06-01", "2032-06-01", "-650", "100000", "105000", "semiannual", "5"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Invalid Non-Numeric Value Tests")
    class InvalidNonNumericValueTests {

        @Test
        @DisplayName("Should reject alphabetic coupon rate in portfolio")
        void shouldRejectAlphabeticCouponRate() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "abc", "100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject alphabetic face value in portfolio")
        void shouldRejectAlphabeticFaceValue() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "hundred", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject alphabetic market value in portfolio")
        void shouldRejectAlphabeticMarketValue() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "ninety-five", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject alphabetic quantity in portfolio")
        void shouldRejectAlphabeticQuantity() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100000", "95000", "semiannual", "ten"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject mixed alphanumeric values in portfolio")
        void shouldRejectMixedAlphanumericValues() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500bps", "100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject value with currency symbol in portfolio")
        void shouldRejectValueWithCurrencySymbol() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "$100000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject value with spaces in portfolio")
        void shouldRejectValueWithSpaces() throws Exception {
            String invalidJson = createSingleBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15",
                "500", "100 000", "95000", "semiannual", "10"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject if any bond in portfolio has invalid non-numeric values")
        void shouldRejectIfAnyBondHasInvalidValues() throws Exception {
            String invalidJson = createTwoBondPortfolioJson(
                VALID_ISIN_1, "2023-01-15", "2033-01-15", "500", "100000", "95000", "semiannual", "10",
                VALID_ISIN_2, "2022-06-01", "2032-06-01", "abc", "100000", "105000", "semiannual", "5"
            );

            mockMvc.perform(post("/api/portfolios/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }
}

