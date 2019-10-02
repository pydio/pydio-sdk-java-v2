package com.pydio.sdk.core.api.p8;

import com.pydio.sdk.core.api.p8.consts.Action;
import com.pydio.sdk.core.api.p8.consts.Const;
import com.pydio.sdk.core.api.p8.consts.Param;
import com.pydio.sdk.core.common.http.ContentBody;
import com.pydio.sdk.core.security.Credentials;
import com.pydio.sdk.core.utils.Params;

import java.io.File;
import java.io.IOException;

public class P8RequestBuilder {

    private P8Request request;

    public P8RequestBuilder() {
        request = new P8Request();
    }

    public P8RequestBuilder(P8Request request) {
        this.request = request;
    }

    public static P8RequestBuilder update(P8Request request) {
        return new P8RequestBuilder(request);
    }

    public static P8RequestBuilder getUserData(String user, String binary) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.getBinaryParam)
                .setParam(Param.userId, user)
                .setParam(Param.binaryId, binary);
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder getSeed() {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.getSeed).setMethod(Method.get);
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder login(Credentials credentials) {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.login).
                setParam(Param.loginSeed, credentials.getSeed()).
                setParam(Param.userId, credentials.getLogin()).
                setParam(Param.password, credentials.getPassword());
        if (credentials.getCaptcha() != null) {
            builder.setParam(Param.captchaCode, credentials.getCaptcha());
        }
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder logout() {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.logout).setMethod(Method.get);
        return builder;
    }

    public static P8RequestBuilder serverRegistry() {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.getXmlRegistry).ignoreCookies(true);
        return builder;
    }

    public static P8RequestBuilder workspaceRegistry(String wsId) {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.getXmlRegistry).
                setParam(Param.tmpRepositoryId, wsId).
                ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder workspaceList() {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.getXmlRegistry).
                setParam(Param.xPath, Const.xPathUserRepositories).
                ignoreCookies(false)
        ;
        return builder;
    }

    public static P8RequestBuilder nodeInfo(String ws, String file) {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.ls)
                .setParam(Param.options, "al")
                .setParam(Param.file, file)
                .setParam(Param.tmpRepositoryId, ws)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder ls(String ws, String dir) {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.ls)
                .setParam(Param.options, "al")
                .setParam(Param.dir, dir)
                .setParam(Param.tmpRepositoryId, ws)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder listBookmarked(String ws, String dir) {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.searchByKeyword)
                .setParam(Param.options, "al")
                .setParam(Param.dir, dir)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.field, Const.ajxpBookmarked)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder unbookmark(String ws, String path) {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.getBookmarks)
                .setParam(Param.bmAction, Action.addBookmark)
                .setParam(Param.bmPath, path)
                .setParam(Param.tmpRepositoryId, ws)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder bookmark(String ws, String path) {
        P8RequestBuilder builder = new P8RequestBuilder();
        builder = builder.setAction(Action.getBookmarks)
                .setParam(Param.bmAction, Action.addBookmark)
                .setParam(Param.bmPath, path)
                .setParam(Param.tmpRepositoryId, ws)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder search(String ws, String dir, String query) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.search)
                .setParam(Param.dir, dir)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.query, query)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder upload(String ws, String dir, String name, boolean autoRename, ContentBody body) throws IOException {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.upload)
                .setParam(Param.dir, dir)
                .setParam(Param.tmpRepositoryId, ws);
        String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
        builder.setParam(Param.urlencodedFilename, urlEncodedName)
                .setParam(Param.autoRename, String.valueOf(autoRename))
                .setParam(Param.xhrUploader, "true")
                .setBody(body);
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder download(String ws, String path) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.download)
                .setParam(Param.file, path)
                .setParam(Param.tmpRepositoryId, ws);
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder delete(String ws, String[] files) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setParam(Param.tmpRepositoryId, ws)
                .setAction(Action.delete);

        if (files.length > 1) {
            builder.setParam(Param.file, files[0]);
        } else {
            int count = 0;
            for (String file : files) {
                String item = String.format("file_%d", count);
                builder.setParam(item, file);
                count++;
            }
        }
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder restore(String ws, String[] files) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.restore)
                .setParam(Param.tmpRepositoryId, ws);

        int count = 0;
        for (String file : files) {
            builder.setParam(Param.file + "_" + count, file);
        }
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder move(String ws, String[] files, String dest) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.move)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.dest, dest);
        int count = 0;
        for (String file : files) {
            builder.setParam(Param.file + "_" + count, file);
        }
        return builder.ignoreCookies(false).setParam(Param.forceCopyDelete, "true");
    }

    public static P8RequestBuilder rename(String ws, String file, String newFilename) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file)
                .setParam(Param.filenameNew, newFilename)
                .setAction(Action.rename)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder copy(String ws, String[] files, String dest) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.copy)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.dest, dest);
        int count = 0;
        for (String file : files) {
            builder.setParam(Param.file + "_" + count, file);
        }
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder mkdir(String ws, String dir, String name) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.mkdir)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.dir, dir)
                .setParam(Param.dirname, name);
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder previewImage(String ws, String file, int dim) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.previewDataProxy)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file)
                .setParam(Param.getThumb, "true");
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder previewPDF(String ws, String file, int dim) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.imagickDataProxy)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file)
                .setParam(Param.getThumb, "true");
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder streamingAudio(String ws, String file) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.audioProxy)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file)
                .setParam(Param.richPreview, "true");
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder streamingVideo(String ws, String file) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.readVideoData)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file);
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder stats(String ws, String file, boolean withHash) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file);

        if (withHash) {
            builder.setAction(Action.stats + "_hash");
        } else {
            builder.setAction(Action.stats);
        }
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder changes(String ws, String filter, int seq, boolean flatten) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.changes)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.flatten, String.valueOf(flatten))
                .setParam(Param.stream, "true")
                .setParam(Param.seqId, String.valueOf(seq));
        if (filter != null) {
            builder.setParam(Param.filter, filter);
        } else {
            builder.setParam(Param.filter, "/");
        }
        return builder.ignoreCookies(false);
    }

    public static P8RequestBuilder share(String ws, String file, String descripton) {
        File f = new File(file);
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.share)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file)
                .setParam(Param.subAction, Action.createMinisite)
                .setParam(Param.createGuestUser, "true")
                .setParam(Param.workspaceLabel, f.getName())
                .setParam(Param.workspaceDescription, descripton)
                .setParam(Param.simpleRightDownload, "on")
                .setParam(Param.simpleRightRead, "on")
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder unShare(String ws, String file) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.unshare)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder shareInfo(String ws, String file) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.loadSharedElementData)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.merged, "true")
                .setParam(Param.file, file)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder restore(String ws, String file) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.restore)
                .setParam(Param.tmpRepositoryId, ws)
                .setParam(Param.file, file)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder emptyRecycle(String ws) {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.emptyRecycle)
                .setParam(Param.tmpRepositoryId, ws)
                .ignoreCookies(false);
        return builder;
    }

    public static P8RequestBuilder getCaptcha() {
        P8RequestBuilder builder = new P8RequestBuilder()
                .setAction(Action.getCaptcha)
                .ignoreCookies(false);
        return builder;
    }

    public P8RequestBuilder setAction(String action) {
        request.action = action;
        return this;
    }

    public P8RequestBuilder setParam(String name, String value) {
        if (request.params == null) {
            request.params = Params.create(name, value);
        } else {
            request.params.set(name, value);
        }
        return this;
    }

    public P8RequestBuilder addHeader(String name, String value) {
        if (request.headers == null) {
            request.headers = Params.create(name, value);
        } else {
            request.headers.set(name, value);
        }
        return this;
    }

    public P8RequestBuilder setBody(ContentBody body) {
        request.body = body;
        return this;
    }

    public P8RequestBuilder setSecureToken(String token) {
        if(token != null) {
            request.secureToken = token;
            setParam(Param.secureToken, token);
        }
        return this;
    }

    public P8RequestBuilder ignoreCookies(boolean ignore) {
        request.ignoreCookies = ignore;
        return this;
    }

    public P8RequestBuilder setCredentials(com.pydio.sdk.core.security.Credentials credentials) {
        request.credentials = credentials;
        return this;
    }

    public P8RequestBuilder setMethod(String method) {
        request.method = method;
        return this;
    }

    public P8Request getRequest() {
        return request;
    }

}
