package kz.bsbnb.usci.receiver.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author k.tulbassiyev
 */
@Component
public class Global {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DOT = "dd.MM.yyyy";

    @Value("${batches.dir}")
    private String batchesDir;

    @Value("${rules.enabled}")
    private boolean rulesEnabled;

    public String getBatchesDir() {
        return batchesDir;
    }

    public void setBatchesDir(String batchesDir) {
        this.batchesDir = batchesDir;
    }

    public boolean isRulesEnabled() {
        return rulesEnabled;
    }

    public void setRulesEnabled(boolean rulesEnabled) {
        this.rulesEnabled = rulesEnabled;
    }
}
