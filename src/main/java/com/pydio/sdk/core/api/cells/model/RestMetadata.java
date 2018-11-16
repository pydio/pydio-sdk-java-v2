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
 * RestMetadata
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class RestMetadata {
    @SerializedName("Namespace")
    private String namespace = null;

    @SerializedName("JsonMeta")
    private String jsonMeta = null;

    public RestMetadata namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Get namespace
     *
     * @return namespace
     **/
    @ApiModelProperty(value = "")
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public RestMetadata jsonMeta(String jsonMeta) {
        this.jsonMeta = jsonMeta;
        return this;
    }

    /**
     * Get jsonMeta
     *
     * @return jsonMeta
     **/
    @ApiModelProperty(value = "")
    public String getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(String jsonMeta) {
        this.jsonMeta = jsonMeta;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestMetadata restMetadata = (RestMetadata) o;
        return Objects.equals(this.namespace, restMetadata.namespace) &&
                Objects.equals(this.jsonMeta, restMetadata.jsonMeta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, jsonMeta);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RestMetadata {\n");

        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("    jsonMeta: ").append(toIndentedString(jsonMeta)).append("\n");
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
