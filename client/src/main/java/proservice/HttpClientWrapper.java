package proservice;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mirian Dzhachvadze
 */
public class HttpClientWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClientWrapper.class);

    private final CloseableHttpClient httpClient;
    private final String url;

    public HttpClientWrapper(CloseableHttpClient httpClient, String url) {
        this.httpClient = httpClient;
        this.url = url;
    }

    public Long getAmount(Integer id) {
        LOGGER.info("HttpClientWrapper.getAmount - Request for balance [" + id + "]");
        HttpGet httpGet = new HttpGet(url + id);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);

            HttpEntity responseEntity = response.getEntity();
            String amount = IOUtils.toString(responseEntity.getContent());

            return Long.valueOf(amount);
        } catch (Exception e) {
            LOGGER.error("HttpClientWrapper.getAmount - "
                         + "Failed to get balance for account [" + id + "]", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    //ignore it
                }
            }
        }

        return null;
    }

    public void addAmount(Integer id, Long value) {
        LOGGER.info("HttpClientWrapper.addAmount - "
                    + "Add value [" + value + "] for balance [" + id + "]");

        List<NameValuePair> formParams = Lists.newArrayList();
        formParams.add(new BasicNameValuePair("id", id.toString()));
        formParams.add(new BasicNameValuePair("value", value.toString()));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);

        HttpPost httpPost = new HttpPost(url + "add");
        httpPost.setEntity(entity);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (Exception e) {
            LOGGER.error("HttpClientWrapper.getAmount - "
                         + "Failed to get balance for [" + id + "]", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    //ignore it
                }
            }
        }
    }

    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            // ignore it
        }
    }
}
