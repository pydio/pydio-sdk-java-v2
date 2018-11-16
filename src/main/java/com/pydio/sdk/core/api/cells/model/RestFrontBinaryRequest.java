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
 * RestFrontBinaryRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class RestFrontBinaryRequest {
    @SerializedName("BinaryType")
    private String binaryType = null;

    @SerializedName("Uuid")
    private String uuid = null;

    public RestFrontBinaryRequest binaryType(String binaryType) {
        this.binaryType = binaryType;
        return this;
    }

    /**
     * Get binaryType
     *
     * @return binaryType
     **/
    @ApiModelProperty(value = "")
    public String getBinaryType() {
        return binaryType;
    }

    public void setBinaryType(String binaryType) {
        this.binaryType = binaryType;
    }

    public RestFrontBinaryRequest uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Get uuid
     *
     * @return uuid
     **/
    @ApiModelProperty(value = "")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestFrontBinaryRequest restFrontBinaryRequest = (RestFrontBinaryRequest) o;
        return Objects.equals(this.binaryType, restFrontBinaryRequest.binaryType) &&
                Objects.equals(this.uuid, restFrontBinaryRequest.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(binaryType, uuid);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RestFrontBinaryRequest {\n");

        sb.append("    binaryType: ").append(toIndentedString(binaryType)).append("\n");
        sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
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

