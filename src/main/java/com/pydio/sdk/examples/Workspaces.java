package com.pydio.sdk.examples;

import com.pydio.sdk.core.api.p8.Configuration;
import com.pydio.sdk.core.api.p8.P8Client;
import com.pydio.sdk.core.api.p8.P8Request;
import com.pydio.sdk.core.api.p8.P8RequestBuilder;
import com.pydio.sdk.core.api.p8.P8Response;
import com.pydio.sdk.core.api.p8.auth.DefaultP8Credentials;
import com.pydio.sdk.core.api.p8.consts.Param;
import com.pydio.sdk.core.common.errors.Code;
import com.pydio.sdk.core.security.Credentials;

import org.w3c.dom.Document;

public class Workspaces {

    public static String login(P8Client client, Credentials credentials) {
        P8Response seedResponse = client.execute(P8RequestBuilder.getSeed().getRequest());
        String seed = seedResponse.toString();
        boolean withCaptcha = false;
        if (!"-1".equals(seed)) {
            seed = seed.trim();
            if (seed.contains("\"seed\":-1") || seed.contains("\"seed\": -1")) {
                withCaptcha = seed.contains("\"captcha\": true") || seed.contains("\"captcha\":true");
                seed = "-1";

            } else {
                String contentType = seedResponse.getHeaders("Content-Type").get(0);
                boolean seemsToBePydio = (contentType != null) && (
                        (contentType.toLowerCase().contains("text/plain"))
                                | (contentType.toLowerCase().contains("text/xml"))
                                | (contentType.toLowerCase().contains("text/json"))
                                | (contentType.toLowerCase().contains("application/json")));

                if (!seemsToBePydio) {
                    //throw SDKException.unexpectedContent(new IOException(seed));
                    return null;
                }
                seed = "-1";
            }
        }

        if (withCaptcha) {
            return null;
        }

        P8RequestBuilder builder = P8RequestBuilder.login(credentials);
        builder.setParam(Param.seed, seed);
        P8Response rsp = client.execute(builder.getRequest());

        final int code = rsp.code();
        if (code != Code.ok) {
            return null;
        }

        Document doc = rsp.toXMLDocument();
        if (doc != null) {
            if (doc.getElementsByTagName("logging_result").getLength() > 0) {
                String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
                if (result.equals("1")) {
                    return doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem(Param.secureToken).getNodeValue();
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.endpoint = "https://domain/path";
        config.selfSigned = false;
        config.userAgent = "Java-SDK-Example";

        P8Client client = new P8Client(config);
        P8Request req = P8RequestBuilder.workspaceList().setSecureToken(null).getRequest();
        P8Response rsp = client.execute(req);

        final int code = rsp.code();
        if (code != Code.ok) {
            if (code == Code.authentication_required) {
                String secureToken = login(client, new DefaultP8Credentials(config.endpoint, "username", (url, user) -> "password"));
                rsp = client.execute(P8RequestBuilder.workspaceList().setSecureToken(secureToken).getRequest());
            }
        }

        String workspacesStr = rsp.toString();
        System.out.println(workspacesStr);
    }
}
