package com.ice.bonds.helper;

import com.ice.bonds.model.Bond;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class YTMHelper {

    private final CommonHelper commonHelper;

    public YTMHelper(CommonHelper commonHelper) {

        this.commonHelper = commonHelper;
    }

    //Face Value = $1000 or 1000 * 100 = 100000 cents
    //Annual Coupon Rate = 6.0% = 600 bps
    //Number of Years to Maturity = 10 = 20 semiannual periods
    //Price of a Bond (Present Value) = $1050 = 1050 * 100 = 105000 cents
    //Payment Term = Semiannual = 2

    /**
    * APPROXIMATES the Yield to Maturity (YTM) on a bond using the simplified formula.
    *
    * NOTE: This is an approximation. The exact YTM requires iterative numerical methods
    * to solve the bond pricing equation.
    *
    * YTM (per period) = [C + ((FV - PV) / N)] / [(FV + PV) / 2]
    *
    * For annualized YTM, multiply result by Payment Term (e.g., 2 for semiannual).
    *
    * C = couponRate Annual coupon rate in basis points (e.g., 600 for 6%)
    * FV = faceValue Face value in cents (e.g., 100000 for $1000)
    * presentValue **CLEAN PRICE** in cents (market price MINUS accrued interest)
    *    If you have the dirty price (invoice price), subtract accrued
    *    interest before passing to this method.
    * N = REMAINING periods until maturity (not original term)
    *
    * @param currentDate The current/settlement date
    * @param bond The bond object containing couponRate, faceValue, marketValue, issueDate, maturityDate, and paymentTerm
    * @return Approximate  annualized YTM in basis points (e.g., 500 for 5%)
     * //TODO: return both annualized and per period YTM?
     * //TODO : ENUM for paymentTerm
     * //TODO : Day Count Convention Option, ACT/ACT default, 30/360 for Corporate/Municipal/Agency bonds, see findNextPaymentDate, MacCaulay Duration,  and calculateRemainingPeriods for details
    */
    public double calculateYTM(LocalDate currentDate, Bond bond) {

        int couponRate = bond.getCouponRate();
        int faceValue = bond.getFaceValue();
        int marketValue = bond.getMarketValue();
        LocalDate issueDate = bond.getIssueDate();
        LocalDate maturationDate = bond.getMaturityDate();
        String paymentTerm = bond.getPaymentTerm();

        // find periods for payment term
        int periodsPerPaymentTerm = commonHelper.periodsPerPaymentTerm(paymentTerm);

        // Calculate coupon payment per period
        double couponPayment = (couponRate / 100.0) * faceValue / periodsPerPaymentTerm;

//        // Calculate accrued interest
//        double accruedInterest = calculateAccruedInterest(couponPayment, periodsPerPaymentTerm, issueDate, currentDate);

        // Calculate remaining periods to maturity
        int n = commonHelper.calculateRemainingPeriods(currentDate, issueDate, maturationDate, periodsPerPaymentTerm);

        // Calculate fractional period for more precision
        double fractionalPeriod = commonHelper.calculateFractionalPeriod(issueDate, currentDate, periodsPerPaymentTerm);

        // Adjust N by fractional period, to account for time elapsed in current period
        double adjustedN = n - fractionalPeriod;

        if (adjustedN < 0) {
            throw new IllegalArgumentException("Bond too close to maturity for YTM calculation");
        }

        // Special case: bond matures today, throw exception as YTM is not meaningful
        // could skew results when aggregating portfolio YTM
        if (adjustedN == 0) {
            throw new IllegalArgumentException(
                    "Bond matures today - YTM is not meaningful for portfolio calculations. " +
                            "Use direct price comparison (receives: " + (faceValue + couponPayment) +
                            " cents, pays: " + marketValue + " cents)");
        }

        // After calculating adjustedN
        if (Math.abs(adjustedN - n) > 1.0) {
            throw new IllegalStateException(
                    "Fractional period adjustment too large. " +
                            "Remaining periods: " + n + ", Fractional: " + fractionalPeriod);
        }


        // Calculate present value (clean price), subtracting accrued interest
        //MARKET VALUE IS THE CLEAN PRICE and ACCRUED INTEREST IS ADDED TO GET THE DIRTY PRICE
        // PRESENT VALUE = CLEAN PRICE = MARKET VALUE


        // YTM formula: [C + (FV - PV) / N] / [(FV + PV) / 2]
        double ytmPerPeriod = (couponPayment + ((faceValue - marketValue) / adjustedN))
                / ((faceValue + marketValue) / 2.0);

        // Annualize by multiplying by periods per year
        return ytmPerPeriod * periodsPerPaymentTerm * 100; // Convert to basis points
    }


    /**
     * Calculates accrued interest since the last coupon payment.
     *
     * @param couponPayment Coupon payment amount in cents
     * @param periodsPerPaymentTerm Payment frequency (1, 2, 4, 12)
     * @param issueDate Date of bond issue date
     * @param settlementDate Date of bond purchase/valuation (typically current date)
     * @return Accrued interest in cents
     * NOT NEEDED ASSUMING MARKET VALUE IS CLEAN PRICE
     */
    private double calculateAccruedInterest(double couponPayment,
                                            int periodsPerPaymentTerm,
                                            LocalDate issueDate,
                                            LocalDate settlementDate) {

        // Find the next payment date after settlement
        LocalDate nextPaymentDate = commonHelper.findNextPaymentDate(issueDate, settlementDate, periodsPerPaymentTerm);

        // Calculate the previous payment date by going back one period
        int monthsPerPeriod = 12 / periodsPerPaymentTerm;
        LocalDate lastPaymentDate = nextPaymentDate.minusMonths(monthsPerPeriod);

        // Days in the current coupon period
        long daysInPeriod = ChronoUnit.DAYS.between(lastPaymentDate, nextPaymentDate);

        // Days accrued since last payment
        long daysAccrued = ChronoUnit.DAYS.between(lastPaymentDate, settlementDate);

        return couponPayment * (daysAccrued / (double) daysInPeriod);
    }



}
