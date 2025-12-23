package com.ice.bonds.helper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.ice.bonds.model.Bond;
import org.springframework.stereotype.Component;

@Component
public class DurationHelper {

    private final CommonHelper commonHelper;

    public DurationHelper(CommonHelper commonHelper) {
        this.commonHelper = commonHelper;
    }

    /**
     * Calculates the Macaulay Duration of a bond.
     *
     * Macaulay Duration is the weighted average time until cash flows are received,
     * where weights are the present values of cash flows divided by bond price.
     *
     * Formula: D = Σ [t × PV(CFt)] / Price
     * where
     * t = time period in years
     * PV(CFt) = present value of cash flow at time t
     * Price = sum of all present values of cash flows
     *
     *
     * @param bond The bond object
     * @param ytm Annualized yield to maturity in basis points
     * @return Macaulay Duration in years
     * //TODO ytm per period to be calculated from YTMHelper in the future
     * Currently supports ACT/ACT day count convention, TODO support needed for other day count conventions, see Macaulay Duration and YTM calculations
     */
    public double calculateMacaulayDuration(Bond bond, double ytm) {
        LocalDate today = LocalDate.now();

        double ytmDecimal = ytm / 10000.0;

        int couponFrequency = commonHelper.periodsPerPaymentTerm(bond.getPaymentTerm());

        // Generate all cash flow dates and amounts
        List<CashFlow> cashFlows = generateCashFlows(bond, today, couponFrequency);

        // Calculate days per period based on coupon frequency
        double daysPerPeriod = 365.25 / couponFrequency;

        // Calculate present value of each cash flow and weighted time
        double totalPV = 0.0;
        double weightedTime = 0.0;
        double yieldPerPeriod = ytmDecimal / couponFrequency;

        for (CashFlow cf : cashFlows) {
            // Calculate periods from days directly
            double periodsToPayment = cf.daysFromToday / daysPerPeriod;
            double pv = cf.amount / Math.pow(1 + yieldPerPeriod, periodsToPayment);

            totalPV += pv;
            weightedTime += cf.daysFromToday * pv;  // Keep in days
        }

        // Convert final result from days to years
        return (weightedTime / totalPV) / 365.25;
    }

    /**
     * Calculates Modified Duration from Macaulay Duration.
     *
     * Modified Duration = Macaulay Duration / (1 + YTM per period)
     *
     * @param macaulayDuration Macaulay Duration in years
     * @param ytmBasisPoints Yield to maturity in basis points
     * @param paymentTerm Payment frequency
     * @return Modified Duration in years
     * //TODO ytmPerPeriod to be calculated from YTMHelper in the future
     */
    public double calculateModifiedDuration(double macaulayDuration, double ytmBasisPoints,
                                                   String paymentTerm) {
        int periodsPerYear = commonHelper.periodsPerPaymentTerm(paymentTerm);
        double ytmPerPeriod = (ytmBasisPoints / 10000.0) / periodsPerYear;
        return macaulayDuration / (1 + ytmPerPeriod);
    }

    /**
     * Inner class to represent a cash flow
     * Package-private for testing purposes
     */
    static class CashFlow {
        long daysFromToday; // in days
        double amount;  // in cents

        CashFlow(long daysFromToday, double amount) {
            this.daysFromToday = daysFromToday;
            this.amount = amount;
        }
    }


    /**
     * Generates all future cash flows for the bond
     *
     * IMPORTANT NOTE, findNextPaymentDate, calculateRemainingPeriods, and calculateFractionalPeriod, and generateCashFlows
     * must all use the same logic regarding whether to include currentDate as a payment date
     */
    private List<CashFlow> generateCashFlows(Bond bond, LocalDate today, int couponFrequency) {
        List<CashFlow> cashFlows = new ArrayList<>();

        if (bond.getMaturityDate().isBefore(today)) {
            throw new IllegalArgumentException("Bond has already matured");
        }

        // Calculate coupon payment amount (in cents, same as faceValue)
        // couponRate is in basis points, so divide by 10000 to get decimal
        double annualCouponAmount = bond.getFaceValue() * (bond.getCouponRate() / 10000.0);
        double couponPayment = annualCouponAmount / couponFrequency;

        // Determine the payment schedule
        // Assuming payments are made on anniversary of issue date
        LocalDate nextPaymentDate = commonHelper.findNextPaymentDate(bond.getIssueDate(), today, couponFrequency);

        // Generate all coupon payments until maturity
        if(bond.getCouponRate() > 0) {
            // Maturity date is inclusive as final payment date, but issue date is not considered a payment date
            while (!nextPaymentDate.isAfter(bond.getMaturityDate())) {
                long daysFromToday = ChronoUnit.DAYS.between(today, nextPaymentDate);

                if (daysFromToday >= 0) {  // INCLUDE FUTURE PAYMENTS INCLUDING TODAY
                    cashFlows.add(new CashFlow(daysFromToday, couponPayment));
                }

                // Move to next payment date
                nextPaymentDate = nextPaymentDate.plusMonths(12 / couponFrequency);
            }
        }

        if (cashFlows.isEmpty() && bond.getCouponRate() > 0) {
            throw new IllegalStateException("No cash flows generated for the coupon paying bond.");
        }

        // Add principal repayment at maturity
        long daysToMaturity = ChronoUnit.DAYS.between(today, bond.getMaturityDate());

        if(cashFlows.isEmpty()){
            // No coupon payments, only principal repayment
            cashFlows.add(new CashFlow(daysToMaturity, bond.getFaceValue()));
        }
        else if (cashFlows.getLast().daysFromToday == daysToMaturity) {

            // Add principal to the last coupon payment (which should be at or before maturity)
            CashFlow lastFlow = cashFlows.getLast();
            cashFlows.set(cashFlows.size() - 1, //TODO Modification of last element. OK because this is a constrained private method. Future elaboration should reconsider mutability of this list.
                    new CashFlow(lastFlow.daysFromToday, lastFlow.amount + bond.getFaceValue()));
        } else {
            throw new IllegalStateException("Principal Addition to final Cash Flow failed, last cash flow date does not match maturity date");
        }

        return cashFlows;
    }

}
