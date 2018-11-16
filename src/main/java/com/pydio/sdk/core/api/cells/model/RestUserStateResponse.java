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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * RestUserStateResponse
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class RestUserStateResponse {
    @SerializedName("Workspaces")
    private List<IdmWorkspace> workspaces = null;

    @SerializedName("WorkspacesAccesses")
    private Map<String, String> workspacesAccesses = null;

    public RestUserStateResponse workspaces(List<IdmWorkspace> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    public RestUserStateResponse addWorkspacesItem(IdmWorkspace workspacesItem) {
        if (this.workspaces == null) {
            this.workspaces = new ArrayList<IdmWorkspace>();
        }
        this.workspaces.add(workspacesItem);
        return this;
    }

    /**
     * Get workspaces
     *
     * @return workspaces
     **/
    @ApiModelProperty(value = "")
    public List<IdmWorkspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<IdmWorkspace> workspaces) {
        this.workspaces = workspaces;
    }

    public RestUserStateResponse workspacesAccesses(Map<String, String> workspacesAccesses) {
        this.workspacesAccesses = workspacesAccesses;
        return this;
    }

    public RestUserStateResponse putWorkspacesAccessesItem(String key, String workspacesAccessesItem) {
        if (this.workspacesAccesses == null) {
            this.workspacesAccesses = new HashMap<String, String>();
        }
        this.workspacesAccesses.put(key, workspacesAccessesItem);
        return this;
    }

    /**
     * Get workspacesAccesses
     *
     * @return workspacesAccesses
     **/
    @ApiModelProperty(value = "")
    public Map<String, String> getWorkspacesAccesses() {
        return workspacesAccesses;
    }

    public void setWorkspacesAccesses(Map<String, String> workspacesAccesses) {
        this.workspacesAccesses = workspacesAccesses;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestUserStateResponse restUserStateResponse = (RestUserStateResponse) o;
        return Objects.equals(this.workspaces, restUserStateResponse.workspaces) &&
                Objects.equals(this.workspacesAccesses, restUserStateResponse.workspacesAccesses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaces, workspacesAccesses);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RestUserStateResponse {\n");

        sb.append("    workspaces: ").append(toIndentedString(workspaces)).append("\n");
        sb.append("    workspacesAccesses: ").append(toIndentedString(workspacesAccesses)).append("\n");
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

