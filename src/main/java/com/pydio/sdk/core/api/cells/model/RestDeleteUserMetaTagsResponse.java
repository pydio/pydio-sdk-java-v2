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
 * RestDeleteUserMetaTagsResponse
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class RestDeleteUserMetaTagsResponse {
    @SerializedName("Success")
    private Boolean success = null;

    public RestDeleteUserMetaTagsResponse success(Boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Get success
     *
     * @return success
     **/
    @ApiModelProperty(value = "")
    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestDeleteUserMetaTagsResponse restDeleteUserMetaTagsResponse = (RestDeleteUserMetaTagsResponse) o;
        return Objects.equals(this.success, restDeleteUserMetaTagsResponse.success);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RestDeleteUserMetaTagsResponse {\n");

        sb.append("    success: ").append(toIndentedString(success)).append("\n");
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

