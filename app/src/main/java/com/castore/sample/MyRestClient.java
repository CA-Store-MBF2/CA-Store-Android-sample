package com.castore.sample;

/**
* Created by rlonguet on 31/03/2015.
*/


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;


public class MyRestClient implements Serializable {

    /**
     * Seules les méthodes GET et DELETE sont implémentés
     * Il est possible de faire des appels en POST et en PUT (voir documentation CA Store)
     */
    private static final long serialVersionUID = -5641823795092320969L;

    private CommonsHttpOAuthConsumer consumer;
    private HttpClient httpClient;
    private String consumerKey;
    private String consumerSecret;
    private String base;

    public MyRestClient(String token, String secret, String consumerKey, String consumerSecret, String base) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.base = base;
        this.consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        consumer.setTokenWithSecret(token, secret);
        this.httpClient = new DefaultHttpClient();
    }

    /*
    Méthode pour faire des GET sur les webservices castore
     */
    public String callGET(String endPath) {

        try {
            HttpGet request = new HttpGet(this.base + endPath);
            //Signe la requête avec les entêtes OAuth
            consumer.sign(request);
            //Possible de mettre un header (application/xml)
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            HttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                return EntityUtils.toString(responseEntity);
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Erreur lors de l'appel WS";
    }

    /*
    Méthode pour faire des DELETE sur les webservices castore
    */
    public void callDELETE(String endPath) {
        try {
            HttpDelete request = new HttpDelete(this.base + endPath);
            //Signe la requête avec les entêtes OAuth
            consumer.sign(request);
            HttpResponse response = httpClient.execute(request);
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
        } catch (OAuthCommunicationException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
