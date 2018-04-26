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
import io.swagger.client.model.InstallCheckResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * InstallInstallConfig
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-04-25T15:42:51.155Z")
public class InstallInstallConfig {
  @SerializedName("internalUrl")
  private String internalUrl = null;

  @SerializedName("dbConnectionType")
  private String dbConnectionType = null;

  @SerializedName("dbTCPHostname")
  private String dbTCPHostname = null;

  @SerializedName("dbTCPPort")
  private String dbTCPPort = null;

  @SerializedName("dbTCPName")
  private String dbTCPName = null;

  @SerializedName("dbTCPUser")
  private String dbTCPUser = null;

  @SerializedName("dbTCPPassword")
  private String dbTCPPassword = null;

  @SerializedName("dbSocketFile")
  private String dbSocketFile = null;

  @SerializedName("dbSocketName")
  private String dbSocketName = null;

  @SerializedName("dbSocketUser")
  private String dbSocketUser = null;

  @SerializedName("dbSocketPassword")
  private String dbSocketPassword = null;

  @SerializedName("dbManualDSN")
  private String dbManualDSN = null;

  @SerializedName("dsName")
  private String dsName = null;

  @SerializedName("dsPort")
  private String dsPort = null;

  @SerializedName("dsFolder")
  private String dsFolder = null;

  @SerializedName("externalMicro")
  private String externalMicro = null;

  @SerializedName("externalGateway")
  private String externalGateway = null;

  @SerializedName("externalWebsocket")
  private String externalWebsocket = null;

  @SerializedName("externalFrontPlugins")
  private String externalFrontPlugins = null;

  @SerializedName("externalDex")
  private String externalDex = null;

  @SerializedName("externalDexID")
  private String externalDexID = null;

  @SerializedName("externalDexSecret")
  private String externalDexSecret = null;

  @SerializedName("frontendHosts")
  private String frontendHosts = null;

  @SerializedName("frontendLogin")
  private String frontendLogin = null;

  @SerializedName("frontendPassword")
  private String frontendPassword = null;

  @SerializedName("frontendRepeatPassword")
  private String frontendRepeatPassword = null;

  @SerializedName("fpmAddress")
  private String fpmAddress = null;

  @SerializedName("licenseRequired")
  private Boolean licenseRequired = null;

  @SerializedName("licenseString")
  private String licenseString = null;

  @SerializedName("CheckResults")
  private List<InstallCheckResult> checkResults = null;

  public InstallInstallConfig internalUrl(String internalUrl) {
    this.internalUrl = internalUrl;
    return this;
  }

   /**
   * Get internalUrl
   * @return internalUrl
  **/
  @ApiModelProperty(value = "")
  public String getInternalUrl() {
    return internalUrl;
  }

  public void setInternalUrl(String internalUrl) {
    this.internalUrl = internalUrl;
  }

  public InstallInstallConfig dbConnectionType(String dbConnectionType) {
    this.dbConnectionType = dbConnectionType;
    return this;
  }

   /**
   * Get dbConnectionType
   * @return dbConnectionType
  **/
  @ApiModelProperty(value = "")
  public String getDbConnectionType() {
    return dbConnectionType;
  }

  public void setDbConnectionType(String dbConnectionType) {
    this.dbConnectionType = dbConnectionType;
  }

  public InstallInstallConfig dbTCPHostname(String dbTCPHostname) {
    this.dbTCPHostname = dbTCPHostname;
    return this;
  }

   /**
   * Get dbTCPHostname
   * @return dbTCPHostname
  **/
  @ApiModelProperty(value = "")
  public String getDbTCPHostname() {
    return dbTCPHostname;
  }

  public void setDbTCPHostname(String dbTCPHostname) {
    this.dbTCPHostname = dbTCPHostname;
  }

  public InstallInstallConfig dbTCPPort(String dbTCPPort) {
    this.dbTCPPort = dbTCPPort;
    return this;
  }

   /**
   * Get dbTCPPort
   * @return dbTCPPort
  **/
  @ApiModelProperty(value = "")
  public String getDbTCPPort() {
    return dbTCPPort;
  }

  public void setDbTCPPort(String dbTCPPort) {
    this.dbTCPPort = dbTCPPort;
  }

  public InstallInstallConfig dbTCPName(String dbTCPName) {
    this.dbTCPName = dbTCPName;
    return this;
  }

   /**
   * Get dbTCPName
   * @return dbTCPName
  **/
  @ApiModelProperty(value = "")
  public String getDbTCPName() {
    return dbTCPName;
  }

  public void setDbTCPName(String dbTCPName) {
    this.dbTCPName = dbTCPName;
  }

  public InstallInstallConfig dbTCPUser(String dbTCPUser) {
    this.dbTCPUser = dbTCPUser;
    return this;
  }

   /**
   * Get dbTCPUser
   * @return dbTCPUser
  **/
  @ApiModelProperty(value = "")
  public String getDbTCPUser() {
    return dbTCPUser;
  }

  public void setDbTCPUser(String dbTCPUser) {
    this.dbTCPUser = dbTCPUser;
  }

  public InstallInstallConfig dbTCPPassword(String dbTCPPassword) {
    this.dbTCPPassword = dbTCPPassword;
    return this;
  }

   /**
   * Get dbTCPPassword
   * @return dbTCPPassword
  **/
  @ApiModelProperty(value = "")
  public String getDbTCPPassword() {
    return dbTCPPassword;
  }

  public void setDbTCPPassword(String dbTCPPassword) {
    this.dbTCPPassword = dbTCPPassword;
  }

  public InstallInstallConfig dbSocketFile(String dbSocketFile) {
    this.dbSocketFile = dbSocketFile;
    return this;
  }

   /**
   * Get dbSocketFile
   * @return dbSocketFile
  **/
  @ApiModelProperty(value = "")
  public String getDbSocketFile() {
    return dbSocketFile;
  }

  public void setDbSocketFile(String dbSocketFile) {
    this.dbSocketFile = dbSocketFile;
  }

  public InstallInstallConfig dbSocketName(String dbSocketName) {
    this.dbSocketName = dbSocketName;
    return this;
  }

   /**
   * Get dbSocketName
   * @return dbSocketName
  **/
  @ApiModelProperty(value = "")
  public String getDbSocketName() {
    return dbSocketName;
  }

  public void setDbSocketName(String dbSocketName) {
    this.dbSocketName = dbSocketName;
  }

  public InstallInstallConfig dbSocketUser(String dbSocketUser) {
    this.dbSocketUser = dbSocketUser;
    return this;
  }

   /**
   * Get dbSocketUser
   * @return dbSocketUser
  **/
  @ApiModelProperty(value = "")
  public String getDbSocketUser() {
    return dbSocketUser;
  }

  public void setDbSocketUser(String dbSocketUser) {
    this.dbSocketUser = dbSocketUser;
  }

  public InstallInstallConfig dbSocketPassword(String dbSocketPassword) {
    this.dbSocketPassword = dbSocketPassword;
    return this;
  }

   /**
   * Get dbSocketPassword
   * @return dbSocketPassword
  **/
  @ApiModelProperty(value = "")
  public String getDbSocketPassword() {
    return dbSocketPassword;
  }

  public void setDbSocketPassword(String dbSocketPassword) {
    this.dbSocketPassword = dbSocketPassword;
  }

  public InstallInstallConfig dbManualDSN(String dbManualDSN) {
    this.dbManualDSN = dbManualDSN;
    return this;
  }

   /**
   * Get dbManualDSN
   * @return dbManualDSN
  **/
  @ApiModelProperty(value = "")
  public String getDbManualDSN() {
    return dbManualDSN;
  }

  public void setDbManualDSN(String dbManualDSN) {
    this.dbManualDSN = dbManualDSN;
  }

  public InstallInstallConfig dsName(String dsName) {
    this.dsName = dsName;
    return this;
  }

   /**
   * Get dsName
   * @return dsName
  **/
  @ApiModelProperty(value = "")
  public String getDsName() {
    return dsName;
  }

  public void setDsName(String dsName) {
    this.dsName = dsName;
  }

  public InstallInstallConfig dsPort(String dsPort) {
    this.dsPort = dsPort;
    return this;
  }

   /**
   * Get dsPort
   * @return dsPort
  **/
  @ApiModelProperty(value = "")
  public String getDsPort() {
    return dsPort;
  }

  public void setDsPort(String dsPort) {
    this.dsPort = dsPort;
  }

  public InstallInstallConfig dsFolder(String dsFolder) {
    this.dsFolder = dsFolder;
    return this;
  }

   /**
   * Get dsFolder
   * @return dsFolder
  **/
  @ApiModelProperty(value = "")
  public String getDsFolder() {
    return dsFolder;
  }

  public void setDsFolder(String dsFolder) {
    this.dsFolder = dsFolder;
  }

  public InstallInstallConfig externalMicro(String externalMicro) {
    this.externalMicro = externalMicro;
    return this;
  }

   /**
   * Get externalMicro
   * @return externalMicro
  **/
  @ApiModelProperty(value = "")
  public String getExternalMicro() {
    return externalMicro;
  }

  public void setExternalMicro(String externalMicro) {
    this.externalMicro = externalMicro;
  }

  public InstallInstallConfig externalGateway(String externalGateway) {
    this.externalGateway = externalGateway;
    return this;
  }

   /**
   * Get externalGateway
   * @return externalGateway
  **/
  @ApiModelProperty(value = "")
  public String getExternalGateway() {
    return externalGateway;
  }

  public void setExternalGateway(String externalGateway) {
    this.externalGateway = externalGateway;
  }

  public InstallInstallConfig externalWebsocket(String externalWebsocket) {
    this.externalWebsocket = externalWebsocket;
    return this;
  }

   /**
   * Get externalWebsocket
   * @return externalWebsocket
  **/
  @ApiModelProperty(value = "")
  public String getExternalWebsocket() {
    return externalWebsocket;
  }

  public void setExternalWebsocket(String externalWebsocket) {
    this.externalWebsocket = externalWebsocket;
  }

  public InstallInstallConfig externalFrontPlugins(String externalFrontPlugins) {
    this.externalFrontPlugins = externalFrontPlugins;
    return this;
  }

   /**
   * Get externalFrontPlugins
   * @return externalFrontPlugins
  **/
  @ApiModelProperty(value = "")
  public String getExternalFrontPlugins() {
    return externalFrontPlugins;
  }

  public void setExternalFrontPlugins(String externalFrontPlugins) {
    this.externalFrontPlugins = externalFrontPlugins;
  }

  public InstallInstallConfig externalDex(String externalDex) {
    this.externalDex = externalDex;
    return this;
  }

   /**
   * Get externalDex
   * @return externalDex
  **/
  @ApiModelProperty(value = "")
  public String getExternalDex() {
    return externalDex;
  }

  public void setExternalDex(String externalDex) {
    this.externalDex = externalDex;
  }

  public InstallInstallConfig externalDexID(String externalDexID) {
    this.externalDexID = externalDexID;
    return this;
  }

   /**
   * Get externalDexID
   * @return externalDexID
  **/
  @ApiModelProperty(value = "")
  public String getExternalDexID() {
    return externalDexID;
  }

  public void setExternalDexID(String externalDexID) {
    this.externalDexID = externalDexID;
  }

  public InstallInstallConfig externalDexSecret(String externalDexSecret) {
    this.externalDexSecret = externalDexSecret;
    return this;
  }

   /**
   * Get externalDexSecret
   * @return externalDexSecret
  **/
  @ApiModelProperty(value = "")
  public String getExternalDexSecret() {
    return externalDexSecret;
  }

  public void setExternalDexSecret(String externalDexSecret) {
    this.externalDexSecret = externalDexSecret;
  }

  public InstallInstallConfig frontendHosts(String frontendHosts) {
    this.frontendHosts = frontendHosts;
    return this;
  }

   /**
   * Get frontendHosts
   * @return frontendHosts
  **/
  @ApiModelProperty(value = "")
  public String getFrontendHosts() {
    return frontendHosts;
  }

  public void setFrontendHosts(String frontendHosts) {
    this.frontendHosts = frontendHosts;
  }

  public InstallInstallConfig frontendLogin(String frontendLogin) {
    this.frontendLogin = frontendLogin;
    return this;
  }

   /**
   * Get frontendLogin
   * @return frontendLogin
  **/
  @ApiModelProperty(value = "")
  public String getFrontendLogin() {
    return frontendLogin;
  }

  public void setFrontendLogin(String frontendLogin) {
    this.frontendLogin = frontendLogin;
  }

  public InstallInstallConfig frontendPassword(String frontendPassword) {
    this.frontendPassword = frontendPassword;
    return this;
  }

   /**
   * Get frontendPassword
   * @return frontendPassword
  **/
  @ApiModelProperty(value = "")
  public String getFrontendPassword() {
    return frontendPassword;
  }

  public void setFrontendPassword(String frontendPassword) {
    this.frontendPassword = frontendPassword;
  }

  public InstallInstallConfig frontendRepeatPassword(String frontendRepeatPassword) {
    this.frontendRepeatPassword = frontendRepeatPassword;
    return this;
  }

   /**
   * Get frontendRepeatPassword
   * @return frontendRepeatPassword
  **/
  @ApiModelProperty(value = "")
  public String getFrontendRepeatPassword() {
    return frontendRepeatPassword;
  }

  public void setFrontendRepeatPassword(String frontendRepeatPassword) {
    this.frontendRepeatPassword = frontendRepeatPassword;
  }

  public InstallInstallConfig fpmAddress(String fpmAddress) {
    this.fpmAddress = fpmAddress;
    return this;
  }

   /**
   * Get fpmAddress
   * @return fpmAddress
  **/
  @ApiModelProperty(value = "")
  public String getFpmAddress() {
    return fpmAddress;
  }

  public void setFpmAddress(String fpmAddress) {
    this.fpmAddress = fpmAddress;
  }

  public InstallInstallConfig licenseRequired(Boolean licenseRequired) {
    this.licenseRequired = licenseRequired;
    return this;
  }

   /**
   * Get licenseRequired
   * @return licenseRequired
  **/
  @ApiModelProperty(value = "")
  public Boolean isLicenseRequired() {
    return licenseRequired;
  }

  public void setLicenseRequired(Boolean licenseRequired) {
    this.licenseRequired = licenseRequired;
  }

  public InstallInstallConfig licenseString(String licenseString) {
    this.licenseString = licenseString;
    return this;
  }

   /**
   * Get licenseString
   * @return licenseString
  **/
  @ApiModelProperty(value = "")
  public String getLicenseString() {
    return licenseString;
  }

  public void setLicenseString(String licenseString) {
    this.licenseString = licenseString;
  }

  public InstallInstallConfig checkResults(List<InstallCheckResult> checkResults) {
    this.checkResults = checkResults;
    return this;
  }

  public InstallInstallConfig addCheckResultsItem(InstallCheckResult checkResultsItem) {
    if (this.checkResults == null) {
      this.checkResults = new ArrayList<InstallCheckResult>();
    }
    this.checkResults.add(checkResultsItem);
    return this;
  }

   /**
   * Get checkResults
   * @return checkResults
  **/
  @ApiModelProperty(value = "")
  public List<InstallCheckResult> getCheckResults() {
    return checkResults;
  }

  public void setCheckResults(List<InstallCheckResult> checkResults) {
    this.checkResults = checkResults;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InstallInstallConfig installInstallConfig = (InstallInstallConfig) o;
    return Objects.equals(this.internalUrl, installInstallConfig.internalUrl) &&
        Objects.equals(this.dbConnectionType, installInstallConfig.dbConnectionType) &&
        Objects.equals(this.dbTCPHostname, installInstallConfig.dbTCPHostname) &&
        Objects.equals(this.dbTCPPort, installInstallConfig.dbTCPPort) &&
        Objects.equals(this.dbTCPName, installInstallConfig.dbTCPName) &&
        Objects.equals(this.dbTCPUser, installInstallConfig.dbTCPUser) &&
        Objects.equals(this.dbTCPPassword, installInstallConfig.dbTCPPassword) &&
        Objects.equals(this.dbSocketFile, installInstallConfig.dbSocketFile) &&
        Objects.equals(this.dbSocketName, installInstallConfig.dbSocketName) &&
        Objects.equals(this.dbSocketUser, installInstallConfig.dbSocketUser) &&
        Objects.equals(this.dbSocketPassword, installInstallConfig.dbSocketPassword) &&
        Objects.equals(this.dbManualDSN, installInstallConfig.dbManualDSN) &&
        Objects.equals(this.dsName, installInstallConfig.dsName) &&
        Objects.equals(this.dsPort, installInstallConfig.dsPort) &&
        Objects.equals(this.dsFolder, installInstallConfig.dsFolder) &&
        Objects.equals(this.externalMicro, installInstallConfig.externalMicro) &&
        Objects.equals(this.externalGateway, installInstallConfig.externalGateway) &&
        Objects.equals(this.externalWebsocket, installInstallConfig.externalWebsocket) &&
        Objects.equals(this.externalFrontPlugins, installInstallConfig.externalFrontPlugins) &&
        Objects.equals(this.externalDex, installInstallConfig.externalDex) &&
        Objects.equals(this.externalDexID, installInstallConfig.externalDexID) &&
        Objects.equals(this.externalDexSecret, installInstallConfig.externalDexSecret) &&
        Objects.equals(this.frontendHosts, installInstallConfig.frontendHosts) &&
        Objects.equals(this.frontendLogin, installInstallConfig.frontendLogin) &&
        Objects.equals(this.frontendPassword, installInstallConfig.frontendPassword) &&
        Objects.equals(this.frontendRepeatPassword, installInstallConfig.frontendRepeatPassword) &&
        Objects.equals(this.fpmAddress, installInstallConfig.fpmAddress) &&
        Objects.equals(this.licenseRequired, installInstallConfig.licenseRequired) &&
        Objects.equals(this.licenseString, installInstallConfig.licenseString) &&
        Objects.equals(this.checkResults, installInstallConfig.checkResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(internalUrl, dbConnectionType, dbTCPHostname, dbTCPPort, dbTCPName, dbTCPUser, dbTCPPassword, dbSocketFile, dbSocketName, dbSocketUser, dbSocketPassword, dbManualDSN, dsName, dsPort, dsFolder, externalMicro, externalGateway, externalWebsocket, externalFrontPlugins, externalDex, externalDexID, externalDexSecret, frontendHosts, frontendLogin, frontendPassword, frontendRepeatPassword, fpmAddress, licenseRequired, licenseString, checkResults);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InstallInstallConfig {\n");
    
    sb.append("    internalUrl: ").append(toIndentedString(internalUrl)).append("\n");
    sb.append("    dbConnectionType: ").append(toIndentedString(dbConnectionType)).append("\n");
    sb.append("    dbTCPHostname: ").append(toIndentedString(dbTCPHostname)).append("\n");
    sb.append("    dbTCPPort: ").append(toIndentedString(dbTCPPort)).append("\n");
    sb.append("    dbTCPName: ").append(toIndentedString(dbTCPName)).append("\n");
    sb.append("    dbTCPUser: ").append(toIndentedString(dbTCPUser)).append("\n");
    sb.append("    dbTCPPassword: ").append(toIndentedString(dbTCPPassword)).append("\n");
    sb.append("    dbSocketFile: ").append(toIndentedString(dbSocketFile)).append("\n");
    sb.append("    dbSocketName: ").append(toIndentedString(dbSocketName)).append("\n");
    sb.append("    dbSocketUser: ").append(toIndentedString(dbSocketUser)).append("\n");
    sb.append("    dbSocketPassword: ").append(toIndentedString(dbSocketPassword)).append("\n");
    sb.append("    dbManualDSN: ").append(toIndentedString(dbManualDSN)).append("\n");
    sb.append("    dsName: ").append(toIndentedString(dsName)).append("\n");
    sb.append("    dsPort: ").append(toIndentedString(dsPort)).append("\n");
    sb.append("    dsFolder: ").append(toIndentedString(dsFolder)).append("\n");
    sb.append("    externalMicro: ").append(toIndentedString(externalMicro)).append("\n");
    sb.append("    externalGateway: ").append(toIndentedString(externalGateway)).append("\n");
    sb.append("    externalWebsocket: ").append(toIndentedString(externalWebsocket)).append("\n");
    sb.append("    externalFrontPlugins: ").append(toIndentedString(externalFrontPlugins)).append("\n");
    sb.append("    externalDex: ").append(toIndentedString(externalDex)).append("\n");
    sb.append("    externalDexID: ").append(toIndentedString(externalDexID)).append("\n");
    sb.append("    externalDexSecret: ").append(toIndentedString(externalDexSecret)).append("\n");
    sb.append("    frontendHosts: ").append(toIndentedString(frontendHosts)).append("\n");
    sb.append("    frontendLogin: ").append(toIndentedString(frontendLogin)).append("\n");
    sb.append("    frontendPassword: ").append(toIndentedString(frontendPassword)).append("\n");
    sb.append("    frontendRepeatPassword: ").append(toIndentedString(frontendRepeatPassword)).append("\n");
    sb.append("    fpmAddress: ").append(toIndentedString(fpmAddress)).append("\n");
    sb.append("    licenseRequired: ").append(toIndentedString(licenseRequired)).append("\n");
    sb.append("    licenseString: ").append(toIndentedString(licenseString)).append("\n");
    sb.append("    checkResults: ").append(toIndentedString(checkResults)).append("\n");
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

