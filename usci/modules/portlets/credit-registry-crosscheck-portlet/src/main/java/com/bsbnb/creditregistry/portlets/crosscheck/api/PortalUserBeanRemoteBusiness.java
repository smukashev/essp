package com.bsbnb.creditregistry.portlets.crosscheck.api;


import com.bsbnb.creditregistry.portlets.crosscheck.model.Creditor;
import java.math.BigInteger;
import java.util.List;

/**
 * Общий интерфейс для удаленного и локального бизнес-интерфейса EJB 
 * {@link com.bsbnb.creditregistry.ejb.impl.maintenance.PortalUserBean}
 * Необходимо использовать только в случаях употребления метода и для удаленного, и для локального интерфейсов.
 * @see com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness
 * @see com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanLocalBusiness
 * @author alexandr.motov
 */
public interface PortalUserBeanRemoteBusiness {
    
    /**
     * Возвращает список БВУ/НО пользователя в алфавитном порядке без филиалов
     * @param userId - Идентификатор пользователя портала
     * @return Список БВУ/НО
     */
    public List<Creditor> getMainCreditorsInAlphabeticalOrder(long userId);
    
}
