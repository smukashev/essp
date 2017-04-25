package kz.bsbnb.usci.eav.persistance.dao;

/**
 * Created by Yerlan.Zhumashev on 24.04.2017.
 */
public interface IBaseLogsDao {

    void insertLogs(String portletname,String username,String comment);

}
