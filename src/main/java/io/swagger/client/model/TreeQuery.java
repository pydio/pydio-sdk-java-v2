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
import io.swagger.client.model.TreeGeoQuery;
import io.swagger.client.model.TreeNodeType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TreeQuery
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class TreeQuery {
  @SerializedName("PathPrefix")
  private List<String> pathPrefix = null;

  @SerializedName("MinSize")
  private String minSize = null;

  @SerializedName("MaxSize")
  private String maxSize = null;

  @SerializedName("MinDate")
  private String minDate = null;

  @SerializedName("MaxDate")
  private String maxDate = null;

  @SerializedName("Type")
  private TreeNodeType type = null;

  @SerializedName("FileName")
  private String fileName = null;

  @SerializedName("Content")
  private String content = null;

  @SerializedName("FreeString")
  private String freeString = null;

  @SerializedName("Extension")
  private String extension = null;

  @SerializedName("GeoQuery")
  private TreeGeoQuery geoQuery = null;

  public TreeQuery pathPrefix(List<String> pathPrefix) {
    this.pathPrefix = pathPrefix;
    return this;
  }

  public TreeQuery addPathPrefixItem(String pathPrefixItem) {
    if (this.pathPrefix == null) {
      this.pathPrefix = new ArrayList<String>();
    }
    this.pathPrefix.add(pathPrefixItem);
    return this;
  }

   /**
   * Get pathPrefix
   * @return pathPrefix
  **/
  @ApiModelProperty(value = "")
  public List<String> getPathPrefix() {
    return pathPrefix;
  }

  public void setPathPrefix(List<String> pathPrefix) {
    this.pathPrefix = pathPrefix;
  }

  public TreeQuery minSize(String minSize) {
    this.minSize = minSize;
    return this;
  }

   /**
   * Get minSize
   * @return minSize
  **/
  @ApiModelProperty(value = "")
  public String getMinSize() {
    return minSize;
  }

  public void setMinSize(String minSize) {
    this.minSize = minSize;
  }

  public TreeQuery maxSize(String maxSize) {
    this.maxSize = maxSize;
    return this;
  }

   /**
   * Get maxSize
   * @return maxSize
  **/
  @ApiModelProperty(value = "")
  public String getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(String maxSize) {
    this.maxSize = maxSize;
  }

  public TreeQuery minDate(String minDate) {
    this.minDate = minDate;
    return this;
  }

   /**
   * Get minDate
   * @return minDate
  **/
  @ApiModelProperty(value = "")
  public String getMinDate() {
    return minDate;
  }

  public void setMinDate(String minDate) {
    this.minDate = minDate;
  }

  public TreeQuery maxDate(String maxDate) {
    this.maxDate = maxDate;
    return this;
  }

   /**
   * Get maxDate
   * @return maxDate
  **/
  @ApiModelProperty(value = "")
  public String getMaxDate() {
    return maxDate;
  }

  public void setMaxDate(String maxDate) {
    this.maxDate = maxDate;
  }

  public TreeQuery type(TreeNodeType type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")
  public TreeNodeType getType() {
    return type;
  }

  public void setType(TreeNodeType type) {
    this.type = type;
  }

  public TreeQuery fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

   /**
   * Get fileName
   * @return fileName
  **/
  @ApiModelProperty(value = "")
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public TreeQuery content(String content) {
    this.content = content;
    return this;
  }

   /**
   * Get content
   * @return content
  **/
  @ApiModelProperty(value = "")
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public TreeQuery freeString(String freeString) {
    this.freeString = freeString;
    return this;
  }

   /**
   * Get freeString
   * @return freeString
  **/
  @ApiModelProperty(value = "")
  public String getFreeString() {
    return freeString;
  }

  public void setFreeString(String freeString) {
    this.freeString = freeString;
  }

  public TreeQuery extension(String extension) {
    this.extension = extension;
    return this;
  }

   /**
   * Get extension
   * @return extension
  **/
  @ApiModelProperty(value = "")
  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public TreeQuery geoQuery(TreeGeoQuery geoQuery) {
    this.geoQuery = geoQuery;
    return this;
  }

   /**
   * Get geoQuery
   * @return geoQuery
  **/
  @ApiModelProperty(value = "")
  public TreeGeoQuery getGeoQuery() {
    return geoQuery;
  }

  public void setGeoQuery(TreeGeoQuery geoQuery) {
    this.geoQuery = geoQuery;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TreeQuery treeQuery = (TreeQuery) o;
    return Objects.equals(this.pathPrefix, treeQuery.pathPrefix) &&
        Objects.equals(this.minSize, treeQuery.minSize) &&
        Objects.equals(this.maxSize, treeQuery.maxSize) &&
        Objects.equals(this.minDate, treeQuery.minDate) &&
        Objects.equals(this.maxDate, treeQuery.maxDate) &&
        Objects.equals(this.type, treeQuery.type) &&
        Objects.equals(this.fileName, treeQuery.fileName) &&
        Objects.equals(this.content, treeQuery.content) &&
        Objects.equals(this.freeString, treeQuery.freeString) &&
        Objects.equals(this.extension, treeQuery.extension) &&
        Objects.equals(this.geoQuery, treeQuery.geoQuery);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pathPrefix, minSize, maxSize, minDate, maxDate, type, fileName, content, freeString, extension, geoQuery);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TreeQuery {\n");
    
    sb.append("    pathPrefix: ").append(toIndentedString(pathPrefix)).append("\n");
    sb.append("    minSize: ").append(toIndentedString(minSize)).append("\n");
    sb.append("    maxSize: ").append(toIndentedString(maxSize)).append("\n");
    sb.append("    minDate: ").append(toIndentedString(minDate)).append("\n");
    sb.append("    maxDate: ").append(toIndentedString(maxDate)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    freeString: ").append(toIndentedString(freeString)).append("\n");
    sb.append("    extension: ").append(toIndentedString(extension)).append("\n");
    sb.append("    geoQuery: ").append(toIndentedString(geoQuery)).append("\n");
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
