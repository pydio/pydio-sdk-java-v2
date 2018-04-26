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
 * RestChangeRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class RestChangeRequest {
  @SerializedName("SeqID")
  private String seqID = null;

  @SerializedName("filter")
  private String filter = null;

  @SerializedName("flatten")
  private Boolean flatten = null;

  @SerializedName("stream")
  private Boolean stream = null;

  public RestChangeRequest seqID(String seqID) {
    this.seqID = seqID;
    return this;
  }

   /**
   * Get seqID
   * @return seqID
  **/
  @ApiModelProperty(value = "")
  public String getSeqID() {
    return seqID;
  }

  public void setSeqID(String seqID) {
    this.seqID = seqID;
  }

  public RestChangeRequest filter(String filter) {
    this.filter = filter;
    return this;
  }

   /**
   * Get filter
   * @return filter
  **/
  @ApiModelProperty(value = "")
  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public RestChangeRequest flatten(Boolean flatten) {
    this.flatten = flatten;
    return this;
  }

   /**
   * Get flatten
   * @return flatten
  **/
  @ApiModelProperty(value = "")
  public Boolean isFlatten() {
    return flatten;
  }

  public void setFlatten(Boolean flatten) {
    this.flatten = flatten;
  }

  public RestChangeRequest stream(Boolean stream) {
    this.stream = stream;
    return this;
  }

   /**
   * Get stream
   * @return stream
  **/
  @ApiModelProperty(value = "")
  public Boolean isStream() {
    return stream;
  }

  public void setStream(Boolean stream) {
    this.stream = stream;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestChangeRequest restChangeRequest = (RestChangeRequest) o;
    return Objects.equals(this.seqID, restChangeRequest.seqID) &&
        Objects.equals(this.filter, restChangeRequest.filter) &&
        Objects.equals(this.flatten, restChangeRequest.flatten) &&
        Objects.equals(this.stream, restChangeRequest.stream);
  }

  @Override
  public int hashCode() {
    return Objects.hash(seqID, filter, flatten, stream);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestChangeRequest {\n");
    
    sb.append("    seqID: ").append(toIndentedString(seqID)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
    sb.append("    flatten: ").append(toIndentedString(flatten)).append("\n");
    sb.append("    stream: ").append(toIndentedString(stream)).append("\n");
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

