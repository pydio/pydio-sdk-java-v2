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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * JobsAction
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-11-30T14:51:15.861Z")
public class JobsAction {
  @SerializedName("ID")
  private String ID = null;

  @SerializedName("NodesSelector")
  private JobsNodesSelector nodesSelector = null;

  @SerializedName("UsersSelector")
  private JobsUsersSelector usersSelector = null;

  @SerializedName("NodesFilter")
  private JobsNodesSelector nodesFilter = null;

  @SerializedName("UsersFilter")
  private JobsUsersSelector usersFilter = null;

  @SerializedName("SourceFilter")
  private JobsSourceFilter sourceFilter = null;

  @SerializedName("Parameters")
  private Map<String, String> parameters = null;

  @SerializedName("ChainedActions")
  private List<JobsAction> chainedActions = null;

  public JobsAction ID(String ID) {
    this.ID = ID;
    return this;
  }

   /**
   * Get ID
   * @return ID
  **/
  @ApiModelProperty(value = "")
  public String getID() {
    return ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  public JobsAction nodesSelector(JobsNodesSelector nodesSelector) {
    this.nodesSelector = nodesSelector;
    return this;
  }

   /**
   * Get nodesSelector
   * @return nodesSelector
  **/
  @ApiModelProperty(value = "")
  public JobsNodesSelector getNodesSelector() {
    return nodesSelector;
  }

  public void setNodesSelector(JobsNodesSelector nodesSelector) {
    this.nodesSelector = nodesSelector;
  }

  public JobsAction usersSelector(JobsUsersSelector usersSelector) {
    this.usersSelector = usersSelector;
    return this;
  }

   /**
   * Get usersSelector
   * @return usersSelector
  **/
  @ApiModelProperty(value = "")
  public JobsUsersSelector getUsersSelector() {
    return usersSelector;
  }

  public void setUsersSelector(JobsUsersSelector usersSelector) {
    this.usersSelector = usersSelector;
  }

  public JobsAction nodesFilter(JobsNodesSelector nodesFilter) {
    this.nodesFilter = nodesFilter;
    return this;
  }

   /**
   * Get nodesFilter
   * @return nodesFilter
  **/
  @ApiModelProperty(value = "")
  public JobsNodesSelector getNodesFilter() {
    return nodesFilter;
  }

  public void setNodesFilter(JobsNodesSelector nodesFilter) {
    this.nodesFilter = nodesFilter;
  }

  public JobsAction usersFilter(JobsUsersSelector usersFilter) {
    this.usersFilter = usersFilter;
    return this;
  }

   /**
   * Get usersFilter
   * @return usersFilter
  **/
  @ApiModelProperty(value = "")
  public JobsUsersSelector getUsersFilter() {
    return usersFilter;
  }

  public void setUsersFilter(JobsUsersSelector usersFilter) {
    this.usersFilter = usersFilter;
  }

  public JobsAction sourceFilter(JobsSourceFilter sourceFilter) {
    this.sourceFilter = sourceFilter;
    return this;
  }

   /**
   * Get sourceFilter
   * @return sourceFilter
  **/
  @ApiModelProperty(value = "")
  public JobsSourceFilter getSourceFilter() {
    return sourceFilter;
  }

  public void setSourceFilter(JobsSourceFilter sourceFilter) {
    this.sourceFilter = sourceFilter;
  }

  public JobsAction parameters(Map<String, String> parameters) {
    this.parameters = parameters;
    return this;
  }

  public JobsAction putParametersItem(String key, String parametersItem) {
    if (this.parameters == null) {
      this.parameters = new HashMap<String, String>();
    }
    this.parameters.put(key, parametersItem);
    return this;
  }

   /**
   * Get parameters
   * @return parameters
  **/
  @ApiModelProperty(value = "")
  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public JobsAction chainedActions(List<JobsAction> chainedActions) {
    this.chainedActions = chainedActions;
    return this;
  }

  public JobsAction addChainedActionsItem(JobsAction chainedActionsItem) {
    if (this.chainedActions == null) {
      this.chainedActions = new ArrayList<JobsAction>();
    }
    this.chainedActions.add(chainedActionsItem);
    return this;
  }

   /**
   * Get chainedActions
   * @return chainedActions
  **/
  @ApiModelProperty(value = "")
  public List<JobsAction> getChainedActions() {
    return chainedActions;
  }

  public void setChainedActions(List<JobsAction> chainedActions) {
    this.chainedActions = chainedActions;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JobsAction jobsAction = (JobsAction) o;
    return Objects.equals(this.ID, jobsAction.ID) &&
        Objects.equals(this.nodesSelector, jobsAction.nodesSelector) &&
        Objects.equals(this.usersSelector, jobsAction.usersSelector) &&
        Objects.equals(this.nodesFilter, jobsAction.nodesFilter) &&
        Objects.equals(this.usersFilter, jobsAction.usersFilter) &&
        Objects.equals(this.sourceFilter, jobsAction.sourceFilter) &&
        Objects.equals(this.parameters, jobsAction.parameters) &&
        Objects.equals(this.chainedActions, jobsAction.chainedActions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ID, nodesSelector, usersSelector, nodesFilter, usersFilter, sourceFilter, parameters, chainedActions);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobsAction {\n");
    
    sb.append("    ID: ").append(toIndentedString(ID)).append("\n");
    sb.append("    nodesSelector: ").append(toIndentedString(nodesSelector)).append("\n");
    sb.append("    usersSelector: ").append(toIndentedString(usersSelector)).append("\n");
    sb.append("    nodesFilter: ").append(toIndentedString(nodesFilter)).append("\n");
    sb.append("    usersFilter: ").append(toIndentedString(usersFilter)).append("\n");
    sb.append("    sourceFilter: ").append(toIndentedString(sourceFilter)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
    sb.append("    chainedActions: ").append(toIndentedString(chainedActions)).append("\n");
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

