package pydio.sdk.java.transport;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
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
import pydio.sdk.java.utils.ProgressListener;
import pydio.sdk.java.utils.Pydio;
import pydio.sdk.java.utils.PydioSecureTokenStore;
import pydio.sdk.java.utils.ServerResolution;


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
    boolean loginStateChanged = false;

    AuthenticationHelper helper;

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

    private String getSeed() throws IOException {
        Requester req = new Requester(server);
        HttpResponse resp = null;
        resp = req.issueRequest(this.getActionURI(Pydio.ACTION_GET_SEED), null);
        return HttpResponseParser.getString(resp);
    }

    private void authenticate() throws IOException {
        Requester req;
        String seed = getSeed();
        if(seed.indexOf("\"seed\":-1") != -1 || seed.contains("require_auth")){
            seed = "-1";
        }
        if(seed != null) seed = seed.trim();

        if(helper == null){
            return;
        }
        String[] c = helper.requestForLoginPassword();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(c[0], c[1]);
        String user = credentials.getUserName();
        String password = credentials.getPassword();

        if(!seed.trim().equals("-1")){
            password = AuthenticationUtils.processPydioPassword(password, seed);
        }

        Map<String, String> loginPass = new HashMap<>();
        if(this.request_status == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA) {
            String captcha_code = helper.getChallengeResponse();
            if(captcha_code == null) {
                return;
            }
            loginPass.put(Pydio.PARAM_CAPTCHA_CODE, captcha_code);
        }

        loginPass.put("userid", user);
        loginPass.put("password", password);
        loginPass.put("login_seed", seed);
        loginPass.put("Ajxp-Force-Login", "true");

        req = new Requester(server);
        Document doc = null;
        doc = HttpResponseParser.getXML(req.issueRequest(this.getActionURI("login"), loginPass));
        if(doc.getElementsByTagName("logging_result").getLength() > 0){
            String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
            if(result.equals("1")){
                request_status = Pydio.NO_ERROR;
                String newToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("secure_token").getNodeValue();
                loginStateChanged = true;
                secure_token = newToken;
                String key = helper.requestForLoginPassword()[0] + "@" + server.host() + server.path();
                PydioSecureTokenStore.getInstance().add(key, secure_token);
            }else{
                request_status = Pydio.ERROR_AUTHENTICATION;
                if (result.equals("-4")){
                    request_status = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                }
            }
        }else{
            request_status = Pydio.ERROR_INTERNAL;
        }
    }

    private void refreshToken() throws IOException {
        if(helper == null){
            return;
        }
        Requester req = new Requester(server);
        Map<String, String> loginPass = new HashMap<String, String>();
        String[] c = helper.requestForLoginPassword();
        String login = c[0];
        String pass = c[1];

        String seed = getSeed();
        if(seed != null) seed = seed.trim();
        if(!seed.trim().equals("-1") && !seed.contains("You are not allowed")){
            pass = AuthenticationUtils.processPydioPassword(pass, seed);
        }

        loginPass.put("login_seed", seed);
        loginPass.put("Ajxp-Force-Login", "true");
        loginPass.put("userid", login);
        loginPass.put("password", pass);

        HttpResponse resp = null;
        resp = req.issueRequest(this.getActionURI(Pydio.ACTION_GET_TOKEN), loginPass);

        JSONObject jObject;
        try {
            jObject = new JSONObject(HttpResponseParser.getString(resp));
            loginStateChanged = true;
            secure_token = jObject.getString("SECURE_TOKEN");
        }  catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e){

        }
    }

    private boolean isAuthenticationRequested(HttpResponse response) {

        /*Header[] heads = response.getHeaders("Content-type");
        boolean xml = false;

        for (int i = 0; i < heads.length; i++) {
            if (heads[i].getValue().contains("text/xml")) xml = true;
        }

        if (!xml || loginStateChanged) return false;
        //if(!xml) return false;*/

        HttpEntity ent = response.getEntity();

        final boolean[] is_required = {false};
        try {
            CustomEntity entity = new CustomEntity(ent);
            response.setEntity(entity);
            CustomEntity.ContentStream stream = (CustomEntity.ContentStream) entity.getContent();
            byte[] buffer = new byte[200];
            int read = stream.safeRead(buffer);
            String xmlString = new String(Arrays.copyOfRange(buffer, 0, read), "UTF-8");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            DefaultHandler dh = new DefaultHandler() {
                public boolean tag_repo = false, tag_auth = false, tag_msg = false, tag_auth_message = false;
                public String content;

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if(tag_repo){
                        if(qName.equals("repositories")){
                            is_required[0] = false;
                            throw new SAXException("AUTH");
                        }
                        return;
                    }else if (tag_auth){
                        if(qName.equals("message")){
                            //SessionTransport.this.auth_step = "LOG-USER";
                            is_required[0] = true;
                            throw new SAXException("AUTH");
                        }
                        return;
                    }else if (tag_msg){
                        return;
                    }
                    tag_repo = qName.equals("ajxp_registry_part") && attributes.getValue("xPath") != null && attributes.getValue("xPath").equals("user/repositories");
                    tag_auth = qName.equals("require_auth");
                    tag_msg = qName.equals("message");
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if(tag_repo && qName.equals("ajxp_registry_part") || (tag_auth && qName.equals("require_auth"))){
                        is_required[0] = true;//SessionTransport.this.auth_step = "LOG-USER";
                        throw new SAXException("AUTH");
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    String str = new String(ch);
                    if (tag_msg){
                        if("You are not allowed to access this resource.".equals(str)){
                            is_required[0] = true; //SessionTransport.this.auth_step = "RENEW-TOKEN";
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

    private HttpResponse request(Requester req, URI uri, Map<String, String> params){
        if(!Arrays.asList(Pydio.no_auth_required_actions).contains(this.action)){
            if((request_status == Pydio.ERROR_AUTHENTICATION || request_status == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA)){
                try {
                    authenticate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(request_status == Pydio.ERROR_OLD_AUTHENTICATION_TOKEN){
                try {
                    refreshToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(req == null){
            req = new Requester(server);
        }
        HttpResponse response = null;
        for(;;){
            if((request_status == Pydio.ERROR_OLD_AUTHENTICATION_TOKEN || request_status == Pydio.ERROR_AUTHENTICATION || request_status == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA) && !Arrays.asList(Pydio.no_auth_required_actions).contains(this.action)){
                break;
            }
            if(!"".equals(secure_token)){
                if(params == null) params = new HashMap<String, String>();
                params.put("secure_token", secure_token);
            }else{
                String key = helper.requestForLoginPassword()[0] + "@" + server.host() + server.path();
                secure_token = PydioSecureTokenStore.getInstance().get(key);
                if(!"".equals(secure_token))
                    params.put("secure_token", secure_token);
            }

            try {
                response = req.issueRequest(uri, params);
            }catch (IOException e){
                if(e instanceof SSLPeerUnverifiedException){
                    server.setSelSigned(true);
                    req.setTrustSSL(true);
                    continue;
                }else if ( e instanceof  SSLHandshakeException){
                    request_status = Pydio.ERROR_CON_SSL_SELF_SIGNED_CERT;
                    try{
                        helper.setCertificate(((CustomCertificateException)e.getCause()).cert);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }

                    return null;
                }else if (e instanceof SSLException) {
                    request_status = Pydio.ERROR_CON_FAILED_SSL;
                    return null;
                }else{
                    request_status = Pydio.ERROR_CON_FAILED;
                    return null;
                }
            }catch (Exception e){
                if(e instanceof IllegalArgumentException && e.getMessage().toLowerCase().contains("unreachable")){
                    request_status = Pydio.ERROR_UNREACHABLE_HOST;
                }
                return null;
            }

            if(!isAuthenticationRequested(response)){
                boolean isNotAuthAction = Arrays.asList(Pydio.no_auth_required_actions).contains(this.action);
                if(! isNotAuthAction && request_status != Pydio.NO_ERROR) {
                    request_status = Pydio.NO_ERROR;
                }
                return response;
            }else{
                try {
                    if (request_status == Pydio.ERROR_OLD_AUTHENTICATION_TOKEN) {
                        String key = helper.requestForLoginPassword()[0] + "@" + server.host() + server.path();
                        if("".equals(secure_token)){
                            secure_token = PydioSecureTokenStore.getInstance().get(key);
                            request_status = Pydio.NO_ERROR;
                        }else if (!secure_token.equals(PydioSecureTokenStore.getInstance().get(key))){
                            refreshToken();
                        }
                    } else if (request_status == Pydio.ERROR_AUTHENTICATION) {
                        authenticate();
                    }
                    continue;
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

    public HttpResponse getResponse(String action, Map<String, String> params) {
        this.action = action;
        return request(null, getActionURI(action), params);
    }

    public String getStringContent(String action, Map<String, String> params) {
        this.action = action;
        HttpResponse response  = this.request(null, this.getActionURI(action), params);
        return HttpResponseParser.getString(response);
    }

    public Document getXmlContent(String action, Map<String, String> params){
        this.action = action;
        HttpResponse response  = this.request(null, this.getActionURI(action), params);
        return HttpResponseParser.getXML(response);
    }

    public JSONObject getJsonContent(String action, Map<String, String> params) {
        this.action = action;
        return null;
    }

    public InputStream getResponseStream(String action, Map<String, String> params) {
        this.action = action;
        try {
            return request(null, getActionURI(action), params).getEntity().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document putContent( String action, Map<String, String> params, File file, String filename, ProgressListener listener) {
        this.action = action;
        Requester req = new Requester(server);
        req.setFile(file);
        req.setProgressListener(listener);
        req.setFilename(filename);
        if(!"".equals(secure_token)){
            params.put("secure_token", secure_token);
        }
        HttpResponse response = null;
        response = request(req, getActionURI(action), params);
        return HttpResponseParser.getXML(response);
    }

    public Document putContent(String action, Map<String, String> params, byte[] data, String filename, ProgressListener listener) {
        this.action = action;
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