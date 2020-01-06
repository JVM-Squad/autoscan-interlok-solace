package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of {@link AdaptrisMessageConsumer} will use the Solace Jcsmp Api to consume messages from a Queue on your Solace router.
 * </p>
 * <p>
 * There are four main components that you will need to configure;
 * <ul>
 * <li><b>Destination: </b> The Solace end point to consume messages from.</li>
 * <li><b>End point permissions: </b> Should match the Solace configured end point properties.</li>
 * <li><b>End point access type: </b> Should match either EXCLUSIVE or NONEXCLUSIVE</li>
 * <li><b>Acknowledge mode: </b> Should either be CLIENT or AUTO.</li>
 * </ul>
 * </p>
 * @author aaron
 * @config solace-jcsmp-queue-consumer
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component that consumes your messages from the Solace VPN.", tag="queue,consumer,solace,jcsmp", since="3.9.3")
@XStreamAlias("solace-jcsmp-queue-consumer")
public class SolaceJcsmpQueueConsumer extends SolaceJcsmpAbstractConsumer {
  
  private transient FlowReceiver flowReceiver;
  
  public SolaceJcsmpQueueConsumer() {
    super();
  }

  @Override
  public void startReceive() throws Exception {
    this.setCurrentSession(retrieveConnection(SolaceJcsmpConnection.class).createSession());
    
    final Queue queue = this.jcsmpFactory().createQueue(this.getDestination().getDestination());
    // Actually provision it, and do not fail if it already exists
    this.getCurrentSession().provision(queue, createEndpointProperties(), JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
    this.setFlowReceiver(this.getCurrentSession().createFlow(this, createConsumerFlowProperties(queue), createEndpointProperties()));
        
    this.getFlowReceiver().start();
  }
  
  @Override
  public void stop() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().stop();
    super.stop();
  }

  @Override
  public void close() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().close();
    super.close();
  }

  FlowReceiver getFlowReceiver() {
    return flowReceiver;
  }

  void setFlowReceiver(FlowReceiver flowReceiver) {
    this.flowReceiver = flowReceiver;
  }

}
