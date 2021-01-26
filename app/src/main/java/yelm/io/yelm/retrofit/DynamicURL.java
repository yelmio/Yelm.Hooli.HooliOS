package yelm.io.yelm.retrofit;

public class DynamicURL {

    private static String PLATFORM = "platform=";
    private static String PLATFORM_VALUE = "5f771d465f4191.76733056";
    private static String M_API = "m_api.php?";

    public static String getAllPlatform() {
        return PLATFORM + PLATFORM_VALUE;
    }

    public static String getPlatformValue() {
        return PLATFORM_VALUE;
    }

    public static void setPLATFORM(String value) {
        DynamicURL.PLATFORM_VALUE = value;
    }

    public static String getURL(String path) {
        return M_API + PLATFORM + PLATFORM_VALUE + path;
    }

}
