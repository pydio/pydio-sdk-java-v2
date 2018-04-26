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
import io.swagger.client.model.TreeSyncChange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RestChangeCollection
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class RestChangeCollection {
  @SerializedName("Changes")
  private List<TreeSyncChange> changes = null;

  @SerializedName("LastSeqId")
  private String lastSeqId = null;

  public RestChangeCollection changes(List<TreeSyncChange> changes) {
    this.changes = changes;
    return this;
  }

  public RestChangeCollection addChangesItem(TreeSyncChange changesItem) {
    if (this.changes == null) {
      this.changes = new ArrayList<TreeSyncChange>();
    }
    this.changes.add(changesItem);
    return this;
  }

   /**
   * Get changes
   * @return changes
  **/
  @ApiModelProperty(value = "")
  public List<TreeSyncChange> getChanges() {
    return changes;
  }

  public void setChanges(List<TreeSyncChange> changes) {
    this.changes = changes;
  }

  public RestChangeCollection lastSeqId(String lastSeqId) {
    this.lastSeqId = lastSeqId;
    return this;
  }

   /**
   * Get lastSeqId
   * @return lastSeqId
  **/
  @ApiModelProperty(value = "")
  public String getLastSeqId() {
    return lastSeqId;
  }

  public void setLastSeqId(String lastSeqId) {
    this.lastSeqId = lastSeqId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestChangeCollection restChangeCollection = (RestChangeCollection) o;
    return Objects.equals(this.changes, restChangeCollection.changes) &&
        Objects.equals(this.lastSeqId, restChangeCollection.lastSeqId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(changes, lastSeqId);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestChangeCollection {\n");
    
    sb.append("    changes: ").append(toIndentedString(changes)).append("\n");
    sb.append("    lastSeqId: ").append(toIndentedString(lastSeqId)).append("\n");
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

