package s25601.pjwstk.personalfinanceassistant.util;

import s25601.pjwstk.personalfinanceassistant.model.BudgetPeriod;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

public class BudgetPeriodUtil {

    public static LocalDate[] getPeriodStartEnd(BudgetPeriod period, LocalDate referenceDate) {
        LocalDate start;
        LocalDate end;

        switch (period) {
            case DAILY:
                start = referenceDate;
                end = referenceDate;
                break;
            case WEEKLY:
                start = referenceDate.with(DayOfWeek.MONDAY);
                end = referenceDate.with(DayOfWeek.SUNDAY);
                break;
            case MONTHLY:
                start = referenceDate.with(TemporalAdjusters.firstDayOfMonth());
                end = referenceDate.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case YEARLY:
                start = referenceDate.with(TemporalAdjusters.firstDayOfYear());
                end = referenceDate.with(TemporalAdjusters.lastDayOfYear());
                break;
            default:
                start = referenceDate.with(TemporalAdjusters.firstDayOfMonth());
                end = referenceDate.with(TemporalAdjusters.lastDayOfMonth());
        }

        return new LocalDate[]{start, end};
    }

    public static boolean isInBudgetPeriod(LocalDate transactionDate, BudgetPeriod period) {
        LocalDate today = LocalDate.now();
        LocalDate[] periodRange = getPeriodStartEnd(period, today);
        LocalDate start = periodRange[0];
        LocalDate end = periodRange[1];
        return !transactionDate.isBefore(start) && !transactionDate.isAfter(end);
    }
}