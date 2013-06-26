package kz.bsbnb.usci.eav.tool;

import java.sql.Date;

/**
 * @author a.motov
 */
public class Configuration
{

    public static int historyAlgorithm = Constants.HISTORY_ALGORITHM_FILL_BIG_DATE;
    public static Date historyMaxDate = historyAlgorithm == Constants.HISTORY_ALGORITHM_NOT_FILL ? null : Constants.HISTORY_MAX_DATE;

}
