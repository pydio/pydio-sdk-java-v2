package com.pydio.sdk.sync.fs;

import com.pydio.sdk.core.model.Change;
import com.pydio.sdk.core.utils.io;
import com.pydio.sdk.sync.Error;
import com.pydio.sdk.sync.changes.GetChangeRequest;
import com.pydio.sdk.sync.changes.GetChangesResponse;
import com.pydio.sdk.sync.changes.ProcessChangeRequest;
import com.pydio.sdk.sync.changes.ProcessChangeResponse;
import com.pydio.sdk.sync.content.Content;
import com.pydio.sdk.sync.content.ContentLoader;
import com.pydio.sdk.sync.content.LocalFileContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LocalFs implements Fs, ContentLoader {

    private String root;
    private String id;
    private List<String> watched;

    public LocalFs(String id, String root, List<String> watched) {
        this.id = id;
        this.root = root;
        this.watched = watched;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<String> getWatches() {
        return new ArrayList<>(watched);
    }

    @Override
    public void addWatch(String path) {
        if(this.watched.contains(path)){
            return;
        }
        this.watched.add(path);
    }

    @Override
    public GetChangesResponse getChanges(GetChangeRequest request) {
        return null;
    }

    @Override
    public ProcessChangeResponse processChange(ProcessChangeRequest request) {
        Error e;
        Change c = request.getChange();
        String event = c.getType();
        switch (event) {
            case Change.TYPE_CREATE:
                String md5 = c.getNode().getMd5();
                if ("directory".equals(md5)) {
                    e = mkdir(c);
                } else if (c.getNode().getSize() == 0) {
                    e = mkfile(c);
                } else {
                    e = download(c, request.getContentLoader());
                }
                break;

            case Change.TYPE_PATH:
                e = move(c);
                break;

            case Change.TYPE_CONTENT:
                e = download(c, request.getContentLoader());

                break;
            case Change.TYPE_DELETE:
                e = delete(c);
                if (e == null) {
                    this.watched.remove(c.getSource());
                }
                break;
            default:
                e = Error.unknownOperation(event);
                break;
        }

        ProcessChangeResponse response = new ProcessChangeResponse();
        response.setError(e);
        return response;
    }

    @Override
    public ContentLoader getContentLoader() {
        return this;
    }

    @Override
    public boolean receivesEvents() {
        return true;
    }

    @Override
    public boolean sendsEvents() {
        return false;
    }

    @Override
    public Content getContent(String nodeId) {
        return new LocalFileContent(root + nodeId);
    }

    private Error mkdir(Change c) {
        String fullPath = root + c.getNode().getPath();
        File file = new File(fullPath);
        if(!file.exists() && !file.mkdirs()) {
            return Error.opFailed(c.getType(), id(), fullPath);
        }
        return null;
    }

    private Error mkfile(Change c) {
        String fullPath = root + c.getNode().getPath();
        File file = new File(fullPath);

        boolean parentExists  = file.getParentFile().exists();
        boolean parentCreated = file.getParentFile().mkdirs();
        if(!parentExists && !parentCreated) {
            return Error.opFailed(c.getType(), id(), fullPath);
        }

        try {
            if(!file.createNewFile()) {
                return Error.opFailed(c.getType(), id(), fullPath);
            }
        } catch (IOException e) {
            Error error = Error.opFailed(c.getType(), id(), fullPath);
            error.setDetails(e.getMessage());
            return error;
        }
        return null;
    }

    private Error delete(Change c) {
        String fullPath = root + c.getNode().getPath();
        File file = new File(fullPath);

        if(file.canExecute() && !file.delete()) {
            return Error.opFailed(c.getType(), id(), fullPath);
        }
        return null;
    }

    private Error move(Change c) {
        String srcFullPath = root + c.getSource();
        String dstFullPath = root + c.getTarget();

        File srcFile = new File(srcFullPath);
        if(!srcFile.exists()) {
            return Error.notFound(id(), srcFullPath);
        }

        File dstFile = new File(dstFullPath);
        if(!srcFile.renameTo(dstFile)) {
            return Error.opFailed(c.getType(), id(), srcFullPath + "->" + dstFullPath);
        }
        return null;
    }

    private Error download(Change c, ContentLoader loader) {
        String nodePath = c.getNode().getPath();
        String fullPath = root + nodePath;

        File parentFile = new File(fullPath).getParentFile();
        boolean parentExists  = parentFile.exists();
        boolean parentCreated = parentFile.mkdirs();

        boolean isParentDir = parentFile.isDirectory();

        if(!parentExists && !parentCreated) {
            return Error.opFailed("create", id(), parentFile.getPath());
        }

        if (!isParentDir && parentFile.delete() && parentFile.mkdirs()) {
            return Error.opFailed("create", id(), parentFile.getPath());
        }

        File localFile = new File(fullPath);

        String md5 = md5(fullPath);
        long size = localFile.length();

        Content content = loader.getContent(nodePath);

        if(!content.exists()) {
            return  null;
        }

        if ( content.getMd5().equals(md5) && content.getSize() == size) {
            return null;
        }

        InputStream in;
        try {
            in = content.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return Error.failedToGetContent(c.getTargetSide(), nodePath);
        }

        try {
            io.writeFile(in, fullPath);
        } catch (IOException e) {
            e.printStackTrace();
            Error error = Error.opFailed(c.getType(), id(), fullPath);
            error.setDetails(e.getMessage());
            error.setLoader(c.getTargetSide());
            return error;
        }
        return null;
    }

    private String md5(String path){

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");

            FileInputStream fis = new FileInputStream(path);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<mdbytes.length;i++) {
                String hex=Integer.toHexString(0xff & mdbytes[i]);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }
            return  hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            //Log.e("Runtime", e.getMessage());
        } catch (IOException e) {
            //Log.e("IO", e.getMessage());
        }
        return "";
    }
}
