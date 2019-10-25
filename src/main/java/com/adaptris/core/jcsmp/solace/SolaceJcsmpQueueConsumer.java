package com.adaptris.core.jcsmp.solace;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreException;
import com.adaptris.util.NumberUtils;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;

public class SolaceJcsmpQueueConsumer extends AdaptrisMessageConsumerImp implements SolaceJcsmpReceiverStarter {

  private static final int DEFAULT_MAX_THREADS = 10;
  
  @NotNull
  private String queueName;
  
  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  @NotNull
  @AutoPopulated
  @InputFieldDefault(value = "10")
  private Integer maxThreads;
  
  private transient SolaceJcsmpMessageAcker messageAcker;
  
  private transient JCSMPFactory jcsmpFactory;
  
  private transient JCSMPSession currentSession;
  
  private transient FlowReceiver flowReceiver;
  
  private transient ExecutorService executorService;
  
  public SolaceJcsmpQueueConsumer() {
    this.setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
  }

  @Override
  public void onException(JCSMPException exception) {
    // Assumes a connection error handler...
    log.error("Exception received from the JCSMP consumer, firing connection error handler.", exception);
    this.retrieveConnection(SolaceJcsmpConnection.class).getConnectionErrorHandler().handleConnectionException();
  }

  @Override
  public void onReceive(BytesXMLMessage message) {
    this.getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        try {
          AdaptrisMessage adaptrisMessage = getMessageTranslator().translate(message);
          getMessageAcker().addUnacknowledgedMessage(message, adaptrisMessage.getUniqueId());
          
          retrieveAdaptrisMessageListener().onAdaptrisMessage(adaptrisMessage);
        } catch (Exception e) {
          log.error("Failed to translate message.", e);
        }
      }
    });
  }

  @Override
  public void startReceive() throws Exception {
    this.setCurrentSession(retrieveConnection(SolaceJcsmpConnection.class).createSession());
    
    final Queue queue = this.jcsmpFactory().createQueue(this.getQueueName());
    // Actually provision it, and do not fail if it already exists
    this.getCurrentSession().provision(queue, createEndpointProperties(), JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
    this.setFlowReceiver(this.getCurrentSession().createFlow(this, createConsumerFlowProperties(queue), createEndpointProperties()));
    
    this.getFlowReceiver().start();
  }
  
  @Override
  public void init() throws CoreException {
    this.setExecutorService(new ThreadPoolExecutor(1, maxThreads(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(maxThreads(), true)));
  }
  
  @Override
  public void stop() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().stop();
  }

  @Override
  public void close() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().close();
  }
  
  @Override
  public void prepare() throws CoreException { 
  }

  private ConsumerFlowProperties createConsumerFlowProperties(Queue queue) {
    final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
    flowProps.setEndpoint(queue);
    flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
    
    return flowProps;
  }
  
  private EndpointProperties createEndpointProperties() {
    final EndpointProperties endpointProps = new EndpointProperties();
    // set queue permissions to "consume" and access-type to "exclusive"
    endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
    endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
    
    return endpointProps;
  }
  
  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(this.getJcsmpFactory(), JCSMPFactory.onlyInstance());
  }
  
  JCSMPFactory getJcsmpFactory() {
    return jcsmpFactory;
  }

  void setJcsmpFactory(JCSMPFactory jcsmpFactory) {
    this.jcsmpFactory = jcsmpFactory;
  }

  JCSMPSession getCurrentSession() {
    return currentSession;
  }

  void setCurrentSession(JCSMPSession currentSession) {
    this.currentSession = currentSession;
  }

  FlowReceiver getFlowReceiver() {
    return flowReceiver;
  }

  void setFlowReceiver(FlowReceiver flowReceiver) {
    this.flowReceiver = flowReceiver;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public SolaceJcsmpMessageTranslator getMessageTranslator() {
    return messageTranslator;
  }

  public void setMessageTranslator(SolaceJcsmpMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }
  
  int maxThreads() {
    return NumberUtils.toIntDefaultIfNull(this.getMaxThreads(), DEFAULT_MAX_THREADS);
  }

  public Integer getMaxThreads() {
    return maxThreads;
  }

  public void setMaxThreads(Integer maxThreads) {
    this.maxThreads = maxThreads;
  }

  ExecutorService getExecutorService() {
    return executorService;
  }

  void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  SolaceJcsmpMessageAcker getMessageAcker() {
    return messageAcker;
  }

  void setMessageAcker(SolaceJcsmpMessageAcker messageAcker) {
    this.messageAcker = messageAcker;
  }

}
