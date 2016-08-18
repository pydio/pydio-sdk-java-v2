package pydio.sdk.java.core.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;

/**
 * Created by jabar on 12/07/2016.
 */
public class ServerGeneralRegistrySaxHandler extends DefaultHandler {

    RegistryItemHandler handler;
    boolean mInsideAdminPlugin = false;
    boolean mInsideConfigs = false;
    boolean mInsideCoreUploader = false;
    String mProperty;

    String[] allowedProperties = new String[]{
            //"ALLOWED_EXTENSIONS",
            //"ALLOWED_EXTENSIONS_READABLE",
            //"UPLOAD_MAX_NUMBER",
            "UPLOAD_MAX_SIZE",
            //"UPLOAD_MAX_SIZE_TOTAL",
            //"USER_GENERATE_KEYS",
            "APPLICATION_TITLE",
            //"ZIP_CREATION",
            //"NODENAME_MAX_LENGTH",
            //"ENABLE_USERS",
            //"ALLOW_GUEST_BROWSING",
            //"PASSWORD_MINLENGTH",
            //"SECURE_LOGIN_FORM",
            //"ENABLE_FORGOT_PASSWORD",
            //"FORGOT_PASSWORD_ACTION",
            //"CUSTOM_ICON",
            //"CUSTOM_TOP_TITLE",
            //"CUSTOM_TOP_LOGO_H",
            //"CUSTOM_TOP_LOGO_W",
            //"CUSTOM_TOP_LOGO_T",
            //"CUSTOM_TOP_LOGO_L",
            //"WELCOME_PAGE_BACKGROUND_ATTRIBUTES_1",
            //"WELCOME_PAGE_BACKGROUND_ATTRIBUTES_2",
            //"WELCOME_PAGE_BACKGROUND_ATTRIBUTES_3",
            //"WELCOME_PAGE_BACKGROUND_ATTRIBUTES_4",
            //"WELCOME_PAGE_BACKGROUND_ATTRIBUTES_5",
            //"WELCOME_PAGE_BACKGROUND_ATTRIBUTES_6",
            //"WELCOME_PAGE_BACKGROUND_ATTRIBUTES_LOWRES",
            //"CUSTOM_SHAREPAGE_BACKGROUND_ATTRIBUTES_1",
            //"CUSTOM_SHAREPAGE_BACKGROUND_ATTRIBUTES_2",
            //"CUSTOM_SHAREPAGE_BACKGROUND_ATTRIBUTES_3",
            "MOBILE_SECURITY_FORCE_DONTSAVEPASS",
            "MOBILE_SECURITY_FORCE_PIN_CODE",
            "MOBILE_SECURITY_FORCE_REAUTH",
            "MOBILE_SECURITY_OUTSIDE_SHARE",
            "MOBILE_SECURITY_DISABLE_OFFLINE",
            "MOBILE_SECURITY_DISABLE_BACKUP"

    };

    public ServerGeneralRegistrySaxHandler(RegistryItemHandler handler){
        this.handler = handler;
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if("ajxpcore".equals(qName) && "core.uploader".equals(attributes.getValue("id"))){
            mInsideCoreUploader = true;
            return;
        }


        if("ajxp_plugin".equals(qName) && "admin".equals(attributes.getValue("name"))){
            mInsideAdminPlugin = true;
            return;
        }


        if("plugin_configs".equals(qName) && (mInsideAdminPlugin || mInsideCoreUploader)){
            mInsideConfigs = true;
            return;
        }

        if("property".equals(qName) && mInsideConfigs){
            mProperty = attributes.getValue("name");
            return;
        }
        mProperty = null;
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(mProperty != null && mInsideConfigs && Arrays.asList(allowedProperties).contains(mProperty)){
            String content = new String(ch, start, length);
            if(handler != null){
                if(content.startsWith("\"")){
                    content = content.substring(1);
                }

                if(content.endsWith("\"")){
                    content = content.substring(0, content.length() - 1);
                }
                handler.onPref(mProperty, content);
            }
            mProperty = null;
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if("plugin_configs".equals(qName) && mInsideConfigs){
            mInsideConfigs = false;
            return;
        }

        if(("ajxp_plugin".equals(qName) || "ajxpcore".equals(qName)) && mInsideAdminPlugin){
            mInsideAdminPlugin = false;
        }
    }
}
