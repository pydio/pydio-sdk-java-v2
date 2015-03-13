package pydio.sdk.java.transport;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import pydio.sdk.java.auth.AuthenticationUtils;
import pydio.sdk.java.auth.CredentialsProvider;
import pydio.sdk.java.http.CustomEntity;
import pydio.sdk.java.http.HttpResponseParser;
import pydio.sdk.java.http.Requester;
import pydio.sdk.java.http.XMLDocEntity;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.model.WorkspaceNode;
import pydio.sdk.java.utils.ProgressListener;
import pydio.sdk.java.utils.Pydio;
import pydio.sdk.java.utils.ServerResolution;

//import org.json.JSONException;

/**
 * This class handle a session with a pydio server
 * @author pydio
 *
 */
public class SessionTransport implements Transport{
    public String index = "index.php?";
    public String secure_token = "";
    public String auth_step = "";
    public String repositoryId = "";

    private ServerNode server;
    private WorkspaceNode workspace;
    CredentialsProvider cp;

    String user = "";
    String password ="";

    boolean loginStateChanged = false;
    boolean skipAuth = false;

    public SessionTransport(ServerNode server){
        this.server = server;
    }

    public SessionTransport(){

    }

    private URI getActionURI(String action){
        ServerResolution.resolve(server);
        String url = server.url()+index+ Pydio.PARAM_GET_ACTION+"="+action;
        try{
            return new URI(url);
        }catch(Exception e){}
        return null;
    }

    private String getSeed() {
        Requester req = new Requester(server);
        HttpResponse resp = req.issueRequest(this.getActionURI(Pydio.AUTH_GET_SEED), null);
        return HttpResponseParser.getString(resp);
    }

    public void refreshToken(){

        Requester req = new Requester(server);
        if("".equals(user) || "".equals(password)){
            //throw Message.create(1, 1, "require credentials");
        }
        Map<String, String> loginPass = new HashMap<String, String>();

        if(!skipAuth && !"".equals(user) && !"".equals(password)){
            String seed = getSeed();
            if(seed != null) seed = seed.trim();

            if(seed.indexOf("captcha") > -1){
                //throw Message.create(1,1, "CAPTCHA");
            }

            if(!seed.trim().equals("-1")){
                AuthenticationUtils.processPydioPassword(password, seed);
            }

            loginPass.put("login_seed", seed);
            loginPass.put("Ajxp-Force-Login", "true");
            loginPass.put("userid", user);
            loginPass.put("password", password);
        }

        HttpResponse resp = req.issueRequest(this.getActionURI(Pydio.AUTH_GET_TOKEN), loginPass);
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

    private void authenticate() {

        Requester req = new Requester(this.server);
        String seed = getSeed();

        if(seed != null) seed = seed.trim();
        if(seed.indexOf("captcha") > -1) {
            //throw Message.create(1,1, "CAPTCHA");
        }

        if("".equals(user) && "".equals(password)){
            UsernamePasswordCredentials credentials = cp.requestForLoginPassword();
            user = credentials.getUserName();
            password = credentials.getPassword();
        }

        if(!seed.trim().equals("-1")){
            password = AuthenticationUtils.processPydioPassword(password, seed);
        }

        Map<String, String> loginPass = new HashMap<String, String>();
        loginPass.put("userid", user);
        loginPass.put("password", password);
        loginPass.put("login_seed", seed);
        loginPass.put("Ajxp-Force-Login", "true");

        req = new Requester(server);
        Document doc = HttpResponseParser.getXML(req.issueRequest(this.getActionURI("login"), loginPass));
        if(doc.getElementsByTagName("logging_result").getLength() > 0){
            String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
            if(result.equals("1")){
                String newToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("secure_token").getNodeValue();
                loginStateChanged = true;
                secure_token = newToken;
            }else{
                //authentication failed
            }
        }
    }
    @SuppressWarnings("deprecation")
    private boolean isAuthenticationRequested(HttpResponse response) {

        Header[] heads = response.getHeaders("Content-type");
        boolean xml = false;

        for (int i = 0; i < heads.length; i++) {
            if (heads[i].getValue().contains("text/xml")) xml = true;
        }

        if (!xml || loginStateChanged) return false;
        //if(!xml) return false;

        HttpEntity ent = response.getEntity();
        if (ent.getClass() == XMLDocEntity.class) {
            Document doc;
            try {
                doc = ((XMLDocEntity) ent).getDoc();
                ((XMLDocEntity) ent).toLogger();
                if (doc.getElementsByTagName("ajxp_registry_part").getLength() > 0
                        && doc.getDocumentElement().getAttribute("xPath").equals("user/repositories")
                        && doc.getElementsByTagName("repositories").getLength() == 0) {
                    //Log.d("RestRequest Authentication", "EMPTY REGISTRY : AUTH IS REQUIRED");
                    this.auth_step = "LOG-USER";
                    return true;
                }

                if (doc.getElementsByTagName("message").getLength() > 0) {
                    if (doc.getElementsByTagName("message").item(0).getFirstChild().getNodeValue().trim().contains("You are not allowed to access this resource.")) {
                        //Log.d("RestRequest Authentication", "REQUIRE_AUTH TAG : TOKEN IS REQUIRED");
                        this.auth_step = "RENEW-TOKEN";
                        return true;
                    }
                }

                if (doc.getElementsByTagName("require_auth").getLength() > 0) {
                    //Log.d("RestRequest Authentication", "REQUIRE_AUTH TAG : AUTH IS REQUIRED");
                    /*if (doc.getElementsByTagName("message").getLength() > 0) {
                        throw new Exception(doc.getElementsByTagName("message").item(0).getFirstChild().getNodeValue().trim());
                    }*/
                    this.auth_step = "LOG-USER";
                    return true;
                }
            } catch (SAXException sax) {

            } catch (DOMException e) {
                // TODO Auto-generated catch block
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            return false;
        } else {

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
                                SessionTransport.this.auth_step = "LOG-USER";
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
                        if(tag_repo && qName.equals("ajxp_registry_part")){
                            is_required[0] = true;
                            SessionTransport.this.auth_step = "LOG-USER";
                            throw new SAXException("AUTH");
                        }
                    }

                    public void characters(char ch[], int start, int length) throws SAXException {
                        String str = new String(ch, start, length).trim();
                        if (tag_auth && tag_auth_message){
                            if("You are not allowed to access this resource.".equals(str)){
                                is_required[0] = true;
                                SessionTransport.this.auth_step = "RENEW-TOKEN";
                                throw new SAXException("AUTH");
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
            }catch (Exception e){
                e.printStackTrace();
            }
            return is_required[0];
        }
    }

    private HttpResponse request(Requester req, URI uri, Map<String, String> params){
        if(req == null){
            req = new Requester(server);
        }

        HttpResponse response = null;

        for(;;){
            if(!"".equals(secure_token)){
                if(params == null) params = new HashMap<String, String>();
                params.put("secure_token", secure_token);
            }
            response = req.issueRequest(uri, params);

            if(isAuthenticationRequested(response)){
                if(auth_step.equals("RENEW-TOKEN")) {
                    refreshToken();
                }else if (auth_step.equals("LOG-USER")){
                    authenticate();
                    if(loginStateChanged){
                        loginStateChanged = false;
                    }else{
                        user = "";
                        password ="";
                    }
                }
            }else{
                break;
            }
        }
        return response;
    }


    //*****************************************
    //     TRANSPORT OVERRIDEN METHODS
    //*****************************************
    public HttpResponse getResponse(String action, Map<String, String> params) {
        return request(null, getActionURI(action), params);
    }

    public String getStringContent(String action, Map<String, String> params) {
        HttpResponse response  = this.request(null, this.getActionURI(action), params);
        return HttpResponseParser.getString(response);
    }

    public Document getXmlContent(String action, Map<String, String> params){
        HttpResponse response  = this.request(null, this.getActionURI(action), params);
        return HttpResponseParser.getXML(response);
    }

    public JSONObject getJsonContent(String action, Map<String, String> params) {     return null;  }

    public InputStream getResponseStream(String action, Map<String, String> params) {return null;}

    public Document putContent( String action, Map<String, String> params, File file, String filename, ProgressListener listener) {
        Requester req = new Requester(server);
        req.setFile(file);
        req.setProgressListener(listener);
        req.setFilename(filename);
        if(!"".equals(secure_token)){
            params.put("secure_token", secure_token);
        }
        HttpResponse response = req.issueRequest(getActionURI(action), params);
        return HttpResponseParser.getXML(response);
    }

    public Document putContent(String action, Map<String, String> params, byte[] data, String filename, ProgressListener listener) {
        return null;
    }

    public void setServer(ServerNode server){
        this.server = server;
    }

    public void setWorkspace(WorkspaceNode w){
        this.workspace = w;
    }
    @Override
    public void setCredentialsProvider(CredentialsProvider cp) {
        this.cp = cp;
    }

}