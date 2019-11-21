package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Session;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * The Interlok {@link AdaptrisConnection} that will allow us to connect to your Solace router via the JCSMP api.
 * </p>
 * <p>
 * Simply supply the host, vpn name, user and password.
 * </p>
 * @author aaron
 * @version=�3.9.3�
 * @config solace-jcsmp-connection
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP connection used to create JCSMP sessions to your Solace appliance.", tag="connection,solace,jcsmp")
@XStreamAlias("solace-jcsmp-connection")
@DisplayOrder(order = {"host", "vpn", "username", "password"})
public class SolaceJcsmpConnection extends AllowsRetriesConnection implements SolaceJcsmpSessionCreator {
  
  @NotNull
  private String host;
  
  @NotNull
  private String vpn;
  
  @NotNull
  private String username;
  
  @InputFieldHint(style = "PASSWORD", external=true)
  private String password;
  
  private transient JCSMPFactory jcsmpFactory;

  private transient Boolean additionalDebug;

  public SolaceJcsmpConnection() {
    super();
  }
  
  @Override
  public JCSMPSession createSession() throws Exception {
    return this.jcsmpFactory().createSession(this.generateJcsmpProperties());
  }
  
  @Override
  protected void prepareConnection() throws CoreException {
  }

  @Override
  protected void initConnection() throws CoreException {
    try {
      if(this.getConnectionErrorHandler() == null) {
        this.setConnectionErrorHandler(new SolaceJcsmpConnectionErrorHandler());
        this.getConnectionErrorHandler().registerConnection(this);
      }
      this.connect().closeSession();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }
  
  private Session connect() throws Exception {
    Session session = null;
    int attemptCount = 0;
    
    while (session == null) {
      try {
        attemptCount++;
        if (additionalDebug()) {
          log.trace("Attempting connection to Solace VPN [{}] at [{}]", this.getVpn(), this.getHost());
        }
        session = this.createSession();
      }
      catch (Exception e) {
        if (attemptCount == 1) {
          if (logWarning(attemptCount)) {
            log.warn("Connection attempt [{}] failed for Solace VPN [{}] at [{}]", attemptCount, this.getVpn(), this.getHost(), e);
          }
        }

        if (connectionAttempts() != -1 && attemptCount >= connectionAttempts()) {
          log.error("Failed to connect to Solace VPN [{}] at [{}]", this.getVpn(), this.getHost(), e);
          throw e;
        }
        else {
          log.warn("Attempt [{}] failed for Solace VPN [{}] at [{}], retrying", attemptCount, this.getVpn(), this.getHost());
          log.info(createLoggingStatement(attemptCount));
          Thread.sleep(connectionRetryInterval());
          continue;
        }
      }
    }
    return session;
  }

  @Override
  protected void startConnection() throws CoreException {
  }

  @Override
  protected void stopConnection() {
  }

  @Override
  protected void closeConnection() { 
  }
  
  private JCSMPProperties generateJcsmpProperties() throws PasswordException {
    JCSMPProperties properties = new JCSMPProperties();
    properties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME, JCSMPProperties.AUTHENTICATION_SCHEME_BASIC);
    properties.setProperty(JCSMPProperties.HOST, this.getHost());
    properties.setProperty(JCSMPProperties.VPN_NAME, this.getVpn());
    properties.setProperty(JCSMPProperties.USERNAME, this.getUsername());
    properties.setProperty(JCSMPProperties.PASSWORD, Password.decode(ExternalResolver.resolve(this.getPassword())));
    
    return properties;
  }

  public String getHost() {
    return host;
  }

  /**
   * The tcp address to your Solace router, such as "tcp://localhost:55555".
   * @param host
   */
  public void setHost(String host) {
    this.host = host;
  }

  public String getVpn() {
    return vpn;
  }

  /**
   * The Solave VPN name, such as "default".
   * @param vpn
   */
  public void setVpn(String vpn) {
    this.vpn = vpn;
  }

  public String getUsername() {
    return username;
  }

  /**
   * Your Solace username.
   * @param username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  /**
   * Your Solace password (supports Interlok encoding/decoding).
   * @param password
   */
  public void setPassword(String password) {
    this.password = password;
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
  
  public Boolean getAdditionalDebug() {
    return additionalDebug;
  }

  /**
   * Whether or not to generate additional TRACE level debug when attempting connections.
   * 
   * @param b true to enable additional logging; default false.
   */
  public void setAdditionalDebug(Boolean b) {
    additionalDebug = b;
  }

  protected boolean additionalDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
  }

}
