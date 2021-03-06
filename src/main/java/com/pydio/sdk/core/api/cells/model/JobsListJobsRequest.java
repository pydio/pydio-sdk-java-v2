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

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * JobsListJobsRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-11-30T14:51:15.861Z")
public class JobsListJobsRequest {
  @SerializedName("Owner")
  private String owner = null;

  @SerializedName("EventsOnly")
  private Boolean eventsOnly = null;

  @SerializedName("TimersOnly")
  private Boolean timersOnly = null;

  @SerializedName("LoadTasks")
  private JobsTaskStatus loadTasks = null;

  public JobsListJobsRequest owner(String owner) {
    this.owner = owner;
    return this;
  }

   /**
   * Get owner
   * @return owner
  **/
  @ApiModelProperty(value = "")
  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public JobsListJobsRequest eventsOnly(Boolean eventsOnly) {
    this.eventsOnly = eventsOnly;
    return this;
  }

   /**
   * Get eventsOnly
   * @return eventsOnly
  **/
  @ApiModelProperty(value = "")
  public Boolean isEventsOnly() {
    return eventsOnly;
  }

  public void setEventsOnly(Boolean eventsOnly) {
    this.eventsOnly = eventsOnly;
  }

  public JobsListJobsRequest timersOnly(Boolean timersOnly) {
    this.timersOnly = timersOnly;
    return this;
  }

   /**
   * Get timersOnly
   * @return timersOnly
  **/
  @ApiModelProperty(value = "")
  public Boolean isTimersOnly() {
    return timersOnly;
  }

  public void setTimersOnly(Boolean timersOnly) {
    this.timersOnly = timersOnly;
  }

  public JobsListJobsRequest loadTasks(JobsTaskStatus loadTasks) {
    this.loadTasks = loadTasks;
    return this;
  }

   /**
   * Get loadTasks
   * @return loadTasks
  **/
  @ApiModelProperty(value = "")
  public JobsTaskStatus getLoadTasks() {
    return loadTasks;
  }

  public void setLoadTasks(JobsTaskStatus loadTasks) {
    this.loadTasks = loadTasks;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JobsListJobsRequest jobsListJobsRequest = (JobsListJobsRequest) o;
    return Objects.equals(this.owner, jobsListJobsRequest.owner) &&
        Objects.equals(this.eventsOnly, jobsListJobsRequest.eventsOnly) &&
        Objects.equals(this.timersOnly, jobsListJobsRequest.timersOnly) &&
        Objects.equals(this.loadTasks, jobsListJobsRequest.loadTasks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, eventsOnly, timersOnly, loadTasks);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobsListJobsRequest {\n");
    
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    eventsOnly: ").append(toIndentedString(eventsOnly)).append("\n");
    sb.append("    timersOnly: ").append(toIndentedString(timersOnly)).append("\n");
    sb.append("    loadTasks: ").append(toIndentedString(loadTasks)).append("\n");
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

