package com.ice.bonds.model;

import java.time.LocalDate;

//TODO: How would this model or a future interface look if we wanted to support other bond types (e.g., Zero-Coupon, Floating Rate, etc.)
public class Bond {

    //International Securities Identification Number
    private String ISIN;

    //Maturity date of the bond
    private LocalDate maturityDate;

    //Issue Date of the bond
    //NOTE: Coupon Dates changed to Issue Date as that seems like a more realistic field to have available
    private LocalDate issueDate;

    //Coupon rate as a percentage in basis points(e.g., 500bps is 5%)
    //An increase of 50bps = 500 + 50 = 550bps / 100 = 5.5%
    private int couponRate;

    //Face value of bond in cents
    private int faceValue;

    //Market value of bond in cents (CLEAN price)
    private int marketValue;

    //Payment term (e.g., Semiannual, Annual, Quarterly, Monthly)
    private String paymentTerm;

    // Quantity of bonds held
    private int quantity;

    // Calculated fields
    private double yieldToMaturity;

    // Macaulay Duration
    private double macaulayDuration;

    // Modified Duration
    private double modifiedDuration;

    // Weight of this bond in the portfolio
    private double bondWeightInPortfolio;

    public Bond() {
    }

    // Constructor without quantity, defaults quantity to 0
    public Bond(String ISIN, LocalDate maturityDate, LocalDate issueDate, int couponRate, int faceValue, int marketValue, String paymentTerm) {
        this.ISIN = ISIN;
        this.maturityDate = maturityDate;
        this.issueDate = issueDate;
        this.couponRate = couponRate;
        this.faceValue = faceValue;
        this.marketValue = marketValue;
        this.paymentTerm = paymentTerm;
        this.quantity = 0; // default to 0 when not specified (does not affect bond calculations, needed for portfolio calculations)
        this.yieldToMaturity = 0.0; // default to 0.0 until calculated
        this.macaulayDuration = 0.0; // default to 0.0 until calculated
        this.modifiedDuration = 0.0; // default to 0.0 until calculated
        this.bondWeightInPortfolio = 0.0; // default to 0.0 when quantity is not specified


    }

    public Bond(String ISIN, LocalDate maturityDate, LocalDate issueDate, int couponRate, int faceValue, int marketValue, String paymentTerm, int quantity) {
        this.ISIN = ISIN;
        this.maturityDate = maturityDate;
        this.issueDate = issueDate;
        this.couponRate = couponRate;
        this.faceValue = faceValue;
        this.marketValue = marketValue;
        this.paymentTerm = paymentTerm;
        this.quantity = quantity;
        this.yieldToMaturity = 0.0; // default to 0.0 until calculated
        this.macaulayDuration = 0.0; // default to 0.0 until calculated
        this.modifiedDuration = 0.0; // default to 0.0 until calculated
        this.bondWeightInPortfolio = 1.0; // Default to 1.0 for single bond portfolios
    }

    // Helper method to get total market value for this holding
    public long getTotalMarketValue() {
        return (long) marketValue * quantity;
    }

    public String getISIN() {
        return ISIN;
    }

    public void setISIN(String ISIN) {
        this.ISIN = ISIN;
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

    public double getYieldToMaturity() {
        return yieldToMaturity;
    }

    public void setYieldToMaturity(double yieldToMaturity) {
        this.yieldToMaturity = yieldToMaturity;
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

    public double getBondWeightInPortfolio() {
        return bondWeightInPortfolio;
    }

    public void setBondWeightInPortfolio(double bondWeightInPortfolio) {
        this.bondWeightInPortfolio = bondWeightInPortfolio;
    }
}
