package com.ice.bonds.helper;

import com.ice.bonds.model.Bond;
import com.ice.bonds.model.Portfolio;
import org.springframework.stereotype.Component;

@Component
public class PortfolioHelper {


    public PortfolioHelper(){
    }

    /**
     * Calculate portfolio weighted average Macaulay duration
     * Formula: Σ(Duration_i × MarketValue_i) / Σ(MarketValue_i)
     */
    public double calculateWeightedAverageMacaulayDuration(Portfolio portfolio) {
        long totalMarketValue = 0;
        double weightedDurationSum = 0.0;

        for (Bond bond : portfolio.getBonds()) {
            long bondMarketValue = bond.getTotalMarketValue();
            double macaulayDuration = bond.getMacaulayDuration();

            weightedDurationSum += macaulayDuration * bondMarketValue;
            totalMarketValue += bondMarketValue;
        }

        if (totalMarketValue == 0) {
            return 0.0;
        }

        return weightedDurationSum / totalMarketValue;
    }

    /**
     * Calculate portfolio weighted average Modified duration
     * Formula: Σ(ModDuration_i × MarketValue_i) / Σ(MarketValue_i)
     */
    public double calculateWeightedAverageModifiedDuration(Portfolio portfolio) {
        long totalMarketValue = 0;
        double weightedDurationSum = 0.0;

        for (Bond bond : portfolio.getBonds()) {
            long bondMarketValue = bond.getTotalMarketValue();
            double modifiedDuration = bond.getModifiedDuration();

            weightedDurationSum += modifiedDuration * bondMarketValue;
            totalMarketValue += bondMarketValue;
        }

        if (totalMarketValue == 0) {
            return 0.0;
        }

        return weightedDurationSum / totalMarketValue;
    }

    /**
     * Get total portfolio market value in cents
     */
    public long calculateTotalPortfolioValue(Portfolio portfolio) {
        return portfolio.getBonds().stream()
                .mapToLong(Bond::getTotalMarketValue)
                .sum();
    }

    /**
     * Calculate individual bond's weight in portfolio
     */
    public double calculateBondWeight(Bond bond, Portfolio portfolio) {
        long totalValue = portfolio.getTotalPortfolioValue();
        long totalBondValue = bond.getTotalMarketValue();
        if (totalValue == 0 || totalBondValue == 0) {
            return 0.0;
        }
        return (double) totalBondValue / totalValue;
    }

}
