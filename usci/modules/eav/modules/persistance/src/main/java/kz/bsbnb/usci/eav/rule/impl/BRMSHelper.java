package kz.bsbnb.usci.eav.rule.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class BRMSHelper extends JDBCSupport implements InitializingBean {
    @Qualifier("metaClassRepositoryImpl")
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Qualifier("baseEntityProcessorDaoImpl")
    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    private Map<BalDebtRemains, IBaseEntity> refsMap = new HashMap<>();

    public static IBaseEntityProcessorDao rulesLoadDao;
    public static IMetaClassRepository rulesMetaDao;

    public static void setLoadDao(IBaseEntityProcessorDao loadDao) {
        rulesLoadDao = loadDao;
    }

    public static void setMetaDao(IMetaClassRepository metaDao) {
        rulesMetaDao = metaDao;
    }


    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    private class BalDebtRemains {
        private final String no_;
        private final String code;

        public BalDebtRemains(String code, String no_) {
            this.code = code;
            this.no_ = no_;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BalDebtRemains balDebtRemains = (BalDebtRemains) o;

            if (!no_.equals(balDebtRemains.no_)) return false;
            return code.equals(balDebtRemains.code);

        }

        @Override
        public int hashCode() {
            int result = no_.hashCode();
            result = 31 * result + code.hashCode();
            return result;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            long t1 = System.currentTimeMillis();

            MetaClass refBaDrtMetaClass = metaClassRepository.getMetaClass("ref_ba_drt");

            List<BaseEntity> entities = baseEntityProcessorDao.getEntityByMetaClass(refBaDrtMetaClass);

            for (BaseEntity entity : entities)
                refsMap.put(new BalDebtRemains(entity.getEl("balance_account.no_").toString(), entity.getEl("debt_remains_type.code").toString()), entity);

            System.out.println("Время потраченное для кэширование: " + (System.currentTimeMillis() - t1));
        } catch (Exception e) {
            System.err.println("Необходимо после перезагрузить;");
        }
    }

    public boolean hasBADRT(String balanceAccountNo, String debtRemainTypeCode) {
        return refsMap.get(new BalDebtRemains(balanceAccountNo, debtRemainTypeCode)) != null;
    }

    public static boolean isValidRNN(String rnn) {
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

    /*public static List getInvalidIINs(Object list) {
        List ret = new ArrayList();
        for (Object o : (LinkedList) list) {
            String s = (String) o;
            if (!checkIIN(s))
                ret.add(s);
        }
        return ret;
    }*/

    private static int[] weights = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2};

    public static boolean checkIIN(String iin) {
        int sum = 0;
        if (iin.length() != 12)
            return false;

        if (!isDateValid(iin.substring(0, 6), "yyMMdd"))
            return false;

        if (iin.charAt(6) < '1' || iin.charAt(6) > '6')
            return false;

        for (int i = 0; i < 11; i++)
            sum += (iin.charAt(i) - '0') * weights[i];
        sum %= 11;
        int last = iin.charAt(11) - '0';
        if (sum == 10) {
            sum = 0;
            for (int i = 0; i < 11; i++)
                sum += (iin.charAt(i) - '0') * weights[i + 2];
            sum %= 11;
        }
        return sum == last;
    }

    public static int firstDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isDateValid(String date, String pattern) {
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
