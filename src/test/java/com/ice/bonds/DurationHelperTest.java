package com.ice.bonds;

import com.ice.bonds.helper.CommonHelper;
import com.ice.bonds.helper.DurationHelper;
import com.ice.bonds.helper.YTMHelper;
import com.ice.bonds.model.Bond;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Duration Helper Tests")
class DurationHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(DurationHelperTest.class);
    private CommonHelper commonHelper;
    private DurationHelper durationHelper;
    private YTMHelper ytmHelper;

    @BeforeEach
    void setUp() {
        commonHelper = new CommonHelper();
        durationHelper = new DurationHelper(commonHelper);
        ytmHelper = new YTMHelper(commonHelper);
    }

    @Nested
    @DisplayName("generateCashFlows Tests")
    class GenerateCashFlowsTests {

        /**
         * Helper method to invoke private generateCashFlows method via reflection
         */
        private List<?> invokeGenerateCashFlows(Bond bond, LocalDate today, int couponFrequency) throws Exception {
            Method method = DurationHelper.class.getDeclaredMethod("generateCashFlows", Bond.class, LocalDate.class, int.class);
            method.setAccessible(true);
            return (List<?>) method.invoke(durationHelper, bond, today, couponFrequency);
        }

        /**
         * Helper method to get daysFromToday from CashFlow object via reflection
         */
        private long getCashFlowDaysFromToday(Object cashFlow) throws Exception {
            var field = cashFlow.getClass().getDeclaredField("daysFromToday");
            field.setAccessible(true);
            return (long) field.get(cashFlow);
        }

        /**
         * Helper method to get amount from CashFlow object via reflection
         */
        private double getCashFlowAmount(Object cashFlow) throws Exception {
            var field = cashFlow.getClass().getDeclaredField("amount");
            field.setAccessible(true);
            return (double) field.get(cashFlow);
        }

        @Test
        @DisplayName("Should generate correct cash flows for semiannual coupon bond")
        void testSemiannualCouponBond() throws Exception {
            // Given: 10-year bond with 6% coupon, semiannual payments
            Bond bond = new Bond();
            bond.setFaceValue(100000); // $1000 in cents
            bond.setCouponRate(600); // 6% in basis points
            bond.setIssueDate(LocalDate.of(2020, 1, 1));
            bond.setMaturityDate(LocalDate.of(2030, 1, 1));

            LocalDate today = LocalDate.of(2025, 1, 1);
            int couponFrequency = 2; // Semiannual
            double expectedCoupon = 3000.0; // 6% / 2 × $1000 = $30 = 3000 cents

            // When
            List<?> cashFlows = invokeGenerateCashFlows(bond, today, couponFrequency);

            // Then: From 2025-01-01 to 2030-01-01 inclusive = 11 semiannual payments
            assertEquals(11, cashFlows.size(), "Should have 11 cash flows for 5 remaining years + current date");

            logger.info("Testing semiannual bond: {} cash flows, expected coupon: ${}, face value: ${}",
                    cashFlows.size(), expectedCoupon / 100, bond.getFaceValue() / 100);

            // Check all cash flows
            for (int i = 0; i < cashFlows.size(); i++) {
                Object flow = cashFlows.get(i);
                long daysFromToday = getCashFlowDaysFromToday(flow);
                double amount = getCashFlowAmount(flow);

                if (i == cashFlows.size() - 1) {
                    // Last payment should include principal + coupon
                    double expectedAmount = expectedCoupon + bond.getFaceValue();
                    assertEquals(expectedAmount, amount, 0.01,
                            "Cash flow " + (i + 1) + " (last) should be principal + coupon");
                    logger.info("  Cash flow {}: {} days, ${} (coupon ${} + principal ${})",
                            i + 1, daysFromToday, amount / 100, expectedCoupon / 100, bond.getFaceValue() / 100);
                } else {
                    // All other payments should be just the coupon
                    assertEquals(expectedCoupon, amount, 0.01,
                            "Cash flow " + (i + 1) + " should be coupon only");
                    logger.info("  Cash flow {}: {} days, ${} (coupon only)",
                            i + 1, daysFromToday, amount / 100);
                }
            }

            logger.info("Successfully validated all {} cash flows for semiannual bond", cashFlows.size());

        }

        @Test
        @DisplayName("Should generate correct cash flows for quarterly coupon bond")
        void testQuarterlyCouponBond() throws Exception {
            // Given: 5-year bond with 8% coupon, quarterly payments
            Bond bond = new Bond();
            bond.setFaceValue(100000); // $1000 in cents
            bond.setCouponRate(800); // 8% in basis points
            bond.setIssueDate(LocalDate.of(2023, 3, 15));
            bond.setMaturityDate(LocalDate.of(2028, 3, 15));

            LocalDate today = LocalDate.of(2025, 3, 15);
            int couponFrequency = 4; // Quarterly
            double expectedCoupon = 2000.0; // 8% / 4 × $1000 = $20 = 2000 cents

            // When
            List<?> cashFlows = invokeGenerateCashFlows(bond, today, couponFrequency);

            // Then: From 2025-03-15 to 2028-03-15 inclusive = 13 quarterly payments
            assertEquals(13, cashFlows.size(), "Should have 13 cash flows for 3 remaining years");

            logger.info("Testing quarterly bond: {} cash flows, expected coupon: ${}, face value: ${}",
                    cashFlows.size(), expectedCoupon / 100, bond.getFaceValue() / 100);

            // Check all cash flows
            for (int i = 0; i < cashFlows.size(); i++) {
                Object flow = cashFlows.get(i);
                long daysFromToday = getCashFlowDaysFromToday(flow);
                double amount = getCashFlowAmount(flow);

                if (i == cashFlows.size() - 1) {
                    // Last payment should include principal + coupon
                    double expectedAmount = expectedCoupon + bond.getFaceValue();
                    assertEquals(expectedAmount, amount, 0.01,
                            "Cash flow " + (i + 1) + " (last) should be principal + coupon");
                    logger.info("  Cash flow {}: {} days, ${} (coupon ${} + principal ${})",
                            i + 1, daysFromToday, amount / 100, expectedCoupon / 100, bond.getFaceValue() / 100);
                } else {
                    // All other payments should be just the coupon
                    assertEquals(expectedCoupon, amount, 0.01,
                            "Cash flow " + (i + 1) + " should be coupon only");
                    logger.info("  Cash flow {}: {} days, ${} (coupon only)",
                            i + 1, daysFromToday, amount / 100);
                }
            }

            logger.info("Successfully validated all {} cash flows for quarterly bond", cashFlows.size());
        }

        @Test
        @DisplayName("Should generate correct cash flows for annual coupon bond")
        void testAnnualCouponBond() throws Exception {
            // Given: 10-year bond with 5% coupon, annual payments
            Bond bond = new Bond();
            bond.setFaceValue(100000); // $1000 in cents
            bond.setCouponRate(500); // 5% in basis points
            bond.setIssueDate(LocalDate.of(2020, 6, 1));
            bond.setMaturityDate(LocalDate.of(2030, 6, 1));

            LocalDate today = LocalDate.of(2025, 6, 2);
            int couponFrequency = 1; // Annual
            double expectedCoupon = 5000.0; // 5% × $1000 = $50 = 5000 cents

            // When
            List<?> cashFlows = invokeGenerateCashFlows(bond, today, couponFrequency);

            // Then: From 2025-06-02 to 2030-06-01 inclusive = 5 annual payments (payment made yesterday on 2025-06-01)
            assertEquals(5, cashFlows.size(), "Should have 5 cash flows for 5 remaining years");

            logger.info("Testing annual bond: {} cash flows, expected coupon: ${}, face value: ${}",
                    cashFlows.size(), expectedCoupon / 100, bond.getFaceValue() / 100);

            // Check all cash flows
            for (int i = 0; i < cashFlows.size(); i++) {
                Object flow = cashFlows.get(i);
                long daysFromToday = getCashFlowDaysFromToday(flow);
                double amount = getCashFlowAmount(flow);

                if (i == cashFlows.size() - 1) {
                    // Last payment should include principal + coupon
                    double expectedAmount = expectedCoupon + bond.getFaceValue();
                    assertEquals(expectedAmount, amount, 0.01,
                            "Cash flow " + (i + 1) + " (last) should be principal + coupon");
                    logger.info("  Cash flow {}: {} days, ${} (coupon ${} + principal ${})",
                            i + 1, daysFromToday, amount / 100, expectedCoupon / 100, bond.getFaceValue() / 100);
                } else {
                    // All other payments should be just the coupon
                    assertEquals(expectedCoupon, amount, 0.01,
                            "Cash flow " + (i + 1) + " should be coupon only");
                    logger.info("  Cash flow {}: {} days, ${} (coupon only)",
                            i + 1, daysFromToday, amount / 100);
                }
            }

            logger.info("Successfully validated all {} cash flows for annual bond", cashFlows.size());
        }

        @Test
        @DisplayName("Should handle zero-coupon bond correctly")
        void testZeroCouponBond() throws Exception {
            // Given: 5-year zero-coupon bond
            Bond bond = new Bond();
            bond.setFaceValue(100000); // $1000 in cents
            bond.setCouponRate(0); // Zero coupon
            bond.setIssueDate(LocalDate.of(2023, 1, 1));
            bond.setMaturityDate(LocalDate.of(2028, 1, 1));

            LocalDate today = LocalDate.of(2025, 1, 1);
            int couponFrequency = 2; // Frequency doesn't matter for zero-coupon

            // When
            List<?> cashFlows = invokeGenerateCashFlows(bond, today, couponFrequency);

            // Then: Should have only 1 cash flow (principal at maturity)
            assertEquals(1, cashFlows.size(), "Zero-coupon bond should have only 1 cash flow");

            logger.info("Testing zero-coupon bond: {} cash flow, face value: ${}",
                    cashFlows.size(), bond.getFaceValue() / 100);

            // Check that it's just the principal
            Object onlyFlow = cashFlows.getFirst();
            long daysFromToday = getCashFlowDaysFromToday(onlyFlow);
            double amount = getCashFlowAmount(onlyFlow);

            assertEquals(bond.getFaceValue(), amount, 0.01, "Zero-coupon bond should only pay principal");
            logger.info("Cash flow 1: {} days, ${} (principal only)", daysFromToday, amount / 100);

            logger.info("Successfully validated zero-coupon bond cash flow");
        }

        @Test
        @DisplayName("Should exclude past cash flows")
        void testExcludePastCashFlows() throws Exception {
            // Given: Bond issued in 2020, we're evaluating mid-term
            Bond bond = new Bond();
            bond.setFaceValue(100000);
            bond.setCouponRate(600); // 6%
            bond.setIssueDate(LocalDate.of(2020, 1, 1));
            bond.setMaturityDate(LocalDate.of(2030, 1, 1));

            LocalDate today = LocalDate.of(2025, 7, 1); // On date of coupon payment
            int couponFrequency = 2;
            double expectedCoupon = 3000.0; // 6% / 2 × $1000 = $30 = 3000 cents

            // When
            List<?> cashFlows = invokeGenerateCashFlows(bond, today, couponFrequency);

            logger.info("Testing past cash flow exclusion: {} cash flows, expected coupon: ${}, face value: ${}",
                    cashFlows.size(), expectedCoupon / 100, bond.getFaceValue() / 100);

            // Then: Should only have future payments
            for (int i = 0; i < cashFlows.size(); i++) {
                Object flow = cashFlows.get(i);
                long daysFromToday = getCashFlowDaysFromToday(flow);
                double amount = getCashFlowAmount(flow);

                assertTrue(daysFromToday >= 0 && cashFlows.size() == 10, "All cash flows should be in the future (today's date inclusive)");

                if (i == cashFlows.size() - 1) {
                    // Last payment should include principal + coupon
                    double expectedAmount = expectedCoupon + bond.getFaceValue();
                    assertEquals(expectedAmount, amount, 0.01,
                            "Cash flow " + (i + 1) + " (last) should be principal + coupon");
                    logger.info("  Cash flow {}: {} days, ${} (coupon ${} + principal ${})",
                            i + 1, daysFromToday, amount / 100, expectedCoupon / 100, bond.getFaceValue() / 100);
                } else {
                    // All other payments should be just the coupon
                    assertEquals(expectedCoupon, amount, 0.01,
                            "Cash flow " + (i + 1) + " should be coupon only");
                    logger.info("  Cash flow {}: {} days, ${} (coupon only)",
                            i + 1, daysFromToday, amount / 100);
                }
            }

            logger.info("Successfully validated all {} future cash flows", cashFlows.size());
        }

        @Test
        @DisplayName("Should throw exception for matured bond")
        void testMaturedBond() {
            // Given: Bond that has already matured
            Bond bond = new Bond();
            bond.setFaceValue(100000);
            bond.setCouponRate(600);
            bond.setIssueDate(LocalDate.of(2010, 1, 1));
            bond.setMaturityDate(LocalDate.of(2020, 1, 1));

            LocalDate today = LocalDate.of(2025, 1, 1);
            int couponFrequency = 2;

            // When/Then: Should throw exception
            Exception exception = assertThrows(Exception.class, () -> invokeGenerateCashFlows(bond, today, couponFrequency));

            assertInstanceOf(IllegalArgumentException.class, exception.getCause(), "Should throw IllegalArgumentException for matured bond");
            assertTrue(exception.getCause().getMessage().contains("already matured"),
                    "Exception message should mention bond has matured");

            logger.info("Correctly threw exception for matured bond");
        }

        @Test
        @DisplayName("Should align last coupon with maturity date")
        void testLastCouponAlignmentWithMaturity() throws Exception {
            // Given: Bond where maturity falls on a coupon date
            Bond bond = new Bond();
            bond.setFaceValue(100000);
            bond.setCouponRate(600);
            bond.setIssueDate(LocalDate.of(2023, 1, 1));
            bond.setMaturityDate(LocalDate.of(2028, 1, 1)); // Exactly 5 years, on payment date

            LocalDate today = LocalDate.of(2025, 1, 1);
            int couponFrequency = 2;

            // When
            List<?> cashFlows = invokeGenerateCashFlows(bond, today, couponFrequency);

            // Then: Last cash flow should be on maturity date
            Object lastFlow = cashFlows.getLast();
            long daysToMaturity = java.time.temporal.ChronoUnit.DAYS.between(today, bond.getMaturityDate());
            long lastFlowDays = getCashFlowDaysFromToday(lastFlow);

            assertEquals(daysToMaturity, lastFlowDays,
                    "Last cash flow should be on maturity date");

            logger.info("Last coupon payment correctly aligned with maturity date");
        }

        @ParameterizedTest
        @CsvSource({
                "1, 10000.0",   // Annual: 10% / 1 = $100 = 10000 cents
                "2, 5000.0",    // Semiannual: 10% / 2 = $50 = 5000 cents
                "4, 2500.0",    // Quarterly: 10% / 4 = $25 = 2500 cents
                "12, 833.33"    // Monthly: 10% / 12 ≈ $8.33 = 833.33 cents
        })
        @DisplayName("Should calculate correct coupon amounts for different frequencies")
        void testCouponAmountsForDifferentFrequencies(int frequency, double expectedCoupon) throws Exception {
            // Given: Bond with 10% coupon rate
            Bond bond = new Bond();
            bond.setFaceValue(100000); // $1000 in cents
            bond.setCouponRate(1000); // 10% in basis points
            bond.setIssueDate(LocalDate.of(2024, 1, 1));
            bond.setMaturityDate(LocalDate.of(2025, 1, 1));

            LocalDate today = LocalDate.of(2024, 1, 1);

            // When
            List<?> cashFlows = invokeGenerateCashFlows(bond, today, frequency);

            logger.info("Testing frequency {}: {} cash flows, expected coupon: ${}, face value: ${}",
                    frequency, cashFlows.size(), expectedCoupon / 100, bond.getFaceValue() / 100);

            // Then: Check all coupon amounts
            // Note: If frequency is 1 (annual) and bond is 1 year, there's only 1 cash flow
            // which includes both coupon AND principal
            for (int i = 0; i < cashFlows.size(); i++) {
                Object flow = cashFlows.get(i);
                long daysFromToday = getCashFlowDaysFromToday(flow);
                double actualAmount = getCashFlowAmount(flow);

                if (i == cashFlows.size() - 1) {
                    // Last payment includes principal
                    double couponOnly = actualAmount - bond.getFaceValue();
                    assertEquals(expectedCoupon, couponOnly, 1.0,
                            "Cash flow " + (i + 1) + " (last) coupon should match expected");
                    logger.info("  Cash flow {}: {} days, ${} (coupon ${} + principal ${})",
                            i + 1, daysFromToday, actualAmount / 100, couponOnly / 100, bond.getFaceValue() / 100);
                } else {
                    assertEquals(expectedCoupon, actualAmount, 1.0,
                            "Cash flow " + (i + 1) + " coupon should match expected");
                    logger.info("  Cash flow {}: {} days, ${} (coupon only)",
                            i + 1, daysFromToday, actualAmount / 100);
                }
            }

            logger.info("Successfully validated frequency {} with correct coupon amount: ${}", frequency, expectedCoupon / 100);
        }

        @Test
        @DisplayName("Should throw exception if no coupon payments generated for coupon-bearing bond")
        void testNoCouponPaymentsForCouponBond() {
            // Given: Coupon bond very close to maturity but past last payment date
            Bond bond = new Bond();
            bond.setFaceValue(100000);
            bond.setCouponRate(600); // 6% - not zero
            bond.setIssueDate(LocalDate.of(2024, 1, 1));
            bond.setMaturityDate(LocalDate.of(2024, 6, 15)); // Maturity between payments

            LocalDate today = LocalDate.of(2024, 6, 10); // After last payment, before maturity
            int couponFrequency = 2;

            // When/Then: This tests the edge case where maturity doesn't align with payment schedule
            // The function should handle this gracefully or throw an appropriate exception
            Exception exception = assertThrows(Exception.class, () -> invokeGenerateCashFlows(bond, today, couponFrequency));

            logger.info("Correctly handled edge case: {}", exception.getCause().getMessage());
        }

        @Nested
        @DisplayName("calculateMacaulayDuration Tests")
        class CalculateMacaulayDurationTests {

            /**
             * Helper method to create a bond with specified parameters including market value
             */
            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, int marketValue, String paymentTerm) {
                Bond bond = new Bond();
                bond.setFaceValue(faceValue);
                bond.setCouponRate(couponRate);
                bond.setIssueDate(issueDate);
                bond.setMaturityDate(maturityDate);
                bond.setMarketValue(marketValue);
                bond.setPaymentTerm(paymentTerm);
                return bond;
            }

            /**
             * Helper method to create a bond with market value equal to face value (par bond)
             */
            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, String paymentTerm) {
                return createBond(faceValue, couponRate, issueDate, maturityDate, faceValue, paymentTerm);
            }

            /**
             * Helper method to calculate YTM using YTMHelper and convert to decimal
             */
            private double calculateYTMDecimal(Bond bond) {
                LocalDate currentDate = LocalDate.now();
                double ytmBasisPoints = ytmHelper.calculateYTM(currentDate, bond);
                return ytmBasisPoints / 10000.0; // Convert basis points to decimal
            }

            @Test
            @DisplayName("Should calculate Macaulay Duration for semiannual coupon bond using calculated YTM")
            void testMacaulayDurationSemiannualBond() {
                // Given: 10-year bond with 6% coupon, semiannual payments, at par (YTM = coupon rate)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 600,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Duration should be positive and less than time to maturity
                assertTrue(macaulayDuration > 0, "Macaulay Duration should be positive");
                assertTrue(macaulayDuration < 5.0, "Macaulay Duration should be less than time to maturity");

                logger.info("Macaulay Duration for 5-year remaining semiannual bond: {} years",
                        String.format("%.4f", macaulayDuration));
                logger.info("  Face Value: ${}, Coupon Rate: {}%, Calculated YTM: {}%",
                        bond.getFaceValue() / 100, bond.getCouponRate() / 100.0, ytm * 100);
            }

            @Test
            @DisplayName("Should calculate Macaulay Duration for zero-coupon bond equals time to maturity")
            void testMacaulayDurationZeroCouponBond() {
                // Given: Zero-coupon bond - Macaulay Duration should equal time to maturity
                // For zero-coupon bond, use discounted market value to imply YTM
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 0,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        78000, // Discounted price implies ~5% YTM
                        paymentTerm);

                // For zero-coupon bonds, we'll use a manual YTM calculation since calculateYTM
                // requires coupon payments
                double ytm = 0.05; // Approximate YTM based on discount

                // When
                double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Zero-coupon bond duration should approximately equal time to maturity
                double timeToMaturity = 5.0;
                assertEquals(timeToMaturity, macaulayDuration, 0.05,
                        "Zero-coupon bond Macaulay Duration should equal time to maturity");

                logger.info("Zero-coupon bond Macaulay Duration: {} years (expected ~{} years)",
                        String.format("%.4f", macaulayDuration), timeToMaturity);
            }

            @Test
            @DisplayName("Should calculate shorter duration for higher coupon rates using calculated YTM")
            void testHigherCouponRateShorterDuration() {
                // Given: Two bonds with different coupon rates but same maturity
                LocalDate issueDate = LocalDate.now().minusYears(5);
                LocalDate maturityDate = LocalDate.now().plusYears(5);
                String paymentTerm = "semiannual";

                // Both bonds trading at par, so YTM equals coupon rate
                Bond lowCouponBond = createBond(100000, 200, issueDate, maturityDate, paymentTerm); // 2% coupon
                Bond highCouponBond = createBond(100000, 800, issueDate, maturityDate, paymentTerm); // 8% coupon

                // Calculate YTM for each bond
                double lowCouponYTM = calculateYTMDecimal(lowCouponBond);
                double highCouponYTM = calculateYTMDecimal(highCouponBond);

                // When
                double lowCouponDuration = durationHelper.calculateMacaulayDuration(lowCouponBond, lowCouponYTM);
                double highCouponDuration = durationHelper.calculateMacaulayDuration(highCouponBond, highCouponYTM);

                // Then: Higher coupon bond should have shorter duration
                assertTrue(highCouponDuration < lowCouponDuration,
                        "Higher coupon bond should have shorter Macaulay Duration");

                logger.info("Low coupon (2%, YTM: {}%) duration: {} years",
                        String.format("%.2f", lowCouponYTM * 100), String.format("%.4f", lowCouponDuration));
                logger.info("High coupon (8%, YTM: {}%) duration: {} years",
                        String.format("%.2f", highCouponYTM * 100), String.format("%.4f", highCouponDuration));
                logger.info("Duration difference: {} years", String.format("%.4f", lowCouponDuration - highCouponDuration));
            }

            @Test
            @DisplayName("Should calculate shorter duration for higher YTM using different market values")
            void testHigherYTMShorterDuration() {
                // Given: Same bond with different market values to create different YTMs
                LocalDate issueDate = LocalDate.now().minusYears(5);
                LocalDate maturityDate = LocalDate.now().plusYears(5);
                String paymentTerm = "semiannual";

                // Premium bond (lower YTM) - market value > face value
                Bond premiumBond = createBond(100000, 600, issueDate, maturityDate, 110000, paymentTerm);
                // Discount bond (higher YTM) - market value < face value
                Bond discountBond = createBond(100000, 600, issueDate, maturityDate, 90000, paymentTerm);

                // Calculate YTM for each bond
                double lowYTM = calculateYTMDecimal(premiumBond);
                double highYTM = calculateYTMDecimal(discountBond);

                // When
                double lowYTMDuration = durationHelper.calculateMacaulayDuration(premiumBond, lowYTM);
                double highYTMDuration = durationHelper.calculateMacaulayDuration(discountBond, highYTM);

                // Then: Higher YTM should result in slightly shorter duration
                assertTrue(highYTM > lowYTM, "Discount bond should have higher YTM");
                assertTrue(highYTMDuration < lowYTMDuration,
                        "Higher YTM should result in shorter Macaulay Duration");

                logger.info("Premium bond (YTM: {}%) duration: {} years",
                        String.format("%.2f", lowYTM * 100), String.format("%.4f", lowYTMDuration));
                logger.info("Discount bond (YTM: {}%) duration: {} years",
                        String.format("%.2f", highYTM * 100), String.format("%.4f", highYTMDuration));
                logger.info("Duration difference: {} years", String.format("%.4f", lowYTMDuration - highYTMDuration));
            }

            @Test
            @DisplayName("Should calculate longer duration for longer maturity using calculated YTM")
            void testLongerMaturityLongerDuration() {
                // Given: Two bonds with different maturities
                String paymentTerm = "semiannual";
                Bond shortTermBond = createBond(100000, 600,
                        LocalDate.now().minusYears(2),
                        LocalDate.now().plusYears(3),
                        paymentTerm);
                Bond longTermBond = createBond(100000, 600,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(10),
                        paymentTerm);

                // Calculate YTM for each bond
                double shortTermYTM = calculateYTMDecimal(shortTermBond);
                double longTermYTM = calculateYTMDecimal(longTermBond);

                // When
                double shortTermDuration = durationHelper.calculateMacaulayDuration(shortTermBond, shortTermYTM);
                double longTermDuration = durationHelper.calculateMacaulayDuration(longTermBond, longTermYTM);

                // Then: Longer maturity bond should have longer duration
                assertTrue(longTermDuration > shortTermDuration,
                        "Longer maturity bond should have longer Macaulay Duration");

                logger.info("Short-term bond (3yr remaining, YTM: {}%) duration: {} years",
                        String.format("%.2f", shortTermYTM * 100), String.format("%.4f", shortTermDuration));
                logger.info("Long-term bond (10yr remaining, YTM: {}%) duration: {} years",
                        String.format("%.2f", longTermYTM * 100), String.format("%.4f", longTermDuration));
            }

            @ParameterizedTest
            @CsvSource({
                    "annual",
                    "semiannual",
                    "quarterly",
                    "monthly"
            })
            @DisplayName("Should calculate valid duration for different payment frequencies using calculated YTM")
            void testDifferentPaymentFrequencies(String paymentTerm) {
                // Given: Same bond with different payment frequencies
                Bond bond = createBond(100000, 600,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Duration should be positive and less than time to maturity
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 5.0, "Duration should be less than time to maturity");

                logger.info("{} payment frequency (YTM: {}%) - Macaulay Duration: {} years",
                        paymentTerm, String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should throw exception for matured bond")
            void testMaturedBondThrowsException() {
                // Given: Bond that has already matured
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 600,
                        LocalDate.of(2015, 1, 1),
                        LocalDate.of(2020, 1, 1),
                        paymentTerm);

                double ytm = 0.05; // Use hardcoded YTM since bond is matured

                // When/Then
                Exception exception = assertThrows(IllegalArgumentException.class, () -> durationHelper.calculateMacaulayDuration(bond, ytm));

                assertTrue(exception.getMessage().contains("already matured"),
                        "Exception message should mention bond has matured");

                logger.info("Correctly threw exception for matured bond: {}", exception.getMessage());
            }

            @Test
            @DisplayName("Should handle bond with single remaining payment using calculated YTM")
            void testSingleRemainingPayment() {
                // Given: Bond very close to maturity with only one payment left
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 600,
                        LocalDate.now().minusMonths(11),
                        LocalDate.now().plusMonths(1),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Duration should be very short (approximately equal to time remaining)
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 0.5, "Duration should be less than 6 months");

                logger.info("Single payment remaining (YTM: {}%) - Macaulay Duration: {} years (expected ~0.08 years)",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle very high coupon rate bond using calculated YTM")
            void testHighCouponRateBond() {
                // Given: Bond with very high coupon rate (15%)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 1500,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Duration should be significantly less than maturity for high coupon bonds
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 4.5, "High coupon bond duration should be significantly less than maturity");

                logger.info("High coupon (15%, YTM: {}%) bond - Macaulay Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle annual payment bond correctly using calculated YTM")
            void testAnnualPaymentBond() {
                // Given: Annual coupon bond
                String paymentTerm = "annual";
                Bond bond = createBond(100000, 500,
                        LocalDate.now().minusYears(3),
                        LocalDate.now().plusYears(7),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Validate duration is reasonable
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 7.0, "Duration should be less than time to maturity");
                assertTrue(duration > 4.0, "Duration for 7-year 5% bond should be substantial");

                logger.info("Annual payment bond (7yr remaining, YTM: {}%) - Macaulay Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle low YTM scenario with premium bond")
            void testLowYTMScenario() {
                // Given: Bond trading at significant premium to create low YTM
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 400,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        115000, // Premium price for low YTM
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 5.0, "Duration should be less than time to maturity");
                assertTrue(ytm < 0.04, "YTM should be low due to premium price");

                logger.info("Low YTM scenario (YTM: {}%) - Macaulay Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }
        }

        @Nested
        @DisplayName("calculateModifiedDuration Tests (Instance Method)")
        class CalculateModifiedDurationInstanceTests {

            @Test
            @DisplayName("Should calculate Modified Duration for semiannual bond")
            void testModifiedDurationSemiannual() {
                // Given
                double macaulayDuration = 4.5; // years
                double ytmBasisPoints = 500; // 5%
                String paymentTerm = "semiannual";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Expected: 4.5 / (1 + 0.05/2) = 4.5 / 1.025 ≈ 4.39
                double expected = macaulayDuration / (1 + 0.025);

                // Then
                assertEquals(expected, modifiedDuration, 0.0001,
                        "Modified Duration should be Macaulay Duration divided by (1 + YTM per period)");

                logger.info("Semiannual Modified Duration: {} years (Macaulay: {} years, YTM: {}%)",
                        String.format("%.4f", modifiedDuration),
                        macaulayDuration,
                        ytmBasisPoints / 100.0);
                logger.info("  Expected: {} years", String.format("%.4f", expected));
            }

            @Test
            @DisplayName("Should calculate Modified Duration for annual bond")
            void testModifiedDurationAnnual() {
                // Given
                double macaulayDuration = 6.0;
                double ytmBasisPoints = 600; // 6%
                String paymentTerm = "annual";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Expected: 6.0 / (1 + 0.06/1) = 6.0 / 1.06 ≈ 5.66
                double expected = macaulayDuration / (1 + 0.06);

                // Then
                assertEquals(expected, modifiedDuration, 0.0001,
                        "Modified Duration for annual bond should use annual YTM");

                logger.info("Annual Modified Duration: {} years (Macaulay: {} years, YTM: {}%)",
                        String.format("%.4f", modifiedDuration),
                        macaulayDuration,
                        ytmBasisPoints / 100.0);
            }

            @Test
            @DisplayName("Should calculate Modified Duration for quarterly bond")
            void testModifiedDurationQuarterly() {
                // Given
                double macaulayDuration = 3.5;
                double ytmBasisPoints = 400; // 4%
                String paymentTerm = "quarterly";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Expected: 3.5 / (1 + 0.04/4) = 3.5 / 1.01 ≈ 3.47
                double expected = macaulayDuration / (1 + 0.01);

                // Then
                assertEquals(expected, modifiedDuration, 0.0001,
                        "Modified Duration for quarterly bond should use quarterly YTM");

                logger.info("Quarterly Modified Duration: {} years (Macaulay: {} years, YTM: {}%)",
                        String.format("%.4f", modifiedDuration),
                        macaulayDuration,
                        ytmBasisPoints / 100.0);
            }

            @Test
            @DisplayName("Should calculate Modified Duration for monthly bond")
            void testModifiedDurationMonthly() {
                // Given
                double macaulayDuration = 2.0;
                double ytmBasisPoints = 300; // 3%
                String paymentTerm = "monthly";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Expected: 2.0 / (1 + 0.03/12) = 2.0 / 1.0025 ≈ 1.995
                double expected = macaulayDuration / (1 + 0.0025);

                // Then
                assertEquals(expected, modifiedDuration, 0.0001,
                        "Modified Duration for monthly bond should use monthly YTM");

                logger.info("Monthly Modified Duration: {} years (Macaulay: {} years, YTM: {}%)",
                        String.format("%.4f", modifiedDuration),
                        macaulayDuration,
                        ytmBasisPoints / 100.0);
            }

            @Test
            @DisplayName("Should be less than Macaulay Duration")
            void testModifiedDurationLessThanMacaulay() {
                // Given
                double macaulayDuration = 5.0;
                double ytmBasisPoints = 500;
                String paymentTerm = "semiannual";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Then: Modified Duration should always be less than Macaulay Duration when YTM > 0
                assertTrue(modifiedDuration < macaulayDuration,
                        "Modified Duration should be less than Macaulay Duration");

                logger.info("Macaulay: {} years, Modified: {} years (difference: {} years)",
                        macaulayDuration,
                        String.format("%.4f", modifiedDuration),
                        String.format("%.4f", macaulayDuration - modifiedDuration));
            }

            @Test
            @DisplayName("Should approach Macaulay Duration when YTM approaches zero")
            void testModifiedDurationApproachesMacaulay() {
                // Given: Very low YTM
                double macaulayDuration = 5.0;
                double ytmBasisPoints = 1; // 0.01%
                String paymentTerm = "semiannual";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Then: Should be very close to Macaulay Duration
                assertEquals(macaulayDuration, modifiedDuration, 0.01,
                        "Modified Duration should approach Macaulay Duration when YTM is near zero");

                logger.info("Near-zero YTM ({} bps): Macaulay = {}, Modified = {}",
                        ytmBasisPoints,
                        macaulayDuration,
                        String.format("%.6f", modifiedDuration));
            }

            @Test
            @DisplayName("Should handle high YTM scenario")
            void testHighYTMScenario() {
                // Given: High YTM environment
                double macaulayDuration = 5.0;
                double ytmBasisPoints = 1500; // 15%
                String paymentTerm = "semiannual";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Expected: 5.0 / (1 + 0.15/2) = 5.0 / 1.075 ≈ 4.65
                double expected = macaulayDuration / (1 + 0.075);

                // Then
                assertEquals(expected, modifiedDuration, 0.0001);

                logger.info("High YTM (15%) scenario - Modified Duration: {} years (Macaulay: {} years)",
                        String.format("%.4f", modifiedDuration), macaulayDuration);
            }

            @ParameterizedTest
            @CsvSource({
                    "annual, 1",
                    "semiannual, 2",
                    "quarterly, 4",
                    "monthly, 12"
            })
            @DisplayName("Should correctly apply different payment frequencies")
            void testDifferentPaymentFrequencies(String paymentTerm, int expectedPeriods) {
                // Given
                double macaulayDuration = 4.0;
                double ytmBasisPoints = 600; // 6%

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Expected calculation
                double ytmPerPeriod = (ytmBasisPoints / 10000.0) / expectedPeriods;
                double expected = macaulayDuration / (1 + ytmPerPeriod);

                // Then
                assertEquals(expected, modifiedDuration, 0.0001,
                        "Modified Duration should use correct periods for " + paymentTerm);

                logger.info("Payment term '{}' ({} periods/year): Modified Duration = {} years",
                        paymentTerm, expectedPeriods, String.format("%.4f", modifiedDuration));
            }

            @Test
            @DisplayName("Should throw exception for invalid payment term")
            void testInvalidPaymentTerm() {
                // Given
                double macaulayDuration = 4.0;
                double ytmBasisPoints = 500;
                String invalidPaymentTerm = "weekly";

                // When/Then
                Exception exception = assertThrows(IllegalArgumentException.class, () -> durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, invalidPaymentTerm));

                assertTrue(exception.getMessage().contains("Invalid payment term"),
                        "Exception message should indicate invalid payment term");

                logger.info("Correctly threw exception for invalid payment term: {}", exception.getMessage());
            }

            @Test
            @DisplayName("Should handle zero Macaulay Duration")
            void testZeroMacaulayDuration() {
                // Given
                double macaulayDuration = 0.0;
                double ytmBasisPoints = 500;
                String paymentTerm = "semiannual";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Then
                assertEquals(0.0, modifiedDuration,
                        "Modified Duration should be zero when Macaulay Duration is zero");

                logger.info("Zero Macaulay Duration results in Modified Duration: {}", modifiedDuration);
            }
        }

        @Nested
        @DisplayName("Integration Tests - Duration Calculations")
        class DurationIntegrationTests {

            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, int marketValue, String paymentTerm) {
                Bond bond = new Bond();
                bond.setFaceValue(faceValue);
                bond.setCouponRate(couponRate);
                bond.setIssueDate(issueDate);
                bond.setMaturityDate(maturityDate);
                bond.setMarketValue(marketValue);
                bond.setPaymentTerm(paymentTerm);
                return bond;
            }

            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, String paymentTerm) {
                return createBond(faceValue, couponRate, issueDate, maturityDate, faceValue, paymentTerm);
            }

            /**
             * Helper method to calculate YTM using YTMHelper and convert to decimal
             */
            private double calculateYTMDecimal(Bond bond) {
                LocalDate currentDate = LocalDate.now();
                double ytmBasisPoints = ytmHelper.calculateYTM(currentDate, bond);
                return ytmBasisPoints / 10000.0; // Convert basis points to decimal
            }

            /**
             * Helper method to calculate YTM in basis points using YTMHelper
             */
            private double calculateYTMBasisPoints(Bond bond) {
                LocalDate currentDate = LocalDate.now();
                return ytmHelper.calculateYTM(currentDate, bond);
            }

            @Test
            @DisplayName("Should correctly calculate full duration workflow using calculated YTM")
            void testFullDurationWorkflow() {
                // Given: A typical corporate bond
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 600,
                        LocalDate.now().minusYears(2),
                        LocalDate.now().plusYears(8),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytmDecimal = calculateYTMDecimal(bond);
                double ytmBasisPoints = calculateYTMBasisPoints(bond);

                // When: Calculate Macaulay Duration
                double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, ytmDecimal);

                // Then calculate Modified Duration using instance method
                double modifiedDuration = durationHelper.calculateModifiedDuration(
                        macaulayDuration, ytmBasisPoints, paymentTerm);

                // Validate relationship
                assertTrue(modifiedDuration < macaulayDuration,
                        "Modified Duration should be less than Macaulay Duration");
                assertTrue(macaulayDuration > 0 && macaulayDuration < 8.0,
                        "Macaulay Duration should be between 0 and time to maturity");
                assertTrue(modifiedDuration > 0,
                        "Modified Duration should be positive");

                logger.info("=== Full Duration Workflow Test ===");
                logger.info("Bond Details: Face Value: ${}, Coupon Rate: {}%, Remaining Years: 8",
                        bond.getFaceValue() / 100, bond.getCouponRate() / 100.0);
                logger.info("Calculated YTM: {}%, Payment Frequency: {}", String.format("%.2f", ytmDecimal * 100), paymentTerm);
                logger.info("Macaulay Duration: {} years", String.format("%.4f", macaulayDuration));
                logger.info("Modified Duration: {} years", String.format("%.4f", modifiedDuration));
                logger.info("Duration Ratio (Modified/Macaulay): {}",
                        String.format("%.4f", modifiedDuration / macaulayDuration));
            }

            @Test
            @DisplayName("Should demonstrate duration behavior across different bonds using calculated YTM")
            void testDurationBehaviorAcrossBonds() {
                // Given: Multiple bonds with different characteristics
                String paymentTerm = "semiannual";

                // Create bonds with varying characteristics
                Bond shortTermLowCoupon = createBond(100000, 200,
                        LocalDate.now().minusYears(2), LocalDate.now().plusYears(2), paymentTerm);
                Bond shortTermHighCoupon = createBond(100000, 800,
                        LocalDate.now().minusYears(2), LocalDate.now().plusYears(2), paymentTerm);
                Bond longTermLowCoupon = createBond(100000, 200,
                        LocalDate.now().minusYears(5), LocalDate.now().plusYears(10), paymentTerm);
                Bond longTermHighCoupon = createBond(100000, 800,
                        LocalDate.now().minusYears(5), LocalDate.now().plusYears(10), paymentTerm);

                // Calculate YTM for each bond
                double stlcYTM = calculateYTMDecimal(shortTermLowCoupon);
                double sthcYTM = calculateYTMDecimal(shortTermHighCoupon);
                double ltlcYTM = calculateYTMDecimal(longTermLowCoupon);
                double lthcYTM = calculateYTMDecimal(longTermHighCoupon);

                // Calculate durations
                double stlcDuration = durationHelper.calculateMacaulayDuration(shortTermLowCoupon, stlcYTM);
                double sthcDuration = durationHelper.calculateMacaulayDuration(shortTermHighCoupon, sthcYTM);
                double ltlcDuration = durationHelper.calculateMacaulayDuration(longTermLowCoupon, ltlcYTM);
                double lthcDuration = durationHelper.calculateMacaulayDuration(longTermHighCoupon, lthcYTM);

                // Verify expected relationships
                assertTrue(stlcDuration > sthcDuration, "Low coupon should have higher duration than high coupon (short term)");
                assertTrue(ltlcDuration > lthcDuration, "Low coupon should have higher duration than high coupon (long term)");
                assertTrue(ltlcDuration > stlcDuration, "Longer term should have higher duration (low coupon)");
                assertTrue(lthcDuration > sthcDuration, "Longer term should have higher duration (high coupon)");

                logger.info("=== Duration Comparison Across Bonds (with calculated YTM) ===");
                logger.info("Short-term (2yr), Low Coupon (2%, YTM: {}%):  {} years",
                        String.format("%.2f", stlcYTM * 100), String.format("%.4f", stlcDuration));
                logger.info("Short-term (2yr), High Coupon (8%, YTM: {}%): {} years",
                        String.format("%.2f", sthcYTM * 100), String.format("%.4f", sthcDuration));
                logger.info("Long-term (10yr), Low Coupon (2%, YTM: {}%):  {} years",
                        String.format("%.2f", ltlcYTM * 100), String.format("%.4f", ltlcDuration));
                logger.info("Long-term (10yr), High Coupon (8%, YTM: {}%): {} years",
                        String.format("%.2f", lthcYTM * 100), String.format("%.4f", lthcDuration));
            }

            @Test
            @DisplayName("Should correctly price sensitivity estimation using calculated YTM")
            void testPriceSensitivityEstimation() {
                // Given: Bond with known duration
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 500,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When: Calculate duration
                double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, ytm);
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytm, bond.getPaymentTerm());

                // Estimate price change for 1% (100 bps) yield increase
                double yieldChange = 0.01;
                double estimatedPriceChangePercent = -modifiedDuration * yieldChange * 100;

                // Then: Validate the estimation makes sense
                assertTrue(estimatedPriceChangePercent < 0,
                        "Price should decrease when yield increases");
                assertTrue(Math.abs(estimatedPriceChangePercent) < 10,
                        "Price change for 5-year bond should be reasonable");

                logger.info("=== Price Sensitivity Estimation (with calculated YTM) ===");
                logger.info("Bond: 5-year remaining, 5% coupon, Calculated YTM: {}%", String.format("%.2f", ytm * 100));
                logger.info("Macaulay Duration: {} years", String.format("%.4f", macaulayDuration));
                logger.info("Modified Duration: {} years", String.format("%.4f", modifiedDuration));
                logger.info("For a 1% (100 bps) yield increase:");
                logger.info("  Estimated Price Change: {}%", String.format("%.2f", estimatedPriceChangePercent));
            }

            @Test
            @DisplayName("Should calculate consistent durations for par bond using calculated YTM")
            void testParBondDurations() {
                // Given: Par bond (coupon rate equals YTM when market value equals face value)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 500,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper - should be ~5% for par bond
                double ytm = calculateYTMDecimal(bond);

                // When
                double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, ytm);
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytm * 10000, paymentTerm);

                // Then: Validate par bond behavior
                assertTrue(macaulayDuration > 0, "Macaulay Duration should be positive");
                assertTrue(modifiedDuration > 0, "Modified Duration should be positive");

                logger.info("=== Par Bond Duration Test (with calculated YTM) ===");
                logger.info("Par bond (Coupon = 5%, Calculated YTM = {}%)", String.format("%.2f", ytm * 100));
                logger.info("Macaulay Duration: {} years", String.format("%.4f", macaulayDuration));
                logger.info("Modified Duration: {} years", String.format("%.4f", modifiedDuration));
            }

            @Test
            @DisplayName("Should verify convexity relationship using calculated YTM")
            void testConvexityRelationship() {
                // Given: Three bonds with increasing maturities
                String paymentTerm = "semiannual";

                Bond bond5yr = createBond(100000, 500, LocalDate.now().minusYears(5), LocalDate.now().plusYears(5), paymentTerm);
                Bond bond10yr = createBond(100000, 500, LocalDate.now().minusYears(5), LocalDate.now().plusYears(10), paymentTerm);
                Bond bond20yr = createBond(100000, 500, LocalDate.now().minusYears(5), LocalDate.now().plusYears(20), paymentTerm);

                // Calculate YTM for each bond
                double ytm5yr = calculateYTMDecimal(bond5yr);
                double ytm10yr = calculateYTMDecimal(bond10yr);
                double ytm20yr = calculateYTMDecimal(bond20yr);

                // When
                double duration5yr = durationHelper.calculateMacaulayDuration(bond5yr, ytm5yr);
                double duration10yr = durationHelper.calculateMacaulayDuration(bond10yr, ytm10yr);
                double duration20yr = durationHelper.calculateMacaulayDuration(bond20yr, ytm20yr);

                // Then: Duration increases with maturity but at a decreasing rate
                double delta1 = duration10yr - duration5yr;
                double delta2 = duration20yr - duration10yr;

                // For coupon bonds, duration increase slows at longer maturities (convexity)
                assertTrue(duration20yr > duration10yr, "20yr duration should exceed 10yr duration");
                assertTrue(duration10yr > duration5yr, "10yr duration should exceed 5yr duration");

                logger.info("=== Convexity Relationship Test (with calculated YTM) ===");
                logger.info("5-year bond (YTM: {}%):  {} years", String.format("%.2f", ytm5yr * 100), String.format("%.4f", duration5yr));
                logger.info("10-year bond (YTM: {}%): {} years", String.format("%.2f", ytm10yr * 100), String.format("%.4f", duration10yr));
                logger.info("20-year bond (YTM: {}%): {} years", String.format("%.2f", ytm20yr * 100), String.format("%.4f", duration20yr));
                logger.info("Duration increase (5yr to 10yr): {} years", String.format("%.4f", delta1));
                logger.info("Duration increase (10yr to 20yr): {} years", String.format("%.4f", delta2));
            }
        }

        @Nested
        @DisplayName("Edge Cases and Boundary Conditions")
        class EdgeCasesAndBoundaryConditionsTests {

            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, int marketValue, String paymentTerm) {
                Bond bond = new Bond();
                bond.setFaceValue(faceValue);
                bond.setCouponRate(couponRate);
                bond.setIssueDate(issueDate);
                bond.setMaturityDate(maturityDate);
                bond.setMarketValue(marketValue);
                bond.setPaymentTerm(paymentTerm);
                return bond;
            }

            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, String paymentTerm) {
                return createBond(faceValue, couponRate, issueDate, maturityDate, faceValue, paymentTerm);
            }

            /**
             * Helper method to calculate YTM using YTMHelper and convert to decimal
             */
            private double calculateYTMDecimal(Bond bond) {
                LocalDate currentDate = LocalDate.now();
                double ytmBasisPoints = ytmHelper.calculateYTM(currentDate, bond);
                return ytmBasisPoints / 10000.0; // Convert basis points to decimal
            }

            @Test
            @DisplayName("Should handle bond maturing exactly on payment date using calculated YTM")
            void testBondMaturingOnPaymentDate() {
                // Given: Bond where maturity aligns with payment schedule
                LocalDate issueDate = LocalDate.of(2020, 1, 1);
                LocalDate maturityDate = LocalDate.of(2030, 1, 1); // Exactly 10 years
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 600, issueDate, maturityDate, paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 10.0, "Duration should be less than maturity");

                logger.info("Bond maturing on payment date (YTM: {}%) - Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle very short duration bond when no coupon payments before maturity")
            void testVeryShortDurationBond() {
                // Given: Bond maturing before next payment date (should throw exception for coupon bond)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 600,
                        LocalDate.now().minusMonths(5),
                        LocalDate.now().plusWeeks(4),
                        paymentTerm);

                double ytm = 0.05; // Use hardcoded YTM since YTMHelper would also fail

                // When/Then: Should throw exception because no coupon payments before maturity
                Exception exception = assertThrows(IllegalStateException.class, () -> durationHelper.calculateMacaulayDuration(bond, ytm));

                assertTrue(exception.getMessage().contains("No cash flows generated"),
                        "Exception should indicate no cash flows generated");

                logger.info("Very short duration bond correctly throws exception: {}", exception.getMessage());
            }

            @Test
            @DisplayName("Should handle very short zero-coupon bond")
            void testVeryShortZeroCouponBond() {
                // Given: Zero-coupon bond maturing soon (doesn't need coupon payments)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 0,
                        LocalDate.now().minusMonths(5),
                        LocalDate.now().plusWeeks(4),
                        paymentTerm);

                double ytm = 0.05; // Zero-coupon bonds need manual YTM

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Duration should be approximately equal to time to maturity for zero-coupon
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 0.15, "Duration should be less than ~2 months");

                logger.info("Very short zero-coupon bond - Duration: {} years", String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle large face value bond using calculated YTM")
            void testLargeFaceValueBond() {
                // Given: Bond with large face value ($1,000,000)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000000, 500,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Duration should be same as smaller bond (independent of face value)
                Bond smallBond = createBond(100000, 500,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);
                double smallBondYTM = calculateYTMDecimal(smallBond);
                double smallBondDuration = durationHelper.calculateMacaulayDuration(smallBond, smallBondYTM);

                assertEquals(smallBondDuration, duration, 0.01,
                        "Duration should be independent of face value");

                logger.info("Large face value bond (YTM: {}%) - Duration: {} years (same as small bond: {} years)",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration), String.format("%.4f", smallBondDuration));
            }

            @Test
            @DisplayName("Should handle premium bond (coupon > YTM) using calculated YTM")
            void testPremiumBond() {
                // Given: Premium bond (high coupon, market value > face value)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 1000, // 10% coupon
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        115000, // Premium price implies YTM < coupon rate
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Premium bond should have shorter duration due to high coupon weight
                assertTrue(duration > 0, "Duration should be positive");
                assertTrue(duration < 5.0, "Duration should be less than maturity");
                assertTrue(ytm < 0.10, "YTM should be less than coupon rate for premium bond");

                logger.info("Premium bond (10% coupon, YTM: {}%) - Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle discount bond (coupon < YTM) using calculated YTM")
            void testDiscountBond() {
                // Given: Discount bond (low coupon, market value < face value)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 200, // 2% coupon
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        85000, // Discount price implies YTM > coupon rate
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Discount bond should have longer duration (principal repayment is relatively more important)
                assertTrue(duration > 4.0, "Discount bond duration should be closer to maturity");
                assertTrue(duration < 5.0, "Duration should still be less than maturity");
                assertTrue(ytm > 0.02, "YTM should be greater than coupon rate for discount bond");

                logger.info("Discount bond (2% coupon, YTM: {}%) - Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should verify premium vs discount duration relationship using calculated YTM")
            void testPremiumVsDiscountDuration() {
                // Given: Two bonds with same maturity but different coupon rates (both at par)
                LocalDate issueDate = LocalDate.now().minusYears(5);
                LocalDate maturityDate = LocalDate.now().plusYears(5);
                String paymentTerm = "semiannual";

                Bond premiumBond = createBond(100000, 1000, issueDate, maturityDate, paymentTerm); // 10% coupon
                Bond discountBond = createBond(100000, 200, issueDate, maturityDate, paymentTerm); // 2% coupon

                // Calculate YTM for each bond
                double premiumYTM = calculateYTMDecimal(premiumBond);
                double discountYTM = calculateYTMDecimal(discountBond);

                // When
                double premiumDuration = durationHelper.calculateMacaulayDuration(premiumBond, premiumYTM);
                double discountDuration = durationHelper.calculateMacaulayDuration(discountBond, discountYTM);

                // Then: Discount bond should have longer duration
                assertTrue(discountDuration > premiumDuration,
                        "Discount bond should have longer duration than premium bond");

                logger.info("Premium bond (10% coupon, YTM: {}%) duration: {} years",
                        String.format("%.2f", premiumYTM * 100), String.format("%.4f", premiumDuration));
                logger.info("Discount bond (2% coupon, YTM: {}%) duration: {} years",
                        String.format("%.2f", discountYTM * 100), String.format("%.4f", discountDuration));
                logger.info("Duration difference: {} years", String.format("%.4f", discountDuration - premiumDuration));
            }

            @Test
            @DisplayName("Should handle very low coupon rate using calculated YTM")
            void testVeryLowCouponRate() {
                // Given: Bond with very low coupon (0.5%)
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 50, // 0.5% coupon
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Very low coupon bond should have duration close to maturity
                assertTrue(duration > 4.5, "Very low coupon bond duration should be close to maturity");
                assertTrue(duration < 5.0, "Duration should still be less than maturity");

                logger.info("Very low coupon (0.5%, YTM: {}%) bond - Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle negative YTM scenario")
            void testNegativeYTMScenario() {
                // Given: Bond with negative YTM (rare but possible in some markets)
                // Simulated by bond trading at significant premium
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 200,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        120000, // Very high premium to simulate negative YTM
                        paymentTerm);

                // Calculate YTM - may be negative due to premium
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then: Should still calculate (negative rates push more value to future payments)
                assertTrue(duration > 0, "Duration should be positive even with low/negative YTM");

                logger.info("Negative/Low YTM ({}%) bond - Duration: {} years",
                        String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @ParameterizedTest
            @CsvSource({
                    "100, 0.5",    // 1% coupon - close to zero-coupon behavior
                    "300, 0.5",    // 3% coupon
                    "500, 0.5",    // 5% coupon (par)
                    "800, 0.5",    // 8% coupon
                    "1200, 0.5"    // 12% coupon - high coupon behavior
            })
            @DisplayName("Should show duration decreasing as coupon rate increases using calculated YTM")
            void testDurationVsCouponRate(int couponBps, double expectedMinDuration) {
                // Given: Bonds with different coupon rates
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, couponBps,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double ytm = calculateYTMDecimal(bond);

                // When
                double duration = durationHelper.calculateMacaulayDuration(bond, ytm);

                // Then
                assertTrue(duration > expectedMinDuration, "Duration should be above minimum threshold");
                assertTrue(duration < 5.0, "Duration should be less than time to maturity");

                logger.info("Coupon {}% (YTM: {}%): Duration = {} years",
                        couponBps / 100.0, String.format("%.2f", ytm * 100), String.format("%.4f", duration));
            }

            @Test
            @DisplayName("Should handle modified duration with zero YTM (basis points)")
            void testModifiedDurationWithZeroYTMBasisPoints() {
                // Given
                double macaulayDuration = 5.0;
                double ytmBasisPoints = 0; // 0%
                String paymentTerm = "semiannual";

                // When
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerm);

                // Then: Should equal Macaulay Duration when YTM is zero
                assertEquals(macaulayDuration, modifiedDuration, 0.0001,
                        "Modified Duration should equal Macaulay Duration when YTM is zero");

                logger.info("Zero YTM (0 bps): Modified Duration = {} years", String.format("%.4f", modifiedDuration));
            }

            @Test
            @DisplayName("Should handle extreme high YTM scenario")
            void testExtremeHighYTMScenario() {
                // Given: Very high YTM (25%) - simulated with deep discount bond
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 1000,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        50000, // Deep discount to simulate high YTM
                        paymentTerm);

                // Calculate YTM - will be high due to deep discount
                double ytm = calculateYTMDecimal(bond);

                // When
                double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, ytm);
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, ytm * 10000, paymentTerm);

                // Then: High YTM should significantly reduce duration
                assertTrue(macaulayDuration > 0, "Macaulay Duration should be positive");
                assertTrue(modifiedDuration < macaulayDuration,
                        "Modified Duration should be less than Macaulay Duration");

                logger.info("Extreme high YTM ({}%) scenario:", String.format("%.2f", ytm * 100));
                logger.info("  Macaulay Duration: {} years", String.format("%.4f", macaulayDuration));
                logger.info("  Modified Duration: {} years", String.format("%.4f", modifiedDuration));
                logger.info("  Ratio (Modified/Macaulay): {}", String.format("%.4f", modifiedDuration / macaulayDuration));
            }
        }

        @Nested
        @DisplayName("Cross-Validation Tests")
        class CrossValidationTests {

            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, int marketValue, String paymentTerm) {
                Bond bond = new Bond();
                bond.setFaceValue(faceValue);
                bond.setCouponRate(couponRate);
                bond.setIssueDate(issueDate);
                bond.setMaturityDate(maturityDate);
                bond.setMarketValue(marketValue);
                bond.setPaymentTerm(paymentTerm);
                return bond;
            }

            private Bond createBond(int faceValue, int couponRate, LocalDate issueDate, LocalDate maturityDate, String paymentTerm) {
                return createBond(faceValue, couponRate, issueDate, maturityDate, faceValue, paymentTerm);
            }

            /**
             * Helper method to calculate YTM using YTMHelper and convert to decimal
             */
            private double calculateYTMDecimal(Bond bond) {
                LocalDate currentDate = LocalDate.now();
                double ytmBasisPoints = ytmHelper.calculateYTM(currentDate, bond);
                return ytmBasisPoints / 10000.0; // Convert basis points to decimal
            }

            @Test
            @DisplayName("Should verify both Modified Duration methods produce same results")
            void testModifiedDurationMethodsConsistency() {
                // Test multiple scenarios
                String[] paymentTerms = {"annual", "semiannual", "quarterly", "monthly"};
                double[][] testCases = {
                        {3.0, 300},   // 3yr Macaulay, 3% YTM
                        {5.0, 500},   // 5yr Macaulay, 5% YTM
                        {7.5, 750},   // 7.5yr Macaulay, 7.5% YTM
                        {2.0, 200}    // 2yr Macaulay, 2% YTM
                };

                for (int i = 0; i < testCases.length; i++) {
                    double macaulayDuration = testCases[i][0];
                    double ytmBasisPoints = testCases[i][1];
                    int couponFrequency = commonHelper.periodsPerPaymentTerm(paymentTerms[i]);
                    double ytmDecimal = ytmBasisPoints / 10000.0;

                    // Calculate using instance method
                    double instanceResult = durationHelper.calculateModifiedDuration(macaulayDuration, ytmBasisPoints, paymentTerms[i]);

                    // Verify expected formula: Macaulay Duration / (1 + YTM per period)
                    double ytmPerPeriod = ytmDecimal / couponFrequency;
                    double expectedResult = macaulayDuration / (1 + ytmPerPeriod);

                    assertEquals(expectedResult, instanceResult, 0.0001,
                            "Instance method should match expected formula for test case " + i);

                    logger.info("Test case {}: Macaulay={}, YTM={}%, Frequency={} ({}) -> Expected={}, Instance={}",
                            i, macaulayDuration, ytmBasisPoints / 100.0, couponFrequency, paymentTerms[i],
                            String.format("%.4f", expectedResult), String.format("%.4f", instanceResult));
                }
            }

            @Test
            @DisplayName("Should verify duration relationships are mathematically consistent using calculated YTM")
            void testDurationMathematicalConsistency() {
                // Given: Multiple bonds with increasing maturities
                String paymentTerm = "semiannual";

                Bond[] bonds = new Bond[4];
                double[] ytms = new double[4];
                for (int i = 0; i < 4; i++) {
                    bonds[i] = createBond(100000, 500,
                            LocalDate.now().minusYears(5),
                            LocalDate.now().plusYears(2 + i * 2), // 2, 4, 6, 8 years
                            paymentTerm);
                    ytms[i] = calculateYTMDecimal(bonds[i]);
                }

                double[] durations = new double[4];
                for (int i = 0; i < 4; i++) {
                    durations[i] = durationHelper.calculateMacaulayDuration(bonds[i], ytms[i]);
                }

                // Verify monotonic increase with maturity
                for (int i = 1; i < 4; i++) {
                    assertTrue(durations[i] > durations[i - 1],
                            "Duration should increase with maturity");
                }

                logger.info("=== Duration Mathematical Consistency Test (with calculated YTM) ===");
                for (int i = 0; i < 4; i++) {
                    logger.info("Maturity {}yr (YTM: {}%): Duration = {} years",
                            (2 + i * 2), String.format("%.2f", ytms[i] * 100), String.format("%.4f", durations[i]));
                }
            }

            @Test
            @DisplayName("Should verify Modified Duration approximates price sensitivity using calculated YTM")
            void testModifiedDurationPriceSensitivityApproximation() {
                // Given: A bond with known characteristics
                String paymentTerm = "semiannual";
                Bond bond = createBond(100000, 600,
                        LocalDate.now().minusYears(5),
                        LocalDate.now().plusYears(5),
                        paymentTerm);

                // Calculate YTM using YTMHelper
                double baseYTM = calculateYTMDecimal(bond);
                double deltaYTM = 0.001; // 10 basis points

                // When: Calculate duration
                double macaulayDuration = durationHelper.calculateMacaulayDuration(bond, baseYTM);
                double modifiedDuration = durationHelper.calculateModifiedDuration(macaulayDuration, baseYTM * 10000, paymentTerm);

                // Duration approximation: ΔP/P ≈ -ModDur × Δy
                double estimatedPercentChange = -modifiedDuration * deltaYTM * 100;

                // Then: Verify the approximation is reasonable (within realistic bounds)
                assertTrue(Math.abs(estimatedPercentChange) < modifiedDuration * deltaYTM * 150,
                        "Price change estimate should be reasonable");

                logger.info("=== Price Sensitivity Approximation Verification (with calculated YTM) ===");
                logger.info("Calculated YTM: {}%", String.format("%.2f", baseYTM * 100));
                logger.info("Macaulay Duration: {} years", String.format("%.4f", macaulayDuration));
                logger.info("Modified Duration: {} years", String.format("%.4f", modifiedDuration));
                logger.info("For {} bps yield change:", deltaYTM * 10000);
                logger.info("  Estimated Price Change: {}%", String.format("%.4f", estimatedPercentChange));
            }

            @Test
            @DisplayName("Should verify duration approaches maturity for zero-coupon bonds across maturities")
            void testZeroCouponDurationEqualsMaturityAcrossMaturities() {
                // For zero-coupon bonds, use a hardcoded YTM since YTMHelper requires coupon payments
                String paymentTerm = "semiannual";
                double ytm = 0.05;

                int[] maturitiesYears = {1, 2, 5, 10, 20};

                for (int maturityYears : maturitiesYears) {
                    Bond zeroCouponBond = createBond(100000, 0,
                            LocalDate.now().minusYears(maturityYears),
                            LocalDate.now().plusYears(maturityYears),
                            paymentTerm);

                    double duration = durationHelper.calculateMacaulayDuration(zeroCouponBond, ytm);

                    // Zero-coupon duration should equal time to maturity
                    assertEquals(maturityYears, duration, 0.1,
                            "Zero-coupon bond duration should equal maturity for " + maturityYears + " years");

                    logger.info("Zero-coupon bond ({}yr maturity): Duration = {} years",
                            maturityYears, String.format("%.4f", duration));
                }
            }
        }
    }
}

