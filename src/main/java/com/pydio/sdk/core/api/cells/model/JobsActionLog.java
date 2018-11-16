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
 * JobsActionLog
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class JobsActionLog {
    @SerializedName("Action")
    private JobsAction action = null;

    @SerializedName("InputMessage")
    private JobsActionMessage inputMessage = null;

    @SerializedName("OutputMessage")
    private JobsActionMessage outputMessage = null;

    public JobsActionLog action(JobsAction action) {
        this.action = action;
        return this;
    }

    /**
     * Get action
     *
     * @return action
     **/
    @ApiModelProperty(value = "")
    public JobsAction getAction() {
        return action;
    }

    public void setAction(JobsAction action) {
        this.action = action;
    }

    public JobsActionLog inputMessage(JobsActionMessage inputMessage) {
        this.inputMessage = inputMessage;
        return this;
    }

    /**
     * Get inputMessage
     *
     * @return inputMessage
     **/
    @ApiModelProperty(value = "")
    public JobsActionMessage getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(JobsActionMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    public JobsActionLog outputMessage(JobsActionMessage outputMessage) {
        this.outputMessage = outputMessage;
        return this;
    }

    /**
     * Get outputMessage
     *
     * @return outputMessage
     **/
    @ApiModelProperty(value = "")
    public JobsActionMessage getOutputMessage() {
        return outputMessage;
    }

    public void setOutputMessage(JobsActionMessage outputMessage) {
        this.outputMessage = outputMessage;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JobsActionLog jobsActionLog = (JobsActionLog) o;
        return Objects.equals(this.action, jobsActionLog.action) &&
                Objects.equals(this.inputMessage, jobsActionLog.inputMessage) &&
                Objects.equals(this.outputMessage, jobsActionLog.outputMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, inputMessage, outputMessage);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JobsActionLog {\n");

        sb.append("    action: ").append(toIndentedString(action)).append("\n");
        sb.append("    inputMessage: ").append(toIndentedString(inputMessage)).append("\n");
        sb.append("    outputMessage: ").append(toIndentedString(outputMessage)).append("\n");
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

