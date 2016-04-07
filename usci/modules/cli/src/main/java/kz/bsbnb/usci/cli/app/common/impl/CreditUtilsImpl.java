package kz.bsbnb.usci.cli.app.common.impl;

import kz.bsbnb.usci.cli.app.common.ICreditUtils;
import kz.bsbnb.usci.cli.app.exceptions.CreditorNotFoundException;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.impl.BaseEntityXmlGenerator;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Bauyrzhan.Makhambeto on 17/03/2015.
 */
@Component
@Scope("singleton")
public class CreditUtilsImpl implements ICreditUtils {

    @Autowired
    IMetaClassRepository metaClassRepository;

    private HashMap<Long, IBaseEntity> creditors;
    private HashMap<Long, Creditor> crCreditors;

    public IBaseEntity getCreditor(Connection conn, long creditorId) {
        if (creditors == null) {
            creditors = new HashMap<Long, IBaseEntity>();
            crCreditors = new HashMap<Long, Creditor>();
        }

        if (creditors.containsKey(creditorId))
            return creditors.get(creditorId);

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Batch batch = new Batch(new Date());
        batch.setId(777);
        Creditor creditor = new Creditor();


        try {
            preparedStatement = conn.prepareStatement("select name,code from ref.creditor where id = ?");
            preparedStatement.setLong(1, creditorId);
            resultSet = preparedStatement.executeQuery();
            creditor.setId(creditorId);
            if (resultSet.next()) {
                creditor.setName(resultSet.getString("name"));
                creditor.setCode(resultSet.getString("code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        try {
            preparedStatement = conn.prepareStatement("select t1.no_,  t2.code " +
                    " from ref.creditor_doc t1, ref.doc_type t2 " +
                    "where t1.creditor_id = ? and t1.type_id = t2.id");
            preparedStatement.setLong(1, creditorId);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            // checkme!
            IBaseEntity baseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_creditor"), new Date(), 0);
            IBaseSet baseSet = new BaseSet(metaClassRepository.getMetaClass("document"), creditorId);

            while (resultSet.next()) {
                IBaseEntity doc = new BaseEntity(metaClassRepository.getMetaClass("document"), new Date(), 0);
                IBaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date(), 0);
                String code = resultSet.getString("code");
                String no = resultSet.getString("no_");

                if (code.equals("07"))
                    creditor.setBIN(no);

                if (code.equals("15"))
                    creditor.setBIK(no);

                docType.put("code", new BaseValue(-1, batch.getRepDate(), code));
                doc.put("no", new BaseValue(-1, batch.getRepDate(), no));
                doc.put("doc_type", new BaseValue(-1, batch.getRepDate(), docType));
                baseSet.put(new BaseValue(-1, batch.getRepDate(), doc));
            }

            baseEntity.put("docs", new BaseValue(-1, batch.getRepDate(), baseSet));
            creditors.put(creditorId, baseEntity);
            crCreditors.put(creditorId, creditor);
            return baseEntity;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Creditor getCrCreditor(Connection conn, long creditorId) {
        if (crCreditors.containsKey(creditorId))
            return crCreditors.get(creditorId);

        throw new IllegalArgumentException(Errors.compose(Errors.E2));
    }

    @Override
    public IBaseEntity getCreditHull(Connection connection, Long creditorId, String contractNo, Date contractDate)
            throws CreditorNotFoundException {
        Batch batch = new Batch(new Date());
        batch.setId(777);

        // checkme!
        IBaseEntity credit = new BaseEntity(metaClassRepository.getMetaClass("credit"), new Date(), 0);

        IBaseEntity primaryContract = new BaseEntity(metaClassRepository.getMetaClass("primary_contract"), new Date(), 0);

        primaryContract.put("no", new BaseValue(-1, batch.getRepDate(), contractNo));
        primaryContract.put("date", new BaseValue(-1, batch.getRepDate(), contractDate));

        IBaseEntity creditor = getCreditor(connection, creditorId);

        if (creditor == null) {
            throw new CreditorNotFoundException();
        }

        credit.put("primary_contract", new BaseValue(0, -1, batch.getRepDate(), primaryContract));
        credit.put("creditor", new BaseValue(0, -1, batch.getRepDate(), creditor));

        return credit;
    }

    @Override
    public String zipXmlUtil(Connection connection, String creditIds, Long creditorId, Date reportDate, String path)
            throws TimeoutException, InterruptedException, SQLException, IOException {
        ResultSet result = null;
        PreparedStatement preparedStatement = null;
        String fileName;
        Long maxXmlFileId = 0L;

        Statement st = null;
        //System.out.println(creditIds + " " + reportDate + " " + path);

        try {
            preparedStatement = connection.prepareStatement("SELECT MAX(xf.id) AS MAX_ID FROM core.xml_file2 xf");
            result = preparedStatement.executeQuery();
            result.next();
            maxXmlFileId = result.getLong("MAX_ID");
        } catch (SQLException e) {
            System.out.println("Can't get MAX XML_FILE ID from DB: " + e.getMessage());
            throw new SQLException(e);
        }
        CallableStatement stm = null;
        try {
            stm = connection.prepareCall("{call CORE.PKG_EAV_XML_UTIL.generate_by_credit_list(?, ?, 1, ?)}");
            stm.setString(1, creditIds);
            stm.setDate(2, new java.sql.Date(reportDate.getTime()));
            stm.setLong(3, creditorId);
            stm.execute();
        } catch (SQLException e) {
            System.out.println("Can't call procedure generate_by_credit_list from DB: " + e.getMessage());
            throw new SQLException(e);
        } finally {
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        preparedStatement = null;
        result = null;

        try {
            String query = "SELECT xf.id, xf.file_name, xf.file_content\n" +
                    " FROM core.xml_file2 xf\n" +
                    " WHERE xf.status = 'COMPLETED'\n" +
                    " AND xf.id > " + maxXmlFileId.toString() + "\n" +
                    " ORDER BY xf.id ASC";
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            int tryCount = 0;
            while (!result.next()) {
                Thread.sleep(500);
                result = preparedStatement.executeQuery();
                tryCount++;
                if (tryCount > 2400) {//20 minutes
                    throw new TimeoutException(Errors.compose(Errors.E222));
                }
            }

            fileName = result.getString("file_name");
            Blob blob = result.getBlob("file_content");

            File file = new File(path + fileName + ".zip");
            file.createNewFile();

            InputStream in = blob.getBinaryStream();

            byte[] buffer = new byte[1024];

            FileOutputStream fout = new FileOutputStream(file);

            while (in.read(buffer) > 0) {
                fout.write(buffer);
            }

            fout.close();

        } catch (SQLException e) {
            System.out.println("Can't create prepared statement: " + e.getMessage());
            throw new SQLException(e);
        }


        return fileName;
    }

    private Element getElementString(String name, String value, Document document) {
        Element element = document.createElement(name);
        element.appendChild(document.createTextNode(value));
        return element;
    }

    @Override
    public Document getManifest(Creditor creditor, int count) {
        BaseEntityXmlGenerator baseEntityXmlGenerator = new BaseEntityXmlGenerator();
        Document document = baseEntityXmlGenerator.getDocument();

        Element manifest = document.createElement("manifest");

        manifest.appendChild(getElementString("type", "1", document));
        manifest.appendChild(getElementString("name", "data.xml", document));
        manifest.appendChild(getElementString("userid", "100500", document));
        manifest.appendChild(getElementString("size", String.valueOf(count), document));
        manifest.appendChild(getElementString("date", new SimpleDateFormat("dd.MM.yyyy").format(new Date()), document));

        Element properties = document.createElement("properties");

        Element propertyCode = document.createElement("property");
        propertyCode.appendChild(getElementString("name", "CODE", document));
        propertyCode.appendChild(getElementString("value", String.valueOf(creditor.getCode()), document));

        Element propertyName = document.createElement("property");
        propertyName.appendChild(getElementString("name", "NAME", document));
        propertyName.appendChild(getElementString("value", String.valueOf(creditor.getName()), document));

        Element propertyBIN = document.createElement("property");
        propertyBIN.appendChild(getElementString("name", "BIN", document));
        propertyBIN.appendChild(getElementString("value", String.valueOf(creditor.getBIN()), document));

        Element propertyBIK = document.createElement("property");
        propertyBIK.appendChild(getElementString("name", "BIK", document));
        propertyBIK.appendChild(getElementString("value", String.valueOf(creditor.getBIK()), document));
        properties.appendChild(propertyCode);
        properties.appendChild(propertyName);
        properties.appendChild(propertyBIN);
        properties.appendChild(propertyBIK);

        manifest.appendChild(properties);
        document.appendChild(manifest);
        return document;
    }
}
