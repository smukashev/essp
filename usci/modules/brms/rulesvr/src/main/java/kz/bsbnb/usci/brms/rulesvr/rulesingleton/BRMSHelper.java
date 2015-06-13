package kz.bsbnb.usci.brms.rulesvr.rulesingleton;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

@Component
public class BRMSHelper
{
    private static JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public static boolean hasBACT(String balanceAccountNo, String creditCode){
        String select = "select count(1)\n" +
                "  from eav_be_entities ent,\n" +
                "       eav_be_complex_values cba,\n" +
                "       eav_be_string_values sba,\n" +
                "       eav_be_complex_values cct,\n" +
                "       eav_be_string_values sct\n" +
                "   where ent.id = cba.entity_id\n" +
                "     and cba.attribute_id = (select ct.id from vw_complex_attribute ct \n" +
                "                                    where ct.attribute_name = 'balance_account'\n" +
                "                                       and ct.containing_name = 'ref_ba_ct'\n" +
                "                                       and ct.class_name = 'ref_balance_account')\n" +
                "     and cba.entity_value_id = sba.entity_id\n" +
                "     and sba.attribute_id = (select st.id from vw_simple_attribute st\n" +
                "                                    where st.class_name = 'ref_balance_account'\n" +
                "                                      and st.attribute_name = 'no_')\n" +
                "     and sba.value = ?\n" +
                "     and ent.id = cct.entity_id\n" +
                "     and cct.entity_value_id = sct.entity_id\n" +
                "     and sct.value = ?\n" +
                "     and cct.attribute_id = (select ct.id from vw_complex_attribute ct \n" +
                "                                    where ct.attribute_name = 'credit_type'\n" +
                "                                       and ct.containing_name = 'ref_ba_ct'\n" +
                "                                       and ct.class_name = 'ref_credit_type')\n" +
                "     and sct.attribute_id = (select st.id from vw_simple_attribute st\n" +
                "                                    where st.class_name = 'ref_credit_type'\n" +
                "                                      and st.attribute_name = 'code')\n" +
                "     and rownum = 1\n";

        int ans = jdbcTemplate.queryForInt(select, new String[]{balanceAccountNo, creditCode});

        return ans > 0;
    }
    public static boolean isValidRNN(String rnn)
    {
        String chr;
        String prevChr = rnn.substring(0, 1);
        int chrCnt = 1;
        for (int i = 1; i < rnn.length(); ++i) {
            chr = rnn.substring(i, i + 1);
            if (chr == prevChr)
                ++chrCnt;
            prevChr = chr;
        }
        int i, j, k, s, t;
        k = 0;
        for (i = 1; i <= 10; i++) {
            s = 0;
            t = i - 1;
            for (j = 1; j <= 11; j++) {
                ++t;
                if (t == 11)
                    t = 1;
                s = s + t * Integer.parseInt(rnn.substring(j - 1, j));
            }
            k = s % 11;
            if (k < 10)
                break;
        }
        return (k == Integer.parseInt(rnn.substring(11, 12)));
    }
    public static List getInvalidIINs(Object list){
        List ret = new ArrayList();
        for(Object o : (LinkedList)list) {
            String s = (String) o ;
            if(!checkIIN(s))
                ret.add(s);
        }
        return ret;
    }

    private static int[] weights = new int[]{1,2,3,4,5,6,7,8,9,10,11,1,2};

    public static boolean checkIIN(String iin){
        int sum = 0;
        if(iin.length() != 12)
            return false;

        if(!isDateValid(iin.substring(0,6),"yyMMdd"))
            return false;

        if(iin.charAt(6) < '1' || iin.charAt(6) > '6')
            return false;

        for(int i=0;i<11;i++)
            sum += (iin.charAt(i) - '0' ) * weights[i];
        sum %= 11;
        int last = iin.charAt(11) - '0';
        if(sum ==  10) {
            sum = 0;
            for(int i=0;i<11;i++)
                sum+=(iin.charAt(i) - '0') * weights[i+2];
            sum %= 11;
        }
        return sum == last;
    }

    public static int firstDay(java.util.Date date){
        java.util.Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isDateValid(String date,String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException pe) {
            return false;
        }
    }
}
