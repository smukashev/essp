package kz.bsbnb.usci.eav;

public final class StaticRouter {
    private enum MODE {
        DEV,
        PROD
    }

    /* Set up before compiling */
    private static final MODE mode = MODE.PROD;

    /* Application Server IP */
    private final static String devAsIP = "localhost";
    private final static String prodAsIP = "10.8.1.116";

    /* Core Schema IP */
    private final static String devDBCoreIP = "localhost";
    private final static String prodDBCoreIp = "10.8.1.200";

    /* Showcase Schema IP */
    private final static String devDBShowcaseIP = "localhost";
    private final static String prodDBShowcaseIP = "10.8.1.85";

    /* Reporter Schema IP */
    private final static String devDBReporterIP = "localhost";
    private final static String prodDBReporterIP = "10.8.1.85";

    /* Portal URL */
    private final static String devPortalUrl = "localhost";
    private final static String prodPortalUrl = "192.168.20.50";

    /* Portal port */
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

    public static String getDBReporterIP() {
        switch(mode) {
            case DEV:
                return devDBReporterIP;
            case PROD:
                return prodDBReporterIP;
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
