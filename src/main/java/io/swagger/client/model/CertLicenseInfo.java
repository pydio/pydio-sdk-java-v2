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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CertLicenseInfo
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class CertLicenseInfo {
  @SerializedName("Id")
  private String id = null;

  @SerializedName("AccountName")
  private String accountName = null;

  @SerializedName("ServerDomain")
  private String serverDomain = null;

  @SerializedName("IssueTime")
  private Integer issueTime = null;

  @SerializedName("ExpireTime")
  private Integer expireTime = null;

  @SerializedName("MaxUsers")
  private String maxUsers = null;

  @SerializedName("MaxPeers")
  private String maxPeers = null;

  @SerializedName("Features")
  private Map<String, String> features = null;

  public CertLicenseInfo id(String id) {
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

  public CertLicenseInfo accountName(String accountName) {
    this.accountName = accountName;
    return this;
  }

   /**
   * Get accountName
   * @return accountName
  **/
  @ApiModelProperty(value = "")
  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public CertLicenseInfo serverDomain(String serverDomain) {
    this.serverDomain = serverDomain;
    return this;
  }

   /**
   * Get serverDomain
   * @return serverDomain
  **/
  @ApiModelProperty(value = "")
  public String getServerDomain() {
    return serverDomain;
  }

  public void setServerDomain(String serverDomain) {
    this.serverDomain = serverDomain;
  }

  public CertLicenseInfo issueTime(Integer issueTime) {
    this.issueTime = issueTime;
    return this;
  }

   /**
   * Get issueTime
   * @return issueTime
  **/
  @ApiModelProperty(value = "")
  public Integer getIssueTime() {
    return issueTime;
  }

  public void setIssueTime(Integer issueTime) {
    this.issueTime = issueTime;
  }

  public CertLicenseInfo expireTime(Integer expireTime) {
    this.expireTime = expireTime;
    return this;
  }

   /**
   * Get expireTime
   * @return expireTime
  **/
  @ApiModelProperty(value = "")
  public Integer getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(Integer expireTime) {
    this.expireTime = expireTime;
  }

  public CertLicenseInfo maxUsers(String maxUsers) {
    this.maxUsers = maxUsers;
    return this;
  }

   /**
   * Get maxUsers
   * @return maxUsers
  **/
  @ApiModelProperty(value = "")
  public String getMaxUsers() {
    return maxUsers;
  }

  public void setMaxUsers(String maxUsers) {
    this.maxUsers = maxUsers;
  }

  public CertLicenseInfo maxPeers(String maxPeers) {
    this.maxPeers = maxPeers;
    return this;
  }

   /**
   * Get maxPeers
   * @return maxPeers
  **/
  @ApiModelProperty(value = "")
  public String getMaxPeers() {
    return maxPeers;
  }

  public void setMaxPeers(String maxPeers) {
    this.maxPeers = maxPeers;
  }

  public CertLicenseInfo features(Map<String, String> features) {
    this.features = features;
    return this;
  }

  public CertLicenseInfo putFeaturesItem(String key, String featuresItem) {
    if (this.features == null) {
      this.features = new HashMap<String, String>();
    }
    this.features.put(key, featuresItem);
    return this;
  }

   /**
   * Get features
   * @return features
  **/
  @ApiModelProperty(value = "")
  public Map<String, String> getFeatures() {
    return features;
  }

  public void setFeatures(Map<String, String> features) {
    this.features = features;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CertLicenseInfo certLicenseInfo = (CertLicenseInfo) o;
    return Objects.equals(this.id, certLicenseInfo.id) &&
        Objects.equals(this.accountName, certLicenseInfo.accountName) &&
        Objects.equals(this.serverDomain, certLicenseInfo.serverDomain) &&
        Objects.equals(this.issueTime, certLicenseInfo.issueTime) &&
        Objects.equals(this.expireTime, certLicenseInfo.expireTime) &&
        Objects.equals(this.maxUsers, certLicenseInfo.maxUsers) &&
        Objects.equals(this.maxPeers, certLicenseInfo.maxPeers) &&
        Objects.equals(this.features, certLicenseInfo.features);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, accountName, serverDomain, issueTime, expireTime, maxUsers, maxPeers, features);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertLicenseInfo {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    accountName: ").append(toIndentedString(accountName)).append("\n");
    sb.append("    serverDomain: ").append(toIndentedString(serverDomain)).append("\n");
    sb.append("    issueTime: ").append(toIndentedString(issueTime)).append("\n");
    sb.append("    expireTime: ").append(toIndentedString(expireTime)).append("\n");
    sb.append("    maxUsers: ").append(toIndentedString(maxUsers)).append("\n");
    sb.append("    maxPeers: ").append(toIndentedString(maxPeers)).append("\n");
    sb.append("    features: ").append(toIndentedString(features)).append("\n");
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

