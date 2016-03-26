package pydio.sdk.java.transport;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
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
import pydio.sdk.java.http.CountingMultipartRequestEntity;
import pydio.sdk.java.http.CustomEntity;
import pydio.sdk.java.http.HttpResponseParser;
import pydio.sdk.java.http.Requester;
import pydio.sdk.java.http.UploadFileBody;
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

    public String mIndex = "index.php?";
    public String mSecureToken = "";
    private ServerNode mServerNode;

    int mLastRequestStatus = Pydio.NO_ERROR;
    private String action;
    boolean mAttemptedLogin, mAccessRefused, mLoggedIn = false, mTrustSSL = false;

    AuthenticationHelper mHelper;
    Requester mRequester;
    String mSeed;

    public ByteArrayOutputStream mCaptchaBytes;

    public SessionTransport(ServerNode server){
        this.mServerNode= server;
        mCaptchaBytes = null;
    }

    public SessionTransport(){}

    private URI getActionURI(String action){
        ServerResolution.resolve(mServerNode);
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
        String[] c = mHelper.requestForLoginPassword();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(c[0], c[1]);
        String user = credentials.getUserName();
        String password = credentials.getPassword();

        if(!mSeed.trim().equals("-1")){
            password = AuthenticationUtils.processPydioPassword(password, mSeed);
        }
        Map<String, String> loginPass = new HashMap<>();
        String captcha_code = mHelper.getChallengeResponse();

        if(captcha_code != null && !"".equals(captcha_code)) {
            loginPass.put(Pydio.PARAM_CAPTCHA_CODE, captcha_code);
        }

        loginPass.put("userid", user);
        loginPass.put("login_seed", mSeed);
        loginPass.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
        loginPass.put("password", "*****");
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_LOGIN + Log.paramString(loginPass) + "]");
        loginPass.put("password", password);

        mCaptchaBytes = null;
        mSeed = "";

        Document doc = HttpResponseParser.getXML(request(getActionURI(Pydio.ACTION_LOGIN), loginPass, null));
        if(doc != null) {
            if (doc.getElementsByTagName("logging_result").getLength() > 0) {
                String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
                if (mLoggedIn = result.equals("1")) {
                    Log.info("PYDIO SDK : " + "[LOGIN OK]");
                    mLastRequestStatus = Pydio.NO_ERROR;
                    String newToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem(Pydio.PARAM_SECURE_TOKEN).getNodeValue();
                    mSecureToken = newToken;
                } else {

                    mLastRequestStatus = Pydio.ERROR_AUTHENTICATION;
                    if (result.equals("-4")) {
                        Log.info("PYDIO SDK : " + "[ERROR CAPCHA REQUESTED]");
                        mLastRequestStatus = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
                        loadCaptcha();
                    } else {
                        Log.info("PYDIO SDK : " + "[LOGIN FAILED : " + result + "]");
                    }
                    throw  new IOException();
                }
            } else {
                mLastRequestStatus = Pydio.ERROR_INTERNAL;
            }
        }
    }

    private void refreshToken() throws IOException {
        Requester req = new Requester(mServerNode);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_GET_TOKEN + "]");
        HttpResponse resp = req.issueRequest(this.getActionURI(Pydio.ACTION_GET_TOKEN), null, null);

        try {
            mSecureToken = "";
            JSONObject jObject = new JSONObject(HttpResponseParser.getString(resp));
            mLoggedIn = true;
            mSecureToken = jObject.getString(Pydio.PARAM_SECURE_TOKEN);
            Log.info("PYDIO SDK : " + "[token=" + mSecureToken + "]");
        }  catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e){}
    }

    private void getSeed() throws IOException{
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_GET_SEED + "]");
        HttpResponse resp = request(getActionURI(Pydio.ACTION_GET_SEED), null, null);
        mSeed = HttpResponseParser.getString(resp);
        //Log.info("PYDIO SDK : " + "[seed=" + mSeed + "]");

        if("-1".equals(mSeed)){
            return;
        }

        String seed = mSeed;
        mSeed = null;

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
            mLastRequestStatus = Pydio.ERROR_NOT_A_SERVER;
            throw new IOException();
        }

        seed = seed.trim();

        if(seed.contains("\"seed\":-1")) {
            mSeed = "-1";
        }

        if(seed.contains("\"captcha\": true") || seed.contains("\"captcha\":true")){
            loadCaptcha();
            mLastRequestStatus = Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA;
            throw new IOException();
        }
    }

    private void loadCaptcha() throws IOException {
        boolean image = false;
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_CAPTCHA + "]");
        HttpResponse resp = getResponse(Pydio.ACTION_CAPTCHA, null);
        Header[] heads = resp.getHeaders("Content-type");
        for (int i = 0; i < heads.length; i++) {
            if (heads[i].getValue().contains("image/png")) {
                image = true;
                break;
            }
        }
        if (image){
            HttpEntity entity = resp.getEntity();
            if(entity != null){
                byte[] buffer = new byte[Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE];
                int read;
                InputStream in = entity.getContent();
                mCaptchaBytes = new ByteArrayOutputStream();
                while((read = in.read(buffer, 0, buffer.length)) != -1){
                    mCaptchaBytes.write(buffer, 0, read);
                }
                in.close();
            }
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
                            mAccessRefused = is_required[0] = true; //SessionTransport.this.auth_step = "RENEW-TOKEN";
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
            if("auth".equalsIgnoreCase(m)){
                mLastRequestStatus = Pydio.ERROR_AUTHENTICATION;
            }else if ("token".equalsIgnoreCase(m)){
                mLastRequestStatus = Pydio.ERROR_OLD_AUTHENTICATION_TOKEN;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!is_required[0]){
            mLastRequestStatus = Pydio.NO_ERROR;
        }
        return is_required[0];

    }

    private HttpResponse request(URI uri, Map<String, String> params, UploadFileBody fileBody) throws IOException {

        if(params == null){
            params = new HashMap<String, String>();
        }


        if(!"".equals(mSecureToken))
            params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);


        if(mRequester == null){
            mRequester = new Requester(mServerNode);
            mRequester.setTrustSSL(mTrustSSL);
        }

        if(mRequester.isTrustSSL() && "http".equals(uri.getScheme())){
            mRequester = new Requester(mServerNode);
            mRequester.setTrustSSL(false);
        }

        HttpResponse response = null;
        for(;;){

            if(response != null){
                Log.info("PYDIO SDK : (retry) [action=" + action + Log.paramString(params) + "]");
            }

            try {
                response = mRequester.issueRequest(uri, params, fileBody);
                mTrustSSL = mRequester.isTrustSSL();

            } catch (IOException e){

                if(e instanceof HttpHostConnectException){
                    mLastRequestStatus = Pydio.ERROR_CON_FAILED;
                    return null;
                }

                if(e instanceof SSLPeerUnverifiedException){
                    if(!mRequester.isTrustSSL()) {
                        mRequester.setTrustSSL(true);
                        continue;
                    }
                    throw e;
                }

                if ( e instanceof  SSLHandshakeException){
                    mLastRequestStatus = Pydio.ERROR_CON_SSL_SELF_SIGNED_CERT;

                    Exception cause = (Exception) e.getCause();
                    if(cause == null) {
                        return null;
                    }

                    if(cause instanceof CustomCertificateException){
                        mHelper.setCertificate(((CustomCertificateException) e.getCause()).cert);
                        return null;
                    }

                    Exception causeCause = (Exception) cause.getCause();

                    if(causeCause != null && causeCause instanceof CertPathValidatorException && !mRequester.isTrustSSL()){
                        if(!mRequester.isTrustSSL()) {
                            CertPathValidatorException ex = (CertPathValidatorException) cause.getCause();
                            mHelper.setCertificate((X509Certificate) ex.getCertPath().getCertificates().get(0));
                            mRequester.setTrustSSL(true);
                            continue;
                        }
                        throw e;
                    }
                    return null;
                }

                if (e instanceof SSLException) {
                    mLastRequestStatus = Pydio.ERROR_CON_FAILED_SSL;
                    throw e;
                }

                mLastRequestStatus = Pydio.ERROR_CON_FAILED;
                throw e;


            }catch (Exception e){
                if(e instanceof IllegalArgumentException && e.getMessage().toLowerCase().contains("unreachable")){
                    mLastRequestStatus = Pydio.ERROR_UNREACHABLE_HOST;
                    return null;
                }
                e.printStackTrace();
                return null;
            }

            if(Arrays.asList(Pydio.no_auth_required_actions).contains(action)) return response;

            if(!isAuthenticationRequested(response)){
                boolean isNotAuthAction = Arrays.asList(Pydio.no_auth_required_actions).contains(this.action);
                if(! isNotAuthAction && mLastRequestStatus != Pydio.NO_ERROR) {
                    mLastRequestStatus = Pydio.NO_ERROR;
                }
                return response;

            }else{
                try {
                    if(mLoggedIn && mAccessRefused){
                        mLastRequestStatus = Pydio.ERROR_ACCESS_REFUSED;
                        throw new IOException("access refused");
                    }

                    if (mLastRequestStatus == Pydio.ERROR_OLD_AUTHENTICATION_TOKEN) {
                        Log.info("PYDIO SDK : " + "[ERROR INVALID TOKEN = " + mSecureToken + "]");
                        refreshToken();
                        if("".equals(mSecureToken)){
                            throw new IOException("authentication required");
                        }
                        params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
                        mAttemptedLogin = true;
                        continue;
                    }

                    if (mLastRequestStatus == Pydio.ERROR_AUTHENTICATION) {
                        Log.info("PYDIO SDK : " + "[ERROR AUTH REQUESTED]");
                        mRequester.clearCookies();
                        getSeed();
                        login();
                        params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
                        mLastRequestStatus = Pydio.NO_ERROR;
                        mAttemptedLogin = true;
                        continue;
                    }

                    mLastRequestStatus = Pydio.ERROR_INTERNAL;
                    break;

                }catch (Exception e){
                    return null;
                }
            }
        }
        return response;
    }


    //*****************************************
    //     TRANSPORT METHODS
    //*****************************************
    public HttpResponse getResponse(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        this.action = action;
        return request(getActionURI(action), params, null);
    }

    public String getStringContent(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        this.action = action;
        HttpResponse response  = this.request(this.getActionURI(action), params, null);
        return HttpResponseParser.getString(response);
    }

    public Document getXmlContent(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        this.action = action;
        HttpResponse response  = this.request(this.getActionURI(action), params, null);
        return HttpResponseParser.getXML(response);
    }

    public JSONObject getJsonContent(String action, Map<String, String> params) {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        this.action = action;
        return null;
    }

    public InputStream getResponseStream(String action, Map<String, String> params) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        this.action = action;
        return request(getActionURI(action), params, null).getEntity().getContent();
    }

    public Document putContent( String action, Map<String, String> params, File file, String filename, final UploadStopNotifierProgressListener listener) throws IOException {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        this.action = action;

        if(!"".equals(mSecureToken)){
            params.put(Pydio.PARAM_SECURE_TOKEN, mSecureToken);
        }

        CountingMultipartRequestEntity.ProgressListener progressListener = new CountingMultipartRequestEntity.ProgressListener() {
            @Override
            public void transferred(long num) throws IOException {
                if(listener.onProgress(num)){
                    throw new IOException("");
                }
            }
            @Override
            public void partTransferred(int part, int total) throws IOException {
                if(total == 0) total = 1;
                if(listener.onProgress( part*100 / total )){
                    throw new IOException("");
                }
            }
        };
        UploadFileBody fileBody = mRequester.newFileBody(file);
        fileBody.setListener(progressListener);

        HttpResponse response = request(getActionURI(action), params, fileBody);
        return HttpResponseParser.getXML(response);
    }

    public Document putContent(String action, Map<String, String> params, byte[] data, String filename, UploadStopNotifierProgressListener listener) {
        mAccessRefused = false;
        mLoggedIn = false;
        mAttemptedLogin = false;
        this.action = action;
        return null;
    }

    public void setServer(ServerNode server){
        this.mServerNode = server;
    }
    @Override
    public int requestStatus(){
        return mLastRequestStatus;
    }
    @Override
    public void setAuthenticationHelper(AuthenticationHelper helper) {
        this.mHelper = helper;
    }

}