package com.ice.bonds.dto;

public class BondDTORequest {


    // Bond Details
    private String isin;

    // Maturity date in ISO format (YYYY-MM-DD)
    private String maturityDate;

    // Issue date in ISO format (YYYY-MM-DD)
    private String issueDate;

    // Coupon rate in basis points (e.g., "500" = 5.00%, "650" = 6.50%)
    // Must be a whole number string without decimals or commas
    private String couponRate;

    // Face value in cents - must be a whole number string without decimals or commas
    private String faceValue;

    // Market value in cents - must be a whole number string without decimals or commas
    private String marketValue;

    // Payment term (e.g., Semiannual, Annual, Quarterly, Monthly)
    private String paymentTerm;

    // Quantity of bonds held - must be a whole number string without decimals or commas
    private String quantity;

    public String getIsin() {
        return isin;
    }
    public void setIsin(String isin) {
        this.isin = isin;
    }
    public String getMaturityDate() {
        return maturityDate;
    }
    public void setMaturityDate(String maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getCouponRate() {
        return couponRate;
    }
    public void setCouponRate(String couponRate) {
        this.couponRate = couponRate;
    }

    public String getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(String faceValue) {
        this.faceValue = faceValue;
    }

    public String getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(String paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
