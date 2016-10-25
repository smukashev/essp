package kz.bsbnb.usci.receiver.reader.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import java.util.Date;

/**
 * Created by bauka on 9/22/16.
 */
@Component
public class ParserBuilder<T> {

    @Autowired
    ApplicationContext context;

    public CommonParser<T> getParser(String xml, Date reportDate, long creditorId) throws XMLStreamException {
        CommonParser<T> parser = context.getBean(CommonParser.class);

        parser.setXml(xml);
        parser.setReportDate(reportDate);
        parser.setCreditorId(creditorId);
        return parser;
    }
}
