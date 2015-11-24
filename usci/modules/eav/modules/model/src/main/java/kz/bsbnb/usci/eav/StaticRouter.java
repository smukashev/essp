package kz.bsbnb.usci.eav;

public final class StaticRouter {
    private enum MODE {
        DEV,
        PROD
    }

    private static final MODE mode = MODE.PROD;

    private final static String devAsIP = "localhost";
    private final static String prodAsIP = "10.8.1.116";

    private final static String devDBCoreIP = "localhost";
    private final static String prodDBCoreIp = "10.8.1.200";

    private final static String devDBShowcaseIP = "localhost";
    private final static String prodDBShowcaseIP = "10.8.1.85";

    private final static String devPortalUrl = "localhost";
    private final static String prodPortalUrl = "192.168.20.50";

    private final static String devPortalPort = "80";
    private final static String prodPortalPort = "80";

    public static String getAsIP() {
        switch(mode) {
            case DEV:
                return devAsIP;
            case PROD:
                return prodAsIP;
            default:
                throw new IllegalStateException("Неизвестный мод;");
        }
    }

    public static String getDBCoreIP() {
        switch(mode) {
            case DEV:
                return devDBCoreIP;
            case PROD:
                return prodDBCoreIp;
            default:
                throw new IllegalStateException("Неизвестный мод;");
        }
    }

    public static String getDBShowcaseIP() {
        switch(mode) {
            case DEV:
                return devDBShowcaseIP;
            case PROD:
                return prodDBShowcaseIP;
            default:
                throw new IllegalStateException("Неизвестный мод;");
        }
    }

    public static String getPortalUrl() {
        switch(mode) {
            case DEV:
                return devPortalUrl;
            case PROD:
                return prodPortalUrl;
            default:
                throw new IllegalStateException("Неизвестный мод;");
        }
    }

    public static String getPortalPort() {
        switch(mode) {
            case DEV:
                return devPortalPort;
            case PROD:
                return prodPortalPort;
            default:
                throw new IllegalStateException("Неизвестный мод;");
        }
    }
}
