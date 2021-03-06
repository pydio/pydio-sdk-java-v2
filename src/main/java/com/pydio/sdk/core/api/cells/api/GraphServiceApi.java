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


package com.pydio.sdk.core.api.cells.api;

import com.google.gson.reflect.TypeToken;
import com.pydio.sdk.core.api.cells.ApiCallback;
import com.pydio.sdk.core.api.cells.ApiClient;
import com.pydio.sdk.core.api.cells.ApiException;
import com.pydio.sdk.core.api.cells.ApiResponse;
import com.pydio.sdk.core.api.cells.Configuration;
import com.pydio.sdk.core.api.cells.Pair;
import com.pydio.sdk.core.api.cells.ProgressRequestBody;
import com.pydio.sdk.core.api.cells.ProgressResponseBody;
import com.pydio.sdk.core.api.cells.model.RestRelationResponse;
import com.pydio.sdk.core.api.cells.model.RestUserStateResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphServiceApi {
    private ApiClient apiClient;

    public GraphServiceApi() {
        this(Configuration.getDefaultApiClient());
    }

    public GraphServiceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for relation
     * @param userId  (required)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to encode the request body object
     */
    public com.squareup.okhttp.Call relationCall(String userId, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/graph/relation/{UserId}"
                .replaceAll("\\{" + "UserId" + "\\}", apiClient.escapeString(userId));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] {  };
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call relationValidateBeforeCall(String userId, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException("Missing the required parameter 'userId' when calling relation(Async)");
        }
        

        com.squareup.okhttp.Call call = relationCall(userId, progressListener, progressRequestListener);
        return call;

    }

    /**
     * Compute relation of context user with another user
     * 
     * @param userId  (required)
     * @return RestRelationResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot decode the response body
     */
    public RestRelationResponse relation(String userId) throws ApiException {
        ApiResponse<RestRelationResponse> resp = relationWithHttpInfo(userId);
        return resp.getData();
    }

    /**
     * Compute relation of context user with another user
     * 
     * @param userId  (required)
     * @return ApiResponse&lt;RestRelationResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot decode the response body
     */
    public ApiResponse<RestRelationResponse> relationWithHttpInfo(String userId) throws ApiException {
        com.squareup.okhttp.Call call = relationValidateBeforeCall(userId, null, null);
        Type localVarReturnType = new TypeToken<RestRelationResponse>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Compute relation of context user with another user (asynchronously)
     * 
     * @param userId  (required)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call relationAsync(String userId, final ApiCallback<RestRelationResponse> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = relationValidateBeforeCall(userId, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<RestRelationResponse>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for userState
     * @param segment  (required)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to encode the request body object
     */
    public com.squareup.okhttp.Call userStateCall(String segment, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/graph/state/{Segment}"
                .replaceAll("\\{" + "Segment" + "\\}", apiClient.escapeString(segment));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] {  };
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call userStateValidateBeforeCall(String segment, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        
        // verify the required parameter 'segment' is set
        if (segment == null) {
            throw new ApiException("Missing the required parameter 'segment' when calling userState(Async)");
        }
        

        com.squareup.okhttp.Call call = userStateCall(segment, progressListener, progressRequestListener);
        return call;

    }

    /**
     * Compute accessible workspaces for a given user
     * 
     * @param segment  (required)
     * @return RestUserStateResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot decode the response body
     */
    public RestUserStateResponse userState(String segment) throws ApiException {
        ApiResponse<RestUserStateResponse> resp = userStateWithHttpInfo(segment);
        return resp.getData();
    }

    /**
     * Compute accessible workspaces for a given user
     * 
     * @param segment  (required)
     * @return ApiResponse&lt;RestUserStateResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot decode the response body
     */
    public ApiResponse<RestUserStateResponse> userStateWithHttpInfo(String segment) throws ApiException {
        com.squareup.okhttp.Call call = userStateValidateBeforeCall(segment, null, null);
        Type localVarReturnType = new TypeToken<RestUserStateResponse>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Compute accessible workspaces for a given user (asynchronously)
     * 
     * @param segment  (required)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call userStateAsync(String segment, final ApiCallback<RestUserStateResponse> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = userStateValidateBeforeCall(segment, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<RestUserStateResponse>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
}
