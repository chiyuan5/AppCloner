package android.app.ifma.mts.binderceptor;

public class BinderceptorManager {

    public static final int EBinderceptorDemoFlag_Print_Simple = 0x1;
    public static final int EBinderceptorDemoFlag_Print_Transaction_Data = 0x2;
    public static final int EBinderceptorDemoFlag_Print_Write_Read = 0x4;

    private static boolean initialized = false;

    public static void init() {
        initialized = true;
    }

    public static void setLogger(int flag) {
    }

    public static void registerCallback(IBinderceptorCallback callback) {
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
