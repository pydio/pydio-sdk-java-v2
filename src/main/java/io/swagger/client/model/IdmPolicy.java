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
import io.swagger.client.model.IdmPolicyCondition;
import io.swagger.client.model.IdmPolicyEffect;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IdmPolicy
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class IdmPolicy {
  @SerializedName("id")
  private String id = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("subjects")
  private List<String> subjects = null;

  @SerializedName("resources")
  private List<String> resources = null;

  @SerializedName("actions")
  private List<String> actions = null;

  @SerializedName("effect")
  private IdmPolicyEffect effect = null;

  @SerializedName("conditions")
  private Map<String, IdmPolicyCondition> conditions = null;

  public IdmPolicy id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public IdmPolicy description(String description) {
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

  public IdmPolicy subjects(List<String> subjects) {
    this.subjects = subjects;
    return this;
  }

  public IdmPolicy addSubjectsItem(String subjectsItem) {
    if (this.subjects == null) {
      this.subjects = new ArrayList<String>();
    }
    this.subjects.add(subjectsItem);
    return this;
  }

   /**
   * Get subjects
   * @return subjects
  **/
  @ApiModelProperty(value = "")
  public List<String> getSubjects() {
    return subjects;
  }

  public void setSubjects(List<String> subjects) {
    this.subjects = subjects;
  }

  public IdmPolicy resources(List<String> resources) {
    this.resources = resources;
    return this;
  }

  public IdmPolicy addResourcesItem(String resourcesItem) {
    if (this.resources == null) {
      this.resources = new ArrayList<String>();
    }
    this.resources.add(resourcesItem);
    return this;
  }

   /**
   * Get resources
   * @return resources
  **/
  @ApiModelProperty(value = "")
  public List<String> getResources() {
    return resources;
  }

  public void setResources(List<String> resources) {
    this.resources = resources;
  }

  public IdmPolicy actions(List<String> actions) {
    this.actions = actions;
    return this;
  }

  public IdmPolicy addActionsItem(String actionsItem) {
    if (this.actions == null) {
      this.actions = new ArrayList<String>();
    }
    this.actions.add(actionsItem);
    return this;
  }

   /**
   * Get actions
   * @return actions
  **/
  @ApiModelProperty(value = "")
  public List<String> getActions() {
    return actions;
  }

  public void setActions(List<String> actions) {
    this.actions = actions;
  }

  public IdmPolicy effect(IdmPolicyEffect effect) {
    this.effect = effect;
    return this;
  }

   /**
   * Get effect
   * @return effect
  **/
  @ApiModelProperty(value = "")
  public IdmPolicyEffect getEffect() {
    return effect;
  }

  public void setEffect(IdmPolicyEffect effect) {
    this.effect = effect;
  }

  public IdmPolicy conditions(Map<String, IdmPolicyCondition> conditions) {
    this.conditions = conditions;
    return this;
  }

  public IdmPolicy putConditionsItem(String key, IdmPolicyCondition conditionsItem) {
    if (this.conditions == null) {
      this.conditions = new HashMap<String, IdmPolicyCondition>();
    }
    this.conditions.put(key, conditionsItem);
    return this;
  }

   /**
   * Get conditions
   * @return conditions
  **/
  @ApiModelProperty(value = "")
  public Map<String, IdmPolicyCondition> getConditions() {
    return conditions;
  }

  public void setConditions(Map<String, IdmPolicyCondition> conditions) {
    this.conditions = conditions;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IdmPolicy idmPolicy = (IdmPolicy) o;
    return Objects.equals(this.id, idmPolicy.id) &&
        Objects.equals(this.description, idmPolicy.description) &&
        Objects.equals(this.subjects, idmPolicy.subjects) &&
        Objects.equals(this.resources, idmPolicy.resources) &&
        Objects.equals(this.actions, idmPolicy.actions) &&
        Objects.equals(this.effect, idmPolicy.effect) &&
        Objects.equals(this.conditions, idmPolicy.conditions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, subjects, resources, actions, effect, conditions);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdmPolicy {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    subjects: ").append(toIndentedString(subjects)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
    sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
    sb.append("    effect: ").append(toIndentedString(effect)).append("\n");
    sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
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

