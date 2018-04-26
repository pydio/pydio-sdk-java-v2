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
 * TreeSyncChangeNode
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class TreeSyncChangeNode {
  @SerializedName("bytesize")
  private String bytesize = null;

  @SerializedName("md5")
  private String md5 = null;

  @SerializedName("mtime")
  private String mtime = null;

  @SerializedName("nodePath")
  private String nodePath = null;

  @SerializedName("repositoryIdentifier")
  private String repositoryIdentifier = null;

  public TreeSyncChangeNode bytesize(String bytesize) {
    this.bytesize = bytesize;
    return this;
  }

   /**
   * Get bytesize
   * @return bytesize
  **/
  @ApiModelProperty(value = "")
  public String getBytesize() {
    return bytesize;
  }

  public void setBytesize(String bytesize) {
    this.bytesize = bytesize;
  }

  public TreeSyncChangeNode md5(String md5) {
    this.md5 = md5;
    return this;
  }

   /**
   * Get md5
   * @return md5
  **/
  @ApiModelProperty(value = "")
  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public TreeSyncChangeNode mtime(String mtime) {
    this.mtime = mtime;
    return this;
  }

   /**
   * Get mtime
   * @return mtime
  **/
  @ApiModelProperty(value = "")
  public String getMtime() {
    return mtime;
  }

  public void setMtime(String mtime) {
    this.mtime = mtime;
  }

  public TreeSyncChangeNode nodePath(String nodePath) {
    this.nodePath = nodePath;
    return this;
  }

   /**
   * Get nodePath
   * @return nodePath
  **/
  @ApiModelProperty(value = "")
  public String getNodePath() {
    return nodePath;
  }

  public void setNodePath(String nodePath) {
    this.nodePath = nodePath;
  }

  public TreeSyncChangeNode repositoryIdentifier(String repositoryIdentifier) {
    this.repositoryIdentifier = repositoryIdentifier;
    return this;
  }

   /**
   * Get repositoryIdentifier
   * @return repositoryIdentifier
  **/
  @ApiModelProperty(value = "")
  public String getRepositoryIdentifier() {
    return repositoryIdentifier;
  }

  public void setRepositoryIdentifier(String repositoryIdentifier) {
    this.repositoryIdentifier = repositoryIdentifier;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TreeSyncChangeNode treeSyncChangeNode = (TreeSyncChangeNode) o;
    return Objects.equals(this.bytesize, treeSyncChangeNode.bytesize) &&
        Objects.equals(this.md5, treeSyncChangeNode.md5) &&
        Objects.equals(this.mtime, treeSyncChangeNode.mtime) &&
        Objects.equals(this.nodePath, treeSyncChangeNode.nodePath) &&
        Objects.equals(this.repositoryIdentifier, treeSyncChangeNode.repositoryIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bytesize, md5, mtime, nodePath, repositoryIdentifier);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TreeSyncChangeNode {\n");
    
    sb.append("    bytesize: ").append(toIndentedString(bytesize)).append("\n");
    sb.append("    md5: ").append(toIndentedString(md5)).append("\n");
    sb.append("    mtime: ").append(toIndentedString(mtime)).append("\n");
    sb.append("    nodePath: ").append(toIndentedString(nodePath)).append("\n");
    sb.append("    repositoryIdentifier: ").append(toIndentedString(repositoryIdentifier)).append("\n");
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

