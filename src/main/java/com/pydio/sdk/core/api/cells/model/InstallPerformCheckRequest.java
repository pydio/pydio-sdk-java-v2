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
 * InstallPerformCheckRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class InstallPerformCheckRequest {
    @SerializedName("Name")
    private String name = null;

    @SerializedName("Config")
    private InstallInstallConfig config = null;

    public InstallPerformCheckRequest name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @ApiModelProperty(value = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InstallPerformCheckRequest config(InstallInstallConfig config) {
        this.config = config;
        return this;
    }

    /**
     * Get config
     *
     * @return config
     **/
    @ApiModelProperty(value = "")
    public InstallInstallConfig getConfig() {
        return config;
    }

    public void setConfig(InstallInstallConfig config) {
        this.config = config;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallPerformCheckRequest installPerformCheckRequest = (InstallPerformCheckRequest) o;
        return Objects.equals(this.name, installPerformCheckRequest.name) &&
                Objects.equals(this.config, installPerformCheckRequest.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, config);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InstallPerformCheckRequest {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    config: ").append(toIndentedString(config)).append("\n");
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

