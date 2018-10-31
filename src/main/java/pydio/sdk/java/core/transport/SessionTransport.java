package pydio.sdk.java.core.transport;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import pydio.sdk.java.core.http.ContentBody;
import pydio.sdk.java.core.http.HttpClient;
import pydio.sdk.java.core.http.HttpEntity;
import pydio.sdk.java.core.http.HttpResponse;
import pydio.sdk.java.core.http.PartialRepeatableEntity;
import pydio.sdk.java.core.model.ServerNode;
import pydio.sdk.java.core.security.Crypto;
import pydio.sdk.java.core.security.Passwords;
import pydio.sdk.java.core.utils.HttpResponseParser;
import pydio.sdk.java.core.utils.Pydio;
import pydio.sdk.java.core.utils.ServerResolution;

/**
 * This class handle a session with a pydio server
 * @author pydio
 *
 */
public class SessionTransport implements Transport {
    public String mSecureToken = null;
    public String mUser;

    private int mLastRequestStatus = Pydio.OK;
    private boolean mAccessRefused, mLoggedIn = false, ssIdRefreshed = false;
    private HttpClient mHttpClient;
    private CookieManager mCookieManager;

    private String mSeed;
    private ServerNode mServerNode;
    private String mAction;

    private String mResolvedServerAddress;
    private String mHttpRedirectedUrl;

    SessionTransport(ServerNode server){
        this.mServerNode= server;
    }

    SessionTransport(){}

    public String getGETUrl(String action, Map<String, String> params) throws IOException {
        String u = mServerNode.url();
        if (!u.endsWith("/")){
            u += "/";
        }

        StringBuilder url = new StringBuilder(u);
        if(!url.toString().startsWith("http")){
            url = new StringBuilder(mResolvedServerAddress = ServerResolution.resolve(url.toString(), false));
        }

        url.append("index.php?");
        url.append(Pydio.PARAM_GET_ACTION + "=").append(action);

        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
        url.append("&");
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String name = entry.getKey(), value = entry.getValue();
            url.append(name).append("=").append(URLEncoder.encode(value, "utf-8"));
            url.append("&");
        }

        List<HttpCookie> cookies = mHttpClient.mCookieManager.getCookieStore().getCookies();
        for(HttpCookie c : cookies){
            url.append("ajxp_sessid=").append(URLEncoder.encode(c.getValue(), "utf-8"));
        }
        return url.toString();
    }

    public void setCookieManager(CookieManager m){
        mCookieManager = m;
    }

    private URI getActionURI(String action) throws IOException{
        String url = mServerNode.url();
        if(!url.endsWith("/")) {
            url = url + "/";
        }

        if(!url.startsWith("http")){
            url = mResolvedServerAddress = ServerResolution.resolve(url, false);
        }

        if(action != null && action.startsWith(Pydio.ACTION_CONF_PREFIX)){
            url += action;

        }else{
            String mIndex = "index.php?";

            url += mIndex;
            if(action != null && !"".equals(action)){
                url += Pydio.PARAM_GET_ACTION+"="+action;
            }
        }

        try{return new URI(url);}catch(Exception e){return null;}
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
                //Log.e("System", e.getMessage());
            }
        }

        Map<String, String> loginPass = new HashMap<>();
        if(captcha_code != null && !"".equals(captcha_code)) {
            loginPass.put(Pydio.PARAM_CAPTCHA_CODE, captcha_code);
        }

        loginPass.put("userid", mUser);
        loginPass.put("password", "*****");
        //Log.i("Request" , "[action=" + Pydio.ACTION_LOGIN + //Log.paramString(loginPass) + "]");
        loginPass.put("login_seed", mSeed);
        loginPass.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
        loginPass.put("password", password);


        Document doc = HttpResponseParser.getXML(request(getActionURI(Pydio.ACTION_LOGIN), loginPass, null));
        if(doc != null) {
            if (doc.getElementsByTagName("logging_result").getLength() > 0) {
                String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
                if (mLoggedIn = result.equals("1")) {
                    mLastRequestStatus = Pydio.OK;
                    mSecureToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem(Pydio.PARAM_SECURE_TOKEN).getNodeValue();

                } else {
                    mLastRequestStatus = Pydio.ERROR_AUTHENTICATION;
                    //Log.i("Response", "[Login failed with result code " + result + "]");
                    if (result.equals("-4")) {
                        mLastRequestStatus = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                        mServerNode.setLastRequestResponseCode(Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA);
                        mLastRequestStatus = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                    }
                    mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                }
            } else {
                mLastRequestStatus = Pydio.ERROR_OTHER;
            }
        }
    }

    private void refreshSecureToken() throws IOException {
        //Log.i("Request", "[action=" + Pydio.ACTION_GET_TOKEN + "]");
        try {
            HttpResponse resp = request(this.getActionURI(Pydio.ACTION_GET_TOKEN), null, null);
            mSecureToken = "";
            String stringResponse = HttpResponseParser.getString(resp);
            JSONObject jObject = new JSONObject(stringResponse);
            mSecureToken = jObject.getString(Pydio.PARAM_SECURE_TOKEN.toUpperCase());
        } catch (ParseException e) {
            //Log.e("Incoherent state", e.getMessage());
        }
    }

    private void getSeed() throws IOException{
        int savedStatus = mLastRequestStatus;
        try {
            //Log.i("Request",  "[action=" + Pydio.ACTION_GET_SEED + "]");
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

    public InputStream loadCaptcha() throws IOException {
        int status = mLastRequestStatus;
        HttpResponse resp = getResponse(Pydio.ACTION_CAPTCHA, null);
        HttpEntity entity = resp.getEntity();
        mLastRequestStatus = status;
        return entity.getContent();
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
            //Log.i("response", xmlString);
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
            //Log.w("Warning", e.getMessage());
        } catch (SAXException e) {
            String m = e.getMessage();
            if("auth".equalsIgnoreCase(m)){
                mLastRequestStatus = Pydio.ERROR_AUTHENTICATION;
                mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                mHttpRedirectedUrl = null;
            }else if ("token".equalsIgnoreCase(m)){
                mLastRequestStatus = Pydio.ERROR_OLD_AUTHENTICATION_TOKEN;
                mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                mHttpRedirectedUrl = null;
            } else if(m.startsWith("redirect=")){
                mHttpRedirectedUrl = m.substring(10);
                return true;
            }
        }catch (Exception e){
            //Log.w("Warning", e.getMessage());
        }

        if(!is_required[0]){
            mLastRequestStatus = Pydio.OK;
        }
        return is_required[0];
    }

    private HttpResponse request(URI uri, Map<String, String> params, ContentBody contentBody) throws IOException {
        if(mHttpClient == null){
            mHttpClient = new HttpClient(mServerNode.unVerifiedSSL(), mCookieManager);
        }

        if(params == null){
            params = new HashMap<String, String>();
        }

        if(mSecureToken != null) {
            //refreshSecureToken();
            //Log.i("SECURE_TOKEN", mSecureToken);
            params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
        }

        HttpResponse response;
        for(;;){
            try {
                response = mHttpClient.send(uri.toString(), params, contentBody);
            } catch (IOException e){
                if(e instanceof SSLException){
                    if(mServerNode.unVerifiedSSL()) {
                        mLastRequestStatus = Pydio.ERROR_UNVERIFIED_CERTIFICATE;
                        mServerNode.setLastRequestResponseCode(Pydio.ERROR_UNVERIFIED_CERTIFICATE);
                        throw e;
                    }
                    mHttpClient.enableUnverifiedMode(mServerNode.setUnverifiedSSL(true).getTrustHelper());
                    continue;
                }

                //Log.e("CONNECTION", e.getMessage());
                mLastRequestStatus = Pydio.ERROR_CON_FAILED;
                mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                throw e;
            }catch (Exception e){
                if(e instanceof IllegalArgumentException && e.getMessage().toLowerCase().contains("unreachable")){
                    //Log.e("CONNECTION", "Unreachable host");
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
                    if(ssIdRefreshed) {
                        mLastRequestStatus = Pydio.ERROR_ACCESS_REFUSED;
                        throw new IOException("access refused");
                    } else {
                        ssIdRefreshed = true;
                        mLoggedIn = mAccessRefused = false;
                        mHttpClient.mCookieManager.getCookieStore().removeAll();

                        getSeed();
                        if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA){
                            throw new IOException();
                        }

                        login();

                        if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA){
                            throw new IOException();
                        }

                        if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION){
                            throw new IOException();
                        }

                        params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
                        mLastRequestStatus = Pydio.OK;
                        mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                        continue;
                    }
                }

                if (mLastRequestStatus == Pydio.ERROR_OLD_AUTHENTICATION_TOKEN) {
                    //Log.e("Response",  "Invalid session token");
                    refreshSecureToken();
                    if("".equals(mSecureToken)){
                        throw new IOException("authentication required");
                    }
                    params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
                    continue;
                }

                if (mLastRequestStatus == Pydio.ERROR_AUTHENTICATION) {
                    //Log.e("Response", "Authentication is required");
                    getSeed();
                    if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA){
                        throw new IOException();
                    }

                    login();

                    if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA){
                        throw new IOException();
                    }

                    if(mLastRequestStatus == Pydio.ERROR_AUTHENTICATION){
                        throw new IOException();
                    }

                    params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
                    mLastRequestStatus = Pydio.OK;
                    mServerNode.setLastRequestResponseCode(mLastRequestStatus);
                    continue;
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
        /*mAccessRefused = false;
        mLoggedIn = false;
        ssIdRefreshed = false;*/
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        return request(getActionURI(action), params, null);
    }
    @Override
    public String getStringContent(String action, Map<String, String> params) throws IOException {
        /*mAccessRefused = false;
        mLoggedIn = false;
        ssIdRefreshed = false;*/
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
        /*mAccessRefused = false;
        mLoggedIn = false;
        ssIdRefreshed = false;*/
        mAction = action;
        mLastRequestStatus = Pydio.OK;

        HttpResponse response = request(getActionURI(action), params, null);
        return HttpResponseParser.getXML(response);
    }
    @Override
    public JSONObject getJsonContent(String action, Map<String, String> params) {
        /*mAccessRefused = false;
        mLoggedIn = false;
        ssIdRefreshed = false;*/
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        return null;
    }
    @Override
    public InputStream getResponseStream(String action, Map<String, String> params) throws IOException {
        /*mAccessRefused = false;
        mLoggedIn = false;
        ssIdRefreshed = false;*/
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        HttpResponse response = request(getActionURI(action), params, null);
        return response.getEntity().getContent();
    }
    @Override
    public HttpResponse putContent( String action, Map<String, String> params, ContentBody contentBody) throws IOException {
        /*mAccessRefused = false;
        mLoggedIn = false;
        ssIdRefreshed = false;*/
        mAction = action;
        mLastRequestStatus = Pydio.OK;
        URI uriAction = getActionURI(action);
        return request(uriAction, params, contentBody);
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