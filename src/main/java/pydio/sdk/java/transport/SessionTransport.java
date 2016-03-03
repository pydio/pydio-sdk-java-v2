package pydio.sdk.java.transport;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import pydio.sdk.java.auth.AuthenticationHelper;
import pydio.sdk.java.auth.AuthenticationUtils;
import pydio.sdk.java.http.CustomEntity;
import pydio.sdk.java.http.HttpResponseParser;
import pydio.sdk.java.http.Requester;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.utils.CustomCertificateException;
import pydio.sdk.java.utils.Log;
import pydio.sdk.java.utils.Pydio;
import pydio.sdk.java.utils.ServerResolution;
import pydio.sdk.java.utils.UploadStopNotifierProgressListener;

/**
 * This class handle a session with a pydio server
 * @author pydio
 *
 */

public class SessionTransport implements Transport{

    public String index = "index.php?";
    public String secure_token = "";
    private ServerNode server;

    private String action;
    int request_status = Pydio.NO_ERROR;
    boolean attemptedLogin, accessRefused, loggedIn = false, trustSSL = false;
    AuthenticationHelper helper;
    Requester mRequester;

    public SessionTransport(ServerNode server){
        this.server = server;
    }
    public SessionTransport(){}
    private URI getActionURI(String action){
        ServerResolution.resolve(server);
        String url = server.url();
        if(action != null && action.startsWith(Pydio.ACTION_CONF_PREFIX)){
            url += action;
        }else{
            url += index;
            if(action != null && !"".equals(action)){
                url += Pydio.PARAM_GET_ACTION+"="+action;
            }
        }
        try{
            return new URI(url);
        }catch(Exception e){
            return null;
        }
    }


    public void login() throws IOException {
        HttpResponse resp = request(getActionURI(Pydio.ACTION_GET_SEED), null);
        if(request_status == Pydio.ERROR_CON_SSL_SELF_SIGNED_CERT || request_status == Pydio.ERROR_CON_FAILED_SSL){
            throw new IOException();
        }

        String seed = HttpResponseParser.getString(resp);
        boolean seemsToBePydio = false;
        Header[] headers = resp.getHeaders("Content-Type");

        for(int i = 0; i < headers.length; i++){
            Header h = headers[i];
            seemsToBePydio |= (h.getValue().toLowerCase().contains("text/plain"));
            seemsToBePydio |= (h.getValue().toLowerCase().contains("text/xml"));
            seemsToBePydio |= (h.getValue().toLowerCase().contains("text/json"));
            seemsToBePydio |= (h.getValue().toLowerCase().contains("application/json"));
        }

        if(seed == null || !seemsToBePydio){
            request_status = Pydio.ERROR_NOT_A_SERVER;
            return;
        }

        seed = seed.trim();
        if(!"-1".equals(seed) && !seed.contains("seed") && !seed.contains("require_auth")){
            request_status = Pydio.ERROR_NOT_A_SERVER;
            return;
        }

        if(seed.contains("\"seed\":-1")) {
            seed = "-1";
        }

        String[] c = helper.requestForLoginPassword();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(c[0], c[1]);
        String user = credentials.getUserName();
        String password = credentials.getPassword();

        if(!seed.trim().equals("-1")){
            password = AuthenticationUtils.processPydioPassword(password, seed);
        }
        Map<String, String> loginPass = new HashMap<>();
        String captcha_code = helper.getChallengeResponse();

        if(captcha_code != null && !"".equals(captcha_code)) {
            loginPass.put(Pydio.PARAM_CAPTCHA_CODE, captcha_code);
        }

        loginPass.put("userid", user);
        loginPass.put("login_seed", seed);
        loginPass.put("password", "*****");
        loginPass.put("Ajxp-Force-Login", "true");
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_LOGIN + Log.paramString(loginPass) + "]");
        loginPass.put("password", password);

        secure_token = "";
        Document doc = HttpResponseParser.getXML(request(getActionURI(Pydio.ACTION_LOGIN), loginPass));

        if(doc != null) {
            if (doc.getElementsByTagName("logging_result").getLength() > 0) {
                String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
                if (loggedIn = result.equals("1")) {
                    Log.info("PYDIO SDK : " + "[LOGIN OK]");
                    request_status = Pydio.NO_ERROR;
                    String newToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("secure_token").getNodeValue();
                    secure_token = newToken;
                } else {
                    request_status = Pydio.ERROR_AUTHENTICATION;
                    if (result.equals("-4")) {
                        Log.info("PYDIO SDK : " + "[ERROR CAPCHA REQUESTED]");
                        request_status = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                    } else {
                        Log.info("PYDIO SDK : " + "[LOGIN FAILED : " + result + "]");
                    }
                }
            } else {
                request_status = Pydio.ERROR_INTERNAL;
            }
        }
    }
    private void refreshToken() throws IOException {
        if(helper == null){
            return;
        }

        Requester req = new Requester(server);
        HttpResponse resp = req.issueRequest(this.getActionURI(Pydio.ACTION_GET_TOKEN), null);

        try {
            secure_token = "";
            JSONObject jObject = new JSONObject(HttpResponseParser.getString(resp));
            loggedIn = true;
            secure_token = jObject.getString("SECURE_TOKEN");
        }  catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e){
        }
    }


    private boolean isAuthenticationRequested(HttpResponse response) {
        HttpEntity ent = response.getEntity();

        final boolean[] is_required = {false};
        try {
            CustomEntity entity = new CustomEntity(ent);
            response.setEntity(entity);
            CustomEntity.ContentStream stream = (CustomEntity.ContentStream) entity.getContent();
            byte[] buffer = new byte[1024];
            int read = stream.safeRead(buffer);
            if(read == - 1) return false;
            String xmlString = new String(Arrays.copyOfRange(buffer, 0, read), "utf-8");
            //Log.info("PYDIO SDK : " + "[response head=" + xmlString.substring(0, Math.min(xmlString.length(), 150)) + "]");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            DefaultHandler dh = new DefaultHandler() {
                public boolean tag_repo = false, tag_auth = false, tag_msg = false, tag_auth_message = false;
                String xPath;
                public String content;

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if(tag_repo){
                        if(qName.equals(xPath)){
                            is_required[0] = false;
                            throw new SAXException("AUTH");
                        }
                        return;
                    }else if (tag_auth){
                        if(qName.equals("message")){
                            is_required[0] = true;
                            throw new SAXException("AUTH");
                        }
                        return;
                    }else if (tag_msg){
                        return;
                    }

                    boolean registryPart = qName.equals("ajxp_registry_part") && attributes.getValue("xPath") != null;
                    if(tag_repo = registryPart){
                        String attr = attributes.getValue("xPath");
                        if(attr != null){
                            String[] splits = attr.split("/");
                            xPath = splits[splits.length - 1];
                        }
                    }

                    tag_auth = qName.equals("require_auth");
                    tag_msg = qName.equals("message");
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if(tag_repo && xPath != null || (tag_auth && qName.equals("require_auth"))){
                        is_required[0] = true;//SessionTransport.this.auth_step = "LOG-USER";
                        throw new SAXException("AUTH");
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    String str = new String(ch);
                    if (tag_msg){
                        if(str.toLowerCase().contains("you are not allowed to access")){
                            accessRefused = is_required[0] = true; //SessionTransport.this.auth_step = "RENEW-TOKEN";
                            throw new SAXException("TOKEN");
                        }
                    }
                }

                public void endDocument() throws SAXException {
                }
            };
            parser.parse(new InputSource(new StringReader(xmlString)), dh);

        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            String m = e.getMessage();
            if("AUTH".equals(m)){
                request_status = Pydio.ERROR_AUTHENTICATION;
            }else if ("TOKEN".equals(m)){
                request_status = Pydio.ERROR_OLD_AUTHENTICATION_TOKEN;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!is_required[0]){
            request_status = Pydio.NO_ERROR;
        }
        return is_required[0];

    }
    private HttpResponse request(URI uri, Map<String, String> params) throws IOException {

        if(params == null){
            params = new HashMap<String, String>();
        }

        //load saved secure token if there is one
        if(!"".equals(secure_token))
            params.put("secure_token", secure_token);


        if(mRequester == null){
            mRequester = new Requester(server);
            mRequester.trustSSL = trustSSL;
        }

        if(mRequester.trustSSL && "http".equals(uri.getScheme())){
            mRequester = new Requester(server);
            mRequester.trustSSL = false;
        }

        HttpResponse response = null;
        for(;;){

            if(response != null){
                Log.info("PYDIO SDK : " + "re : [action=" + action + Log.paramString(params) + "]");
            }
            /*if(!"".equals(secure_token)){
                if(params == null) params = new HashMap<String, String>();
                params.put("secure_token", secure_token);
            }else{
                String key = helper.requestForLoginPassword()[0] + "@" + server.address();
                if(!key.endsWith("/")){
                    key += "/";
                }
                secure_token = PydioSecureTokenStore.getInstance().get(key);
                if(!"".equals(secure_token) && params != null)
                    params.put("secure_token", secure_token);
            }*/

            try {
                response = mRequester.issueRequest(uri, params);
                trustSSL = mRequester.trustSSL;

            } catch (IOException e){

                if(e instanceof HttpHostConnectException){
                    request_status = Pydio.ERROR_CON_FAILED;
                    return null;
                }

                if(e instanceof SSLPeerUnverifiedException){
                    mRequester.setTrustSSL(true);
                    continue;

                }

                if ( e instanceof  SSLHandshakeException){
                    request_status = Pydio.ERROR_CON_SSL_SELF_SIGNED_CERT;

                    Exception cause = (Exception) e.getCause();
                    if(cause == null) {
                        return null;
                    }

                    if(cause instanceof CustomCertificateException){
                        helper.setCertificate(((CustomCertificateException) e.getCause()).cert);
                        return null;
                    }

                    Exception causeCause = (Exception) cause.getCause();

                    if(causeCause != null && causeCause instanceof CertPathValidatorException && !mRequester.trustSSL){
                        if(!mRequester.trustSSL) {
                            CertPathValidatorException ex = (CertPathValidatorException) cause.getCause();
                            helper.setCertificate((X509Certificate) ex.getCertPath().getCertificates().get(0));
                            mRequester.trustSSL = true;
                            continue;
                        }
                        throw e;
                    }
                    return null;

                }

                if (e instanceof SSLException) {
                    request_status = Pydio.ERROR_CON_FAILED_SSL;
                    throw e;
                }

                request_status = Pydio.ERROR_CON_FAILED;
                throw e;


            }catch (Exception e){
                if(e instanceof IllegalArgumentException && e.getMessage().toLowerCase().contains("unreachable")){
                    request_status = Pydio.ERROR_UNREACHABLE_HOST;
                    return null;
                }

                e.printStackTrace();
                return null;
            }

            if(Arrays.asList(Pydio.no_auth_required_actions).contains(action)) return response;

            if(!isAuthenticationRequested(response)){
                boolean isNotAuthAction = Arrays.asList(Pydio.no_auth_required_actions).contains(this.action);
                if(! isNotAuthAction && request_status != Pydio.NO_ERROR) {
                    request_status = Pydio.NO_ERROR;
                }
                return response;

            }else{
                try {
                    if(loggedIn && accessRefused){
                        request_status = Pydio.ERROR_ACCESS_REFUSED;
                        throw new IOException("access refused");
                    }

                    if (request_status == Pydio.ERROR_OLD_AUTHENTICATION_TOKEN) {
                        Log.info("PYDIO SDK : " + "[ERROR INVALID TOKEN = " + secure_token + "]");
                        refreshToken();
                        if("".equals(secure_token)){
                            throw new IOException("authentication required");
                        }
                        params.put("secure_token", secure_token);
                        attemptedLogin = true;
                        continue;
                    }

                    if (request_status == Pydio.ERROR_AUTHENTICATION) {
                        Log.info("PYDIO SDK : " + "[ERROR AUTH REQUESTED]");

                        login();
                        if(!loggedIn){
                            throw new IOException("authentication required");
                        }
                        request_status = Pydio.NO_ERROR;
                        params.put("secure_token", secure_token);
                        attemptedLogin = true;
                        continue;
                    }

                    request_status = Pydio.ERROR_INTERNAL;
                    break;

                }catch (Exception e){
                    return null;
                }
            }
        }
        return response;
    }

    //*****************************************
    //     TRANSPORT OVERRIDEN METHODS
    //*****************************************
    public HttpResponse getResponse(String action, Map<String, String> params) throws IOException {
        accessRefused = false;
        loggedIn = false;
        attemptedLogin = false;
        this.action = action;
        return request(getActionURI(action), params);
    }

    public String getStringContent(String action, Map<String, String> params) throws IOException {
        accessRefused = false;
        loggedIn = false;
        attemptedLogin = false;
        this.action = action;
        HttpResponse response  = this.request(this.getActionURI(action), params);
        return HttpResponseParser.getString(response);
    }

    public Document getXmlContent(String action, Map<String, String> params) throws IOException {
        accessRefused = false;
        loggedIn = false;
        attemptedLogin = false;
        this.action = action;
        HttpResponse response  = this.request(this.getActionURI(action), params);
        return HttpResponseParser.getXML(response);
    }

    public JSONObject getJsonContent(String action, Map<String, String> params) {
        accessRefused = false;
        loggedIn = false;
        attemptedLogin = false;
        this.action = action;
        return null;
    }

    public InputStream getResponseStream(String action, Map<String, String> params) throws IOException {
        accessRefused = false;
        loggedIn = false;
        attemptedLogin = false;
        this.action = action;
        return request(getActionURI(action), params).getEntity().getContent();
    }

    public Document putContent( String action, Map<String, String> params, File file, String filename, UploadStopNotifierProgressListener listener) throws IOException {
        accessRefused = false;
        loggedIn = false;
        attemptedLogin = false;
        this.action = action;
        mRequester.clearUploadData();
        mRequester.setFile(file);
        mRequester.setProgressListener(listener);
        mRequester.setFilename(filename);

        if(!"".equals(secure_token)){
            params.put("secure_token", secure_token);
        }

        HttpResponse response = null;
        response = request(getActionURI(action), params);

        mRequester.setFile(null);
        mRequester.setProgressListener(null);
        mRequester.setFilename(null);
        return HttpResponseParser.getXML(response);
    }

    public Document putContent(String action, Map<String, String> params, byte[] data, String filename, UploadStopNotifierProgressListener listener) {
        accessRefused = false;
        loggedIn = false;
        attemptedLogin = false;
        this.action = action;
        mRequester.clearUploadData();
        return null;
    }

    public void setServer(ServerNode server){
        this.server = server;
    }
    @Override
    public int requestStatus(){
        return request_status;
    }
    @Override
    public void setAuthenticationHelper(AuthenticationHelper helper) {
        this.helper = helper;
    }
}