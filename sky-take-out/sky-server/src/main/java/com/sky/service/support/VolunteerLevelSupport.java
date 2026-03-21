package com.sky.service.support;

public final class VolunteerLevelSupport {

    private VolunteerLevelSupport() {
    }

    public static int calculateLevel(Integer totalOrders) {
        int normalizedOrders = totalOrders == null ? 0 : Math.max(totalOrders, 0);
        if (normalizedOrders >= 100) {
            return 5;
        }
        if (normalizedOrders >= 60) {
            return 4;
        }
        if (normalizedOrders >= 30) {
            return 3;
        }
        if (normalizedOrders >= 10) {
            return 2;
        }
        return 1;
    }
}
