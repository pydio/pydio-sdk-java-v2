package pydio.sdk.java.core.model;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;

import pydio.sdk.java.core.errors.Error;
import pydio.sdk.java.core.handlers.Completion;
import pydio.sdk.java.core.security.CertificateTrust;
import pydio.sdk.java.core.security.CertificateTrustManager;
import pydio.sdk.java.core.thread.Background;
import pydio.sdk.java.core.utils.Pydio;

/**
 * Class that wrap a server properties
 * @author pydio
 *
 */

public class ServerNode implements Node {
		
	private boolean mLegacy = false;
	private String mScheme = null;
	private String mHost = null;
	private String mPath = null;
	private String mVersion = null;
	private String mVersionName = null;
	private String mIconURL;
	private String mWelcomeMessage;
	private int mPort = 80;
	private String mLabel = null;
	private String mUrl = null;
	private boolean mSSLUnverified = false;
	private Properties mProperties = null;
	private byte[] mChallengeData;
	private JSONObject bootConf;

	private X509Certificate[] mLastUnverifiedCertificateChain;
	private CertificateTrust.Helper mGivenTrustHelper, mTrustHelper;
	private String mCaptcha;
	private int mLastResponseCode = Pydio.OK;
	public Map<String, WorkspaceNode> workspaces;
	public SSLContext HttpSecureContext;

	public ServerNode() {}

	public static ServerNode fromAddress(String address) throws IOException {
		URL url = new URL(address);
		ServerNode node = new ServerNode();
		node.mScheme = url.getProtocol();
		node.mHost = url.getHost();
		node.mPort = url.getPort();
		node.mPath = url.getPath();
		node.mUrl = address;
		return node;
	}

	//********************************************************************************************
	//                  Super class: NODE METHODS
	//********************************************************************************************
    @Override
    public String getProperty(String key) {
		if(mProperties == null) return null;
		return mProperties.getProperty(key);
	}

	@Override
	public void setProperty(String key, String value) {
		if(mProperties == null){
			mProperties = new Properties();
		}
		mProperties.setProperty(key, value);
	}

	@Override
	public void deleteProperty(String key) {
		if(mProperties != null && mProperties.contains(key)){
			mProperties.remove(key);
		}
	}

	@Override
	public String path(){
		return mPath;
	}

	@Override
	public String label() {
		return mLabel;
	}

	@Override
	public int type() {
		return Node.TYPE_SERVER;
	}

	@Override
	public String id() {
		String id = mScheme + "://" + mHost ;
		if (mPort != 80) {
			id = id + ":" + mPort;
		}
		id = id + mPath;
		return id;
	}

	@Override
	public void setProperties(Properties p) {
		mProperties = p;
	}

	@Override
	public String getEncoded() {
		return null;
	}

	@Override
	public String getEncodedHash() {
		return null;
	}

	@Override
	public int compare(Node node) {
		return 0;
	}

	//*****************************************************************************
	//						RESOLVE
	//*****************************************************************************
	public void resolve(String address, boolean unverifiedSSL, CertificateTrust.Helper h, Completion c){
		Background.go(() -> {
			resolveRemote(address, unverifiedSSL, h, c);
		});
	}

	private void resolveRemote(final String address, boolean unverifiedSSL, final CertificateTrust.Helper h, final Completion c) {
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			c.onComplete(new Error(400, "unable to parse URL", e));
			return;
		}

		mScheme = url.getProtocol();
		mHost = url.getHost();
		mPort = url.getPort();
		mPath = url.getPath();
		mUrl = address;
		setUnverifiedSSL(unverifiedSSL);
		setCertificateTrustHelper(h);

		int err = downloadBootConf("a/frontend/bootconf");
		if (err == 0) {
			c.onComplete(null);
			return;
		}

		if (err != Pydio.ERROR_UNEXPECTED_RESPONSE) {
			err = downloadBootConf("index.php?get_action=get_boot_conf");
			if (err == 0) {
				c.onComplete(null);
				return;
			}
		}
		c.onComplete(new Error(err, "failed to download boot configs", null));
	}

	private int downloadBootConf(String apiURLTail) {
		InputStream in;
		SSLContext sslCtx;
		HttpURLConnection con;


		String apiURL = url();
		boolean addressEndsWithSlash = apiURL.endsWith("/");
		boolean tailStartsWithSlash = apiURLTail.startsWith("/");

		if (addressEndsWithSlash && tailStartsWithSlash) {
            apiURL = apiURL + apiURLTail.substring(1);
        } else if(!addressEndsWithSlash && !tailStartsWithSlash) {
            apiURL = apiURL + "/" + apiURLTail;
        } else {
		    apiURL = apiURL + apiURLTail;
        }

		if (unVerifiedSSL()) {
			try {
				sslCtx = SSLContext.getInstance("TLS");
				sslCtx.init(null, new TrustManager[]{trustManager()}, null);
			} catch (Exception e) {
				return Pydio.ERROR_TLS_INIT;
			}

			HttpsURLConnection scon;
			try {
				scon = (HttpsURLConnection) new URL(apiURL).openConnection();
			} catch (IOException e) {
				return Pydio.ERROR_CON_FAILED;
			}
			scon.setSSLSocketFactory(sslCtx.getSocketFactory());
			con = scon;
		} else {
			try {
				con = (HttpURLConnection) new URL(apiURL).openConnection();
			} catch (IOException e) {
				return Pydio.ERROR_CON_FAILED;
			}
		}

		try {
			in = con.getInputStream();
		} catch (IOException e) {
			if (e instanceof SSLException) {
				return Pydio.ERROR_UNVERIFIED_CERTIFICATE;
			}
			return Pydio.ERROR_CON_FAILED;

		}catch (Exception e){
			if(e instanceof IllegalArgumentException && e.getMessage().toLowerCase().contains("unreachable")){
				return Pydio.ERROR_UNREACHABLE_HOST;
			}
			return Pydio.ERROR_OTHER;
		}


		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		for(;;) {
			int n = 0;
			try {
				n = in.read(buffer);
			} catch (IOException e) {
				return Pydio.ERROR_OTHER;
			}
			if (n == -1) {
				break;
			}
			out.write(buffer, 0, n);
		}

		try {
			bootConf = new JSONObject(new String(out.toByteArray(), "UTF-8"));
		} catch (Exception ignored){
			return Pydio.ERROR_UNEXPECTED_RESPONSE;
		}

		boolean isCells = bootConf.has("backend");
		mVersion = bootConf.getString("ajxpVersion");
		mVersionName = isCells ? "cells" : "pydio";

		JSONObject customWordings = bootConf.getJSONObject("customWording");
		mLabel = customWordings.getString("title");
		mIconURL = customWordings.getString("icon");

		tailStartsWithSlash = mIconURL.startsWith("/");
		if (addressEndsWithSlash && tailStartsWithSlash) {
			mIconURL = url() + mIconURL;
		} else if(!addressEndsWithSlash && !tailStartsWithSlash) {
			mIconURL = url() + "/" + mIconURL;
		} else {
			mIconURL = url() + mIconURL;
		}

		if(customWordings.has("welcomeMessage")) {
			mWelcomeMessage = customWordings.getString("welcomeMessage");
		}
		return 0;
	}


	//*****************************************************************************
	//						PROPERTIES
	//*****************************************************************************
	public String version() {
		return mVersion;
	}

	public String versionName() {
		return mVersionName;
	}

	public CertificateTrust.Helper getTrustHelper(){
		if(mTrustHelper == null) {
			return mTrustHelper = new CertificateTrust.Helper() {
				@Override
				public boolean isServerTrusted(X509Certificate[] chain) {
					mLastUnverifiedCertificateChain = chain;
					return mGivenTrustHelper != null && mGivenTrustHelper.isServerTrusted(chain);
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					if(mGivenTrustHelper != null){
						return mGivenTrustHelper.getAcceptedIssuers();
					}
					return new X509Certificate[0];
				}
			};
		}
		return mTrustHelper;
	}

	public ServerNode init(String url){
		if(!url.endsWith("/")){
			url += "/";
		}
		mUrl = url;
		URI uri = URI.create(url);
		mScheme = uri.getScheme();
		mHost = uri.getHost();
		mPath = uri.getPath();
		mPort = uri.getPort();
		return this;
	}

	public ServerNode init(String url, CertificateTrust.Helper helper){
		this.init(url);
		mGivenTrustHelper = helper;
		return this;
	}

	public ServerNode init(String url, String user, CertificateTrust.Helper helper){
		this.init(url);
		//mUser = user;
		mGivenTrustHelper = helper;
		return this;
	}

    public boolean legacy(){
        return mLegacy;
    }

	public boolean unVerifiedSSL(){
		return mSSLUnverified;
	}

	public String host(){
		return mHost;
	}
	
	public String scheme(){
		return mScheme;
	}

	public int port(){
		return mPort;
	}

	public String url(){
		if(mUrl != null) return mUrl;

		String path = mScheme.toLowerCase() + "://" + mHost;
		if(mPort > 0 && mPort != 80){
			path += ":" + mPort;
		}
		path += path();
		if(!path.endsWith("/"))
			return path + "/";
		return mUrl = path;
	}

	public String welcomeMessage(){
		return mWelcomeMessage;
	}

	public String iconURL() {
		return mIconURL;
	}

	public String apiURL() {
		return bootConf.getString("ENDPOINT_REST_API");
	}

	public TrustManager trustManager() {
		return new CertificateTrustManager(mTrustHelper);
	}

    public boolean equals(Object o){
        try{
            return this == o || (o instanceof Node) && ((Node)o).type() == type() && label().equals(((Node)o).label()) && path().equals(((Node)o).path());
        }catch(NullPointerException e){
            return false;
        }
    }

	public int lastResponseCode(){
		return mLastResponseCode;
	}

	public String getAuthenticationChallengeResponse(){
		String c = mCaptcha;
		mCaptcha = null;
		return c;
	}

	public X509Certificate[] certificateChain() {
		return mLastUnverifiedCertificateChain;
	}

	public byte[] getChallengeData(){
		return  mChallengeData;
	}

	public WorkspaceNode getWorkspace(String id) {
		if (workspaces != null && workspaces.containsKey(id)) {
			return workspaces.get(id);
		}
		return null;
	}

	public ServerNode setLastUnverifiedCertificateChain(X509Certificate[] chain){
		mLastUnverifiedCertificateChain = chain;
		return this;
	}

	public ServerNode setLegacy(boolean leg){
		mLegacy = leg;
		return this;
	}

	public ServerNode setUnverifiedSSL(boolean unverified){
		mSSLUnverified = unverified;
		return this;
	}

	public ServerNode setLabel(String label){
		mLabel = label;
		return this;
	}

	public ServerNode setScheme(String scheme){
		this.mScheme = scheme;
		return this;
	}

	public ServerNode setWorkspaces(List<WorkspaceNode> nodes){
		if(this.workspaces == null) {
			this.workspaces = new HashMap<>();
		}
		for (WorkspaceNode wn: nodes) {
			this.workspaces.put(wn.getId(), wn);
		}
		return this;
	}

	public ServerNode addWorkspace(WorkspaceNode node){
		if(this.workspaces == null) {
			this.workspaces = new HashMap<>();
		}
		this.workspaces.put(node.getId(), node);
		return this;
	}

	public ServerNode setCertificateTrustHelper(CertificateTrust.Helper helper){
		mGivenTrustHelper = helper;
		return this;
	}

	public ServerNode setLastRequestResponseCode(int code){
		mLastResponseCode = code;
		return this;
	}

	public ServerNode setAuthenticationChallengeResponse(String captchaCode){
		mCaptcha = captchaCode;
		return this;
	}

	public ServerNode setChallengeData(byte[] data){
		mChallengeData = data;
		return this;
	}
}
