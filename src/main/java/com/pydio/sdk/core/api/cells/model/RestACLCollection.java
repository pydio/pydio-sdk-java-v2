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
 * RestACLCollection
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class RestACLCollection {
    @SerializedName("ACLs")
    private List<IdmACL> acLs = null;

    @SerializedName("Total")
    private Integer total = null;

    public RestACLCollection acLs(List<IdmACL> acLs) {
        this.acLs = acLs;
        return this;
    }

    public RestACLCollection addAcLsItem(IdmACL acLsItem) {
        if (this.acLs == null) {
            this.acLs = new ArrayList<IdmACL>();
        }
        this.acLs.add(acLsItem);
        return this;
    }

    /**
     * Get acLs
     *
     * @return acLs
     **/
    @ApiModelProperty(value = "")
    public List<IdmACL> getAcLs() {
        return acLs;
    }

    public void setAcLs(List<IdmACL> acLs) {
        this.acLs = acLs;
    }

    public RestACLCollection total(Integer total) {
        this.total = total;
        return this;
    }

    /**
     * Get total
     *
     * @return total
     **/
    @ApiModelProperty(value = "")
    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestACLCollection restACLCollection = (RestACLCollection) o;
        return Objects.equals(this.acLs, restACLCollection.acLs) &&
                Objects.equals(this.total, restACLCollection.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acLs, total);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RestACLCollection {\n");

        sb.append("    acLs: ").append(toIndentedString(acLs)).append("\n");
        sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

