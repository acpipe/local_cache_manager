package common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;


@Slf4j
public class KeepAliveHttpPoster {
    private static final ConnectionKeepAliveStrategy KEEP_ALIVE_START = new DefaultConnectionKeepAliveStrategy() {
        @Override
        public long getKeepAliveDuration(
            HttpResponse response,
            HttpContext context) {
            long keepAlive = super.getKeepAliveDuration(response, context);
            if (keepAlive == -1) {
                // Keep connections alive 120 seconds if a keep-alive value
                // has not be explicitly set by the server
                keepAlive = CoastConstants.HTTP_KEEP_ALIVE_MS;
            }
            return keepAlive;
        }
    };

    private CloseableHttpClient httpClient;

    public KeepAliveHttpPoster() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
        cm.setMaxTotal(CoastConstants.HTTP_MAX_POOL);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(CoastConstants.HTTP_INCREASE_PER);
        // Increase max connections for alimama
        // cm.setMaxPerRoute(new HttpRoute(new HttpHost("http://ssp.m.taobao.com/api/data", 80)), 50);
        // close all connections that have been idle over 4 seconds.
        cm.closeIdleConnections(CoastConstants.HTTP_IDLE_CLOSE_SECOND, TimeUnit.SECONDS);

        // setConnectTimeout: Client tries to connect to the server. setConnectTimeout denotes the time elapsed
        // before the connection established or Server responded to connection request.

        // setSoTimeout: After establishing the connection, the client socket waits for response after sending the request.
        // setSoTimeout is the elapsed time since the client has sent request to the server before server responds.
        // Please note that this is not not same as HTTP Error 408 which the server sends to the client.
        // In other words its maximum period inactivity between two consecutive data packets arriving at client
        // side after connection is established.
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(CoastConstants.HTTP_CONNECT_TIMEOUT_MS)
            .setConnectionRequestTimeout(CoastConstants.HTTP_REQUEST_TIMEOUT_MS)
            .setSocketTimeout(CoastConstants.HTTP_REQUEST_TIMEOUT_MS)
            .build();

        SocketConfig socketConfig = SocketConfig.custom()
            .setSoKeepAlive(true)
            .setTcpNoDelay(true)
            .build();

        // create HttpClient
        httpClient = HttpClients.custom()
            .setConnectionManager(cm)
            .setKeepAliveStrategy(KEEP_ALIVE_START)
            // Sets maximum time to live for persistent connections
            .setConnectionTimeToLive(CoastConstants.HTTP_CONNECT_TIME_TO_LIVE_S, TimeUnit.MINUTES)
            .setDefaultRequestConfig(requestConfig)
            .setDefaultSocketConfig(socketConfig)
            .disableConnectionState()
            .build();
    }

    public String send(String host, String requestJsonString) {
        HttpPost httpPost = new HttpPost(host);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setEntity(new StringEntity(requestJsonString, "UTF-8"));
        String responseString = "";
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            log.debug("host:{}, status: {}", host, status);
            InputStream inputStream = null;
            String statusMsg = "http.status." + HttpStatus.getStatusText(status);
            if (status == HttpStatus.SC_OK) {
                try {
                    if (response.getEntity() == null) {
                        log.error("host :{} response.getEntity() is null, response is :{}", host, response);
                        throw new Exception(statusMsg + ".response.entity.null");
                    }
                    inputStream = response.getEntity().getContent();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(inputStream, writer, "UTF-8");
                    responseString = writer.toString();
                    log.debug("host :{} response.getEntity().getContent().toString() :{}", host, responseString);
                } catch (Exception e) {
                    log.error("{} for requestString:{}", ExceptionUtils.getStackTrace(e), requestJsonString);
                } finally {
                    assert inputStream != null;
                    inputStream.close();
                    response.close();
                }
            } else {
                if (status != HttpStatus.SC_NO_CONTENT) {
                    log.error(statusMsg + " for requestString: " + requestJsonString);
                }
            }
        } catch (IOException e) {
            log.error("execute.post.e", e);
        } finally {
            httpPost.releaseConnection();
        }

        return responseString;
    }
}
