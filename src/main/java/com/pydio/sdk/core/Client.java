package com.pydio.sdk.core;

import com.pydio.sdk.core.common.callback.ChangeHandler;
import com.pydio.sdk.core.common.callback.NodeHandler;
import com.pydio.sdk.core.common.callback.RegistryItemHandler;
import com.pydio.sdk.core.common.callback.TransferProgressListener;
import com.pydio.sdk.core.common.errors.SDKException;
import com.pydio.sdk.core.model.FileNode;
import com.pydio.sdk.core.model.Message;
import com.pydio.sdk.core.model.ServerNode;
import com.pydio.sdk.core.model.Stats;
import com.pydio.sdk.core.auth.Token;
import com.pydio.sdk.core.security.Credentials;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;

public interface Client {

    static Client get(ServerNode node) {
        if (node.versionName().startsWith("cells")) {
            return PydioCells.getFactory().get(node);
        }
        return new Pydio8(node);
    }

    ServerNode getServerNode();

    void setCredentials(Credentials c);

    void setTokenProvider(Token.Provider p);

    void setTokenStore(Token.Store s);

    String getUser();

    InputStream getUserData(String binary) throws SDKException;

    void login() throws SDKException;

    void logout() throws SDKException;

    JSONObject userInfo() throws SDKException;

    X509Certificate[] remoteCertificateChain();

    void downloadServerRegistry(RegistryItemHandler itemHandler) throws SDKException;

    void downloadWorkspaceRegistry(String ws, RegistryItemHandler itemHandler) throws SDKException;

    void workspaceList(final NodeHandler handler) throws SDKException;

    FileNode nodeInfo(String ws, String path) throws SDKException;

    FileNode ls(String ws, String folder, NodeHandler handler) throws SDKException;

    void search(String ws, String dir, String searched, NodeHandler h) throws SDKException;

    void bookmarks(NodeHandler h) throws SDKException;

    Message upload(InputStream source, long length, String ws, String path, String name, boolean autoRename, final TransferProgressListener progressListener) throws SDKException;

    Message upload(File source, String ws, String path, String name, boolean autoRename, final TransferProgressListener progressListener) throws SDKException;

    String uploadURL(String ws, String folder, String name, boolean autoRename) throws SDKException;

    long download(String ws, String file, OutputStream target, TransferProgressListener progressListener) throws SDKException;

    long download(String ws, String file, File target, TransferProgressListener progressListener) throws SDKException;

    String downloadURL(String ws, String file) throws SDKException;

    Message delete(String ws, String[] files) throws SDKException;

    Message restore(String ws, String[] files) throws SDKException;

    Message move(String ws, String[] files, String dstFolder) throws SDKException;

    Message rename(String ws, String srcFile, String newName) throws SDKException;

    Message copy(String ws, String[] files, String folder) throws SDKException;

    Message bookmark(String ws, String file) throws SDKException;

    Message unbookmark(String ws, String file) throws SDKException;

    Message mkdir(String ws, String parent, String name) throws SDKException;

    InputStream previewData(String ws, String file, int dim) throws SDKException;

    String streamingAudioURL(String ws, String file) throws SDKException;

    String streamingVideoURL(String ws, String file) throws SDKException;

    Stats stats(String ws, String file, boolean withHash) throws SDKException;

    long changes(String ws, String folder, int seq, boolean flatten, ChangeHandler changeHandler) throws SDKException;

    String share(String ws, String file, String ws_label, boolean isFolder, String ws_description, String password, int expiration, int download, boolean canPreview, boolean canDownload) throws SDKException;

    void unshare(String ws, String file) throws SDKException;

    JSONObject shareInfo(String ws, String file) throws SDKException;

    InputStream getCaptcha() throws SDKException;

    JSONObject authenticationInfo() throws SDKException;

    Message emptyRecycleBin(String ws) throws SDKException;
}
