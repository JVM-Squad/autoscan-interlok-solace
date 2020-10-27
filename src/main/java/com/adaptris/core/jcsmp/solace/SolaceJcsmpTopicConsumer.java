package com.adaptris.core.jcsmp.solace;

import javax.validation.Valid;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.LoggingHelper;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * This implementation of {@link AdaptrisMessageConsumer} will use the Solace Jcsmp Api to consume messages from a Topic on your Solace router.
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
 * @config solace-jcsmp-topic-consumer
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component that consumes your topic messages from the Solace VPN.", tag="subscription,topic,consumer,solace,jcsmp", since="3.9.3")
@XStreamAlias("solace-jcsmp-topic-consumer")
public class SolaceJcsmpTopicConsumer extends SolaceJcsmpAbstractConsumer {

  private transient XMLMessageConsumer messageConsumer;
  private transient boolean destinationWarningLogged = false;

  /**
   * The consume destination is the topic that we receive messages from.
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @ConfigDeprecated(removalVersion = "4.0.0", message = "Use 'topic' instead", groups = Deprecated.class)
  private ConsumeDestination destination;
  /**
   * The Solace Topic
   *
   */
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String topic;

  public SolaceJcsmpTopicConsumer() {
    super();
  }

  @Override
  public void startReceive() throws Exception {
    setCurrentSession(retrieveConnection(SolaceJcsmpConnection.class).createSession());
    getCurrentSession().connect();

    setMessageConsumer(getCurrentSession().getMessageConsumer(this));

    final Topic topic = jcsmpFactory().createTopic(topicName());
    getCurrentSession().addSubscription(topic);
    getMessageConsumer().start();
  }

  @Override
  public void stop() {
    if(getMessageConsumer() != null)
      getMessageConsumer().stop();
    super.stop();
  }

  @Override
  public void close() {
    if(getMessageConsumer() != null)
      getMessageConsumer().close();
    super.close();
  }

  XMLMessageConsumer getMessageConsumer() {
    return messageConsumer;
  }

  void setMessageConsumer(XMLMessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  @Override
  public void prepare() throws CoreException {
    DestinationHelper.logConsumeDestinationWarning(destinationWarningLogged,
        () -> destinationWarningLogged = true, getDestination(),
        "{} uses destination, use 'queue' instead", LoggingHelper.friendlyName(this));
    DestinationHelper.mustHaveEither(getTopic(), getDestination());
  }

  @Override
  protected String newThreadName() {
    return DestinationHelper.threadName(retrieveAdaptrisMessageListener(), getDestination());
  }

  private String topicName() {
    return DestinationHelper.consumeDestination(getTopic(), getDestination());
  }

}
