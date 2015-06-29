package kz.bsbnb.usci.core.listener.cr;

import kz.bsbnb.usci.core.listener.IRefLoadedListener;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrRefLoadedListener implements IRefLoadedListener {
    @Autowired
    IUserDao userDao;

    @Override
    public void process(long userId, IMetaClass metaClass, String attr, List<RefListItem> list) {

        if(metaClass.getClassName().equals("ref_creditor")) {
            List<Creditor> creditors = userDao.getPortalUserCreditorList(userId);

            if (creditors.size() == 1) {
                //значит пользователь БВУ/НО
                Creditor creditor = creditors.get(0);
                RefListItem refItem = null;

                for (RefListItem refListItem : list)
                    if (refListItem.getId() == creditor.getId()) {
                        refItem = refListItem;
                        break;
                    }

                if (refItem != null){
                    list.clear();
                    list.add(refItem);
                }
            }
        }
    }
}
