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


package com.pydio.sdk.core.api.cells.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * ServiceQuery
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-11-30T14:51:15.861Z")
public class ServiceQuery {
  @SerializedName("SubQueries")
  private List<ProtobufAny> subQueries = null;

  @SerializedName("Operation")
  private ServiceOperationType operation = null;

  @SerializedName("ResourcePolicyQuery")
  private ServiceResourcePolicyQuery resourcePolicyQuery = null;

  @SerializedName("Offset")
  private String offset = null;

  @SerializedName("Limit")
  private String limit = null;

  @SerializedName("groupBy")
  private Integer groupBy = null;

  public ServiceQuery subQueries(List<ProtobufAny> subQueries) {
    this.subQueries = subQueries;
    return this;
  }

  public ServiceQuery addSubQueriesItem(ProtobufAny subQueriesItem) {
    if (this.subQueries == null) {
      this.subQueries = new ArrayList<ProtobufAny>();
    }
    this.subQueries.add(subQueriesItem);
    return this;
  }

   /**
   * Get subQueries
   * @return subQueries
  **/
  @ApiModelProperty(value = "")
  public List<ProtobufAny> getSubQueries() {
    return subQueries;
  }

  public void setSubQueries(List<ProtobufAny> subQueries) {
    this.subQueries = subQueries;
  }

  public ServiceQuery operation(ServiceOperationType operation) {
    this.operation = operation;
    return this;
  }

   /**
   * Get operation
   * @return operation
  **/
  @ApiModelProperty(value = "")
  public ServiceOperationType getOperation() {
    return operation;
  }

  public void setOperation(ServiceOperationType operation) {
    this.operation = operation;
  }

  public ServiceQuery resourcePolicyQuery(ServiceResourcePolicyQuery resourcePolicyQuery) {
    this.resourcePolicyQuery = resourcePolicyQuery;
    return this;
  }

   /**
   * Get resourcePolicyQuery
   * @return resourcePolicyQuery
  **/
  @ApiModelProperty(value = "")
  public ServiceResourcePolicyQuery getResourcePolicyQuery() {
    return resourcePolicyQuery;
  }

  public void setResourcePolicyQuery(ServiceResourcePolicyQuery resourcePolicyQuery) {
    this.resourcePolicyQuery = resourcePolicyQuery;
  }

  public ServiceQuery offset(String offset) {
    this.offset = offset;
    return this;
  }

   /**
   * Get offset
   * @return offset
  **/
  @ApiModelProperty(value = "")
  public String getOffset() {
    return offset;
  }

  public void setOffset(String offset) {
    this.offset = offset;
  }

  public ServiceQuery limit(String limit) {
    this.limit = limit;
    return this;
  }

   /**
   * Get limit
   * @return limit
  **/
  @ApiModelProperty(value = "")
  public String getLimit() {
    return limit;
  }

  public void setLimit(String limit) {
    this.limit = limit;
  }

  public ServiceQuery groupBy(Integer groupBy) {
    this.groupBy = groupBy;
    return this;
  }

   /**
   * Get groupBy
   * @return groupBy
  **/
  @ApiModelProperty(value = "")
  public Integer getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(Integer groupBy) {
    this.groupBy = groupBy;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceQuery serviceQuery = (ServiceQuery) o;
    return Objects.equals(this.subQueries, serviceQuery.subQueries) &&
        Objects.equals(this.operation, serviceQuery.operation) &&
        Objects.equals(this.resourcePolicyQuery, serviceQuery.resourcePolicyQuery) &&
        Objects.equals(this.offset, serviceQuery.offset) &&
        Objects.equals(this.limit, serviceQuery.limit) &&
        Objects.equals(this.groupBy, serviceQuery.groupBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subQueries, operation, resourcePolicyQuery, offset, limit, groupBy);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceQuery {\n");
    
    sb.append("    subQueries: ").append(toIndentedString(subQueries)).append("\n");
    sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
    sb.append("    resourcePolicyQuery: ").append(toIndentedString(resourcePolicyQuery)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    groupBy: ").append(toIndentedString(groupBy)).append("\n");
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

