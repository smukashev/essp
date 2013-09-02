package kz.bsbnb.usci.bconv.cr.parser.impl;

//import com.bsbnb.creditregistry.util.DataTypeUtil;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

//import com.bsbnb.creditregistry.ws.objects.BatchInfo;

/**
 * @author k.tulbassiyev
 */
@Component
public class PackageParser extends BatchParser {
    @Autowired
    private PrimaryContractParser primaryContractParser;

    @Autowired
    private CreditParser creditParser;

    @Autowired
    private SubjectsParser subjectsParser;

    @Autowired
    private ChangeParser changeParser;

    @Autowired
    private PledgesParser pledgesParser;
    
    private Logger logger = Logger.getLogger(PackageParser.class);

    @Override
    public void init()
    {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("ct_package"), new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {

        if(localName.equals("packages")) {
        } else if(localName.equals("package")) {
            /*currentPackage = new Package();
            currentPackage.setNo(new BigInteger(attributes.getValue("no")));
            
            if(attributes.getValue("operation_type").equals("insert")) {
                currentPackage.setOperationType(StOperationType.INSERT);
            } else if(attributes.getValue("operation_type").equals("update")) {
                currentPackage.setOperationType(StOperationType.UPDATE);
            } else {
                throw new UnknownValException(localName, attributes.getValue("operation_type"));
            } */
            currentBaseEntity.put("operation_type", new BaseValue(batch, 0,
                    event.asStartElement().getAttributeByName(new QName("operation_type")).getValue()));
            currentBaseEntity.put("no", new BaseValue(batch, 0,
                    new Double(event.asStartElement().getAttributeByName(new QName("no")).getValue())));
        } else if(localName.equals("primary_contract")) {
            primaryContractParser.parse(xmlReader, batch);
            BaseEntity primaryContract = primaryContractParser.getCurrentBaseEntity();

            currentBaseEntity.put("primary_contract", new BaseValue(batch, 0, primaryContract));
        } else if(localName.equals("credit")) {
            //attributes.getValue("credit_type")
            creditParser.parse(xmlReader, batch);
            BaseEntity credit = creditParser.getCurrentBaseEntity();
            credit.put("credit_type", new BaseValue(batch, 0,
                    event.asStartElement().getAttributeByName(new QName("credit_type")).getValue()));
            //System.out.println(credit.toString());
            currentBaseEntity.put("credit", new BaseValue(batch, 0, credit));
        } else if(localName.equals("subjects")) {
            BaseEntity subjects = new BaseEntity(metaClassRepository.getMetaClass("subject"), new Date());

            while(true) {
                subjectsParser.parse(xmlReader, batch);
                if (subjectsParser.hasMore()) {
                    BaseEntity subject = subjectsParser.getCurrentBaseEntity();
                    subjects.put(subject.getMeta().getClassName(),
                            new BaseValue(batch, 0, subject));
                } else {
                    break;
                }
            }

            currentBaseEntity.put("subjects", new BaseValue(batch, 0, subjects));
        } else if(localName.equals("pledges")) {
            pledgesParser.parse(xmlReader, batch);
        } else if(localName.equals("change")) {
            changeParser.parse(xmlReader, batch);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("packages")) {
            //waitForThreads();
            
            //batch.setPackages(packages);
            //xmlReader.setContentHandler(contentHandler);
            hasMore = false;
        } else if(localName.equals("package")) {
            //handlePackage(currentPackage);
            hasMore = true;
        } else {
            throw new UnknownTagException(localName);
        }

        return true;
    }

    /*public void handlePackage(Package p) {
        PackageJob packageJob = new PackageJob();
        packageJob.setInputInfo(inputInfo);
        packageJob.setFacadeBean(facadeBean);
        packageJob.setCreditorObject(creditorObject);
        packageJob.setHashSingleton(hashSingleton);
        packageJob.setProtocolBean(protocolBean);
        packageJob.setPack(p);       

        if(p.getCredit() != null && p.getCredit().getPortfolio() != null
                && p.getChange() != null && p.getChange().getCreditFlow() != null
                && p.getChange().getCreditFlow().getProvision() != null) {

            boolean hasProvisionError = false;

            if(!p.getCredit().getCreditType().equals("10")) {
                if(p.getCredit().getPortfolio().getPortfolio() != null &&
                        !p.getCredit().getPortfolio().getPortfolio().toString().equals("0") &&
                        p.getChange().getCreditFlow().getProvision().getValue() != null &&
                        !p.getChange().getCreditFlow().getProvision().getValue().toString().equals("0"))    {

                    protocolBean.writeMessageToProtocol(inputInfo, p.getNo(), MessageCode.INCORRECT_PROVISION,
                            ProtocolType.CREDIT, MessageType.CRITICAL_ERROR, p.getPrimaryContract().getNo(),
                            p.getPrimaryContract().getDate().getTime(),
                            "Если заполнено поле Наименование однородного портфеля по УО, не должно быть " +
                                    "провизии по УО или 0");

                    hasProvisionError = true;
                }

                if(p.getCredit().getPortfolio().getPortfolioMsfo() != null &&
                        !p.getCredit().getPortfolio().getPortfolioMsfo().toString().equals("0") &&
                        p.getChange().getCreditFlow().getProvision().getValueMsfo() != null &&
                        !p.getChange().getCreditFlow().getProvision().getValueMsfo().toString().equals("0")) {

                    protocolBean.writeMessageToProtocol(inputInfo, p.getNo(), MessageCode.INCORRECT_PROVISION,
                            ProtocolType.CREDIT, MessageType.CRITICAL_ERROR, p.getPrimaryContract().getNo(),
                            p.getPrimaryContract().getDate().getTime(),
                            "Если заполнено поле Наименование однородного портфеля по МСФО, не должно быть " +
                                    "провизии по МСФО или 0");

                    hasProvisionError = true;
                }

                if(hasProvisionError) {
                    if(p.getOperationType().value().equals(OPERATION_TYPE_INSERT)) {
                        protocolBean.writeMessageToProtocol(inputInfo, p.getNo(), MessageCode.CREDIT_INSERT_FAILURE,
                                ProtocolType.CREDIT, MessageType.INFO, p.getPrimaryContract().getNo(),
                                DataTypeUtil.convertCalendarToDate(p.getPrimaryContract().getDate()), null);
                        totalInsertFailed++;
                    } else if(p.getOperationType().value().equals(OPERATION_TYPE_UPDATE)) {
                        protocolBean.writeMessageToProtocol(inputInfo, p.getNo(), MessageCode.CREDIT_UPDATE_FAILURE,
                                ProtocolType.CREDIT, MessageType.INFO, p.getPrimaryContract().getNo(),
                                DataTypeUtil.convertCalendarToDate(p.getPrimaryContract().getDate()), null);
                        totalUpdateFailed++;
                    }

                    return;
                }
            }
        }
        
        while(true) {
            Iterator i = jobs.iterator();

            while(i.hasNext()) {
                PackageJob job = (PackageJob) i.next();
                
                if(!job.isAlive()) {
                    totalInserted += job.getTotalInserted();
                    totalUpdated += job.getTotalUpdated(); 
                    totalInsertFailed += job.getTotalInsertFailed();
                    totalUpdateFailed += job.getTotalUpdateFailed();
                    totalInsertLost += job.getTotalInsertLost();
                    totalUpdateLost += job.getTotalUpdateLost();                    
                        
                    Package pack = job.getPack();
                    
                    if(pack.getSubjects() != null && pack.getSubjects().getSubject() != null) {
                        if(pack.getCredit() != null) {
                            hashSingleton.execute(pack.getCredit().getContract(), pack.getSubjects().getSubject(),
                                    HashSingleton.OPERATION_REMOVE, pack.getNo().toString());
                        } else {
                            hashSingleton.execute(null, pack.getSubjects().getSubject(),
                                    HashSingleton.OPERATION_REMOVE, pack.getNo().toString());
                        }
                    }

                    i.remove();                   
                }
            }
            
            if(jobs.size() <= JOBS_COUNT)
                break;
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
        }
        
        jobs.add(packageJob);
        packageJob.start();
    }
    
    public void waitForThreads() {
        while(true) {
            Iterator i = jobs.iterator();

            while(i.hasNext()) {
                PackageJob job = (PackageJob) i.next();
                
                if(!job.isAlive()) {
                    totalInserted       += job.getTotalInserted();
                    totalUpdated        += job.getTotalUpdated();
                    totalInsertFailed   += job.getTotalInsertFailed();
                    totalUpdateFailed   += job.getTotalUpdateFailed();
                    totalInsertLost     += job.getTotalInsertLost();
                    totalUpdateLost     += job.getTotalUpdateLost();
                    
                    Package pack = job.getPack();
                    
                    if(pack.getSubjects() != null && pack.getSubjects().getSubject() != null)
                        if(!hashSingleton.execute(pack.getPrimaryContract(), pack.getSubjects().getSubject(),
                                HashSingleton.OPERATION_REMOVE, pack.getNo().toString()))
                            logger.error("waitForThreads: Incorrect number of documents were deleted.");

                    i.remove();
                }
            }

            if(jobs.isEmpty())
                break;

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long total = (totalInserted + totalInsertFailed + totalInsertLost + totalUpdated +
                                totalUpdateFailed + totalUpdateLost);

        List<Protocol> protocolMessages = new ArrayList<Protocol>();

        protocolMessages.add(protocolBean.createProtocolMessage(null, MessageCode.TOTAL,
                ProtocolType.FILE, MessageType.INFO, null, null, Long.toString(total)));

        protocolMessages.add(protocolBean.createProtocolMessage(null, MessageCode.TOTAL_INSERTED,
                ProtocolType.FILE, MessageType.INFO, null, null, Long.toString(totalInserted)));

        protocolMessages.add(protocolBean.createProtocolMessage(null, MessageCode.TOTAL_INSERT_FAILED,
                ProtocolType.FILE, MessageType.INFO, null, null, Long.toString(totalInsertFailed)));

        protocolMessages.add(protocolBean.createProtocolMessage(null, MessageCode.TOTAL_INSERT_LOST,
                ProtocolType.FILE, MessageType.INFO, null, null, Long.toString(totalInsertLost)));

        protocolMessages.add(protocolBean.createProtocolMessage(null, MessageCode.TOTAL_UPDATED,
                ProtocolType.FILE, MessageType.INFO, null, null, Long.toString(totalUpdated)));

        protocolMessages.add(protocolBean.createProtocolMessage(null, MessageCode.TOTAL_UPDATE_FAILED,
                ProtocolType.FILE, MessageType.INFO, null, null, Long.toString(totalUpdateFailed)));

        protocolMessages.add(protocolBean.createProtocolMessage(null, MessageCode.TOTAL_UPDATE_LOST,
                ProtocolType.FILE, MessageType.INFO, null, null, Long.toString(totalUpdateLost)));

        protocolBean.writeMessagesToProtocol(inputInfo, protocolMessages);
        
        facadeBean.updateInputInfoStatistics(inputInfo, total, totalInserted, totalUpdated, totalInsertFailed,
                totalUpdateFailed, totalInsertLost, totalUpdateLost);
    }*/
}