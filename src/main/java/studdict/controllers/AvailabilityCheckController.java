package studdict.controllers;

import studdict.core.Table;

public class AvailabilityCheckController {
    public boolean checkAvailability(Table table, int newGroupSize, String newTime) {
        boolean isAvailable = table.checkAvailability(newGroupSize, newTime);

        if (isAvailable) {
            return true;
        } else {
            return false;
        }
    }
}
