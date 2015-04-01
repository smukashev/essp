package kz.bsbnb.usci.cli.app.mnt;

import kz.bsbnb.usci.cli.app.common.ICreditUtils;
import kz.bsbnb.usci.cli.app.exceptions.CreditorNotFoundException;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MntThread extends Thread {

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    ICreditUtils creditUtils;

    private boolean paused = false;
    Connection connection;
    long editId;
    long creditorId;
    Date reportDate;
    long limit;
    String path;

    public MntThread(Connection connection, long creditorId, Date reportDate, long editId, long limit,  String path){
        this.connection = connection;
        this.creditorId = creditorId;
        this.editId = editId;
        this.limit = limit;
        if(limit == -1)
            this.limit = 100;
        this.reportDate = reportDate;
        this.path = path;
        //start();
    }

    public void init(){

    }

    public void run(){

        long mntId;

        String select = "select cec.id as mnt_id, cec.credit_id, " +
                "cec.contract_no, cec.contract_date, root.creditor_id, root.open_date " +
                "  from maintenance.credreg_edit root, " +
                " maintenance.credreg_edit_credit cec" +
                " where root.id = cec.edit_id" +
                "   and (cec.processed_usci  is null or cec.processed_usci = 0)" +
                "   and (cec.contract_no_old is null and cec.contract_date_old is null)";


        if(creditorId > 0) {
            select += " and root.creditor_id = ? ";
        }

        if(reportDate != null) {
            select += " and root.open_date = ? ";
        }

        if(editId > 0) {
            select += " and root.id = ? ";
        }

        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(select);
            int paramId = 1;

            if(creditorId > 0) {
                statement.setLong(paramId++, creditorId);
            }

            if(reportDate != null) {
                statement.setDate(paramId++, new java.sql.Date(reportDate.getTime()));
            }

            if(editId > 0) {
                statement.setLong(paramId++ , editId);
            }

            select += " order by cec.contract_no, cec.contract_date";

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            ResultSet resultSet = statement.executeQuery();
            //System.out.println(select.toString());

            Long prevCreditorId = 0L;
            Date prevReportDate = null;
            StringBuffer creditIds = new StringBuffer();
            List<Long> listSuccess = new ArrayList<Long>();

            while(resultSet.next() && limit > 0) {

                synchronized (this){
                    if(paused)
                        this.wait();
                }

                mntId = resultSet.getLong("mnt_id");
                Long creditorId = resultSet.getLong("creditor_id");
                Date reportDate = resultSet.getDate("open_date");
                String contractNo = resultSet.getString("contract_no");
                Date contractDate =resultSet.getDate("contract_date");
                Long creditId = resultSet.getLong("credit_id");
                IBaseEntity credit;

                try {
                    credit = creditUtils.getCreditHull(connection, creditorId, contractNo, contractDate);
                } catch (CreditorNotFoundException e) {
                    System.out.println("creditor not found " + creditorId);
                    continue;
                }

                baseEntityProcessorDao.prepare(credit);

                if(credit.getId() <= 0) {
                    System.out.println("credit not found contractNo=" + contractNo + " contractDate=" + contractDate );
                    continue;
                }

                listSuccess.add(mntId);

                if(!prevCreditorId.equals(creditorId) || !prevReportDate.equals(reportDate)) {

                    if(creditIds.length() > 0) {
                        String file = creditUtils.zipXmlUtil(connection,
                                creditIds.toString(), prevCreditorId, prevReportDate, path);

                        MntUtils.updateMntStatus(connection, "changeScript", listSuccess);
                        creditIds.delete(0, creditIds.length());
                        listSuccess = new ArrayList<Long>();
                    }
                    prevCreditorId = creditorId;
                    prevReportDate = reportDate;
                }

                creditIds.append(creditId + ",");

                limit--;
            }

            if(creditIds.length() > 0) {
                String file = creditUtils.zipXmlUtil(connection,
                        creditIds.toString(), prevCreditorId, prevReportDate, path);
                MntUtils.updateMntStatus(connection, "changeScript", listSuccess);
                creditIds.delete(0, creditIds.length());
            }

            System.out.println("success");

        } catch (InterruptedException e) {
            System.out.println("interrupted with status 0");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause(){
        this.paused = true;
        System.out.println("pause status changed to: "  + this.paused);
    }

    public void unpause(){
        this.paused = false;
    }


    public void setBaseEntityProcessorDao(IBaseEntityProcessorDao baseEntityProcessorDao) {
        this.baseEntityProcessorDao = baseEntityProcessorDao;
    }

    public void setCreditUtils(ICreditUtils creditUtils) {
        this.creditUtils = creditUtils;
    }
}