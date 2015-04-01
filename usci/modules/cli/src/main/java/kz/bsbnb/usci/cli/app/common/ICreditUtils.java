package kz.bsbnb.usci.cli.app.common;

import kz.bsbnb.usci.cli.app.exceptions.CreditorNotFoundException;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * Created by Bauyrzhan.Makhambeto on 17/03/2015.
 */
public interface ICreditUtils {
    /**
     * get creditor from remote connection by id in table ref.creditor
     */
    IBaseEntity getCreditor(Connection conn, long creditorId);

    IBaseEntity getCreditHull(Connection connection, Long creditorId, String contractNo, Date contractDate)
            throws CreditorNotFoundException;

    /**
     * get creditor as Creditor object instance
     */
    Creditor getCrCreditor(Connection conn, long creditorId);

    /**
     * call procedure pkg_eav_xml_util.generate_by_credit_list and save zip in dir
     * @return name of the file
     */
    String zipXmlUtil(Connection connection, String creditIds, Long creditorId, Date reportDate, String path)
            throws TimeoutException, InterruptedException, SQLException, IOException;

    Document getManifest(Creditor creditor, int count);
}
