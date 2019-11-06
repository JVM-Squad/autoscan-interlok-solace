package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.CoreException;
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component used to acknowledge consumed messages, based on produced message events.", tag="ack,solace,jcsmp")
@XStreamAlias("solace-jcsmp-bridge-message-acker")
public class SolaceJcsmpBridgeMessageAcker extends SolaceJcsmpBaseMessageAcker implements JCSMPStreamingPublishCorrelatingEventHandler {

  public SolaceJcsmpBridgeMessageAcker() {
  }
  
  @Override
  public void responseReceivedEx(Object messageId) {
    Timer.start("OnReceive", "Ack", 1000);
    super.acknowledge((String) messageId);
    Timer.stop("OnReceive", "Ack");
  }
  
  @Override
  public void handleErrorEx(Object messageId, JCSMPException exception, long timestamp) {
    log.error("Received producer callback error from Solace for message [{}]", messageId, exception);
  }

  @Override
  public void prepare() throws CoreException {
    SolaceJcsmpWorkflow workflow = this.getParentWorkflow();
    if(workflow.getProducer() instanceof SolaceJcsmpQueueProducer) {
      SolaceJcsmpQueueProducer producer = (SolaceJcsmpQueueProducer) workflow.getProducer();
      producer.setProducerEventHandler(this);
    } else {
      throw new CoreException("Your workflow producer needs to be a Solace JCSMP producer to use Bridge mode acknowledgements.");
    }
  }
  
  @Override
  public void responseReceived(String messageId) {
    // never called - deprecated
  }
  
  @Override
  public void handleError(String messageId, JCSMPException exception, long timestamp) {
    // never called - deprecated.
  }

}
