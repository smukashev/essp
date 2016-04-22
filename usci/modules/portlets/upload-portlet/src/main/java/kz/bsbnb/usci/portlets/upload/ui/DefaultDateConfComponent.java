package kz.bsbnb.usci.portlets.upload.ui;

import com.vaadin.ui.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
import kz.bsbnb.usci.portlets.upload.data.DataProvider;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by bauka on 4/11/16.
 */
public class DefaultDateConfComponent extends VerticalLayout {

    private final PortletEnvironmentFacade env;
    private final DataProvider provider;


    Logger logger = Logger.getLogger(DefaultDateConfComponent.class);

    private DateField reportDateField;

    public DefaultDateConfComponent(PortletEnvironmentFacade env, DataProvider provider) {
        this.env = env;
        this.provider = provider;
    }

    @Override
    public void attach() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        List<Creditor> creditors = provider.getOrganizations();
        final Map<Long, String> firstDates = provider.getOrganizationFirstDates();

        //final List<DateField> dateFields = new LinkedList<>();
        final Map<Long, DateField> dateFields = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        for (Creditor creditor : creditors) {
            try {
                reportDateField = new DateField(creditor.getName(), sdf.parse(firstDates.get(creditor.getId())));
                dateFields.put(creditor.getId(), reportDateField);
                reportDateField.setDateFormat("dd.MM.yyyy");
                addComponent(reportDateField);
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }

        addComponent(new Button("Сохранить", new Button.ClickListener() {


            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    Date getDefaultDate = provider.getDefaultDate();
                    StringBuffer firstDateString = new StringBuffer();

                    for (DateField dateField : dateFields.values()) {
                        if (!sdf.format(((Date) dateField.getValue())).startsWith("01")) {
                            getWindow().showNotification("ERROR", dateField.getCaption() + "- " + sdf.format((Date) dateField.getValue()) + " не является первой датой");
                            break;
                        }
                    }

                    String separator = "";

                    for (Long cid : dateFields.keySet()) {
                        Date date = (Date) dateFields.get(cid).getValue();
                        if (!date.equals(getDefaultDate)) {
                            firstDateString.append(separator);
                            separator = ",";
                            firstDateString.append(cid + "=" + sdf.format(date));
                        }
                    }

                    provider.saveOrganizationFirstDates(firstDateString.toString());

                    getWindow().showNotification("WARN", "Сохранено успешно");
                } catch (Exception e) {
                    getWindow().showNotification("ERROR","Ошибка " + e.getMessage());
                }
            }
        }));

    }

}
