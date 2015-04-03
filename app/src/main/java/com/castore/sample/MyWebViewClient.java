package com.castore.sample;

import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

/**
 * Created by rlonguet on 31/03/2015.
 */
public class MyWebViewClient extends WebViewClient {

    private OAuthProvider provider;
    private OAuthConsumer consumer;
    private Context ctx;


    public MyWebViewClient(Context ct, OAuthProvider prov,OAuthConsumer cons)
    {
        provider = prov;
        consumer = cons;
        ctx = ct;
    }

    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed() ;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if (url.startsWith("http://google.fr")) {
            List<NameValuePair> parameters;
            try {
                parameters = URLEncodedUtils.parse(new URI(url), "UTF-8");
                String oauth_verifier = "";
                for (NameValuePair p : parameters) {
                    if (p.getName().equals("oauth_verifier"));
                    oauth_verifier = p.getValue();
                }

                provider.retrieveAccessToken(consumer, oauth_verifier);
                /*On envoit un intent à la MainActivity pour rendre la main*/
                Intent startSession = new Intent();
                startSession.setAction(MainActivity.SESSION);
                /*Un LocalBroadcastManager est plus sécurisé pour des intent intra-application*/
                LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(ctx);
                broadcaster.sendBroadcast(startSession);

            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (OAuthMessageSignerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (OAuthNotAuthorizedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }


}
