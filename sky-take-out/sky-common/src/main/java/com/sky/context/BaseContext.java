package com.sky.context;

public class BaseContext {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_DINING_POINT_ID = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void setCurrentRole(String role) {
        CURRENT_ROLE.set(role);
    }

    public static String getCurrentRole() {
        return CURRENT_ROLE.get();
    }

    public static void setCurrentDiningPointId(Long diningPointId) {
        CURRENT_DINING_POINT_ID.set(diningPointId);
    }

    public static Long getCurrentDiningPointId() {
        return CURRENT_DINING_POINT_ID.get();
    }

    public static void removeCurrentId() {
        CURRENT_ID.remove();
    }

    public static void clear() {
        CURRENT_ID.remove();
        CURRENT_ROLE.remove();
        CURRENT_DINING_POINT_ID.remove();
    }
}
