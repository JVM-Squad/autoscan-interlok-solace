package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Used with the message translator to move headers and metedata between the Solace and Adaptris messages.
 * </p>
 * <p>
 * When translating from Solace to Adaptris messages, the header keys will be used to generate a getter call.
 * For example, if we set the headerKey to "MessageId" and the metadataKey to "message-id" then Interlok will call the getter <code>getMessageId()</code>,
 * the value for which will be copied into the Adaptris Message metadata item with the key "message-id".
 * </p>
 * <p>
 * When translating from Adaptris to Solace messages, the header keys will be used to generate a setter call.
 * For example, if we set the headerKey to "MessageId" and the metadataKey to "message-id" then Interlok will call the setter <code>setMessageId(value)</code>,
 * the value for which will come from the Adaptris Message metadata item with the kay "message-id".
 * </p>
 * @author aaron
 * @config solace-jcsmp-metadata-mapping
 */
@AdapterComponent
@ComponentProfile(summary="Used with the message translator to map Solace headers to Interlok metadata and the reverse.", tag="solace,jcsmp", since="3.9.3")
@XStreamAlias("solace-jcsmp-metadata-mapping")
public class SolaceJcsmpMetadataMapping {
  
  /**
   * Set the key of the Adaptris message metadata item.
   * @param metadataKey
   */
  @NotBlank
  @Getter
  @Setter
  private String metadataKey;
  
  /**
   * Set the key of the Solace message header item.
   * This will match the getter/setter of the property name of the Solace BytesXmlMessage.
   * @param headerKey
   */
  @Getter
  @Setter
  @NotBlank
  private String headerKey;
  
  /**
   * If left null, it is assumed that the Solace property value is a String, otherwise specify
   * "Integer / Boolean / Long"
   * @param dataType
   */
  @Getter
  @Setter
  @NotBlank
  @AutoPopulated
  private String dataType;
  
  public SolaceJcsmpMetadataMapping() {
  }
  
  public SolaceJcsmpMetadataMapping(String metadataKey, String headerKey) {
    this(metadataKey, headerKey, null);
  }
  
  public SolaceJcsmpMetadataMapping(String metadataKey, String headerKey, String dataType) {
    this();
    this.setMetadataKey(metadataKey);
    this.setHeaderKey(headerKey);
    this.setDataType(dataType);
  }

}
