package com.bsbnb.creditregistry.portlets.audit.dm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditController {

    private static Connection connection;

    private List<AuditTableRecord> getAuditTableRecordsList(List list) {
        int listSize = list.size();
        List<AuditTableRecord> result = new ArrayList<AuditTableRecord>(listSize);
        for(int i=0; i<listSize; i++) {
            result.add(new AuditTableRecord((AuditEvent) list.get(i)));
        }
        return result;
    }

    public List<AuditTableRecord> getAuditRecords() {
//        EntityManager em = getEntityManager();
//        try {
//            Query q = em.createQuery("SELECT auditEvent FROM AuditEvent as auditEvent ORDER BY auditEvent.beginDate DESC");
//            List events = q.getResultList();
//            return getAuditTableRecordsList(events);
//        } finally {
//            em.close();
//        }
        List<AuditTableRecord> ret = new ArrayList<AuditTableRecord>();

        try {
               ResultSet rows = getStatement().executeQuery("SELECT * FROM Audit_Event order by EVENT_BEGIN_DT");
               while(rows.next()){
                    ret.add(mapAuditTableRecord(rows));
               }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return ret;
    }
    
    public AuditEventKind getAuditEventKindsByCode(String code) {
//        EntityManager em = getEntityManager();
//        try {
//            Query kindQuery = em.createNamedQuery("AuditEventKind.findByCode", AuditEventKind.class);
//            List results = kindQuery.getResultList();
//            return (AuditEventKind) (results.isEmpty() ? null : results.get(0));
//        } finally {
//            em.close();
//        }
        return  new AuditEventKind();
    }

    public List<AuditTableRecord> getFilteredAuditRecords(String noteKZFilter, String noteRUFilter, String code) {
//        EntityManager em = getEntityManager();
//        try {
//
//            Query q = em.createQuery("SELECT auditEvent FROM AuditEvent auditEvent INNER JOIN auditEvent.kind WHERE "
//                    + "auditEvent.noteKz LIKE '%:noteKZFilter:%' "
//                    + "AND auditEvent.noteRu LIKE '%:noteRUFilter%' "
//                    + "AND kind.code=':code' "
//                    + "ORDER BY auditEvent.beginDate DESC")
//                    .setParameter("noteKZFilter", noteKZFilter)
//                    .setParameter("noteRUFilter", noteRUFilter)
//                    .setParameter("code", code);
//            List eventsList = q.getResultList();
//            return getAuditTableRecordsList(eventsList);
//        } finally {
//            em.close();
//        }

        return new ArrayList<AuditTableRecord>();
    }
    
    public void deleteAuditsBeforeDate(Date date) {
//        EntityManager em = getEntityManager();
//        try {
//            EntityTransaction transaction = em.getTransaction();
//            transaction.begin();
//            Query q = em.createQuery("DELETE FROM Audit a WHERE a.auditDate<:custdate").setParameter("custdate", date);
//            q.executeUpdate();
//            transaction.commit();
//        } finally {
//            em.close();
//        }
    }

    public static Statement getStatement(){
        try {
            if(connection == null){
                Class.forName("oracle.jdbc.OracleDriver");
                connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.44:1521:USCI", "BAUKA", "123123");
                return connection.createStatement();
            }
            return connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public AuditTableRecord mapAuditTableRecord(ResultSet row){
       AuditEvent ae = new AuditEvent();
       AuditTableRecord ret = new AuditTableRecord(ae);

        String s = "";

       DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            ret.setUserId(Long.parseLong(row.getString("USER_ID")));
            ret.setBeginDate(df.parse(row.getString("EVENT_BEGIN_DT")));
            ret.setErrorCode(Integer.parseInt(row.getString("ERR_CODE")));
            ret.setInfo(row.getString("ADD_INFO"));
            ret.setTableName(row.getString("TABLE_NAME"));
            ret.setEndDate(df.parse(row.getString("EVENT_END_DT")));
        } catch (Exception e) {
            s = e.getMessage();
        }

        return ret;
    }
}
