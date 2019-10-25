package com.adaptris.core.jcsmp.solace;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.Channel;
import com.adaptris.core.util.LifecycleHelper;

public class SolaceJcsmpWorkflowTest {

  private Channel mockChannel;
  
  private SolaceJcsmpWorkflow workflow;
  
  @Mock private AdaptrisMessageConsumer mockConsumer;
  
  @Mock private SolaceJcsmpQueueConsumer mockJcsmpConsumer;
  
  @Mock private AdaptrisMessageProducer mockProducer;
  
  @Mock private SolaceJcsmpMessageAcker mockMessageAcker;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    mockChannel = new Channel();
    
    workflow = new SolaceJcsmpWorkflow();
    workflow.setConsumer(mockConsumer);
    workflow.setProducer(mockProducer);
    workflow.setMessageAcker(mockMessageAcker);
    
    mockChannel.getWorkflowList().add(workflow);
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(mockChannel);
  }
  
  @Test
  public void testPrepareWithJcsmpConsumer() throws Exception {
    workflow.setConsumer(mockJcsmpConsumer);
    LifecycleHelper.prepare(mockChannel);
    
    verify(mockJcsmpConsumer).prepare();
    verify(mockProducer).prepare();
    verify(mockMessageAcker).prepare();
    verify(mockJcsmpConsumer).setMessageAcker(mockMessageAcker);
  }
  
  @Test
  public void testInit() throws Exception {
    LifecycleHelper.init(mockChannel);
    
    verify(mockConsumer).init();
    verify(mockProducer).init();
    verify(mockMessageAcker).init();
  }
  
  @Test
  public void testStart() throws Exception {
    LifecycleHelper.start(mockChannel);
    
    verify(mockConsumer).start();
    verify(mockProducer).start();
    verify(mockMessageAcker).start();
  }
  
  @Test
  public void testStop() throws Exception {
    LifecycleHelper.initAndStart(mockChannel);
    LifecycleHelper.stop(mockChannel);
    
    verify(mockConsumer, atLeast(1)).stop();
    verify(mockProducer, atLeast(1)).stop();
    verify(mockMessageAcker, atLeast(1)).stop();
  }
  
  @Test
  public void testClose() throws Exception {
    LifecycleHelper.initAndStart(mockChannel);
    LifecycleHelper.stop(mockChannel);
    LifecycleHelper.close(mockChannel);
    
    verify(mockConsumer, atLeast(1)).close();
    verify(mockProducer, atLeast(1)).close();
    verify(mockMessageAcker, atLeast(1)).close();
  }
  
}
