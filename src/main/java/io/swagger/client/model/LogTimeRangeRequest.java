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
import java.io.IOException;

/**
 * TimeRangeRequest contains the parameter to configure the query to  retrieve the number of audit events of this type for a given time range defined by last timestamp and a range type.
 */
@ApiModel(description = "TimeRangeRequest contains the parameter to configure the query to  retrieve the number of audit events of this type for a given time range defined by last timestamp and a range type.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class LogTimeRangeRequest {
  @SerializedName("MsgId")
  private String msgId = null;

  @SerializedName("TimeRangeType")
  private String timeRangeType = null;

  @SerializedName("RefTime")
  private Integer refTime = null;

  public LogTimeRangeRequest msgId(String msgId) {
    this.msgId = msgId;
    return this;
  }

   /**
   * Get msgId
   * @return msgId
  **/
  @ApiModelProperty(value = "")
  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public LogTimeRangeRequest timeRangeType(String timeRangeType) {
    this.timeRangeType = timeRangeType;
    return this;
  }

   /**
   * Get timeRangeType
   * @return timeRangeType
  **/
  @ApiModelProperty(value = "")
  public String getTimeRangeType() {
    return timeRangeType;
  }

  public void setTimeRangeType(String timeRangeType) {
    this.timeRangeType = timeRangeType;
  }

  public LogTimeRangeRequest refTime(Integer refTime) {
    this.refTime = refTime;
    return this;
  }

   /**
   * Get refTime
   * @return refTime
  **/
  @ApiModelProperty(value = "")
  public Integer getRefTime() {
    return refTime;
  }

  public void setRefTime(Integer refTime) {
    this.refTime = refTime;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LogTimeRangeRequest logTimeRangeRequest = (LogTimeRangeRequest) o;
    return Objects.equals(this.msgId, logTimeRangeRequest.msgId) &&
        Objects.equals(this.timeRangeType, logTimeRangeRequest.timeRangeType) &&
        Objects.equals(this.refTime, logTimeRangeRequest.refTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(msgId, timeRangeType, refTime);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LogTimeRangeRequest {\n");
    
    sb.append("    msgId: ").append(toIndentedString(msgId)).append("\n");
    sb.append("    timeRangeType: ").append(toIndentedString(timeRangeType)).append("\n");
    sb.append("    refTime: ").append(toIndentedString(refTime)).append("\n");
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

