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
 * IdmPolicyGroup
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-11-30T14:51:15.861Z")
public class IdmPolicyGroup {
  @SerializedName("Uuid")
  private String uuid = null;

  @SerializedName("Name")
  private String name = null;

  @SerializedName("Description")
  private String description = null;

  @SerializedName("OwnerUuid")
  private String ownerUuid = null;

  @SerializedName("ResourceGroup")
  private IdmPolicyResourceGroup resourceGroup = null;

  @SerializedName("LastUpdated")
  private Integer lastUpdated = null;

  @SerializedName("Policies")
  private List<IdmPolicy> policies = null;

  public IdmPolicyGroup uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

   /**
   * Get uuid
   * @return uuid
  **/
  @ApiModelProperty(value = "")
  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public IdmPolicyGroup name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public IdmPolicyGroup description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public IdmPolicyGroup ownerUuid(String ownerUuid) {
    this.ownerUuid = ownerUuid;
    return this;
  }

   /**
   * Get ownerUuid
   * @return ownerUuid
  **/
  @ApiModelProperty(value = "")
  public String getOwnerUuid() {
    return ownerUuid;
  }

  public void setOwnerUuid(String ownerUuid) {
    this.ownerUuid = ownerUuid;
  }

  public IdmPolicyGroup resourceGroup(IdmPolicyResourceGroup resourceGroup) {
    this.resourceGroup = resourceGroup;
    return this;
  }

   /**
   * Get resourceGroup
   * @return resourceGroup
  **/
  @ApiModelProperty(value = "")
  public IdmPolicyResourceGroup getResourceGroup() {
    return resourceGroup;
  }

  public void setResourceGroup(IdmPolicyResourceGroup resourceGroup) {
    this.resourceGroup = resourceGroup;
  }

  public IdmPolicyGroup lastUpdated(Integer lastUpdated) {
    this.lastUpdated = lastUpdated;
    return this;
  }

   /**
   * Get lastUpdated
   * @return lastUpdated
  **/
  @ApiModelProperty(value = "")
  public Integer getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Integer lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public IdmPolicyGroup policies(List<IdmPolicy> policies) {
    this.policies = policies;
    return this;
  }

  public IdmPolicyGroup addPoliciesItem(IdmPolicy policiesItem) {
    if (this.policies == null) {
      this.policies = new ArrayList<IdmPolicy>();
    }
    this.policies.add(policiesItem);
    return this;
  }

   /**
   * Get policies
   * @return policies
  **/
  @ApiModelProperty(value = "")
  public List<IdmPolicy> getPolicies() {
    return policies;
  }

  public void setPolicies(List<IdmPolicy> policies) {
    this.policies = policies;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IdmPolicyGroup idmPolicyGroup = (IdmPolicyGroup) o;
    return Objects.equals(this.uuid, idmPolicyGroup.uuid) &&
        Objects.equals(this.name, idmPolicyGroup.name) &&
        Objects.equals(this.description, idmPolicyGroup.description) &&
        Objects.equals(this.ownerUuid, idmPolicyGroup.ownerUuid) &&
        Objects.equals(this.resourceGroup, idmPolicyGroup.resourceGroup) &&
        Objects.equals(this.lastUpdated, idmPolicyGroup.lastUpdated) &&
        Objects.equals(this.policies, idmPolicyGroup.policies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, name, description, ownerUuid, resourceGroup, lastUpdated, policies);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdmPolicyGroup {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    ownerUuid: ").append(toIndentedString(ownerUuid)).append("\n");
    sb.append("    resourceGroup: ").append(toIndentedString(resourceGroup)).append("\n");
    sb.append("    lastUpdated: ").append(toIndentedString(lastUpdated)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
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

