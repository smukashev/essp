package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.postgresql.dao.PostgreSQLUserDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PortalUserBeanRemoteBusinessImpl implements PortalUserBeanRemoteBusiness
{
    @Autowired
    PostgreSQLUserDaoImpl userDao;

    /**
     * Проверяет наличие связи между пользователем портала и БВУ/НО.
     * @param userId Id пользователя портала.
     * @param creditorId Id БВУ/НО.
     * @return true, если связь есть, false в противном случае.
     */
    @Override
    public boolean hasPortalUserCreditor(long userId, long creditorId)
    {
        return false;
    }

    /**
     * Устанавливает связь между пользователем портала и БВУ/НО.
     * @param userId Id пользователя Liferay-я.
     * @param creditorId Id БВУ/НО.
     */
    @Override
    public void setPortalUserCreditors(long userId, long creditorId)
    {

    }

    /**
     * Удаляет связь между пользователем портала и БВУ/НО.
     * @param userId Id пользователя Liferay-я.
     * @param creditorId Id БВУ/НО.
     */
    @Override
    public void unsetPortalUserCreditors(long userId, long creditorId)
    {

    }

    /**
     * Возвращает список БВУ/НО пользователя портала.
     * @param userId Id пользователя Liferay-я.
     * @return Список БВУ/НО.
     */
    @Override
    public List<Creditor> getPortalUserCreditorList(long userId)
    {
        Creditor creditor = new Creditor();

        creditor.setId(1);
        creditor.setName("Creditor1");
        creditor.setShortName("C1");
        creditor.setCode("CODE1");

        ArrayList<Creditor> list = new ArrayList<Creditor>();

        list.add(creditor);

        return list;
    }

    /**
     * Проводит синхранизацию между пользователями Liferay-я и пользователями портала.
     * Если пользователю Liferay-я нет соответствующего пользователя портала, то он будет добавлен.
     * Если пользователю Liferay-я соответствует более одного пользователя портала,
     * то сначала они будут удалены, после этого будет добавлен соответствующий пользователь.
     * В случае присутствия соответствующего пользователя портала,
     * то он будет проверен по полю modifiedDate и при необходимости изменен.
     * @param users Список пользователей Liferay-я.
     * @exception Exception Неожиданная ошибка.
     */
    @Override
    public void synchronize(List<PortalUser> users)
    {

    }

    @Override
    public List<Creditor> getMainCreditorsInAlphabeticalOrder(long userId)
    {
        return null;
    }
}
