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

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Gets or Sets UpdateUserMetaRequestUserMetaOp
 */
@JsonAdapter(UpdateUserMetaRequestUserMetaOp.Adapter.class)
public enum UpdateUserMetaRequestUserMetaOp {

    PUT("PUT"),

    DELETE("DELETE");

    private String value;

    UpdateUserMetaRequestUserMetaOp(String value) {
        this.value = value;
    }

    public static UpdateUserMetaRequestUserMetaOp fromValue(String text) {
        for (UpdateUserMetaRequestUserMetaOp b : UpdateUserMetaRequestUserMetaOp.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static class Adapter extends TypeAdapter<UpdateUserMetaRequestUserMetaOp> {
        @Override
        public void write(final JsonWriter jsonWriter, final UpdateUserMetaRequestUserMetaOp enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public UpdateUserMetaRequestUserMetaOp read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return UpdateUserMetaRequestUserMetaOp.fromValue(String.valueOf(value));
        }
    }
}

