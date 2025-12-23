package com.ice.bonds.helper;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CommonHelper {
    /**
     * Calculates the number of remaining payment periods for a bond.
     * Uses day-based calculation for precision.
     *
     * @param issueDate Date when the bond was issued
     * @param maturationDate Date when the bond matures
     * @param periodsPerPaymentTerm Number of payment periods per year (1=annual, 2=semiannual, 4=quarterly, 12=monthly)
     * @return Counts only Future Payment Periods
     *
     * IMPORTANT NOTE, findNextPaymentDate, calculateRemainingPeriods, and calculateFractionalPeriod, and generateCashFlows
     * must all use the same logic regarding whether to include currentDate as a payment date
     *
     * if currentDate is a payment date,
     * it is included. This is assuming the bond is trading on that date before payment.
     * (payment typically executed at the end of the day, so if the bond changes owners before that,
     * the buyer would be entitled to that payment) -- NEEDS REVIEW
     */
    public int calculateRemainingPeriods(LocalDate currentDate, LocalDate issueDate, LocalDate maturationDate, int periodsPerPaymentTerm) {

        if (maturationDate.isBefore(currentDate)) {
            throw new IllegalArgumentException("Bond Matured. Maturation date must be in the future");
        }

        int monthsPerPeriod = 12 / periodsPerPaymentTerm;
        int remainingPeriods = 0;

        // Use findNextPaymentDate to get the first payment on or after currentDate
        LocalDate paymentDate = findNextPaymentDate(issueDate, currentDate, periodsPerPaymentTerm);

        // Count all payment dates from there up to and including maturity
        while (!paymentDate.isAfter(maturationDate)) {
            remainingPeriods++;
            paymentDate = paymentDate.plusMonths(monthsPerPeriod);
        }

        return remainingPeriods;
    }

    /**
     * Calculates the fractional period representing how far we are into the current period.
     * This is useful for more precise YTM calculations when between payment dates.
     *
     * IMPORTANT NOTE, findNextPaymentDate, calculateRemainingPeriods, and calculateFractionalPeriod, and generateCashFlows
     * must all use the same logic regarding whether to include currentDate as a payment date
     *
     * @param issueDate Date when the bond was issued
     * @param currentDate Current date (typically LocalDate.now())
     * @param periodsPerPaymentTerm Number of payment periods per year
     * @return Fraction of the current period elapsed (0.0 to 1.0)
     */
    public double calculateFractionalPeriod(LocalDate issueDate, LocalDate currentDate, int periodsPerPaymentTerm) {
        int monthsPerPeriod = 12 / periodsPerPaymentTerm;

        LocalDate nextPaymentDate = findNextPaymentDate(issueDate, currentDate, periodsPerPaymentTerm);


        // Previous payment is one period before the next payment
        LocalDate lastPaymentDate = nextPaymentDate.minusMonths(monthsPerPeriod);

        long daysSinceLastPayment = java.time.temporal.ChronoUnit.DAYS.between(lastPaymentDate, currentDate);
        long daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(lastPaymentDate, nextPaymentDate);

        return (double) daysSinceLastPayment / daysInPeriod;
    }

    /**
     * Finds the next payment date on or after the given current date.
     * If currentDate is a payment date, it returns currentDate (consistent with calculateRemainingPeriods
     * which includes same-day payments, assuming the bond trades before payment execution).
     *
     * IMPORTANT NOTE, findNextPaymentDate, calculateRemainingPeriods, and calculateFractionalPeriod, and generateCashFlows
     * must all use the same logic regarding whether to include currentDate as a payment date
     *
     * @param issueDate Date when the bond was issued
     * @param currentDate Current date to find the next payment on or after
     * @param periodsPerYear Number of payment periods per year (1=annual, 2=semiannual, 4=quarterly, 12=monthly)
     * @return The next payment date on or after currentDate
     * Currently supports ACT/ACT day count convention, TODO support needed for other day count conventions, see Macaulay Duration and YTM calculations
     */
    public LocalDate findNextPaymentDate(LocalDate issueDate, LocalDate currentDate, int periodsPerYear) {
        int monthsPerPeriod = 12 / periodsPerYear;

        LocalDate nextPaymentDate = issueDate.plusMonths(monthsPerPeriod);

        // Keep advancing payment dates until we find one on or after currentDate
        while (nextPaymentDate.isBefore(currentDate)) {
            nextPaymentDate = nextPaymentDate.plusMonths(monthsPerPeriod);
        }

        return nextPaymentDate;
    }

    //TODO: Use ENUM for paymentTerm
    public int periodsPerPaymentTerm(String paymentTerm){
        return switch (paymentTerm.toLowerCase()) {
            case "annual" -> 1;
            case "semiannual" -> 2;
            case "quarterly" -> 4;
            case "monthly" -> 12;
            default -> throw new IllegalArgumentException("Invalid payment term: " + paymentTerm);
        };
    }

    /**
     * Advances a date by one payment period based on the day count convention.
     *
     * @param date The starting date
     * @param periodsPerYear Number of payment periods per year
     * @param dayCountConvention The day count convention (e.g., "ACT/ACT", "30/360")
     * @return The next payment date
     * TODO : Implement different day count conventions to adjust date advancement rather than plusMonths
     * TODO : Use ENUM for dayCountConvention
     * TODO : Need reverseByPeriod function as well
     */
//    public LocalDate advanceByPeriod(LocalDate date, int periodsPerYear, String dayCountConvention) {
//        int monthsPerPeriod = 12 / periodsPerYear;
//
//        return switch (dayCountConvention.toUpperCase()) {
//            case "30/360", "30E/360" -> {
//                // 30/360 assumes each month has 30 days
//                // Payment dates still advance by calendar months
//                yield date.plusMonths(monthsPerPeriod);
//            }
//            case "ACT/ACT", "ACT/365", "ACT/360" -> {
//                // Actual calendar-based advancement
//                yield date.plusMonths(monthsPerPeriod);
//            }
//            default -> throw new IllegalArgumentException("Unsupported day count convention: " + dayCountConvention);
//        };
//    }
}
