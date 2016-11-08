package kz.bsbnb.usci.receiver.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author k.tulbassiyev
 */
@Component
public class Global {
    @Value("${batches.dir}")
    private String batchesDir;

    public String getBatchesDir() {
        return batchesDir;
    }

    public void setBatchesDir(String batchesDir) {
        this.batchesDir = batchesDir;
    }
}
