package pydio.sdk.java.core.transport;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import pydio.sdk.java.core.http.ContentBody;
import pydio.sdk.java.core.http.PartialRepeatableEntity;
import pydio.sdk.java.core.http.HttpClient;
import pydio.sdk.java.core.http.HttpEntity;
import pydio.sdk.java.core.http.HttpResponse;
import pydio.sdk.java.core.model.ResolutionServer;
import pydio.sdk.java.core.security.Passwords;
import pydio.sdk.java.core.utils.HttpResponseParser;
import pydio.sdk.java.core.model.ServerNode;
import pydio.sdk.java.core.security.Crypto;
import pydio.sdk.java.core.utils.Log;
import pydio.sdk.java.core.utils.Pydio;
import pydio.sdk.java.core.utils.ServerResolution;

/**
 * This class handle a session with a pydio server
 * @author pydio
 *
 */
public class SessionTransport implements Transport {
    public String mIndex = "index.php?";
    public String mSecureToken = null;
    public String mUser;

    int mLastRequestStatus = Pydio.OK;
    boolean mAttemptedLogin, mAccessRefused, mLoggedIn = false;
    HttpClient mHttpClient;

    String mSeed;
    private ServerNode mServerNode;
    private String mAction;
    private boolean mRefreshingToken;

    String mRedirectedUrl;

    public SessionTransport(ServerNode server){
        this.mServerNode= server;
    }

    public SessionTransport(){}

    private URI getActionURI(String action){
        if(mServerNode instanceof ResolutionServer){
            try {
                ServerResolution.resolve((ResolutionServer) mServerNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String url = mServerNode.url();
        if(action != null && action.startsWith(Pydio.ACTION_CONF_PREFIX)){
            url += action;
        }else{
            url += mIndex;
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

        String captcha_code = mServerNode.getAuthenticationChallengeResponse();
        if(captcha_code == null || mSeed == null) {
            getSeed();
        }

        String password = Passwords.load(mServerNode.url(), mUser);
        if(!mSeed.trim().equals("-1")){
            try {
                password = Crypto.hexHash(Crypto.HASH_MD5, (Crypto.hexHash(Crypto.HASH_MD5, password.getBytes()) + mSeed).getBytes());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        Map<String, String> loginPass = new HashMap<>();
        if(captcha_code != null && !"".equals(captcha_code)) {
            loginPass.put(Pydio.PARAM_CAPTCHA_CODE, captcha_code);
        }

        loginPass.put("userid", mUser);
        loginPass.put("login_seed", mSeed);
        loginPass.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
        loginPass.put("password", "*****");
        Log.i("PYDIO SDK" , "[action=" + Pydio.ACTION_LOGIN + Log.paramString(loginPass) + "]");
        loginPass.put("password", password);


        Document doc = HttpResponseParser.getXML(request(getActionURI(Pydio.ACTION_LOGIN), loginPass, null));
        if(doc != null) {
            if (doc.getElementsByTagName("logging_result").getLength() > 0) {
                String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
                if (mLoggedIn = result.equals("1")) {
                    Log.i("PYDIO SDK", "[LOGIN OK]");
                    mLastRequestStatus = Pydio.OK;
                    String newToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem(Pydio.PARAM_SECURE_TOKEN).getNodeValue();
                    mSecureToken = newToken;

                } else {
                    mLastRequestStatus = Pydio.ERROR_AUTHENTICATION;
                    if (result.equals("-4")) {
                        Log.i("PYDIO SDK", "[ERROR CAPCHA REQUESTED]");
                        mLastRequestStatus = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                        mServerNode.setLastRequestResponseCode(Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA);
                        int status = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                        if(captcha_code != null){
                            loadCaptcha();
                        }
                        mLastRequestStatus = status;
                    } else {
                        Log.i("PYDIO SDK : ", "[LOGIN FAILED : " + result + "]");
                    }
                    mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                }
            } else {
                mLastRequestStatus = Pydio.ERROR_OTHER;
            }
        }
    }

    private void refreshSecureToken() throws IOException {
        mRefreshingToken = true;
        Log.i("PYDIO SDK", "[action=" + Pydio.ACTION_GET_TOKEN + "]");
        try {
            HttpResponse resp = request(this.getActionURI(Pydio.ACTION_GET_TOKEN), null, null);
            mSecureToken = "";
            String stringResponse = HttpResponseParser.getString(resp);
            //System.out.println(stringResponse);
            JSONObject jObject = new JSONObject(stringResponse);
            mSecureToken = jObject.getString(Pydio.PARAM_SECURE_TOKEN.toUpperCase());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mRefreshingToken = false;
    }

    private void getSeed() throws IOException{
        int savedStatus = mLastRequestStatus;
        try {
            Log.i("PYDIO SDK",  "[action=" + Pydio.ACTION_GET_SEED + "]");
            mAction = Pydio.ACTION_GET_SEED;
            HttpResponse resp = request(getActionURI(Pydio.ACTION_GET_SEED), null, null);
            mSeed = HttpResponseParser.getString(resp);

            if ("-1".equals(mSeed)) {
                return;
            }

            String seed = mSeed.trim();
            if (seed.contains("\"seed\":-1")) {
                mSeed = "-1";
            }

            if (seed.contains("\"captcha\": true") || seed.contains("\"captcha\":true")) {
                mLastRequestStatus = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                mServerNode.setLastRequestResponseCode(Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA);
            }

            if ("-1".equals(mSeed)) {
                return;
            }

            String contentType = resp.getHeaders("Content-Type").get(0);
            boolean seemsToBePydio = (contentType != null) && (
              (contentType.toLowerCase().contains("text/plain"))
            | (contentType.toLowerCase().contains("text/xml"))
            | (contentType.toLowerCase().contains("text/json"))
            | (contentType.toLowerCase().contains("application/json")));

            if (!seemsToBePydio) {
                mLastRequestStatus = Pydio.ERROR_NOT_A_SERVER;
                throw new IOException();
            }
            mSeed = "-1";
            mLastRequestStatus = savedStatus;
        }catch (IOException e){
            mLastRequestStatus = savedStatus;
            throw e;
        }
    }

    public void loadCaptcha() throws IOException {
        int status = mLastRequestStatus;
        try {
            boolean image = false;
            Log.i("PYDIO SDK",  "[action=" + Pydio.ACTION_CAPTCHA + "]");
            HttpResponse resp = getResponse(Pydio.ACTION_CAPTCHA, null);
            List<String> heads = resp.getHeaders("Content-type");
            for (int i = 0; i < heads.size(); i++) {
                if (heads.get(i).contains("image/png")) {
                    image = true;
                    break;
                }
            }
            if (image) {
                HttpEntity entity = resp.getEntity();
                if (entity != null) {
                    byte[] buffer = new byte[Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE];
                    int read;
                    InputStream in = entity.getContent();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    mServerNode.setChallengeData(out.toByteArray());
                    out.close();
                    in.close();
                }
            }
        }catch (Exception e){
            mLastRequestStatus = status;
            throw e;
        }
    }

    private boolean isSpecialActionRequested(HttpResponse response) throws IOException {
        PartialRepeatableEntity entity = (PartialRepeatableEntity) response.getEntity();
        final boolean[] is_required = {false};
        try {
            response.setEntity(entity);
            PartialRepeatableEntity.ContentStream stream = (PartialRepeatableEntity.ContentStream) entity.getContent();
            byte[] buffer = new byte[4096];
            int read = stream.partialRead(buffer);
            if(read == - 1) return false;
            String xmlString = new String(Arrays.copyOfRange(buffer, 0, read), "utf-8");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler dh = new DefaultHandler() {
                //<meta http-equiv="refresh" content="5; URL=http://www.manouvelleadresse.com">
                public boolean tag_repo = false, tag_auth = false, tag_msg = false, tag_meta = false, tag_auth_message = false;
                public String content;
                String xPath;

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
                    } else if(tag_meta){

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

                    if(tag_meta = qName.equals("meta")){
                        String equiv = attributes.getValue("http-equiv");
                        String content = attributes.getValue("xPath");
                        if("refresh".equals(equiv) && content != null){
                            int start = content.toLowerCase().indexOf("url=") + 4, end=content.toLowerCase().lastIndexOf("\"");
                            String newUrl = content.substring(start, end);
                            throw new SAXException ("redirect="+newUrl);
                        } else {

                        }
                    }

                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if(tag_repo && xPath != null || (tag_auth && qName.equals("require_auth"))){
                        is_required[0] = true;//SessionTransport.this.auth_step = "LOG-USER";
                        throw new SAXException("AUTH");
                    } else if(tag_meta && qName.equals("meta")){

                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    String str = new String(ch);
                    if (tag_msg){
                        if(str.toLowerCase().contains("you are not allowed to access")){
                            mAccessRefused = is_required[0] = true; //SessionTransport.this.auth_step = "RENEW-TOKEN";
                            throw new SAXException("TOKEN");
                        }
                    }
                }

                public void endDocument() throws SAXException {
                }
            };
            parser.parse(new InputSource(new StringReader(xmlString)), dh);

            //<meta http-equiv="refresh" content="5; URL=http://www.manouvelleadresse.com">
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            String m = e.getMessage();
            if("auth".equalsIgnoreCase(m)){
                mLastRequestStatus = Pydio.ERROR_AUTHENTICATION;
                mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                mRedirectedUrl = null;
            }else if ("token".equalsIgnoreCase(m)){
                mLastRequestStatus = Pydio.ERROR_OLD_AUTHENTICATION_TOKEN;
                mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                mRedirectedUrl = null;
            } else if(m.startsWith("redirect=")){
                mRedirectedUrl = m.substring(10);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!is_required[0]){
            mLastRequestStatus = Pydio.OK;
        }
        return is_required[0];
    }

    private HttpResponse request(URI uri, Map<String, String> params, ContentBody contentBody) throws IOException {
        if(mHttpClient == null){
            mHttpClient = new HttpClient(mServerNode.SSLUnverified());
        }

        if(params == null){
            params = new HashMap<String, String>();
        }

        if(mSecureToken != null) {
            //refreshSecureToken();
            params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
        }

        HttpResponse response;
        for(;;){
            try {
                response = mHttpClient.send(uri.toString(), params, contentBody);
            } catch (IOException e){
                //e.printStackTrace();

                if(e instanceof SSLException){
                    e.printStackTrace();
                    if(mServerNode.SSLUnverified()) {
                        mLastRequestStatus = Pydio.ERROR_UNVERIFIED_CERTIFICATE;
                        mServerNode.setLastRequestResponseCode(Pydio.ERROR_UNVERIFIED_CERTIFICATE);
                        throw e;
                    }
                    mHttpClient.enableUnverifiedMode(mServerNode.setSSLUnverified(true).getTrustHelper());
                    continue;
                }
                mLastRequestStatus = Pydio.ERROR_CON_FAILED;
                mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                throw e;
            }catch (Exception e){
                e.printStackTrace();
                if(e instanceof IllegalArgumentException && e.getMessage().toLowerCase().contains("unreachable")){
                    mLastRequestStatus = Pydio.ERROR_UNREACHABLE_HOST;
                    mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                }
                throw new IOException();
            }


            if(Arrays.asList(Pydio.no_auth_required_actions).contains(mAction)) return response;


            if(!isSpecialActionRequested(response)){
                boolean isNotAuthAction = Arrays.asList(Pydio.no_auth_required_actions).contains(mAction);
                if(! isNotAuthAction && mLastRequestStatus != Pydio.OK) {
                    mLastRequestStatus = Pydio.OK;
                    mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                }
                return response;

            }else{
                if(mLoggedIn && mAccessRefused){
                    mLastRequestStatus = Pydio.ERROR_ACCESS_REFUSED;
                    throw new IOException("access refused");
                }

                if (mLastRequestStatus == Pydio.ERROR_OLD_AUTHENTICATION_TOKEN) {
                    Log.i("PYDIO SDK",  "[ERROR INVALID TOKEN = " + mSecureToken + "]");
                    refreshSecureToken();
                    if("".equals(mSecureToken)){
                        throw new IOException("authentication required");
                    }
                    params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
                    mAttemptedLogin = true;
                    continue;
                }

                if (mLastRequestStatus == Pydio.ERROR_AUTHENTICATION) {
                    Log.i("PYDIO SDK", "ERROR AUTH REQUESTED");
                    getSeed();
                    if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA){
                        int status = mLastRequestStatus;
                        loadCaptcha();
                        mLastRequestStatus = status;
                        throw new IOException();
                    }

                    login();

                    if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA){
                        loadCaptcha();
                        throw new IOException();
                    }

                    if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION){
                        throw new IOException();
                    }

                    params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
                    mLastRequestStatus = Pydio.OK;
                    mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                    mAttemptedLogin = true;
                    continue;
                }

                if(mRedirectedUrl != null){

                }

                mLastRequestStatus = Pydio.ERROR_OTHER;
                mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                break;
            }
        }
        mServerNode.setLastRequestResponseCode(mLastRequestStatus);
        return response;
    }
    @Override
    public HttpResponse getResponse(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        return request(getActionURI(action), params, null);
    }
    @Override
    public String getStringContent(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        HttpResponse response  = this.request(this.getActionURI(action), params, null);
        if(response == null) return null;
        InputStream in = null;
        StringBuilder sb = new StringBuilder();
        int bufsize = Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE;
        return HttpResponseParser.getString(response);
    }
    @Override
    public Document getXmlContent(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        mAction = action;
        mLastRequestStatus = Pydio.OK;

        HttpResponse response = request(getActionURI(action), params, null);
        return HttpResponseParser.getXML(response);
    }
    @Override
    public JSONObject getJsonContent(String action, Map<String, String> params) {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        return null;
    }
    @Override
    public InputStream getResponseStream(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        HttpResponse response = request(getActionURI(action), params, null);
        return response.getEntity().getContent();
    }
    @Override
    public Document putContent( String action, Map<String, String> params, ContentBody contentBody) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        try {
            HttpResponse response = request(getActionURI(action), params, contentBody);
            return HttpResponseParser.getXML(response);
        }catch (Exception e){
        }
        return null;
    }
    @Override
    public void setServer(ServerNode server){
        this.mServerNode = server;
    }
    @Override
    public int requestStatus(){
        return mLastRequestStatus;
    }

}