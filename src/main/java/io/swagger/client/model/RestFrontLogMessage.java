/*
 * Pydio Cells Rest API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package io.swagger.client.model;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.client.model.RestLogLevel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RestFrontLogMessage
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class RestFrontLogMessage {
  @SerializedName("Level")
  private RestLogLevel level = null;

  @SerializedName("Ip")
  private String ip = null;

  @SerializedName("UserId")
  private String userId = null;

  @SerializedName("WorkspaceId")
  private String workspaceId = null;

  @SerializedName("Source")
  private String source = null;

  @SerializedName("Prefix")
  private String prefix = null;

  @SerializedName("Message")
  private String message = null;

  @SerializedName("Nodes")
  private List<String> nodes = null;

  public RestFrontLogMessage level(RestLogLevel level) {
    this.level = level;
    return this;
  }

   /**
   * Get level
   * @return level
  **/
  @ApiModelProperty(value = "")
  public RestLogLevel getLevel() {
    return level;
  }

  public void setLevel(RestLogLevel level) {
    this.level = level;
  }

  public RestFrontLogMessage ip(String ip) {
    this.ip = ip;
    return this;
  }

   /**
   * Get ip
   * @return ip
  **/
  @ApiModelProperty(value = "")
  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public RestFrontLogMessage userId(String userId) {
    this.userId = userId;
    return this;
  }

   /**
   * Get userId
   * @return userId
  **/
  @ApiModelProperty(value = "")
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public RestFrontLogMessage workspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
    return this;
  }

   /**
   * Get workspaceId
   * @return workspaceId
  **/
  @ApiModelProperty(value = "")
  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public RestFrontLogMessage source(String source) {
    this.source = source;
    return this;
  }

   /**
   * Get source
   * @return source
  **/
  @ApiModelProperty(value = "")
  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public RestFrontLogMessage prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

   /**
   * Get prefix
   * @return prefix
  **/
  @ApiModelProperty(value = "")
  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public RestFrontLogMessage message(String message) {
    this.message = message;
    return this;
  }

   /**
   * Get message
   * @return message
  **/
  @ApiModelProperty(value = "")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public RestFrontLogMessage nodes(List<String> nodes) {
    this.nodes = nodes;
    return this;
  }

  public RestFrontLogMessage addNodesItem(String nodesItem) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<String>();
    }
    this.nodes.add(nodesItem);
    return this;
  }

   /**
   * Get nodes
   * @return nodes
  **/
  @ApiModelProperty(value = "")
  public List<String> getNodes() {
    return nodes;
  }

  public void setNodes(List<String> nodes) {
    this.nodes = nodes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestFrontLogMessage restFrontLogMessage = (RestFrontLogMessage) o;
    return Objects.equals(this.level, restFrontLogMessage.level) &&
        Objects.equals(this.ip, restFrontLogMessage.ip) &&
        Objects.equals(this.userId, restFrontLogMessage.userId) &&
        Objects.equals(this.workspaceId, restFrontLogMessage.workspaceId) &&
        Objects.equals(this.source, restFrontLogMessage.source) &&
        Objects.equals(this.prefix, restFrontLogMessage.prefix) &&
        Objects.equals(this.message, restFrontLogMessage.message) &&
        Objects.equals(this.nodes, restFrontLogMessage.nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(level, ip, userId, workspaceId, source, prefix, message, nodes);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestFrontLogMessage {\n");
    
    sb.append("    level: ").append(toIndentedString(level)).append("\n");
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    prefix: ").append(toIndentedString(prefix)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    nodes: ").append(toIndentedString(nodes)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

