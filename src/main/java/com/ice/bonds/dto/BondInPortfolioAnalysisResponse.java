package com.ice.bonds.dto;

import java.time.LocalDate;

public class BondInPortfolioAnalysisResponse {

    // Fields from BondAnalysisResponse
    private String isin;
    private double ytm;
    private double macaulayDuration;
    private double modifiedDuration;
    private LocalDate maturityDate;
    private LocalDate issueDate;
    private int couponRate;
    private int faceValue;
    private int marketValue;
    private String paymentTerm;

    // Additional fields for portfolio context
    private int quantity;
    private double bondWeightInPortfolio;

    public BondInPortfolioAnalysisResponse() {
    }

    public BondInPortfolioAnalysisResponse(String isin, double ytm, double macaulayDuration, double modifiedDuration,
                                           LocalDate maturityDate, LocalDate issueDate, int couponRate,
                                           int faceValue, int marketValue, String paymentTerm,
                                           int quantity, double bondWeightInPortfolio) {
        this.isin = isin;
        this.ytm = ytm;
        this.macaulayDuration = macaulayDuration;
        this.modifiedDuration = modifiedDuration;
        this.maturityDate = maturityDate;
        this.issueDate = issueDate;
        this.couponRate = couponRate;
        this.faceValue = faceValue;
        this.marketValue = marketValue;
        this.paymentTerm = paymentTerm;
        this.quantity = quantity;
        this.bondWeightInPortfolio = bondWeightInPortfolio;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public double getYtm() {
        return ytm;
    }

    public void setYtm(double ytm) {
        this.ytm = ytm;
    }

    public double getMacaulayDuration() {
        return macaulayDuration;
    }

    public void setMacaulayDuration(double macaulayDuration) {
        this.macaulayDuration = macaulayDuration;
    }

    public double getModifiedDuration() {
        return modifiedDuration;
    }

    public void setModifiedDuration(double modifiedDuration) {
        this.modifiedDuration = modifiedDuration;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public int getCouponRate() {
        return couponRate;
    }

    public void setCouponRate(int couponRate) {
        this.couponRate = couponRate;
    }

    public int getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(int faceValue) {
        this.faceValue = faceValue;
    }

    public int getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(int marketValue) {
        this.marketValue = marketValue;
    }

    public String getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(String paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getBondWeightInPortfolio() {
        return bondWeightInPortfolio;
    }

    public void setBondWeightInPortfolio(double bondWeightInPortfolio) {
        this.bondWeightInPortfolio = bondWeightInPortfolio;
    }
}

