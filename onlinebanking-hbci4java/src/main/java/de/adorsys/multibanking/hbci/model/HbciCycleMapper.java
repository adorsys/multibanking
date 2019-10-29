package de.adorsys.multibanking.hbci.model;

import de.adorsys.multibanking.domain.Cycle;

public interface HbciCycleMapper {
    default String cycleToTurnus(Cycle cycle) {
        switch (cycle) {
            case WEEKLY:
                return "1";
            case TWO_WEEKLY:
                return "2";
            case MONTHLY:
                return "1";
            case TWO_MONTHLY:
                return "2";
            case QUARTERLY:
                return "3";
            case HALF_YEARLY:
                return "6";
            case YEARLY:
                return "12";
            case INVALID:
                return null;
            default:
                return null;
        }
    }

    default String cycleToTimeunit(Cycle cycle) {
        switch (cycle) {
            case WEEKLY:
            case TWO_WEEKLY:
                return "W";
            default:
                return "M";
        }
    }
}
