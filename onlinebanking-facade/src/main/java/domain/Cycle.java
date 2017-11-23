package domain;

/**
 * The predicted cycles for a category.
 */
public enum Cycle {
    /**
     * every week
     */
    WEEKLY,
    /**
     * every month.
     */
    MONTHLY,
    /**
     * every 2nd month.
     */
    TWO_MONTHLY,
    /**
     * every 3 months.
     */
    QUARTERLY,
    /**
     * every 6 months.
     */
    HALF_YEARLY,
    /**
     * every 12 months.
     */
    YEARLY,
    INVALID
}
