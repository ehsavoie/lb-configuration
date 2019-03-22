package org.wildfly.load.balancer.server;


import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

/*
 * Copyright 2019 Emmanuel Hugonnet (c) 2018 Red Hat, inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
@MessageDriven(name = "InQueueMDB", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "/queue/inQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class InQueueMDB implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(InQueueMDB.class.toString());

    @Inject
    private JMSContext context;
    @Resource(lookup = "java:/queue/outQueue")
    private Queue queue;
    /**
     * @param rcvMessage
     * @see MessageListener#onMessage(Message)
     */
    @Override
    public void onMessage(Message rcvMessage) {
        try {
            if (rcvMessage instanceof TextMessage) {
                TextMessage msg = (TextMessage) rcvMessage;
                LOGGER.info("Received Message from queue: " + msg.getText() + " resending it to " + queue);
                context.createProducer().send(queue, msg.getText());
            } else {
                LOGGER.warning("Message of wrong type: " + rcvMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
