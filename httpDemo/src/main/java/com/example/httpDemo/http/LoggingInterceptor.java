package com.example.httpDemo.http;

import com.example.httpDemo.utils.CommonUtils;
import com.example.httpDemo.utils.LogHelper;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;

import static okhttp3.internal.platform.Platform.INFO;

public class LoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public enum Level {
        /** No logs. */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    public interface Logger {
        void log(String requestBodyMsg);

        /** A {@link HttpLoggingInterceptor.Logger} defaults output appropriate for the current platform. */
        HttpLoggingInterceptor.Logger DEFAULT = new HttpLoggingInterceptor.Logger() {
            @Override public void log(String requestBodyMsg) {
                Platform.get().log(INFO, requestBodyMsg, null);
            }
        };
    }

    

    private volatile HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.NONE;

    /** Change the level at which this interceptor logs. */
    public LoggingInterceptor setLevel(HttpLoggingInterceptor.Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public HttpLoggingInterceptor.Level getLevel() {
        return level;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        String requestBodyMsg  ="";
        String responseBodyMsg  ="";
        HttpLoggingInterceptor.Level level = this.level;

        Request request = chain.request();
        if (level == HttpLoggingInterceptor.Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == HttpLoggingInterceptor.Level.BODY;
        boolean logHeaders = logBody || level == HttpLoggingInterceptor.Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        String requestStartMessage = "--> "
                + request.method()
                + ' ' + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        requestBodyMsg += requestStartMessage+"\n";

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestBodyMsg += "Content-Type: " + requestBody.contentType()+"\n";
                }
                if (requestBody.contentLength() != -1) {
                    requestBodyMsg += "Content-Length: " + requestBody.contentLength()+"\n";
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    requestBodyMsg += name + ": " + headers.value(i)+"\n";
                }
            }

            if (!logBody || !hasRequestBody) {
                requestBodyMsg += "--> END " + request.method()+"\n";
            } else if (bodyEncoded(request.headers())) {
                requestBodyMsg += "--> END " + request.method() + " (encoded body omitted)"+"\n";
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }


                if (isPlaintext(buffer)) {
                    requestBodyMsg += buffer.readString(charset)+"\n";
                    requestBodyMsg += "--> END " + request.method()
                            + " (" + requestBody.contentLength() + "-byte body)"+"\n";
                } else {
                    requestBodyMsg += "--> END " + request.method()
                            + " (" + requestBody.contentLength() + "-byte body omitted)"+"\n";
                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {            
            requestBodyMsg += "<-- HTTP FAILED: " + e+"\n";
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        requestBodyMsg += "<-- "
                + response.code()
                + (response.message().isEmpty() ? "" : ' ' + response.message())
                + ' ' + response.request().url()
                + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')'+"\n";

        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                requestBodyMsg +=headers.name(i) + ": " + headers.value(i)+"\n";
            }

            if (!logBody || !HttpHeaders.hasBody(response)) {
                requestBodyMsg +="<-- END HTTP"+"\n";
            } else if (bodyEncoded(response.headers())) {
                requestBodyMsg +="<-- END HTTP (encoded body omitted)"+"\n";
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    requestBodyMsg +=""+"\n";
//                    requestBodyMsg +="<-- END HTTP (binary " + buffer.size() + "-byte body omitted)"+"\n";
                    return response;
                }

                if (contentLength != 0) {
                    requestBodyMsg +=""+"\n";
                    responseBodyMsg =buffer.clone().readString(charset)+"\n";
                }

//                requestBodyMsg +="<-- END HTTP (" + buffer.size() + "-byte body)"+"\n";
            }


            if(CommonUtils.isJson(responseBodyMsg)){
                LogHelper.e(requestStartMessage);
                LogHelper.json(responseBodyMsg);
            }else {
                LogHelper.e(requestBodyMsg);
            }

        }

        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }
}
