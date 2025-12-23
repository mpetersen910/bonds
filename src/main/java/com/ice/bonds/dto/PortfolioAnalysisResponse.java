package com.ice.bonds.dto;

import java.util.List;
import java.util.UUID;

public class PortfolioAnalysisResponse {

    private UUID id;
    private String accountId;
    private List<BondInPortfolioAnalysisResponse> bonds;
    private double weightedMacaulayDuration;
    private double weightedModifiedDuration;
    private long totalPortfolioValue;

    public PortfolioAnalysisResponse(UUID id, String accountId, List<BondInPortfolioAnalysisResponse> bonds,
                                     double weightedMacaulayDuration, double weightedModifiedDuration,
                                     long totalPortfolioValue) {
        this.id = id;
        this.accountId = accountId;
        this.bonds = bonds;
        this.weightedMacaulayDuration = weightedMacaulayDuration;
        this.weightedModifiedDuration = weightedModifiedDuration;
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public List<BondInPortfolioAnalysisResponse> getBonds() {
        return bonds;
    }

    public void setBonds(List<BondInPortfolioAnalysisResponse> bonds) {
        this.bonds = bonds;
    }

    public double getWeightedMacaulayDuration() {
        return weightedMacaulayDuration;
    }

    public void setWeightedMacaulayDuration(double weightedMacaulayDuration) {
        this.weightedMacaulayDuration = weightedMacaulayDuration;
    }

    public double getWeightedModifiedDuration() {
        return weightedModifiedDuration;
    }

    public void setWeightedModifiedDuration(double weightedModifiedDuration) {
        this.weightedModifiedDuration = weightedModifiedDuration;
    }

    public long getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void setTotalPortfolioValue(long totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }
}

