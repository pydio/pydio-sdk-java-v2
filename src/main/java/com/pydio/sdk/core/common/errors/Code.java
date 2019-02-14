package com.pydio.sdk.core.common.errors;

public class Code {
    public static final int ok = 0;
    public static final int bad_uri = 1;
    public static final int con_failed = 2;
    public static final int authentication_required = 3;
    public static final int authentication_with_captcha_required = 4;
    public static final int resource_found = 5;
    public static final int ssl_error = 6;
    public static final int ssl_certificate_not_signed = 7;
    public static final int tls_init = 8;
    public static final int unsupported_method = 9;
    public static final int unsupported_scheme = 10;
    public static final int redirect = 11;
    public static final int unexpected_content = 12;
    public static final int unexpected_response = 13;
    public static final int unreachable_host = 14;
    public static final int bad_config = 15;
    public static final int pydio_server_not_supported = 16;
    public static final int not_pydio_server = 17;
    public static final int encoding_failed = 18;
    public static final int con_read_failed = 19;
    public static final int con_write_failed = 20;
    public static final int con_closed = 21;
    public static final int not_found = 22;
    public static final int no_internet = 23;

    public static int fromHttpStatus(int status) {

        if (status == 200) {
            return ok;
        }

        if (status == 404) {
            return not_found;
        }

        if (status == 401 || status == 403) {
            return authentication_required;
        }

        return unexpected_response;
    }
}
