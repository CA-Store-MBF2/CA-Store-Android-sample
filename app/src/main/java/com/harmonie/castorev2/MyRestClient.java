package com.harmonie.castorev2;

/**
* Created by rlonguet on 31/03/2015.
*/

import com.expertiseandroid.castore.auth.MySSLSocketFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.security.KeyStore;

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
        this.httpClient = getNewHttpClient();
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
    /*
    Création du client Web pour faire les requêtes vers CA Store
    */
    private HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            //Permet de bypass la vérification du certificat SSL CA Store
            //A ne pas faire sur l'environnement de Production
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
}
