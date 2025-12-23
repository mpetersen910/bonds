package com.ice.bonds.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Portfolio {
    private UUID id;
    private String accountId;
    private List<Bond> bonds;
    private double weightedMacaulayDuration;
    private double weightedModifiedDuration;
    private long totalPortfolioValue;

    public Portfolio(String accountId) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.bonds = new ArrayList<>();
        this.weightedMacaulayDuration = 0.0;
        this.weightedModifiedDuration = 0.0;
        this.totalPortfolioValue = 0;
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

    public List<Bond> getBonds() {
        return bonds;
    }

    public void setBonds(List<Bond> bonds) {
        this.bonds = bonds;
    }

    public void addBond(Bond bond) {
        this.bonds.add(bond);
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