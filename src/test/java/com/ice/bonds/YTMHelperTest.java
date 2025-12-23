package com.ice.bonds;

import com.ice.bonds.helper.CommonHelper;
import com.ice.bonds.helper.YTMHelper;
import com.ice.bonds.model.Bond;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("YTM Helper Tests")
class YTMHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(YTMHelperTest.class);

    private CommonHelper commonHelper;
    private YTMHelper ytmHelper;

    @BeforeEach
    void setUp() {
        commonHelper = new CommonHelper();
        ytmHelper = new YTMHelper(commonHelper);
    }

    @Nested
    @DisplayName("calculateYTM Tests")
    class CalculateYTMTests {


        @ParameterizedTest
        @ValueSource(strings = {"annual", "semiannual", "quarterly", "monthly"})
        @DisplayName("Should calculate YTM for bond at par value with different payment terms")
        void testYTMAtParValueDifferentPaymentTerms(String paymentTerm) {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int faceValue = 100000;
            int marketValue = 100000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm,1);

            // When/Then: Should not throw exception
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for payment term {}: {} bps equal to coupon rate: {}", paymentTerm, ytm, couponRate);
                assertEquals(couponRate, ytm, "YTM should equal coupon rate for all payment terms" + paymentTerm);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"2024-01-01", "2026-01-01", "2028-06-01", "2030-10-31"})
        @DisplayName("Should calculate YTM for bond at par value with current dates")
        void testYTMAtParValueDifferentCurrentDates(String currentDateString) {
            // Given
            LocalDate currentDate = LocalDate.parse(currentDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int faceValue = 100000;
            int marketValue = 100000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, "semiannual");

            // When/Then: Should not throw exception
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for Current Date {}: {} bps equal to coupon rate: {}", currentDate, ytm, couponRate);
                assertEquals(couponRate, ytm, "YTM should equal coupon rate for all current dates" + currentDate);
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {100001, 100010, 100100, 101000, 110000,200000,300000,500000,1000000,10000000})
        @DisplayName("Should calculate YTM for bond at premium value with different market values")
        void testYTMAtPremiumDifferentMarketValue(int marketValue) {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int faceValue = 100000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, "semiannual");

            // When/Then: Should not throw exception
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for Market Value {}: {} bps less than coupon rate: {}", marketValue, ytm, couponRate);
                assertTrue(ytm < couponRate, "YTM at premium should be less than coupon rate");
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {99999, 99990, 99900, 99000, 90000,50000,30000,10000,1})
        @DisplayName("Should calculate YTM for bond at premium value with different face values")
        void testYTMAtPremiumDifferentFaceValue(int faceValue) {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int marketValue = 100000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, "semiannual");

            // When/Then: Should not throw exception
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for Face Value {}: {} bps less than coupon rate: {}", faceValue, ytm, couponRate);
                assertTrue(ytm < couponRate, "YTM at premium should be less than coupon rate");
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {99999, 99990, 99900, 99000, 90000,50000,30000,10000,1})
        @DisplayName("Should calculate YTM for bond at discount value with different market values")
        void testYTMAtDiscountDifferentMarketValue(int marketValue) {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int faceValue = 100000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, "semiannual");

            // When/Then: Should not throw exception
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for Market Value {}: {} bps greater than coupon rate: {}", marketValue, ytm, couponRate);
                assertTrue(ytm > couponRate, "YTM at discount should be greater than coupon rate");
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {100001, 100010, 100100, 101000, 110000,200000,300000,500000,1000000,10000000})
        @DisplayName("Should calculate YTM for bond at discount value with different face values")
        void testYTMAtDiscountDifferentFaceValue(int faceValue) {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int marketValue = 100000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, "semiannual");

            // When/Then: Should not throw exception
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for Face Value {}: {} bps greater than coupon rate: {}", faceValue, ytm, couponRate);
                assertTrue(ytm > couponRate, "YTM at discount should be greater than coupon rate");
            });
        }


        @Test
        @DisplayName("Should throw exception for invalid payment term")
        void testInvalidPaymentTerm() {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int faceValue = 100000;
            int marketValue = 100000;
            String invalidPaymentTerm = "biweekly";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, invalidPaymentTerm);

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> ytmHelper.calculateYTM(currentDate, bond));
        }

        @ParameterizedTest
        @ValueSource(strings = {"2025-01-01", "2024-01-02"})
        @DisplayName("Should throw exception for matured bond")
        void testMaturedBond(String currentDateString) {
            // Given: Current date is after maturation date
            LocalDate issueDate = LocalDate.of(2014, 1, 1);
            LocalDate maturationDate = LocalDate.of(2024, 1, 1);

            LocalDate currentDate = LocalDate.parse(currentDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            int couponRate = 600;
            int faceValue = 100000;
            int marketValue = 100000;
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> ytmHelper.calculateYTM(currentDate, bond), "Should throw exception for matured bond");
        }

        @Test
        @DisplayName("Should throw exception for bond maturing today")
        void testBondMaturingToday() {
            // Given
            LocalDate issueDate = LocalDate.of(2014, 1, 1);
            LocalDate maturationDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            int couponRate = 600;
            int faceValue = 100000;
            int marketValue = 100000;
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> ytmHelper.calculateYTM(currentDate, bond), "Should throw exception for bond maturing today");
        }

        @Test
        @DisplayName("Should handle bond close to maturity")
        void testBondCloseToMaturity() {
            // Given: Bond with very short time to maturity
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2033, 12, 31); // Almost at first payment
            LocalDate maturationDate = LocalDate.of(2034, 1, 1); // Just 1 day away
            int couponRate = 600;
            int faceValue = 100000;
            int marketValue = 100000;
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then: Should handle without error
            // Note: YTM approximation formula may produce unusual values for bonds very close to maturity

            assertDoesNotThrow(() -> logger.info("Testing bond close to maturity on date: {}, calculated YTM: {}",  currentDate, ytmHelper.calculateYTM(currentDate, bond)));



        }

        @Test
        @DisplayName("Should handle zero coupon bond")
        void testZeroCouponBond() {
            // Given: Zero coupon bond
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 0; // Zero coupon
            int faceValue = 100000;
            int marketValue = 50000; // Deep discount
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When
            double ytm = ytmHelper.calculateYTM(currentDate, bond);

            logger.info("Calculated YTM for zero coupon bond: {} bps", ytm);

            // Then: YTM should be positive despite zero coupon
            assertTrue(ytm > 0, "Zero coupon bond should have positive YTM");
        }

        @ParameterizedTest
        @DisplayName("Should calculate YTM for short-term bond")
        @ValueSource(strings = {"annual", "semiannual", "quarterly", "monthly"})
        void testShortTermBond(String paymentTerm) {
            // Given: Bond with 1 year to maturity
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2025, 1, 1);
            int couponRate = 400; // 4%
            int faceValue = 100000;
            int marketValue = 99000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for short-term bond: {} bps", ytm);
                assertTrue(ytm > 0, "Short-term bond should have positive YTM");
            });
        }

        @ParameterizedTest
        @DisplayName("Should calculate YTM for long-term bond")
        @ValueSource(strings = {"annual", "semiannual", "quarterly", "monthly"})
        void testLongTermBond(String paymentTerm) {
            // Given: Bond with 30 years to maturity
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2054, 1, 1);
            int couponRate = 500; // 5%
            int faceValue = 100000;
            int marketValue = 99000;

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for long-term bond: {} bps", ytm);
                assertTrue(ytm > 0, "Long-term bond should have positive YTM");
            });
        }
    }

    @Nested
    @DisplayName("calculateRemainingPeriods Tests (via reflection)")
    class CalculateRemainingPeriodsTests {

        private int invokeCalculateRemainingPeriods(LocalDate currentDate, LocalDate issueDate,
                                                    LocalDate maturationDate, int periodsPerPaymentTerm) {
            // This method is now in CommonHelper, which is public
            return commonHelper.calculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);
        }

        @Test
        @DisplayName("Should count correct periods for semiannual bond from issue date")
        void testSemiannualPeriodsFromIssue() throws Exception {
            // Given: 10-year semiannual bond at issue date
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 2;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);

            logger.info("Semiannual periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);

            // Then: Should have 20 periods (10 years * 2 periods/year)
            assertEquals(20, periods, "10-year semiannual bond should have 20 periods");
        }

        @Test
        @DisplayName("Should count correct periods mid-life of bond")
        void testPeriodsAtMidLife() throws Exception {
            // Given: 10-year bond, 5 years elapsed
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2029, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 2;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);

            logger.info("Semiannual periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);

            // Then: Should have 11 periods remaining (includes payment on current date)
            assertEquals(11, periods, "Should have 11 periods remaining");
        }

        @Test
        @DisplayName("Should include payment date when current date equals payment date")
        void testOnPaymentDate() throws Exception {
            // Given: Current date is exactly on a payment date
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 7, 1); // Exactly 6 months (semiannual payment)
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 2;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);


            logger.info("Semiannual periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);


            // Then: Should include that payment in count
            assertEquals(20, periods, "Should include current payment date in remaining periods");
        }

        @Test
        @DisplayName("Should handle quarterly payments")
        void testQuarterlyPayments() throws Exception {
            // Given: 5-year quarterly bond
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2029, 1, 1);
            int periodsPerPaymentTerm = 4;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);


            logger.info("Quarterly periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);


            // Then: Should have 20 periods (5 years * 4 periods/year)
            assertEquals(20, periods, "5-year quarterly bond should have 20 periods");
        }

        @Test
        @DisplayName("Should handle quarterly payments")
        void testQuarterlyPaymentsAtMidLife() throws Exception {
            // Given: 5-year quarterly bond
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2029, 1, 2); // Day after 20th payment
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 4;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);

            logger.info("Quarterly periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);

            // Then: Should have 20 periods (5 years * 4 periods/year)
            assertEquals(20, periods, "5-year quarterly bond should have 20 periods");
        }

        @Test
        @DisplayName("Should handle annual payments")
        void testAnnualPayments() throws Exception {
            // Given: 10-year annual bond
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 1;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);


            logger.info("Annual periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);
            // Then: Should have 10 periods (10 years * 1 period/year)
            assertEquals(10, periods, "10-year annual bond should have 10 periods");
        }

        @Test
        @DisplayName("Should handle annual payments")
        void testAnnualPaymentsAtMidLife() throws Exception {
            // Given: 10-year annual bond
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2029, 1, 2); // Day after payment 5th payment
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 1;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);


            logger.info("Annual periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);


            // Then: Should have 5 periods (5 years remaining)
            assertEquals(5, periods, "10-year annual bond should have 10 periods");
        }

        @Test
        @DisplayName("Should handle annual payments")
        void testMonthlyPayments() throws Exception {
            // Given: 10-year annual bond
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 12;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);


            logger.info("Monthly periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);
            // Then: Should have 10 periods (10 years * 1 period/year)
            assertEquals(120, periods, "10-year annual bond should have 10 periods");
        }

        @Test
        @DisplayName("Should handle annual payments")
        void testMonthlyPaymentsAtMidLife() throws Exception {
            // Given: 10-year annual bond
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2029, 1, 2); // Day after payment 5th payment
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 12;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);


            logger.info("Monthly periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);


            // Then: Should have 5 periods (5 years remaining)
            assertEquals(60, periods, "10-year annual bond should have 10 periods");
        }

        @Test
        @DisplayName("Should throw exception for matured bond")
        void testMaturedBondThrowsException() {
            // Given: Current date is after maturation
            LocalDate issueDate = LocalDate.of(2014, 1, 1);
            LocalDate currentDate = LocalDate.of(2025, 1, 1);
            LocalDate maturationDate = LocalDate.of(2024, 1, 1);
            int periodsPerPaymentTerm = 2;

            // When/Then
            assertThrows(Exception.class, () -> invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm));
        }

        @Test
        @DisplayName("Should handle one period remaining")
        void testOnePeriodRemaining() throws Exception {
            // Given: Just one period left
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2033, 7, 2); // Just after second-to-last payment
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int periodsPerPaymentTerm = 2;

            // When
            int periods = invokeCalculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);

            logger.info("Semiannual periods from issue: {} (Issue: {}, Current: {}, Maturation: {})",
                    periods, issueDate, currentDate, maturationDate);

            // Then: Should have 1 period remaining
            assertEquals(1, periods, "Should have 1 period remaining");
        }
    }

    @Nested
    @DisplayName("calculateFractionalPeriod Tests (via reflection)")
    class CalculateFractionalPeriodTests {

        private double invokeCalculateFractionalPeriod(LocalDate issueDate, LocalDate currentDate,
                                                       int periodsPerPaymentTerm) {
            // This method is now in CommonHelper, which is public
            return commonHelper.calculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);
        }

        @Test
        @DisplayName("Should return 0.0 at start of period")
        void testAtStartOfPeriod() throws Exception {
            // Given: Current date is exactly at issue date
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            int periodsPerPaymentTerm = 2;

            // When
            double fractional = invokeCalculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);

            // Then: Should be 0.0 (no time elapsed)
            assertEquals(0.0, fractional, 0.001, "Fractional period should be 0.0 at start");
        }

        @Test
        @DisplayName("Should return 0.5 at mid-period")
        void testAtMidPeriod() throws Exception {
            // Given: Current date is roughly halfway through semiannual period
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 4, 1); // Approximately 3 months (half of 6-month period)
            int periodsPerPaymentTerm = 2;

            // When
            double fractional = invokeCalculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);

            logger.info("Fractional period at mid-period: {} (Issue: {}, Current: {}, Periods per term: {})",
                    fractional, issueDate, currentDate, periodsPerPaymentTerm);

            // Then: Should be 0.5
            assertEquals(0.5, fractional, "Fractional period should be 0.5 at mid-period, was: " + fractional);
        }

        @Test
        @DisplayName("Should return close to 1.0 at end of period")
        void testAtEndOfPeriod() throws Exception {
            // Given: Current date is just before payment date
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 6, 30); // Day before semiannual payment
            int periodsPerPaymentTerm = 2;

            // When
            double fractional = invokeCalculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);

            logger.info("Fractional period at end of period: {} (Issue: {}, Current: {}, Periods per term: {})",
                    fractional, issueDate, currentDate, periodsPerPaymentTerm);

            // Then: Should be close to 1.0
            assertTrue(fractional > 0.98, "Fractional period should be close to 1.0, was: " + fractional);
        }

        @Test
        @DisplayName("Should handle multiple periods elapsed")
        void testMultiplePeriodsElapsed() throws Exception {
            // Given: Current date is 3 months into second semiannual period
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 10, 1); // 9 months from issue (1.5 periods)
            int periodsPerPaymentTerm = 2;

            // When
            double fractional = invokeCalculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);

            logger.info("Fractional period with multiple periods elapsed: {} (Issue: {}, Current: {}, Periods per term: {})",
                    fractional, issueDate, currentDate, periodsPerPaymentTerm);

            // Then: Should be  0.5 (halfway through second period)
            assertEquals(0.5, fractional, "Fractional period should represent position in current period, was: " + fractional);
        }

        @Test
        @DisplayName("Should handle quarterly periods")
        void testQuarterlyPeriods() throws Exception {
            // Given: Quarterly bond, 1.5 months into period
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate currentDate = LocalDate.of(2024, 2, 15); // ~1.5 months from issue
            int periodsPerPaymentTerm = 4;

            // When
            double fractional = invokeCalculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);

            logger.info("Fractional period for quarterly bond: {} (Issue: {}, Current: {}, Periods per term: {})",
                    fractional, issueDate, currentDate, periodsPerPaymentTerm);

            // Then: Should be around 0.5 (halfway through 3-month period)
            assertTrue(fractional >= 0.48 && fractional <= 0.52,
                      "Fractional period should be between 0 and 1");
        }

        @Test
        @DisplayName("Should always return value between 0.0 and 1.0")
        void testFractionalRangeBounds() throws Exception {
            // Given: Various dates
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            int periodsPerPaymentTerm = 2;

            LocalDate[] testDates = {
                LocalDate.of(2024, 1, 1), // 0 days into period => 0.0
                LocalDate.of(2024, 2, 15),  // ~1.5 months into period => ~0.25
                LocalDate.of(2024, 4, 1), // mid-period => 0.5
                LocalDate.of(2024, 6, 30), // day before 1st period payment =>  < 1.0
                LocalDate.of(2024, 7, 1), // start of 2nd period => 1.0 as payment not yet paid, so fractional should be 1.0
                LocalDate.of(2024, 7, 2), // start of 2nd period + 1 day => small fraction
                LocalDate.of(2025, 1, 1) // start of 3rd period => 1.0 as payment not yet paid, so fractional should be 1.0
            };

            // When/Then
            for (LocalDate currentDate : testDates) {
                double fractional = invokeCalculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);
                logger.info("Fractional period: {} (Issue: {}, Current: {}, Periods per term: {})",
                            fractional, issueDate, currentDate, periodsPerPaymentTerm);
                assertTrue(fractional >= 0.0 && fractional <= 1.0,
                          "Fractional period should be in range [0.0, 1.0] for date " + currentDate + ", was: " + fractional);
            }
        }
    }

    @Nested
    @DisplayName("calculateAccruedInterest Tests (via reflection)")
    class CalculateAccruedInterestTests {

        private double invokeCalculateAccruedInterest(double couponPayment, int periodsPerPaymentTerm,
                                                      LocalDate issueDate, LocalDate settlementDate) throws Exception {
            Method method = YTMHelper.class.getDeclaredMethod("calculateAccruedInterest",
                                                             double.class, int.class,
                                                             LocalDate.class, LocalDate.class);
            method.setAccessible(true);
            return (double) method.invoke(ytmHelper, couponPayment, periodsPerPaymentTerm, issueDate, settlementDate);
        }

        @Test
        @DisplayName("Should return 0 accrued interest at issue date")
        void testNoAccruedInterestAtIssue() throws Exception {
            // Given
            double couponPayment = 3000.0; // $30 per period
            int periodsPerPaymentTerm = 2;
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate settlementDate = LocalDate.of(2024, 1, 1);

            // When
            double accrued = invokeCalculateAccruedInterest(couponPayment, periodsPerPaymentTerm,
                                                           issueDate, settlementDate);

            logger.info("Accrued interest at settlement date: {} (Coupon Payment: {}, Issue: {}, Settlement: {})",
                        accrued, couponPayment, issueDate, settlementDate);

            // Then
            assertEquals(0.0, accrued, "Accrued interest should be 0 at issue date");
        }

        @Test
        @DisplayName("Should calculate accrued interest at mid-period")
        void testAccruedInterestAtMidPeriod() throws Exception {
            // Given: Halfway through semiannual period
            double couponPayment = 3000.0; // $30 per period
            int periodsPerPaymentTerm = 2;
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate settlementDate = LocalDate.of(2024, 4, 1); // ~3 months (half period)

            // When
            double accrued = invokeCalculateAccruedInterest(couponPayment, periodsPerPaymentTerm,
                                                           issueDate, settlementDate);

            logger.info("Accrued interest at settlement date: {} (Coupon Payment: {}, Issue: {}, Settlement: {})",
                    accrued, couponPayment, issueDate, settlementDate);

            // Then: Should be approximately half of coupon payment
            assertEquals(1500, accrued, "Accrued interest should be $15 (half of $30), was: " + accrued);
        }

        @Test
        @DisplayName("Should calculate full period accrued interest just before payment")
        void testAccruedInterestBeforePayment() throws Exception {
            // Given: Just before first payment
            double couponPayment = 3000.0; // $30 per period
            int periodsPerPaymentTerm = 2;
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate settlementDate = LocalDate.of(2024, 6, 30); // Day before payment

            // When
            double accrued = invokeCalculateAccruedInterest(couponPayment, periodsPerPaymentTerm,
                                                           issueDate, settlementDate);

            logger.info("Accrued interest at settlement date: {} (Coupon Payment: {}, Issue: {}, Settlement: {})",
                    accrued, couponPayment, issueDate, settlementDate);

            // Then: Should be close to full coupon payment
            assertTrue(accrued > 2900, "Accrued interest should be close to full coupon payment, was: " + accrued);
        }

        @Test
        @DisplayName("Should handle accrued interest after first payment")
        void testAccruedInterestAfterFirstPayment() throws Exception {
            // Given: 1 month after first payment
            double couponPayment = 3000.0; // $30 per period
            int periodsPerPaymentTerm = 2;
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate settlementDate = LocalDate.of(2024, 8, 1); // ~1 month into second period

            // When
            double accrued = invokeCalculateAccruedInterest(couponPayment, periodsPerPaymentTerm,
                                                           issueDate, settlementDate);

            logger.info("Accrued interest at settlement date: {} (Coupon Payment: {}, Issue: {}, Settlement: {})",
                    accrued, couponPayment, issueDate, settlementDate);

            // Then: Should be small amount for partial second period
            assertTrue(accrued > 0 && accrued < 1000,
                      "Accrued interest should be less than half coupon, was: " + accrued);
        }

        @Test
        @DisplayName("Should handle quarterly payments")
        void testAccruedInterestQuarterly() throws Exception {
            // Given: Quarterly bond, 1.5 months into period
            double couponPayment = 1500.0; // $15 per quarter
            int periodsPerPaymentTerm = 4;
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate settlementDate = LocalDate.of(2024, 2, 15); // ~1.5 months into quarter

            // When
            double accrued = invokeCalculateAccruedInterest(couponPayment, periodsPerPaymentTerm,
                                                           issueDate, settlementDate);

            logger.info("Accrued interest at settlement date: {} (Coupon Payment: {}, Issue: {}, Settlement: {})",
                    accrued, couponPayment, issueDate, settlementDate);

            // Then: Should be around half of quarterly payment
            assertEquals(750.0, accrued, 50, "Accrued interest should be between 0 and full coupon");
        }

        @Test
        @DisplayName("Should handle zero coupon payment")
        void testZeroCouponPayment() throws Exception {
            // Given: Zero coupon bond
            double couponPayment = 0.0;
            int periodsPerPaymentTerm = 2;
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate settlementDate = LocalDate.of(2024, 4, 1); // 3 months into period

            // When
            double accrued = invokeCalculateAccruedInterest(couponPayment, periodsPerPaymentTerm,
                                                           issueDate, settlementDate);

            logger.info("Accrued interest at settlement date: {} (Coupon Payment: {}, Issue: {}, Settlement: {})",
                    accrued, couponPayment, issueDate, settlementDate);

            // Then
            assertEquals(0.0, accrued, "Zero coupon bond should have no accrued interest");
        }
    }

    @Nested
    @DisplayName("periodsPerPaymentTerm Tests (via reflection)")
    class PeriodsPerPaymentTermTests {

        private int invokePeriodsPerPaymentTerm(String paymentTerm) {
            // This method is now in CommonHelper, which is public
            return commonHelper.periodsPerPaymentTerm(paymentTerm);
        }

        @ParameterizedTest
        @CsvSource({
            "annual, 1",
            "semiannual, 2",
            "quarterly, 4",
            "monthly, 12"
        })
        @DisplayName("Should return correct periods for valid payment terms")
        void testValidPaymentTerms(String paymentTerm, int expectedPeriods) throws Exception {
            // When
            int periods = invokePeriodsPerPaymentTerm(paymentTerm);

            // Then
            assertEquals(expectedPeriods, periods);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ANNUAL", "Annual", "SEMIANNUAL", "SemiAnnual","MONTHLY", "Monthly", "QUARTERLY", "Quarterly"})
        @DisplayName("Should be case-insensitive")
        void testCaseInsensitive(String paymentTerm) {
            // When/Then: Should not throw exception
            assertDoesNotThrow(() -> invokePeriodsPerPaymentTerm(paymentTerm));
        }

        @Test
        @DisplayName("Should throw exception for invalid payment term")
        void testInvalidPaymentTerm() {
            // When/Then
            assertThrows(Exception.class, () -> invokePeriodsPerPaymentTerm("biweekly"));
        }

        @Test
        @DisplayName("Should throw exception for empty string")
        void testEmptyString() {
            // When/Then
            assertThrows(Exception.class, () -> invokePeriodsPerPaymentTerm(""));
        }
    }

    @Nested
    @DisplayName("Integration and Edge Case Tests")
    class IntegrationTests {

//        @Test
//        @DisplayName("Should handle bond purchased on payment date with accrued interest")
//        void testPurchaseOnPaymentDate() {
//            // Given: Purchase exactly on a payment date
//            LocalDate issueDate = LocalDate.of(2024, 1, 1);
//            LocalDate currentDate = LocalDate.of(2024, 7, 1); // Exactly on payment date
//            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
//            int couponRate = 600;
//            int faceValue = 100000;
//            int marketValue = 100000;
//            String paymentTerm = "semiannual";
//
//            // When/Then: Should complete without error
//            assertDoesNotThrow(() -> {
//                double ytm = YTMHelper.calculateYTM(currentDate, couponRate, faceValue,
//                                                    marketValue, issueDate, maturationDate, paymentTerm);
//                assertTrue(ytm > 0, "YTM should be positive");
//            });
//        }

        @Test
        @DisplayName("Should handle leap year dates")
        void testLeapYearDates() {
            // Given: Dates spanning leap year
            LocalDate issueDate = LocalDate.of(2024, 1, 1); // 2024 is leap year
            LocalDate currentDate = LocalDate.of(2024, 2, 29);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 500;
            int faceValue = 100000;
            int marketValue = 98000;
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for bond in leap year: {} bps", ytm);
                assertTrue(ytm > 0, "Should handle leap year correctly");
            });
        }

        @Test
        @DisplayName("Should provide consistent results with small date changes")
        void testConsistentResults() {
            // Given: Two dates one day apart
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 600;
            int faceValue = 100000;
            int marketValue = 101000;
            String paymentTerm = "semiannual";

            LocalDate date1 = LocalDate.of(2024, 4, 1);
            LocalDate date2 = LocalDate.of(2024, 4, 2);

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When
            double ytm1 = ytmHelper.calculateYTM(date1, bond);
            double ytm2 = ytmHelper.calculateYTM(date2, bond);

            logger.info("YTM on {}: {} bps, YTM on {}: {} bps", date1, ytm1, date2, ytm2);

            // Then: Results should be relatively close (allowing for approximation variance)
            assertTrue(ytm1 != ytm2 && Math.abs(ytm2-ytm1) < 100, "YTM should be relatively consistent for nearby dates");
        }

        @ParameterizedTest
        @ValueSource(ints = {100, 500, 1000, 1500,2000,2500,5000,7500,9500,9900}) // 0% to 15%
        @DisplayName("Should handle high coupon rate")
        void testHighCouponRate(int couponRate) {
            // Given: Bond with very high coupon rate
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int faceValue = 100000;
            int marketValue = 120000; // Premium
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for range of coupon rates: {} bps", ytm);
                assertTrue(ytm > 0, "Should handle range of coupon rates");
            });
        }

        @Test
        @DisplayName("Should handle 0 coupon rate")
        void testZeroCouponRate() {
            // Given: Bond with very high coupon rate
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int faceValue = 100000;
            int marketValue = 120000; // Premium
            int couponRate = 0;
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for coupon rate of 0: {} bps", ytm);
                assertTrue(ytm < 0, "Should handle coupon rate of 0");
            });
        }

        @Test
        @DisplayName("Should handle deep discount bond")
        void testDeepDiscountBond() {
            // Given: Bond trading at deep discount
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate issueDate = LocalDate.of(2024, 1, 1);
            LocalDate maturationDate = LocalDate.of(2034, 1, 1);
            int couponRate = 200; // 2%
            int faceValue = 100000;
            int marketValue = 60000; // 60% of par
            String paymentTerm = "semiannual";

            Bond bond = new Bond("TEST-ISIN", maturationDate, issueDate, couponRate, faceValue, marketValue, paymentTerm);

            // When/Then
            assertDoesNotThrow(() -> {
                double ytm = ytmHelper.calculateYTM(currentDate, bond);

                logger.info("Calculated YTM for deep discount bond: {} bps", ytm);
                assertTrue(ytm > couponRate, "Deep discount bond YTM should be higher than coupon");
            });
        }
    }
}

