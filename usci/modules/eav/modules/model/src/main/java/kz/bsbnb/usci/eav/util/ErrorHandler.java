package kz.bsbnb.usci.eav.util;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandler {
    public void throwClosedExceptionForImmutable(IBaseEntity baseEntityImmutable) {
        if(baseEntityImmutable.getMeta().isReference()) {
            String keyValue = null;
            String[] possibleKeys = new String[]{"no_", "short_name", "code"};

            for(int i=0;i<possibleKeys.length && keyValue == null;i++) {
                if(baseEntityImmutable.getMeta().hasAttribute(possibleKeys[i])) {
                    keyValue = ((String) baseEntityImmutable.getEl(possibleKeys[i]));
                }
            }

            if(keyValue != null)
                throw new KnownException(Errors.compose(Errors.E298, baseEntityImmutable.getMeta().getClassTitle(), keyValue, baseEntityImmutable.getReportDate()));
        }

        throw new KnownException(Errors.compose(Errors.E57, baseEntityImmutable.getId(), baseEntityImmutable.getReportDate()));
    }
}
