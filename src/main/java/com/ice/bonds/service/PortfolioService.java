package com.ice.bonds.service;

import com.ice.bonds.dto.BondDTORequest;
import com.ice.bonds.helper.PortfolioHelper;
import com.ice.bonds.model.Bond;
import com.ice.bonds.model.Portfolio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private final BondService bondService;
    private final PortfolioHelper portfolioHelper;

    public PortfolioService(BondService bondService, PortfolioHelper portfolioHelper) {
        this.bondService = bondService;
        this.portfolioHelper = portfolioHelper;
    }

    public Portfolio addBondToPortfolio(Portfolio portfolio, BondDTORequest bondDTORequest){
        Bond bond = bondService.analyzeBond(bondDTORequest, java.time.LocalDate.now());
        portfolio.addBond(bond);
        analyzePortfolio(portfolio);
        bond.setBondWeightInPortfolio(portfolioHelper.calculateBondWeight(bond, portfolio));
        return portfolio;

    }

    public Portfolio addBondsToPortfolio(Portfolio portfolio, List<BondDTORequest> bondDTORequests){
        for(BondDTORequest bondDTORequest : bondDTORequests){
            Bond bond = bondService.analyzeBond(bondDTORequest, java.time.LocalDate.now());
            portfolio.addBond(bond);
        }
        analyzePortfolio(portfolio);
        for(Bond bond : portfolio.getBonds()){
            bond.setBondWeightInPortfolio(portfolioHelper.calculateBondWeight(bond, portfolio));
        }
        return portfolio;
    }


    /**
     * Analyze the portfolio to calculate weighted durations and total value.
     * @param portfolio The portfolio to analyze
     * MUTATES the portfolio object by setting calculated values.
     */
    public void analyzePortfolio(Portfolio portfolio){
        double weightedMacaulayDuration = portfolioHelper.calculateWeightedAverageMacaulayDuration(portfolio);
        double weightedModifiedDuration = portfolioHelper.calculateWeightedAverageModifiedDuration(portfolio);
        long totalPortfolioValue = portfolioHelper.calculateTotalPortfolioValue(portfolio);

        // Here you can set these values to the portfolio if needed
         portfolio.setWeightedMacaulayDuration(weightedMacaulayDuration);
         portfolio.setWeightedModifiedDuration(weightedModifiedDuration);
         portfolio.setTotalPortfolioValue(totalPortfolioValue);
    }


}
