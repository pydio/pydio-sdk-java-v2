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
import io.swagger.client.model.IdmUserSingleQuery;
import io.swagger.client.model.RestResourcePolicyQuery;
import io.swagger.client.model.ServiceOperationType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RestSearchUserRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class RestSearchUserRequest {
  @SerializedName("Queries")
  private List<IdmUserSingleQuery> queries = null;

  @SerializedName("ResourcePolicyQuery")
  private RestResourcePolicyQuery resourcePolicyQuery = null;

  @SerializedName("Offset")
  private String offset = null;

  @SerializedName("Limit")
  private String limit = null;

  @SerializedName("GroupBy")
  private Integer groupBy = null;

  @SerializedName("CountOnly")
  private Boolean countOnly = null;

  @SerializedName("Operation")
  private ServiceOperationType operation = null;

  public RestSearchUserRequest queries(List<IdmUserSingleQuery> queries) {
    this.queries = queries;
    return this;
  }

  public RestSearchUserRequest addQueriesItem(IdmUserSingleQuery queriesItem) {
    if (this.queries == null) {
      this.queries = new ArrayList<IdmUserSingleQuery>();
    }
    this.queries.add(queriesItem);
    return this;
  }

   /**
   * Get queries
   * @return queries
  **/
  @ApiModelProperty(value = "")
  public List<IdmUserSingleQuery> getQueries() {
    return queries;
  }

  public void setQueries(List<IdmUserSingleQuery> queries) {
    this.queries = queries;
  }

  public RestSearchUserRequest resourcePolicyQuery(RestResourcePolicyQuery resourcePolicyQuery) {
    this.resourcePolicyQuery = resourcePolicyQuery;
    return this;
  }

   /**
   * Get resourcePolicyQuery
   * @return resourcePolicyQuery
  **/
  @ApiModelProperty(value = "")
  public RestResourcePolicyQuery getResourcePolicyQuery() {
    return resourcePolicyQuery;
  }

  public void setResourcePolicyQuery(RestResourcePolicyQuery resourcePolicyQuery) {
    this.resourcePolicyQuery = resourcePolicyQuery;
  }

  public RestSearchUserRequest offset(String offset) {
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

  public RestSearchUserRequest limit(String limit) {
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

  public RestSearchUserRequest groupBy(Integer groupBy) {
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

  public RestSearchUserRequest countOnly(Boolean countOnly) {
    this.countOnly = countOnly;
    return this;
  }

   /**
   * Get countOnly
   * @return countOnly
  **/
  @ApiModelProperty(value = "")
  public Boolean isCountOnly() {
    return countOnly;
  }

  public void setCountOnly(Boolean countOnly) {
    this.countOnly = countOnly;
  }

  public RestSearchUserRequest operation(ServiceOperationType operation) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestSearchUserRequest restSearchUserRequest = (RestSearchUserRequest) o;
    return Objects.equals(this.queries, restSearchUserRequest.queries) &&
        Objects.equals(this.resourcePolicyQuery, restSearchUserRequest.resourcePolicyQuery) &&
        Objects.equals(this.offset, restSearchUserRequest.offset) &&
        Objects.equals(this.limit, restSearchUserRequest.limit) &&
        Objects.equals(this.groupBy, restSearchUserRequest.groupBy) &&
        Objects.equals(this.countOnly, restSearchUserRequest.countOnly) &&
        Objects.equals(this.operation, restSearchUserRequest.operation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(queries, resourcePolicyQuery, offset, limit, groupBy, countOnly, operation);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestSearchUserRequest {\n");
    
    sb.append("    queries: ").append(toIndentedString(queries)).append("\n");
    sb.append("    resourcePolicyQuery: ").append(toIndentedString(resourcePolicyQuery)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    groupBy: ").append(toIndentedString(groupBy)).append("\n");
    sb.append("    countOnly: ").append(toIndentedString(countOnly)).append("\n");
    sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
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

