package com.bsbnb.usci.portlets.crosscheck.helper;

import com.bsbnb.usci.portlets.crosscheck.dm.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ModelHelper {
    public static Creditor convertToCreditor(ResultSet rs) throws SQLException {
        Creditor c = new Creditor();
        c.setId(rs.getBigDecimal("ID").toBigInteger());
        c.setChangeDate(rs.getDate("CHANGE_DATE"));
        c.setCode(rs.getString("CODE"));
        c.setName(rs.getString("NAME"));
        c.setShortName(rs.getString("SHORT_NAME"));
        c.setShutdownDate(rs.getDate("SHUTDOWN_DATE"));
        // c.setMainOfficeId();
        // c.setSubjectType();

        return c;
    }

    public static CrossCheck convertToCrossCheck(ResultSet rs, Creditor c) throws SQLException {
        CrossCheck cc = new CrossCheck();
        cc.setId(rs.getBigDecimal("ID"));
        cc.setUsername(rs.getString("USER_NAME"));
        cc.setStatus(rs.getInt("STATUS_ID"));
        cc.setStatusName(rs.getString("STATUS_NAME"));
        cc.setDateBegin(rs.getDate("DATE_BEGIN"));
        cc.setDateEnd(rs.getDate("DATE_END"));
        cc.setReportDate(rs.getDate("REPORT_DATE"));
        cc.setCreditor(c);

        return cc;
    }

    public static CrossCheckMessage convertToCrossCheckMessage(ResultSet rs, CrossCheck cc, Message m) throws SQLException {
        CrossCheckMessage cm = new CrossCheckMessage();

        cm.setCrossCheck(cc);
        cm.setDescription(rs.getString("DESCRIPTION"));
        cm.setDiff(rs.getString("DIFF"));
        cm.setId(rs.getBigDecimal("ID").toBigInteger());
        cm.setInnerValue(rs.getString("INNER_VALUE"));
        cm.setIsError(rs.getBoolean("IS_ERROR"));
        cm.setMessage(m);
        cm.setOuterValue(rs.getString("OUTER_VALUE"));

        return cm;
    }

    public static Message convertToMessage(ResultSet rs) throws SQLException {
        Message m = new Message();

        m.setId(rs.getBigDecimal("ID"));
        m.setCode(rs.getString("CODE"));
        m.setNameKz(rs.getString("NAME_KZ"));
        m.setNameRu(rs.getString("NAME_RU"));
        m.setNote(rs.getString("NOTE"));

        return m;
    }

    public static SubjectType convertToSubjectType(ResultSet rs) {
        SubjectType st = new SubjectType();

        /*st.setCloseDate();
        st.setCode();
        st.setCreditorList();
        st.setId();
        st.setIsLast();
        st.setKindId();
        st.setNameKz();
        st.setNameRu();
        st.setOpenDate();
        st.setParentId();
        st.setReportPeriodDurationMonths();
        st.setSubjectTypeList();*/

        return st;
    }
}
