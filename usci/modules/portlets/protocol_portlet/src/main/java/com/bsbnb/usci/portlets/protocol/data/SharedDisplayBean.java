package com.bsbnb.usci.portlets.protocol.data;

//import com.bsbnb.creditregistry.dm.ref.Shared;
import com.bsbnb.usci.portlets.protocol.PortletEnvironmentFacade;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.util.Errors;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class SharedDisplayBean {
    private Shared shared;
    
    public SharedDisplayBean(Shared shared) {
        if(shared==null) {
            throw new IllegalArgumentException(Errors.compose(Errors.E247));
        }
        this.shared = shared;
    }
    
    public String getCode() {
        return shared.getCode();
    }
    
    public String getName() {
        if(PortletEnvironmentFacade.get().isLanguageKazakh()) {
            return shared.getNameKz();
        }
        return shared.getNameRu();
    }
    
    @Override
    public boolean equals(Object another) {
        if(!(another instanceof SharedDisplayBean)) {
            return false;
        }
        return shared.getCode().equals(((SharedDisplayBean) another).getCode());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.shared.getCode() != null ? this.shared.getCode().hashCode() : 0);
        return hash;
    }
}
