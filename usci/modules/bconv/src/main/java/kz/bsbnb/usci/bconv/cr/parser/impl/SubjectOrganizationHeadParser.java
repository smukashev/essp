package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class SubjectOrganizationHeadParser extends BatchParser {
    @Autowired
    private SubjectOrganizationHeadNamesParser subjectOrganizationHeadNamesParser;

    @Autowired
    private SubjectOrganizationHeadDocsParser subjectOrganizationHeadDocsParser;
        
    public SubjectOrganizationHeadParser() {
        super();
    }

    @Override
    public void init(){
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("head"),new Date());
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("head")) {
        } else if(localName.equals("names")) {
            BaseSet headNames = new BaseSet(metaClassRepository.getMetaClass("name2"));
            while(true){
               subjectOrganizationHeadNamesParser.parse(xmlReader,batch,index);
               if(subjectOrganizationHeadNamesParser.hasMore()){
                    headNames.put(new BaseValue(batch,index, subjectOrganizationHeadNamesParser.getCurrentBaseEntity()));
               } else break;
            }
            currentBaseEntity.put("names", new BaseValue(batch, index, headNames));
        } else if(localName.equals("docs")) {
            //subjectOrganizationHeadDocsParser.parse(xmlReader, batch, index);

            //my code
            BaseSet docs = new BaseSet(metaClassRepository.getMetaClass("doc2"));
            while(true){
                subjectOrganizationHeadDocsParser.parse(xmlReader,batch,index);
                if(subjectOrganizationHeadDocsParser.hasMore()){
                    docs.put(new BaseValue(batch,index,subjectOrganizationHeadDocsParser.getCurrentBaseEntity()));
                } else break;
            }
            currentBaseEntity.put("docs", new BaseValue(batch,index,docs));

        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("head")) {
            //ctOrganization.setHead(ctPersonHead);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("names")) {
        } else if(localName.equals("docs")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}
