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
import io.swagger.client.model.ActivityObject;
import io.swagger.client.model.IdmUser;
import io.swagger.client.model.JobsActionOutput;
import io.swagger.client.model.ProtobufAny;
import io.swagger.client.model.TreeNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JobsActionMessage
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class JobsActionMessage {
  @SerializedName("Event")
  private ProtobufAny event = null;

  @SerializedName("Nodes")
  private List<TreeNode> nodes = null;

  @SerializedName("Users")
  private List<IdmUser> users = null;

  @SerializedName("Activities")
  private List<ActivityObject> activities = null;

  @SerializedName("OutputChain")
  private List<JobsActionOutput> outputChain = null;

  public JobsActionMessage event(ProtobufAny event) {
    this.event = event;
    return this;
  }

   /**
   * Get event
   * @return event
  **/
  @ApiModelProperty(value = "")
  public ProtobufAny getEvent() {
    return event;
  }

  public void setEvent(ProtobufAny event) {
    this.event = event;
  }

  public JobsActionMessage nodes(List<TreeNode> nodes) {
    this.nodes = nodes;
    return this;
  }

  public JobsActionMessage addNodesItem(TreeNode nodesItem) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<TreeNode>();
    }
    this.nodes.add(nodesItem);
    return this;
  }

   /**
   * Get nodes
   * @return nodes
  **/
  @ApiModelProperty(value = "")
  public List<TreeNode> getNodes() {
    return nodes;
  }

  public void setNodes(List<TreeNode> nodes) {
    this.nodes = nodes;
  }

  public JobsActionMessage users(List<IdmUser> users) {
    this.users = users;
    return this;
  }

  public JobsActionMessage addUsersItem(IdmUser usersItem) {
    if (this.users == null) {
      this.users = new ArrayList<IdmUser>();
    }
    this.users.add(usersItem);
    return this;
  }

   /**
   * Get users
   * @return users
  **/
  @ApiModelProperty(value = "")
  public List<IdmUser> getUsers() {
    return users;
  }

  public void setUsers(List<IdmUser> users) {
    this.users = users;
  }

  public JobsActionMessage activities(List<ActivityObject> activities) {
    this.activities = activities;
    return this;
  }

  public JobsActionMessage addActivitiesItem(ActivityObject activitiesItem) {
    if (this.activities == null) {
      this.activities = new ArrayList<ActivityObject>();
    }
    this.activities.add(activitiesItem);
    return this;
  }

   /**
   * Get activities
   * @return activities
  **/
  @ApiModelProperty(value = "")
  public List<ActivityObject> getActivities() {
    return activities;
  }

  public void setActivities(List<ActivityObject> activities) {
    this.activities = activities;
  }

  public JobsActionMessage outputChain(List<JobsActionOutput> outputChain) {
    this.outputChain = outputChain;
    return this;
  }

  public JobsActionMessage addOutputChainItem(JobsActionOutput outputChainItem) {
    if (this.outputChain == null) {
      this.outputChain = new ArrayList<JobsActionOutput>();
    }
    this.outputChain.add(outputChainItem);
    return this;
  }

   /**
   * Get outputChain
   * @return outputChain
  **/
  @ApiModelProperty(value = "")
  public List<JobsActionOutput> getOutputChain() {
    return outputChain;
  }

  public void setOutputChain(List<JobsActionOutput> outputChain) {
    this.outputChain = outputChain;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JobsActionMessage jobsActionMessage = (JobsActionMessage) o;
    return Objects.equals(this.event, jobsActionMessage.event) &&
        Objects.equals(this.nodes, jobsActionMessage.nodes) &&
        Objects.equals(this.users, jobsActionMessage.users) &&
        Objects.equals(this.activities, jobsActionMessage.activities) &&
        Objects.equals(this.outputChain, jobsActionMessage.outputChain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(event, nodes, users, activities, outputChain);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobsActionMessage {\n");
    
    sb.append("    event: ").append(toIndentedString(event)).append("\n");
    sb.append("    nodes: ").append(toIndentedString(nodes)).append("\n");
    sb.append("    users: ").append(toIndentedString(users)).append("\n");
    sb.append("    activities: ").append(toIndentedString(activities)).append("\n");
    sb.append("    outputChain: ").append(toIndentedString(outputChain)).append("\n");
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

