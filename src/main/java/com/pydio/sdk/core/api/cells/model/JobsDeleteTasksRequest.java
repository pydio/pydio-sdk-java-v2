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
 * JobsDeleteTasksRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-09-25T16:31:30.189Z")
public class JobsDeleteTasksRequest {
    @SerializedName("JobId")
    private String jobId = null;

    @SerializedName("TaskID")
    private List<String> taskID = null;

    @SerializedName("Status")
    private List<JobsTaskStatus> status = null;

    @SerializedName("PruneLimit")
    private Integer pruneLimit = null;

    public JobsDeleteTasksRequest jobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    /**
     * Get jobId
     *
     * @return jobId
     **/
    @ApiModelProperty(value = "")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobsDeleteTasksRequest taskID(List<String> taskID) {
        this.taskID = taskID;
        return this;
    }

    public JobsDeleteTasksRequest addTaskIDItem(String taskIDItem) {
        if (this.taskID == null) {
            this.taskID = new ArrayList<String>();
        }
        this.taskID.add(taskIDItem);
        return this;
    }

    /**
     * Get taskID
     *
     * @return taskID
     **/
    @ApiModelProperty(value = "")
    public List<String> getTaskID() {
        return taskID;
    }

    public void setTaskID(List<String> taskID) {
        this.taskID = taskID;
    }

    public JobsDeleteTasksRequest status(List<JobsTaskStatus> status) {
        this.status = status;
        return this;
    }

    public JobsDeleteTasksRequest addStatusItem(JobsTaskStatus statusItem) {
        if (this.status == null) {
            this.status = new ArrayList<JobsTaskStatus>();
        }
        this.status.add(statusItem);
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @ApiModelProperty(value = "")
    public List<JobsTaskStatus> getStatus() {
        return status;
    }

    public void setStatus(List<JobsTaskStatus> status) {
        this.status = status;
    }

    public JobsDeleteTasksRequest pruneLimit(Integer pruneLimit) {
        this.pruneLimit = pruneLimit;
        return this;
    }

    /**
     * Get pruneLimit
     *
     * @return pruneLimit
     **/
    @ApiModelProperty(value = "")
    public Integer getPruneLimit() {
        return pruneLimit;
    }

    public void setPruneLimit(Integer pruneLimit) {
        this.pruneLimit = pruneLimit;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JobsDeleteTasksRequest jobsDeleteTasksRequest = (JobsDeleteTasksRequest) o;
        return Objects.equals(this.jobId, jobsDeleteTasksRequest.jobId) &&
                Objects.equals(this.taskID, jobsDeleteTasksRequest.taskID) &&
                Objects.equals(this.status, jobsDeleteTasksRequest.status) &&
                Objects.equals(this.pruneLimit, jobsDeleteTasksRequest.pruneLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, taskID, status, pruneLimit);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JobsDeleteTasksRequest {\n");

        sb.append("    jobId: ").append(toIndentedString(jobId)).append("\n");
        sb.append("    taskID: ").append(toIndentedString(taskID)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    pruneLimit: ").append(toIndentedString(pruneLimit)).append("\n");
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

