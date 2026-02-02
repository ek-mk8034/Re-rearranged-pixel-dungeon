package com.shatteredpixel.shatteredpixeldungeon.utils;

public final class MyeongProcContext {

    private static final ThreadLocal<Boolean> FORCE = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private MyeongProcContext() {}

    public static boolean forceProc() {
        return Boolean.TRUE.equals(FORCE.get());
    }

    public static void setForceProc(boolean on) {
        FORCE.set(on);
    }
}
