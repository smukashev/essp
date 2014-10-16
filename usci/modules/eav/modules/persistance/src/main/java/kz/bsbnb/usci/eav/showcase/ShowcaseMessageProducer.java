package kz.bsbnb.usci.eav.showcase;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 * Created by almaz on 6/24/14.
 */

@Component
public class ShowcaseMessageProducer {

    final static Logger logger = Logger.getLogger(ShowcaseMessageProducer.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    public void produce(final QueueEntry queueEntry) throws Exception {

        if (jmsTemplate != null) {
            MessageCreator mc = new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    try {
                        ObjectMessage message = session.createObjectMessage(queueEntry);
                        return message;
                    }
                    catch (JMSException je) {
                        logger.error("JMS Exception : ", je);
                        return null;
                    }
                }
            };
            jmsTemplate.send(mc);
        }
    }
}
