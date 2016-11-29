package kz.bsbnb.usci.eav;

import kz.bsbnb.usci.eav.util.Errors;

@SuppressWarnings("all")
public final class StaticRouter {
    private enum MODE {
        STEND,
        DEV,
        PROD
    }

    /* Set up before compiling */
    private static final MODE mode = MODE.PROD;

    /* Application Server IP */
    private final static String stendAsIP = "10.10.32.28";
    private final static String devAsIP = "localhost";
    private final static String prodAsIP = "10.8.1.116";

    /* Core Schema IP */
    private final static String stendDBCoreIP = "170.7.15.69";
    private final static String devDBCoreIP = "localhost";
    private final static String prodDBCoreIp = "10.8.2.200";

    /* Showcase Schema IP */
    private final static String stendDBShowcaseIP = "170.7.15.69";
    private final static String devDBShowcaseIP = "localhost";
    private final static String prodDBShowcaseIP = "10.8.1.206";

    /* Reporter Schema IP */
    private final static String stendDBReporterIP = "170.7.15.69";
    private final static String devDBReporterIP = "localhost";
    private final static String prodDBReporterIP = "10.8.1.206";

    /* Portal URL */
    private final static String stendPortalUrl = "10.10.32.28";
    private final static String devPortalUrl = "localhost";
    private final static String prodPortalUrl = "essp.nationalbank.kz";

    /* Portal port */
    private final static String stendPortalPort = "8080";
    private final static String devPortalPort = "80";
    private final static String prodPortalPort = "80";

    /* Core Schema name */
    private final static String stendCoreSchemaName = "CORE";
    private final static String devCoreSchemaName = "CORE";
    private final static String prodCoreSchemaName = "CORE";

    /* Showcase Schema name */
    private final static String stendShowcaseSchemaName = "SHOWCASE";
    private final static String devShowcaseSchemaName = "SHOWCASE";
    private final static String prodShowcaseSchemaName = "SHOWCASE";

    /* Credit Registry DB IP */
    private final static String stendCRDBIP = "10.8.1.250";
    private final static String devCRDBIP = "10.10.32.44";
    private final static String prodCRDBIP  = "10.8.1.250";

    /* Credit Registry DB Username */
    private final static String stendCRDBUsername = "core";
    private final static String devCRDBUsername = "core";
    private final static String prodCRDBUsername = "core";

    /* Credit Registry DB Password */
    private final static String stendCRDBPassword = "core_sep_2014";
    private final static String devCRDBPassword = "core_sep_2014";
    private final static String prodCRDBPassword = "core_oct_2013";

    /* Report files catalog */
    private final static String stendReportFilesCatalog = "/home/essp/Portal_afn/Report/";
    private final static String devReportFilesCatalog = "/home/essp/Portal_afn/Report/";
    private final static String prodReportFilesCatalog = "C:\\Portal_afn\\Report\\";

    /* Report files folder */
    private final static String stendReportFilesFolder = "/home/essp/Portal_afn/generated_reports/";
    private final static String devReportFilesFolder = "/home/essp/Portal_afn/generated_reports/";
    private final static String prodReportFilesFolder = "C:\\Portal_afn\\generated_reports\\";


    private final static String stendXSDSourceFilePath = "/home/baur/IdeaProjects/usci/usci/modules/receiver/src/main/resources/usci.xsd";//todo: change that path
    private final static String devXSDSourceFilePath = "/home/baur/IdeaProjects/usci/usci/modules/receiver/src/main/resources/usci.xsd";
    private final static String prodXSDSourceFilePath = "D:\\usci\\usci\\modules\\receiver\\src\\main\\resources\\usci.xsd";

    private final static String stendXSDTargetFilePath = "/home/baur/IdeaProjects/usci/usci/modules/receiver/target/classes/usci.xsd"; //todo: change that path
    private final static String devXSDTargetFilePath = "/home/baur/IdeaProjects/usci/usci/modules/receiver/target/classes/usci.xsd";
    private final static String prodXSDTargetFilePath = "D:\\usci\\usci\\modules\\receiver\\target\\classes\\usci.xsd";

    private final static boolean stendStatsEnabled = true;
    private final static boolean devStatsEnabled = true;
    private final static boolean prodStatsEnabled = true;

    private final static int stendThreadLimit = 20;
    private final static int devThreadLimit = 20;
    private final static int prodThreadLimit = 100;

    private final static String[] GODModes = new String[]{"XML_DATA_BY_CID", "XML_PORTFOLIO_DATA_BY_CID", "GGGGODGGG"};
    private final static String[] DEVILModes = new String[]{"GGGDEVILGGG"};

    public static String getAsIP() {
        switch(mode) {
            case STEND:
                return stendAsIP;
            case DEV:
                return devAsIP;
            case PROD:
                return prodAsIP;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getXSDSourceFilePath() {
        switch(mode) {
            case STEND:
                return stendXSDSourceFilePath;
            case DEV:
                return devXSDSourceFilePath;
            case PROD:
                return prodXSDSourceFilePath;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getXSDTargetFilePath() {
        switch(mode) {
            case STEND:
                return stendXSDTargetFilePath;
            case DEV:
                return devXSDTargetFilePath;
            case PROD:
                return prodXSDTargetFilePath;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getDBCoreIP() {
        switch(mode) {
            case STEND:
                return stendDBCoreIP;
            case DEV:
                return devDBCoreIP;
            case PROD:
                return prodDBCoreIp;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getDBShowcaseIP() {
        switch(mode) {
            case STEND:
                return stendDBShowcaseIP;
            case DEV:
                return devDBShowcaseIP;
            case PROD:
                return prodDBShowcaseIP;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getDBReporterIP() {
        switch(mode) {
            case STEND:
                return stendDBReporterIP;
            case DEV:
                return devDBReporterIP;
            case PROD:
                return prodDBReporterIP;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getPortalUrl() {
        switch(mode) {
            case STEND:
                return stendPortalUrl;
            case DEV:
                return devPortalUrl;
            case PROD:
                return prodPortalUrl;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getPortalPort() {
        switch(mode) {
            case STEND:
                return stendPortalPort;
            case DEV:
                return devPortalPort;
            case PROD:
                return prodPortalPort;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getCoreSchemaName() {
        switch(mode) {
            case STEND:
                return stendCoreSchemaName;
            case DEV:
                return devCoreSchemaName;
            case PROD:
                return prodCoreSchemaName;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getShowcaseSchemaName() {
        switch(mode) {
            case STEND:
                return stendShowcaseSchemaName;
            case DEV:
                return devShowcaseSchemaName;
            case PROD:
                return prodShowcaseSchemaName;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getCRDBIP() {
        switch(mode) {
            case STEND:
                return stendCRDBIP;
            case DEV:
                return devCRDBIP;
            case PROD:
                return prodCRDBIP;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getCRDBUsername() {
        switch(mode) {
            case STEND:
                return stendCRDBUsername;
            case DEV:
                return devCRDBUsername;
            case PROD:
                return prodCRDBUsername;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getCRDBPassword() {
        switch(mode) {
            case STEND:
                return stendCRDBPassword;
            case DEV:
                return devCRDBPassword;
            case PROD:
                return prodCRDBPassword;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getReportFilesCatalog() {
        switch(mode) {
            case STEND:
                return stendReportFilesCatalog;
            case DEV:
                return devReportFilesCatalog;
            case PROD:
                return prodReportFilesCatalog;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String getReportFilesFolder() {
        switch(mode) {
            case STEND:
                return stendReportFilesFolder;
            case DEV:
                return devReportFilesFolder;
            case PROD:
                return prodReportFilesFolder;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static boolean getStatsEnabled() {
        switch(mode) {
            case STEND:
                return stendStatsEnabled;
            case DEV:
                return devStatsEnabled;
            case PROD:
                return prodStatsEnabled;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static int getThreadLimit() {
        switch(mode) {
            case STEND:
                return stendThreadLimit;
            case DEV:
                return devThreadLimit;
            case PROD:
                return prodThreadLimit;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static String convertUploadPortletPath(String path){
        switch (mode) {
            case STEND:
            case DEV:
                return path;
            case PROD:
                return path.replace("\\\\" + StaticRouter.getAsIP() + "\\download$\\","E:\\download\\");
            default:
                throw new IllegalStateException(Errors.compose(Errors.E284));
        }
    }

    public static boolean isGODMode(String filename) {
        for (String tmpStr : GODModes) {
            if (filename.contains(tmpStr))
                return true;
        }

        return false;
    }

    public static boolean isDEVILMode(String filename) {
        for (String tmpStr : DEVILModes) {
            if (filename.contains(tmpStr))
                return true;
        }

        return false;
    }

    public static String[] getGODModes() {
        return GODModes;
    }

    public static boolean isInMode(String filename) {
        return isGODMode(filename) || isDEVILMode(filename);

    }

    public static boolean isDevMode(){
        return mode == MODE.DEV;
    }
}
