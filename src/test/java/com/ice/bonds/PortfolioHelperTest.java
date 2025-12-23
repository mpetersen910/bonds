package com.ice.bonds;

import com.ice.bonds.helper.PortfolioHelper;
import com.ice.bonds.model.Bond;
import com.ice.bonds.model.Portfolio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Portfolio Helper Tests")
class PortfolioHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioHelperTest.class);
    private PortfolioHelper portfolioHelper;

    @BeforeEach
    void setUp() {
        portfolioHelper = new PortfolioHelper();
    }

    /**
     * Helper method to create a bond with specified parameters
     */
    private Bond createBond(String isin, int marketValue, int quantity, double macaulayDuration, double modifiedDuration) {
        Bond bond = new Bond();
        bond.setISIN(isin);
        bond.setMarketValue(marketValue);
        bond.setQuantity(quantity);
        bond.setMacaulayDuration(macaulayDuration);
        bond.setModifiedDuration(modifiedDuration);
        bond.setFaceValue(100000); // $1000 default
        bond.setCouponRate(500); // 5% default
        bond.setIssueDate(LocalDate.of(2020, 1, 1));
        bond.setMaturityDate(LocalDate.of(2030, 1, 1));
        bond.setPaymentTerm("Semiannual");
        return bond;
    }

    /**
     * Helper method to create a portfolio with bonds
     */
    private Portfolio createPortfolio(String accountId, List<Bond> bonds) {
        Portfolio portfolio = new Portfolio(accountId);
        for (Bond bond : bonds) {
            portfolio.addBond(bond);
        }
        // Update total portfolio value
        long totalValue = bonds.stream().mapToLong(Bond::getTotalMarketValue).sum();
        portfolio.setTotalPortfolioValue(totalValue);
        return portfolio;
    }

    @Nested
    @DisplayName("calculateWeightedAverageMacaulayDuration Tests")
    class CalculateWeightedAverageMacaulayDurationTests {

        @Test
        @DisplayName("Should calculate correct weighted average for single bond portfolio")
        void testSingleBondPortfolio() {
            // Given: Single bond with Macaulay duration of 5.5 years
            Bond bond = createBond("US0378331005", 100000, 10, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: Single bond should have its own duration as weighted average
            logger.info("Single bond portfolio weighted Macaulay duration: {}", weightedDuration);
            assertEquals(5.5, weightedDuration, 0.001,
                    "Single bond portfolio should have duration equal to the bond's duration");
        }

        @Test
        @DisplayName("Should calculate correct weighted average for two equal-weighted bonds")
        void testTwoEqualWeightedBonds() {
            // Given: Two bonds with equal market values
            Bond bond1 = createBond("US0378331005", 100000, 10, 4.0, 3.8); // MV = 1,000,000
            Bond bond2 = createBond("US5949181045", 100000, 10, 6.0, 5.7); // MV = 1,000,000
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: (4.0 × 1M + 6.0 × 1M) / 2M = 5.0
            double expectedDuration = (4.0 + 6.0) / 2;
            logger.info("Two equal-weighted bonds: bond1 duration={}, bond2 duration={}, weighted avg={}",
                    bond1.getMacaulayDuration(), bond2.getMacaulayDuration(), weightedDuration);
            assertEquals(expectedDuration, weightedDuration, 0.001,
                    "Equal-weighted portfolio should be simple average");
        }

        @Test
        @DisplayName("Should calculate correct weighted average for unequal-weighted bonds")
        void testUnequalWeightedBonds() {
            // Given: Two bonds with different market values
            // Bond1: MV = $1000, Qty = 20 -> Total = $20,000, Duration = 3.0
            // Bond2: MV = $1000, Qty = 80 -> Total = $80,000, Duration = 7.0
            Bond bond1 = createBond("US0378331005", 100000, 20, 3.0, 2.8);
            Bond bond2 = createBond("US5949181045", 100000, 80, 7.0, 6.6);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: (3.0 × 20,000 + 7.0 × 80,000) / 100,000 = (60,000 + 560,000) / 100,000 = 6.2
            long totalValue = bond1.getTotalMarketValue() + bond2.getTotalMarketValue();
            double expectedDuration = (3.0 * 2000000 + 7.0 * 8000000) / totalValue;
            logger.info("Unequal-weighted bonds: bond1 MV={}, bond2 MV={}, total={}, weighted avg={}",
                    bond1.getTotalMarketValue() / 100, bond2.getTotalMarketValue() / 100, totalValue / 100, weightedDuration);
            assertEquals(expectedDuration, weightedDuration, 0.001,
                    "Weighted average should favor higher weighted bond");
        }

        @Test
        @DisplayName("Should calculate correct weighted average for multiple bonds")
        void testMultipleBonds() {
            // Given: Three bonds with different weights
            Bond bond1 = createBond("US0378331005", 100000, 10, 2.0, 1.9); // MV = 1M
            Bond bond2 = createBond("US5949181045", 100000, 20, 5.0, 4.7); // MV = 2M
            Bond bond3 = createBond("GB0002634946", 100000, 30, 8.0, 7.5); // MV = 3M
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: (2×1M + 5×2M + 8×3M) / 6M = (2M + 10M + 24M) / 6M = 36M / 6M = 6.0
            double expectedDuration = (2.0 * 1000000 + 5.0 * 2000000 + 8.0 * 3000000) / 6000000;
            logger.info("Multiple bonds weighted duration: {}, expected: {}", weightedDuration, expectedDuration);
            assertEquals(expectedDuration, weightedDuration, 0.001,
                    "Weighted average should account for all bond weights");
        }

        @Test
        @DisplayName("Should return 0 for empty portfolio")
        void testEmptyPortfolio() {
            // Given: Empty portfolio
            Portfolio portfolio = createPortfolio("ACC001", new ArrayList<>());

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then
            logger.info("Empty portfolio weighted Macaulay duration: {}", weightedDuration);
            assertEquals(0.0, weightedDuration, 0.001,
                    "Empty portfolio should return 0 duration");
        }

        @Test
        @DisplayName("Should return 0 when total market value is 0")
        void testZeroMarketValue() {
            // Given: Bond with zero quantity
            Bond bond = createBond("US0378331005", 100000, 0, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then
            logger.info("Zero market value portfolio weighted duration: {}", weightedDuration);
            assertEquals(0.0, weightedDuration, 0.001,
                    "Portfolio with zero market value should return 0 duration");
        }

        @Test
        @DisplayName("Should handle bonds with zero duration")
        void testZeroDurationBonds() {
            // Given: Mix of bonds with zero and non-zero durations
            Bond bond1 = createBond("US0378331005", 100000, 10, 0.0, 0.0); // Zero duration
            Bond bond2 = createBond("US5949181045", 100000, 10, 6.0, 5.7);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: (0 × 1M + 6 × 1M) / 2M = 3.0
            logger.info("Mixed zero/non-zero duration portfolio: {}", weightedDuration);
            assertEquals(3.0, weightedDuration, 0.001,
                    "Should correctly weight bond with zero duration");
        }

        @Test
        @DisplayName("Should handle very large portfolio values")
        void testLargePortfolioValues() {
            // Given: Very large market values
            Bond bond1 = createBond("US0378331005", Integer.MAX_VALUE / 2, 2, 4.0, 3.8);
            Bond bond2 = createBond("US5949181045", Integer.MAX_VALUE / 2, 2, 6.0, 5.7);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then
            logger.info("Large portfolio weighted duration: {}", weightedDuration);
            assertEquals(5.0, weightedDuration, 0.001,
                    "Should handle large values without overflow");
        }

        @Test
        @DisplayName("Should handle high precision duration values")
        void testHighPrecisionDurations() {
            // Given: Bonds with high precision durations
            Bond bond1 = createBond("US0378331005", 100000, 10, 4.123456789, 3.9);
            Bond bond2 = createBond("US5949181045", 100000, 10, 5.987654321, 5.6);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then
            double expectedDuration = (4.123456789 + 5.987654321) / 2;
            logger.info("High precision duration: {}, expected: {}", weightedDuration, expectedDuration);
            assertEquals(expectedDuration, weightedDuration, 0.000001,
                    "Should preserve high precision");
        }
    }

    @Nested
    @DisplayName("calculateWeightedAverageModifiedDuration Tests")
    class CalculateWeightedAverageModifiedDurationTests {

        @Test
        @DisplayName("Should calculate correct weighted average for single bond portfolio")
        void testSingleBondPortfolio() {
            // Given: Single bond with Modified duration of 5.2 years
            Bond bond = createBond("US0378331005", 100000, 10, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then
            logger.info("Single bond portfolio weighted Modified duration: {}", weightedDuration);
            assertEquals(5.2, weightedDuration, 0.001,
                    "Single bond portfolio should have duration equal to the bond's duration");
        }

        @Test
        @DisplayName("Should calculate correct weighted average for two equal-weighted bonds")
        void testTwoEqualWeightedBonds() {
            // Given: Two bonds with equal market values
            Bond bond1 = createBond("US0378331005", 100000, 10, 4.0, 3.8);
            Bond bond2 = createBond("US5949181045", 100000, 10, 6.0, 5.6);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then: (3.8 + 5.6) / 2 = 4.7
            double expectedDuration = (3.8 + 5.6) / 2;
            logger.info("Two equal-weighted bonds Modified duration: {}", weightedDuration);
            assertEquals(expectedDuration, weightedDuration, 0.001,
                    "Equal-weighted portfolio should be simple average");
        }

        @Test
        @DisplayName("Should calculate correct weighted average for unequal-weighted bonds")
        void testUnequalWeightedBonds() {
            // Given: Two bonds with different market values
            Bond bond1 = createBond("US0378331005", 100000, 30, 3.0, 2.5); // MV = 3M
            Bond bond2 = createBond("US5949181045", 100000, 70, 7.0, 6.5); // MV = 7M
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then: (2.5 × 3M + 6.5 × 7M) / 10M = (7.5M + 45.5M) / 10M = 5.3
            double expectedDuration = (2.5 * 3000000 + 6.5 * 7000000) / 10000000;
            logger.info("Unequal-weighted bonds Modified duration: {}, expected: {}", weightedDuration, expectedDuration);
            assertEquals(expectedDuration, weightedDuration, 0.001,
                    "Weighted average should favor higher weighted bond");
        }

        @Test
        @DisplayName("Should return 0 for empty portfolio")
        void testEmptyPortfolio() {
            // Given: Empty portfolio
            Portfolio portfolio = createPortfolio("ACC001", new ArrayList<>());

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then
            logger.info("Empty portfolio weighted Modified duration: {}", weightedDuration);
            assertEquals(0.0, weightedDuration, 0.001,
                    "Empty portfolio should return 0 duration");
        }

        @Test
        @DisplayName("Should return 0 when total market value is 0")
        void testZeroMarketValue() {
            // Given: Bond with zero quantity
            Bond bond = createBond("US0378331005", 100000, 0, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then
            logger.info("Zero market value portfolio weighted Modified duration: {}", weightedDuration);
            assertEquals(0.0, weightedDuration, 0.001,
                    "Portfolio with zero market value should return 0 duration");
        }

        @Test
        @DisplayName("Should handle multiple bonds with varying durations")
        void testMultipleBonds() {
            // Given: Five bonds with different weights and durations
            Bond bond1 = createBond("US0378331005", 100000, 10, 2.0, 1.8);
            Bond bond2 = createBond("US5949181045", 100000, 20, 4.0, 3.6);
            Bond bond3 = createBond("GB0002634946", 100000, 30, 6.0, 5.4);
            Bond bond4 = createBond("DE0007164600", 100000, 25, 5.0, 4.5);
            Bond bond5 = createBond("FR0000120578", 100000, 15, 3.0, 2.7);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3, bond4, bond5));

            // When
            double weightedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then: Calculate expected weighted average
            long totalValue = 10000000L; // 100 bonds × $1000
            double expectedDuration = (1.8 * 1000000 + 3.6 * 2000000 + 5.4 * 3000000 + 4.5 * 2500000 + 2.7 * 1500000) / totalValue;
            logger.info("Multiple bonds weighted Modified duration: {}, expected: {}", weightedDuration, expectedDuration);
            assertEquals(expectedDuration, weightedDuration, 0.001,
                    "Should correctly calculate weighted average for multiple bonds");
        }

        @Test
        @DisplayName("Modified duration should always be less than or equal to Macaulay duration")
        void testModifiedLessThanMacaulay() {
            // Given: Portfolio with bonds where Modified < Macaulay
            Bond bond1 = createBond("US0378331005", 100000, 10, 5.0, 4.8);
            Bond bond2 = createBond("US5949181045", 100000, 10, 7.0, 6.6);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double modifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then
            logger.info("Macaulay: {}, Modified: {}", macaulayDuration, modifiedDuration);
            assertTrue(modifiedDuration <= macaulayDuration,
                    "Weighted Modified duration should be less than or equal to Weighted Macaulay duration");
        }
    }

    @Nested
    @DisplayName("calculateTotalPortfolioValue Tests")
    class CalculateTotalPortfolioValueTests {

        @Test
        @DisplayName("Should calculate correct total for single bond")
        void testSingleBond() {
            // Given: Single bond with MV = $1000, Qty = 10
            Bond bond = createBond("US0378331005", 100000, 10, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then: 100000 cents × 10 = 1,000,000 cents = $10,000
            logger.info("Single bond total value: {} cents (${}) ", totalValue, totalValue / 100);
            assertEquals(1000000, totalValue,
                    "Total value should be market value × quantity");
        }

        @Test
        @DisplayName("Should calculate correct total for multiple bonds")
        void testMultipleBonds() {
            // Given: Multiple bonds
            Bond bond1 = createBond("US0378331005", 100000, 10, 5.5, 5.2); // 1,000,000
            Bond bond2 = createBond("US5949181045", 150000, 20, 4.0, 3.8); // 3,000,000
            Bond bond3 = createBond("GB0002634946", 200000, 5, 6.0, 5.7);  // 1,000,000
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then: 1M + 3M + 1M = 5M cents = $50,000
            logger.info("Multiple bonds total value: {} cents (${}) ", totalValue, totalValue / 100);
            assertEquals(5000000, totalValue,
                    "Total value should be sum of all bond values");
        }

        @Test
        @DisplayName("Should return 0 for empty portfolio")
        void testEmptyPortfolio() {
            // Given: Empty portfolio
            Portfolio portfolio = createPortfolio("ACC001", new ArrayList<>());

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then
            logger.info("Empty portfolio total value: {}", totalValue);
            assertEquals(0, totalValue,
                    "Empty portfolio should have zero value");
        }

        @Test
        @DisplayName("Should return 0 when all bonds have zero quantity")
        void testZeroQuantityBonds() {
            // Given: Bonds with zero quantity
            Bond bond1 = createBond("US0378331005", 100000, 0, 5.5, 5.2);
            Bond bond2 = createBond("US5949181045", 150000, 0, 4.0, 3.8);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then
            logger.info("Zero quantity bonds total value: {}", totalValue);
            assertEquals(0, totalValue,
                    "Bonds with zero quantity should contribute zero value");
        }

        @Test
        @DisplayName("Should handle large quantities without overflow")
        void testLargeQuantities() {
            // Given: Bonds with large quantities
            Bond bond1 = createBond("US0378331005", 100000, 10000, 5.5, 5.2);
            Bond bond2 = createBond("US5949181045", 100000, 10000, 4.0, 3.8);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then: 100000 × 10000 × 2 = 2,000,000,000 cents = $20,000,000
            logger.info("Large quantities total value: {} cents (${}) ", totalValue, totalValue / 100);
            assertEquals(2000000000L, totalValue,
                    "Should handle large quantities correctly");
        }

        @ParameterizedTest
        @DisplayName("Should calculate correct totals for various market values and quantities")
        @CsvSource({
            "100000, 1, 100000",      // $1000 × 1 = $1000
            "100000, 100, 10000000",  // $1000 × 100 = $100,000
            "50000, 50, 2500000",     // $500 × 50 = $25,000
            "1, 1000000, 1000000",    // $0.01 × 1,000,000 = $10,000
        })
        void testVariousMarketValuesAndQuantities(int marketValue, int quantity, long expectedTotal) {
            // Given
            Bond bond = createBond("US0378331005", marketValue, quantity, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then
            logger.info("MV={}, Qty={}, Total={} (expected={})", marketValue, quantity, totalValue, expectedTotal);
            assertEquals(expectedTotal, totalValue,
                    String.format("MV %d × Qty %d should equal %d", marketValue, quantity, expectedTotal));
        }
    }

    @Nested
    @DisplayName("calculateBondWeight Tests")
    class CalculateBondWeightTests {

        @Test
        @DisplayName("Should return 1.0 for single bond portfolio")
        void testSingleBondWeight() {
            // Given: Single bond portfolio
            Bond bond = createBond("US0378331005", 100000, 10, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));
            portfolio.setTotalPortfolioValue(bond.getTotalMarketValue());

            // When
            double weight = portfolioHelper.calculateBondWeight(bond, portfolio);

            // Then
            logger.info("Single bond weight: {}", weight);
            assertEquals(1.0, weight, 0.001,
                    "Single bond should have weight of 1.0");
        }

        @Test
        @DisplayName("Should return correct weights for two equal bonds")
        void testTwoEqualBondsWeights() {
            // Given: Two bonds with equal values
            Bond bond1 = createBond("US0378331005", 100000, 10, 5.5, 5.2);
            Bond bond2 = createBond("US5949181045", 100000, 10, 4.0, 3.8);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));
            portfolio.setTotalPortfolioValue(bond1.getTotalMarketValue() + bond2.getTotalMarketValue());

            // When
            double weight1 = portfolioHelper.calculateBondWeight(bond1, portfolio);
            double weight2 = portfolioHelper.calculateBondWeight(bond2, portfolio);

            // Then
            logger.info("Bond1 weight: {}, Bond2 weight: {}", weight1, weight2);
            assertEquals(0.5, weight1, 0.001, "First bond should have weight 0.5");
            assertEquals(0.5, weight2, 0.001, "Second bond should have weight 0.5");
            assertEquals(1.0, weight1 + weight2, 0.001, "Weights should sum to 1.0");
        }

        @Test
        @DisplayName("Should return correct weights for unequal bonds")
        void testUnequalBondsWeights() {
            // Given: Bonds with different values (1:3 ratio)
            Bond bond1 = createBond("US0378331005", 100000, 10, 5.5, 5.2);  // 1M
            Bond bond2 = createBond("US5949181045", 100000, 30, 4.0, 3.8);  // 3M
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));
            portfolio.setTotalPortfolioValue(bond1.getTotalMarketValue() + bond2.getTotalMarketValue());

            // When
            double weight1 = portfolioHelper.calculateBondWeight(bond1, portfolio);
            double weight2 = portfolioHelper.calculateBondWeight(bond2, portfolio);

            // Then
            logger.info("Bond1 weight: {}, Bond2 weight: {}", weight1, weight2);
            assertEquals(0.25, weight1, 0.001, "Smaller bond should have weight 0.25");
            assertEquals(0.75, weight2, 0.001, "Larger bond should have weight 0.75");
            assertEquals(1.0, weight1 + weight2, 0.001, "Weights should sum to 1.0");
        }

        @Test
        @DisplayName("Should return 0 when total portfolio value is 0")
        void testZeroPortfolioValue() {
            // Given: Portfolio with zero value
            Bond bond = createBond("US0378331005", 100000, 0, 5.5, 5.2);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond));
            portfolio.setTotalPortfolioValue(0);

            // When
            double weight = portfolioHelper.calculateBondWeight(bond, portfolio);

            // Then
            logger.info("Zero portfolio value weight: {}", weight);
            assertEquals(0.0, weight, 0.001,
                    "Weight should be 0 when portfolio value is 0");
        }

        @Test
        @DisplayName("Should return 0 when bond has zero market value")
        void testZeroBondValue() {
            // Given: Bond with zero quantity in a portfolio with other bonds
            Bond zeroBond = createBond("US0378331005", 100000, 0, 5.5, 5.2);  // 0 value
            Bond normalBond = createBond("US5949181045", 100000, 10, 4.0, 3.8); // 1M
            Portfolio portfolio = createPortfolio("ACC001", List.of(zeroBond, normalBond));
            portfolio.setTotalPortfolioValue(normalBond.getTotalMarketValue());

            // When
            double zeroWeight = portfolioHelper.calculateBondWeight(zeroBond, portfolio);
            double normalWeight = portfolioHelper.calculateBondWeight(normalBond, portfolio);

            // Then
            logger.info("Zero value bond weight: {}, Normal bond weight: {}", zeroWeight, normalWeight);
            assertEquals(0.0, zeroWeight, 0.001,
                    "Bond with zero value should have zero weight");
            assertEquals(1.0, normalWeight, 0.001,
                    "Normal bond should have full weight");
        }

        @Test
        @DisplayName("Should calculate correct weights for multiple bonds")
        void testMultipleBondsWeights() {
            // Given: Four bonds with values: 1M, 2M, 3M, 4M (total 10M)
            Bond bond1 = createBond("US0378331005", 100000, 10, 5.5, 5.2);   // 1M
            Bond bond2 = createBond("US5949181045", 100000, 20, 4.0, 3.8);   // 2M
            Bond bond3 = createBond("GB0002634946", 100000, 30, 6.0, 5.7);   // 3M
            Bond bond4 = createBond("DE0007164600", 100000, 40, 5.0, 4.7);   // 4M
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3, bond4));
            long totalValue = bond1.getTotalMarketValue() + bond2.getTotalMarketValue() +
                    bond3.getTotalMarketValue() + bond4.getTotalMarketValue();
            portfolio.setTotalPortfolioValue(totalValue);

            // When
            double weight1 = portfolioHelper.calculateBondWeight(bond1, portfolio);
            double weight2 = portfolioHelper.calculateBondWeight(bond2, portfolio);
            double weight3 = portfolioHelper.calculateBondWeight(bond3, portfolio);
            double weight4 = portfolioHelper.calculateBondWeight(bond4, portfolio);

            // Then
            logger.info("Bond weights: {}, {}, {}, {}", weight1, weight2, weight3, weight4);
            assertEquals(0.1, weight1, 0.001, "Bond1 should have weight 0.1");
            assertEquals(0.2, weight2, 0.001, "Bond2 should have weight 0.2");
            assertEquals(0.3, weight3, 0.001, "Bond3 should have weight 0.3");
            assertEquals(0.4, weight4, 0.001, "Bond4 should have weight 0.4");
            assertEquals(1.0, weight1 + weight2 + weight3 + weight4, 0.001,
                    "All weights should sum to 1.0");
        }

        @ParameterizedTest
        @DisplayName("Should calculate correct weights for various proportions")
        @CsvSource({
            "10, 90, 0.1, 0.9",
            "25, 75, 0.25, 0.75",
            "33, 67, 0.33, 0.67",
            "50, 50, 0.5, 0.5",
            "1, 99, 0.01, 0.99"
        })
        void testVariousProportions(int qty1, int qty2, double expectedWeight1, double expectedWeight2) {
            // Given: Two bonds with specified quantities
            Bond bond1 = createBond("US0378331005", 100000, qty1, 5.5, 5.2);
            Bond bond2 = createBond("US5949181045", 100000, qty2, 4.0, 3.8);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));
            portfolio.setTotalPortfolioValue(bond1.getTotalMarketValue() + bond2.getTotalMarketValue());

            // When
            double weight1 = portfolioHelper.calculateBondWeight(bond1, portfolio);
            double weight2 = portfolioHelper.calculateBondWeight(bond2, portfolio);

            // Then
            logger.info("Quantities: {}/{}, Weights: {}/{}", qty1, qty2, weight1, weight2);
            assertEquals(expectedWeight1, weight1, 0.01,
                    String.format("Bond1 with qty %d should have weight %.2f", qty1, expectedWeight1));
            assertEquals(expectedWeight2, weight2, 0.01,
                    String.format("Bond2 with qty %d should have weight %.2f", qty2, expectedWeight2));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditionsTests {

        @Test
        @DisplayName("Should handle portfolio with very small market values")
        void testVerySmallMarketValues() {
            // Given: Bonds with minimal market values (1 cent)
            Bond bond1 = createBond("US0378331005", 1, 10, 5.5, 5.2);
            Bond bond2 = createBond("US5949181045", 1, 10, 4.0, 3.8);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then
            logger.info("Minimal values: total={}, duration={}", totalValue, macaulayDuration);
            assertEquals(20, totalValue, "Should correctly sum minimal values");
            assertEquals(4.75, macaulayDuration, 0.001, "Should calculate correct weighted average");
        }

        @Test
        @DisplayName("Should handle bond weights that don't divide evenly")
        void testIrrationalWeights() {
            // Given: Three equal bonds (weights should be 1/3 each)
            Bond bond1 = createBond("US0378331005", 100000, 10, 3.0, 2.8);
            Bond bond2 = createBond("US5949181045", 100000, 10, 6.0, 5.6);
            Bond bond3 = createBond("GB0002634946", 100000, 10, 9.0, 8.4);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));
            portfolio.setTotalPortfolioValue(3000000L);

            // When
            double weight1 = portfolioHelper.calculateBondWeight(bond1, portfolio);
            double weight2 = portfolioHelper.calculateBondWeight(bond2, portfolio);
            double weight3 = portfolioHelper.calculateBondWeight(bond3, portfolio);
            double totalWeight = weight1 + weight2 + weight3;

            // Then
            logger.info("Three equal bonds weights: {}, {}, {}, sum={}", weight1, weight2, weight3, totalWeight);
            assertEquals(1.0/3, weight1, 0.001, "Each bond should have weight 1/3");
            assertEquals(1.0/3, weight2, 0.001, "Each bond should have weight 1/3");
            assertEquals(1.0/3, weight3, 0.001, "Each bond should have weight 1/3");
            assertEquals(1.0, totalWeight, 0.001, "Total weight should sum to 1.0");
        }

        @Test
        @DisplayName("Should handle extreme duration values")
        void testExtremeDurationValues() {
            // Given: Bond with very high duration (long-term zero-coupon bond)
            Bond bond1 = createBond("US0378331005", 100000, 10, 30.0, 28.0);  // 30-year duration
            Bond bond2 = createBond("US5949181045", 100000, 10, 0.1, 0.095);  // Very short duration
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double modifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then
            logger.info("Extreme durations: Macaulay={}, Modified={}", macaulayDuration, modifiedDuration);
            assertEquals(15.05, macaulayDuration, 0.001, "Should correctly average extreme durations");
            assertEquals(14.0475, modifiedDuration, 0.001, "Should correctly average extreme modified durations");
        }

        @Test
        @DisplayName("Should handle negative duration values (theoretical case)")
        void testNegativeDurationValues() {
            // Given: Theoretical negative duration (e.g., floating rate notes or inverse floaters)
            Bond bond1 = createBond("US0378331005", 100000, 10, -2.0, -1.9);
            Bond bond2 = createBond("US5949181045", 100000, 10, 8.0, 7.6);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then
            logger.info("Including negative duration: weighted avg={}", macaulayDuration);
            assertEquals(3.0, macaulayDuration, 0.001, "Should handle negative durations correctly");
        }

        @Test
        @DisplayName("Should handle portfolio with single zero-value bond among others")
        void testMixedZeroAndNonZeroBonds() {
            // Given: Mix of zero and non-zero value bonds
            Bond zeroBond = createBond("US0378331005", 0, 10, 5.5, 5.2);    // Zero MV
            Bond normalBond1 = createBond("US5949181045", 100000, 10, 4.0, 3.8);
            Bond normalBond2 = createBond("GB0002634946", 100000, 20, 6.0, 5.7);
            Portfolio portfolio = createPortfolio("ACC001", List.of(zeroBond, normalBond1, normalBond2));
            portfolio.setTotalPortfolioValue(normalBond1.getTotalMarketValue() + normalBond2.getTotalMarketValue());

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double zeroWeight = portfolioHelper.calculateBondWeight(zeroBond, portfolio);

            // Then: (4.0 × 1M + 6.0 × 2M) / 3M = 16M / 3M ≈ 5.333
            double expectedDuration = (4.0 * 1000000 + 6.0 * 2000000) / 3000000;
            logger.info("Mixed portfolio duration: {}, zero bond weight: {}", macaulayDuration, zeroWeight);
            assertEquals(expectedDuration, macaulayDuration, 0.001,
                    "Zero-value bond should not affect weighted average");
            assertEquals(0.0, zeroWeight, 0.001,
                    "Zero-value bond should have zero weight");
        }

        @Test
        @DisplayName("Should handle all bonds with same duration")
        void testAllBondsSameDuration() {
            // Given: All bonds have the same duration
            Bond bond1 = createBond("US0378331005", 100000, 10, 5.0, 4.7);
            Bond bond2 = createBond("US5949181045", 150000, 20, 5.0, 4.7);
            Bond bond3 = createBond("GB0002634946", 200000, 30, 5.0, 4.7);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double modifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then: Regardless of weights, all having same duration = that duration
            logger.info("All same duration: Macaulay={}, Modified={}", macaulayDuration, modifiedDuration);
            assertEquals(5.0, macaulayDuration, 0.001,
                    "All same duration should equal that duration");
            assertEquals(4.7, modifiedDuration, 0.001,
                    "All same modified duration should equal that duration");
        }

        @Test
        @DisplayName("Should handle massive portfolio with max int market values and quantities")
        void testMassivePortfolioMaxIntValues() {
            // Given: Bonds with maximum integer values to test overflow protection
            // marketValue is int, quantity is int, but getTotalMarketValue returns long
            // Max int × Max int would overflow, so use large but safe values
            Bond bond1 = createBond("US0378331005", Integer.MAX_VALUE, 1, 5.0, 4.8);
            Bond bond2 = createBond("US5949181045", Integer.MAX_VALUE, 1, 7.0, 6.6);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: Should handle large values without overflow
            long expectedTotal = (long) Integer.MAX_VALUE * 2;
            logger.info("Max int values portfolio: total={}, expected={}, duration={}",
                    totalValue, expectedTotal, macaulayDuration);
            assertEquals(expectedTotal, totalValue, "Should correctly sum max int values");
            assertEquals(6.0, macaulayDuration, 0.001, "Duration should be simple average");
        }

        @Test
        @DisplayName("Should handle portfolio value near Long.MAX_VALUE")
        void testPortfolioValueNearLongMax() {
            // Given: Portfolio designed to approach Long.MAX_VALUE
            // Long.MAX_VALUE = 9,223,372,036,854,775,807
            // Using multiple bonds with large market values × quantities
            Bond bond1 = createBond("US0378331005", 1000000000, 1000, 5.0, 4.8); // 1 trillion cents
            Bond bond2 = createBond("US5949181045", 1000000000, 1000, 6.0, 5.7); // 1 trillion cents
            Bond bond3 = createBond("GB0002634946", 1000000000, 1000, 7.0, 6.6); // 1 trillion cents
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: 3 trillion cents = $30 billion
            long expectedTotal = 3_000_000_000_000L;
            logger.info("Near Long.MAX_VALUE portfolio: total={} cents (${} billion), duration={}",
                    totalValue, totalValue / 100_000_000_000.0, macaulayDuration);
            assertEquals(expectedTotal, totalValue, "Should handle multi-trillion cent values");
            assertEquals(6.0, macaulayDuration, 0.001, "Duration should be simple average for equal weights");
        }

        @Test
        @DisplayName("Should handle massive portfolio with 1000 bonds")
        void testMassivePortfolioThousandBonds() {
            // Given: 1000 bonds in a portfolio
            List<Bond> bonds = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                // Varying durations from 1.0 to 10.0
                double duration = 1.0 + (i % 10);
                double modDuration = duration * 0.95;
                bonds.add(createBond("US" + String.format("%09d", i) + "5", 100000, 100, duration, modDuration));
            }
            Portfolio portfolio = createPortfolio("MASSIVE_FUND", bonds);

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double modifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then: 1000 bonds × $1000 × 100 qty = $100 million = 10 billion cents
            logger.info("1000-bond portfolio: total={} cents (${} million), Macaulay={}, Modified={}",
                    totalValue, totalValue / 100_000_000.0, macaulayDuration, modifiedDuration);
            assertEquals(10_000_000_000L, totalValue, "Should handle 1000-bond portfolio");
            // Average duration: (1+2+3+4+5+6+7+8+9+10) / 10 = 5.5 (repeated 100 times)
            assertEquals(5.5, macaulayDuration, 0.001, "Duration should average correctly across 1000 bonds");
        }

        @Test
        @DisplayName("Should handle portfolio with extreme quantity multipliers")
        void testExtremeQuantityMultipliers() {
            // Given: Very high quantities (institutional investor)
            // 1 million bonds held
            Bond bond1 = createBond("US0378331005", 100000, 500000, 4.0, 3.8);  // $500M
            Bond bond2 = createBond("US5949181045", 100000, 500000, 8.0, 7.6);  // $500M
            Portfolio portfolio = createPortfolio("SOVEREIGN_FUND", List.of(bond1, bond2));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: $1 billion = 100 billion cents
            logger.info("Extreme quantity portfolio: total={} cents (${} billion), duration={}",
                    totalValue, totalValue / 100_000_000_000.0, macaulayDuration);
            assertEquals(100_000_000_000L, totalValue, "Should handle $1B portfolio");
            assertEquals(6.0, macaulayDuration, 0.001, "Duration should be simple average");
        }

        @Test
        @DisplayName("Should handle portfolio with maximum safe integer multiplication")
        void testMaxSafeIntegerMultiplication() {
            // Given: Test the boundary where int × int approaches overflow
            // Integer.MAX_VALUE = 2,147,483,647
            // To safely multiply two ints and fit in long: sqrt(Long.MAX_VALUE) ≈ 3.04 billion
            // Using values that would overflow if computed as int
            int largeMarketValue = 2_000_000_000; // $20 million in cents
            int largeQuantity = 4; // Just 4 bonds
            // largeMarketValue * largeQuantity = 8 billion - fits in long, NOT in int

            Bond bond = createBond("US0378331005", largeMarketValue, largeQuantity, 5.0, 4.8);
            Portfolio portfolio = createPortfolio("OVERFLOW_TEST", List.of(bond));

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then
            long expectedTotal = (long) largeMarketValue * largeQuantity;
            logger.info("Max safe multiplication: MV={}, Qty={}, Total={}, Expected={}",
                    largeMarketValue, largeQuantity, totalValue, expectedTotal);
            assertEquals(expectedTotal, totalValue, "Should correctly compute without int overflow");
            assertTrue(totalValue > Integer.MAX_VALUE, "Total should exceed Integer.MAX_VALUE");
        }

        @Test
        @DisplayName("Should maintain precision with very large weighted sums")
        void testPrecisionWithLargeWeightedSums() {
            // Given: Large market values that test floating-point precision in weighted calculations
            Bond bond1 = createBond("US0378331005", 999999999, 1000, 5.123456789, 4.987654321);
            Bond bond2 = createBond("US5949181045", 999999999, 1000, 6.234567890, 5.876543210);
            Portfolio portfolio = createPortfolio("PRECISION_TEST", List.of(bond1, bond2));

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double modifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Then: Equal weights, so simple average
            double expectedMacaulay = (5.123456789 + 6.234567890) / 2;
            double expectedModified = (4.987654321 + 5.876543210) / 2;
            logger.info("Precision test: Macaulay={} (expected {}), Modified={} (expected {})",
                    macaulayDuration, expectedMacaulay, modifiedDuration, expectedModified);
            assertEquals(expectedMacaulay, macaulayDuration, 0.000001, "Should maintain Macaulay precision");
            assertEquals(expectedModified, modifiedDuration, 0.000001, "Should maintain Modified precision");
        }

        @Test
        @DisplayName("Should handle asymmetric massive portfolio (1 whale, many minnows)")
        void testAsymmetricMassivePortfolio() {
            // Given: One very large holding dominating small holdings
            List<Bond> bonds = new ArrayList<>();
            // The whale: $1 billion holding with 10-year duration
            bonds.add(createBond("US0000000001", 1000000000, 1000, 10.0, 9.5));

            // 999 minnows: $1 million each with 2-year duration
            for (int i = 1; i < 1000; i++) {
                bonds.add(createBond("US" + String.format("%09d", i) + "5", 100000, 100, 2.0, 1.9));
            }
            Portfolio portfolio = createPortfolio("WHALE_PORTFOLIO", bonds);

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: Whale = $1B, Minnows = 999 × $10M = $9.99B total ≈ $10.99B
            // Duration should be heavily weighted toward whale
            long whaleValue = 1_000_000_000_000L; // 1 trillion cents
            long minnowsValue = 999L * 10_000_000L; // ~10 billion cents
            long expectedTotal = whaleValue + minnowsValue;

            logger.info("Asymmetric portfolio: total={} cents, whale%={}%, duration={}",
                    totalValue, (double) whaleValue / totalValue * 100, macaulayDuration);

            assertEquals(expectedTotal, totalValue, "Total should include whale + minnows");
            // Whale weight ≈ 99%, minnows ≈ 1%, so duration ≈ 0.99×10 + 0.01×2 ≈ 9.92
            assertTrue(macaulayDuration > 9.0 && macaulayDuration < 10.0,
                    "Duration should be dominated by whale (10-year) but pulled slightly down");
        }

        @Test
        @DisplayName("Should handle portfolio approaching total value of US bond market")
        void testUSBondMarketScalePortfolio() {
            // Given: US bond market is ~$50 trillion = 5 quadrillion cents
            // We'll test with 1% of that = $500 billion = 50 trillion cents
            // This exceeds safe int multiplication and requires long arithmetic
            List<Bond> bonds = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                // Each bond position: $5 billion = 500 billion cents
                // MV = $50,000 = 5,000,000 cents, Qty = 100,000
                bonds.add(createBond("US" + String.format("%09d", i) + "5",
                        5000000, 100000, 5.0 + (i % 10) * 0.5, 4.8 + (i % 10) * 0.48));
            }
            Portfolio portfolio = createPortfolio("MARKET_SCALE_FUND", bonds);

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: 100 bonds × $5B = $500B = 50 trillion cents
            long expectedTotal = 100L * 5_000_000L * 100_000L;
            logger.info("US bond market scale: total={} cents (${} billion), duration={}",
                    totalValue, totalValue / 100_000_000_000.0, macaulayDuration);
            assertEquals(expectedTotal, totalValue, "Should handle $500B portfolio");
            assertTrue(totalValue > 0, "Value should be positive (no overflow to negative)");
        }

        @Test
        @DisplayName("Should correctly weight when one bond dominates by 1000x")
        void testExtremeDominanceRatio() {
            // Given: One bond worth 1000x the other
            Bond dominant = createBond("US0378331005", 100000, 10000, 8.0, 7.6);  // $10 million
            Bond small = createBond("US5949181045", 100000, 10, 2.0, 1.9);        // $10 thousand
            Portfolio portfolio = createPortfolio("DOMINANCE_TEST", List.of(dominant, small));
            portfolio.setTotalPortfolioValue(dominant.getTotalMarketValue() + small.getTotalMarketValue());

            // When
            double dominantWeight = portfolioHelper.calculateBondWeight(dominant, portfolio);
            double smallWeight = portfolioHelper.calculateBondWeight(small, portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: Dominant weight ≈ 99.9%, duration ≈ 8.0
            logger.info("1000x dominance: dominant weight={}, small weight={}, duration={}",
                    dominantWeight, smallWeight, macaulayDuration);
            assertTrue(dominantWeight > 0.999, "Dominant bond should have >99.9% weight");
            assertTrue(smallWeight < 0.001, "Small bond should have <0.1% weight");
            assertEquals(1.0, dominantWeight + smallWeight, 0.0001, "Weights should sum to 1.0");
            assertTrue(macaulayDuration > 7.99 && macaulayDuration < 8.0,
                    "Duration should be very close to dominant bond's duration");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("All calculations should be consistent with each other")
        void testCalculationConsistency() {
            // Given: Portfolio with multiple bonds
            Bond bond1 = createBond("US0378331005", 100000, 10, 4.0, 3.8);
            Bond bond2 = createBond("US5949181045", 100000, 20, 5.0, 4.7);
            Bond bond3 = createBond("GB0002634946", 100000, 30, 6.0, 5.6);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));
            portfolio.setTotalPortfolioValue(
                    bond1.getTotalMarketValue() + bond2.getTotalMarketValue() + bond3.getTotalMarketValue());

            // When
            long totalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            double weight1 = portfolioHelper.calculateBondWeight(bond1, portfolio);
            double weight2 = portfolioHelper.calculateBondWeight(bond2, portfolio);
            double weight3 = portfolioHelper.calculateBondWeight(bond3, portfolio);
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: Manual calculation should match
            double manualMacaulay = bond1.getMacaulayDuration() * weight1 +
                    bond2.getMacaulayDuration() * weight2 +
                    bond3.getMacaulayDuration() * weight3;

            logger.info("Integration test: total={}, weights=[{},{},{}], duration={}",
                    totalValue, weight1, weight2, weight3, macaulayDuration);

            assertEquals(6000000L, totalValue, "Total value should match");
            assertEquals(1.0, weight1 + weight2 + weight3, 0.001, "Weights should sum to 1");
            assertEquals(manualMacaulay, macaulayDuration, 0.001,
                    "Weighted duration should match manual calculation with weights");
        }

        @Test
        @DisplayName("Portfolio with varying bond characteristics")
        void testVariedPortfolio() {
            // Given: Realistic portfolio with varied bonds
            Bond treasuryBond = createBond("US912828ZT25", 99500, 100, 9.5, 8.8);   // 10-year Treasury
            Bond corporateBond = createBond("US037833DV95", 102300, 50, 7.2, 6.7);   // Apple corporate
            Bond zeroCouponBond = createBond("US912796XY45", 85000, 200, 5.0, 5.0);  // Zero coupon
            Bond highYieldBond = createBond("US345370CR50", 95000, 75, 4.3, 4.0);    // High yield

            Portfolio portfolio = createPortfolio("ACC001",
                    List.of(treasuryBond, corporateBond, zeroCouponBond, highYieldBond));
            long totalValue = portfolio.getBonds().stream().mapToLong(Bond::getTotalMarketValue).sum();
            portfolio.setTotalPortfolioValue(totalValue);

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double modifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);
            long calcTotalValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then
            logger.info("Varied portfolio: total=${}, Macaulay={}, Modified={}",
                    calcTotalValue / 100, macaulayDuration, modifiedDuration);

            assertTrue(macaulayDuration > 0, "Macaulay duration should be positive");
            assertTrue(modifiedDuration > 0, "Modified duration should be positive");
            assertTrue(modifiedDuration <= macaulayDuration,
                    "Modified duration should be ≤ Macaulay duration");
            assertEquals(totalValue, calcTotalValue, "Calculated total should match");
        }

        @Test
        @DisplayName("Weights and durations should produce correct portfolio sensitivity")
        void testPortfolioSensitivity() {
            // Given: Portfolio optimized for specific duration target
            Bond shortDuration = createBond("US0378331005", 100000, 70, 2.0, 1.9);
            Bond longDuration = createBond("US5949181045", 100000, 30, 8.0, 7.5);
            Portfolio portfolio = createPortfolio("ACC001", List.of(shortDuration, longDuration));
            portfolio.setTotalPortfolioValue(
                    shortDuration.getTotalMarketValue() + longDuration.getTotalMarketValue());

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            // Then: (2.0 × 70% + 8.0 × 30%) = 1.4 + 2.4 = 3.8
            double expectedDuration = 2.0 * 0.7 + 8.0 * 0.3;
            logger.info("Portfolio sensitivity: weighted duration={}, expected={}", macaulayDuration, expectedDuration);
            assertEquals(expectedDuration, macaulayDuration, 0.001,
                    "Portfolio duration should reflect weighted combination");
        }

        @Test
        @DisplayName("Should correctly handle real-world like portfolio scenario")
        void testRealWorldScenario() {
            // Given: Realistic pension fund portfolio
            List<Bond> bonds = new ArrayList<>();
            bonds.add(createBond("US912828ZT25", 98750, 500, 8.5, 8.0));    // Treasury 10Y
            bonds.add(createBond("US912828ZT26", 99250, 300, 15.2, 14.0)); // Treasury 30Y
            bonds.add(createBond("US037833DV95", 101500, 200, 6.8, 6.4));  // Corporate AA
            bonds.add(createBond("US345370CR50", 97500, 150, 4.5, 4.2));   // Corporate BBB
            bonds.add(createBond("US912796XY45", 85000, 100, 3.0, 3.0));   // Short-term

            Portfolio portfolio = createPortfolio("PENSION_FUND_001", bonds);
            long totalValue = portfolio.getBonds().stream().mapToLong(Bond::getTotalMarketValue).sum();
            portfolio.setTotalPortfolioValue(totalValue);

            // When
            double macaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double modifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);
            long portfolioValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

            // Then: Verify reasonable values for a pension fund
            logger.info("Real-world portfolio: value=${}, Macaulay={}, Modified={}",
                    portfolioValue / 100, macaulayDuration, modifiedDuration);

            assertTrue(portfolioValue > 0, "Portfolio should have positive value");
            assertTrue(macaulayDuration > 5 && macaulayDuration < 20,
                    "Pension fund duration typically between 5-20 years");
            assertTrue(modifiedDuration < macaulayDuration,
                    "Modified duration should be less than Macaulay");

            // Verify weights sum to 1
            double totalWeight = bonds.stream()
                    .mapToDouble(b -> portfolioHelper.calculateBondWeight(b, portfolio))
                    .sum();
            assertEquals(1.0, totalWeight, 0.001, "All weights should sum to 1.0");
        }
    }

    @Nested
    @DisplayName("Cross Validation Tests")
    class CrossValidationTests {

        @Test
        @DisplayName("Manual weighted average calculation should match helper result")
        void testManualCalculationMatch() {
            // Given
            Bond bond1 = createBond("US0378331005", 100000, 15, 3.5, 3.3);
            Bond bond2 = createBond("US5949181045", 100000, 25, 5.5, 5.2);
            Bond bond3 = createBond("GB0002634946", 100000, 60, 7.5, 7.0);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));

            // When: Calculate using helper
            double helperMacaulay = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
            double helperModified = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);

            // Calculate manually
            long total = bond1.getTotalMarketValue() + bond2.getTotalMarketValue() + bond3.getTotalMarketValue();
            double manualMacaulay = (bond1.getMacaulayDuration() * bond1.getTotalMarketValue() +
                    bond2.getMacaulayDuration() * bond2.getTotalMarketValue() +
                    bond3.getMacaulayDuration() * bond3.getTotalMarketValue()) / total;
            double manualModified = (bond1.getModifiedDuration() * bond1.getTotalMarketValue() +
                    bond2.getModifiedDuration() * bond2.getTotalMarketValue() +
                    bond3.getModifiedDuration() * bond3.getTotalMarketValue()) / total;

            // Then
            logger.info("Cross validation: helper Macaulay={}, manual={}", helperMacaulay, manualMacaulay);
            logger.info("Cross validation: helper Modified={}, manual={}", helperModified, manualModified);

            assertEquals(manualMacaulay, helperMacaulay, 0.0001,
                    "Helper Macaulay should match manual calculation");
            assertEquals(manualModified, helperModified, 0.0001,
                    "Helper Modified should match manual calculation");
        }

        @Test
        @DisplayName("Sum of weighted bond durations should equal portfolio duration")
        void testWeightedSumEquality() {
            // Given
            Bond bond1 = createBond("US0378331005", 100000, 20, 4.0, 3.8);
            Bond bond2 = createBond("US5949181045", 100000, 30, 6.0, 5.6);
            Bond bond3 = createBond("GB0002634946", 100000, 50, 8.0, 7.4);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));
            portfolio.setTotalPortfolioValue(
                    bond1.getTotalMarketValue() + bond2.getTotalMarketValue() + bond3.getTotalMarketValue());

            // When
            double portfolioDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);

            double weight1 = portfolioHelper.calculateBondWeight(bond1, portfolio);
            double weight2 = portfolioHelper.calculateBondWeight(bond2, portfolio);
            double weight3 = portfolioHelper.calculateBondWeight(bond3, portfolio);

            double sumOfWeightedDurations = weight1 * bond1.getMacaulayDuration() +
                    weight2 * bond2.getMacaulayDuration() +
                    weight3 * bond3.getMacaulayDuration();

            // Then
            logger.info("Portfolio duration: {}, Sum of weighted: {}", portfolioDuration, sumOfWeightedDurations);
            assertEquals(portfolioDuration, sumOfWeightedDurations, 0.0001,
                    "Sum of weighted durations should equal portfolio duration");
        }

        @Test
        @DisplayName("Total portfolio value should equal sum of individual bond values")
        void testTotalValueEquality() {
            // Given
            Bond bond1 = createBond("US0378331005", 98500, 45, 4.0, 3.8);
            Bond bond2 = createBond("US5949181045", 101200, 67, 6.0, 5.6);
            Bond bond3 = createBond("GB0002634946", 99800, 33, 8.0, 7.4);
            Portfolio portfolio = createPortfolio("ACC001", List.of(bond1, bond2, bond3));

            // When
            long calculatedTotal = portfolioHelper.calculateTotalPortfolioValue(portfolio);
            long manualTotal = bond1.getTotalMarketValue() + bond2.getTotalMarketValue() + bond3.getTotalMarketValue();

            // Then
            logger.info("Calculated total: {}, Manual total: {}", calculatedTotal, manualTotal);
            assertEquals(manualTotal, calculatedTotal,
                    "Calculated total should match sum of individual values");
        }
    }
}

