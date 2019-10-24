package com.pydio.sdk.examples;

import com.pydio.sdk.core.Client;
import com.pydio.sdk.core.api.p8.Configuration;
import com.pydio.sdk.core.api.p8.P8Client;
import com.pydio.sdk.core.api.p8.P8Request;
import com.pydio.sdk.core.api.p8.P8RequestBuilder;
import com.pydio.sdk.core.api.p8.P8Response;
import com.pydio.sdk.core.api.p8.auth.DefaultP8Credentials;
import com.pydio.sdk.core.api.p8.consts.Param;
import com.pydio.sdk.core.common.errors.Code;
import com.pydio.sdk.core.common.errors.Error;
import com.pydio.sdk.core.common.errors.SDKException;
import com.pydio.sdk.core.model.ServerNode;

import org.w3c.dom.Document;

import java.io.IOException;

public class Workspaces {


    public static void main(String[] args) {
        ServerNode node = new ServerNode();
        Error error = node.resolve("https://server-address");
        if (error != null) {
            // handle code here
            System.out.println(error);
            return;
        }
        Client client = Client.get(node);
        client.setCredentials(new Credentials("login", "password"));
        try {
            client.workspaceList((n) -> System.out.println(n.label()));
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }
}
