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
 * ServiceResourcePolicyQuery
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-11-30T14:51:15.861Z")
public class ServiceResourcePolicyQuery {
    @SerializedName("Subjects")
    private List<String> subjects = null;

    @SerializedName("Empty")
    private Boolean empty = null;

    @SerializedName("Any")
    private Boolean any = null;

    public ServiceResourcePolicyQuery subjects(List<String> subjects) {
        this.subjects = subjects;
        return this;
    }

    public ServiceResourcePolicyQuery addSubjectsItem(String subjectsItem) {
        if (this.subjects == null) {
            this.subjects = new ArrayList<String>();
        }
        this.subjects.add(subjectsItem);
        return this;
    }

    /**
     * Get subjects
     *
     * @return subjects
     **/
    @ApiModelProperty(value = "")
    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public ServiceResourcePolicyQuery empty(Boolean empty) {
        this.empty = empty;
        return this;
    }

    /**
     * Get empty
     *
     * @return empty
     **/
    @ApiModelProperty(value = "")
    public Boolean isEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public ServiceResourcePolicyQuery any(Boolean any) {
        this.any = any;
        return this;
    }

    /**
     * Get any
     *
     * @return any
     **/
    @ApiModelProperty(value = "")
    public Boolean isAny() {
        return any;
    }

    public void setAny(Boolean any) {
        this.any = any;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceResourcePolicyQuery serviceResourcePolicyQuery = (ServiceResourcePolicyQuery) o;
        return Objects.equals(this.subjects, serviceResourcePolicyQuery.subjects) &&
                Objects.equals(this.empty, serviceResourcePolicyQuery.empty) &&
                Objects.equals(this.any, serviceResourcePolicyQuery.any);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjects, empty, any);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceResourcePolicyQuery {\n");

        sb.append("    subjects: ").append(toIndentedString(subjects)).append("\n");
        sb.append("    empty: ").append(toIndentedString(empty)).append("\n");
        sb.append("    any: ").append(toIndentedString(any)).append("\n");
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

