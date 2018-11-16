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
 * EncryptionAdminExportKeyRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class EncryptionAdminExportKeyRequest {
    @SerializedName("KeyID")
    private String keyID = null;

    @SerializedName("StrPassword")
    private String strPassword = null;

    public EncryptionAdminExportKeyRequest keyID(String keyID) {
        this.keyID = keyID;
        return this;
    }

    /**
     * Get keyID
     *
     * @return keyID
     **/
    @ApiModelProperty(value = "")
    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public EncryptionAdminExportKeyRequest strPassword(String strPassword) {
        this.strPassword = strPassword;
        return this;
    }

    /**
     * Get strPassword
     *
     * @return strPassword
     **/
    @ApiModelProperty(value = "")
    public String getStrPassword() {
        return strPassword;
    }

    public void setStrPassword(String strPassword) {
        this.strPassword = strPassword;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EncryptionAdminExportKeyRequest encryptionAdminExportKeyRequest = (EncryptionAdminExportKeyRequest) o;
        return Objects.equals(this.keyID, encryptionAdminExportKeyRequest.keyID) &&
                Objects.equals(this.strPassword, encryptionAdminExportKeyRequest.strPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyID, strPassword);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EncryptionAdminExportKeyRequest {\n");

        sb.append("    keyID: ").append(toIndentedString(keyID)).append("\n");
        sb.append("    strPassword: ").append(toIndentedString(strPassword)).append("\n");
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

