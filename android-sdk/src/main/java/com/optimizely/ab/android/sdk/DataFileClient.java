package com.optimizely.ab.android.sdk;

import android.support.annotation.NonNull;

import com.optimizely.ab.android.shared.Client;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jdeffibaugh on 7/28/16 for Optimizely.
 * <p/>
 * Makes requests to the Optly CDN to get the data file
 */
public class DataFileClient {

    @NonNull private final Client client;
    @NonNull private final Logger logger;

    DataFileClient(@NonNull Client client, @NonNull Logger logger) {
        this.client = client;
        this.logger = logger;
    }

    public String request(String urlString) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            logger.info("Requesting data file from {}", url);
            urlConnection = client.openConnection(url);

            client.setIfModifiedSince(urlConnection);

            urlConnection.setConnectTimeout(5 * 1000);
            urlConnection.connect();

            int status = urlConnection.getResponseCode();
            if (status >= 200 && status < 300) {
                client.saveLastModified(urlConnection);
                return client.readStream(urlConnection);
            } else if (status == 304) {
                logger.info("Data file has not been modified on the cdn");
                return null;
            } else {
                logger.error("Unexpected response from data file cdn, status: {}", status);
                return null;
            }
        } catch (IOException e) {
            logger.error("Error making request", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


}