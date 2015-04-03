package com.harmonie.castorev2;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import fr.creditagricole.simone.dataprovider.dto.json.CompteBAMDTOList;
import fr.creditagricole.simone.dataprovider.dto.json.CompteDTOList;
import fr.creditagricole.simone.dataprovider.dto.json.OperationDTOList;
import fr.creditagricole.simone.dataprovider.dto.json.UtilisateurDTO;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;



public class MainActivity extends Activity {

    public static final String SESSION="SESSION";

    private WebView myWebView;
    private ListView listView;
    private MyBroadcastReceiver myReceiver;
    private Context ctx;
    private  IntentFilter filter;

    /*objets OAuth*/
    private OAuthProvider provider;
    private CommonsHttpOAuthConsumer consumer;

    /*Client REST*/
    private MyRestClient restClient;

    /*Objets DTO*/
    private UtilisateurDTO utilisateurDTO;
    private CompteBAMDTOList comptesBAMDTO;
    private CompteDTOList comptesDTO;
    private OperationDTOList operationsDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = getApplicationContext();

        /*Initialisation de la webview*/
        myWebView  = (WebView) this.findViewById(R.id.webview);
        myWebView.setInitialScale(1);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        myWebView.setScrollbarFadingEnabled(false);

        /*Initialisation de la listview*/
        listView = (ListView) this.findViewById(R.id.environement);
        final ArrayList<Environements> list_objects = new ArrayList<Environements>();
        list_objects.add(new Environements());
        final EnvironementsAdapter adapter = new EnvironementsAdapter(this,list_objects);
        listView.setAdapter(adapter);

        /* Initialisation du BroadcastReceiver et d'un filtre
        *  On utilise un BroadcastReceiver pour signaler quand la Webview rend
        * la main */
        filter = new IntentFilter();
        filter.addAction(SESSION);
        myReceiver = new MyBroadcastReceiver();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                authentification();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(ctx).registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onDestroy()
    {
        this.unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    private void authentification()
    {
        myWebView.setVisibility(View.VISIBLE);

        /* ICS n’autorise  pas les requêtes HTTP dans le UI thread (sinon erreur "Communication with the service provider failed"
        * StrictMode.enableDefaults() permet de contourner le problème, mais il faudrait privilégier l'appel des fonctions "DefaultOAuthProvider"
        * et "CommonsHttpOAuthConsumer" dans un thread parallèle*/
        StrictMode.enableDefaults();


        provider = new DefaultOAuthProvider(
                Environements.ENV_OAUTH_HOST + "get_request_token",
                Environements.ENV_OAUTH_HOST + "get_access_token",
                "");

        consumer = new CommonsHttpOAuthConsumer(
                Environements.ENV_APP_KEY,
                Environements.ENV_APP_SECRET);

        String uri = null;
        try {
            uri = provider.retrieveRequestToken(consumer, "http://google.fr");
            Log.d("Uri", uri);
        } catch (OAuthMessageSignerException e) {
            Log.d("UriError", e.getMessage());
        } catch (OAuthNotAuthorizedException e) {
            Log.d("UriError", e.getMessage());
        } catch (OAuthExpectationFailedException e) {
            Log.d("UriError", e.getMessage());
        } catch (OAuthCommunicationException e) {
            Log.d("UriError", e.getMessage());
        };

        myWebView.setWebViewClient(new MyWebViewClient(ctx, provider, consumer));
        myWebView.loadUrl(Environements.ENV_AUTHENT + uri);

    }

    private void session()
    {
        restClient = new MyRestClient (consumer.getToken(), consumer.getTokenSecret(), Environements.ENV_APP_KEY,
                Environements.ENV_APP_SECRET, Environements.ENV_BASE);
        String responseString = restClient.callGET("/session");

        ObjectMapper mapper = new ObjectMapper();
        try {
            utilisateurDTO = mapper.readValue(responseString, UtilisateurDTO.class);
            if(utilisateurDTO != null)
            {
                showAlertDialog("Utilisateur",responseString);
            }
        } catch (IOException e) {e.printStackTrace();}
    }

    private void seeComptes()
    {
        String responseString = restClient.callGET("/utilisateurs/" + utilisateurDTO.getId() + "/comptesBAM");
        ObjectMapper mapper = new ObjectMapper();
        try {
            comptesBAMDTO = mapper.readValue(responseString, CompteBAMDTOList.class);
            if(comptesBAMDTO != null)
            {
                String compteResString = restClient.callGET("/" + comptesBAMDTO.getCompteBAMDTOs().get(0).getLinks().get(0).getHref()+ "/comptes");
                comptesDTO = mapper.readValue(compteResString, CompteDTOList.class);
                if(comptesDTO != null)
                {
                    showAlertDialog("Comptes", compteResString);
                }
            }
        } catch (IOException e) {e.printStackTrace();}
    }

    private void seeOperations()
    {
        String responseString = restClient.callGET("/utilisateurs/" + utilisateurDTO.getId() + "/comptesBAM/" + comptesBAMDTO.getCompteBAMDTOs().get(0).getId()  +
                "/comptes/" + comptesDTO.getCompteDTOs().get(0).getId() + "/operations");
        ObjectMapper mapper = new ObjectMapper();
        try {
            operationsDTO= mapper.readValue(responseString, OperationDTOList.class);
            if(operationsDTO != null)
            {
                showAlertDialog("Operations", responseString);
            }
        }catch (IOException e) {e.printStackTrace();}

    }

    private void seeGeolocalisation()
    {
        String responseString = restClient.callGET("/utilisateurs/" + utilisateurDTO.getId()+ "/geolocalisation/agences/rechercherParCoordonnees" +
                "?latitude=45.5391206" + "&longitude=4.870891571");
        showAlertDialog("Géolocalisation", responseString);
    }

    private void deleteSession()
    {
        restClient.callDELETE("/session");
        showAlertDialog("Session", "Session fermée");
    }

    private void showAlertDialog(String Titre, String message)
    {
        AlertDialog.Builder customBuilder = new AlertDialog.Builder(MainActivity.this).setTitle(Titre).setMessage(message).
                setPositiveButton(getString(android.R.string.ok),new myClickListener(Titre)).setCancelable(false);
        customBuilder.show();
    }

    public class myClickListener implements DialogInterface.OnClickListener {

        private String Titre;
        private final String TitreDeleteSession = "Session";
        private final String TitreDialogUtilisateur = "Utilisateur";
        private final String TitreDialogComptes = "Comptes";
        private final String TitreDialogOperations = "Operations";
        private final String TitreDialogGeoloc = "Géolocalisation";

        public myClickListener(String titre)
        {
            Titre = titre;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            if(Titre.equals(TitreDialogUtilisateur))
            {
                seeComptes();
            }
            else if(Titre.equals(TitreDialogComptes))
            {
                seeOperations();
            }
            else if(Titre.equals(TitreDialogOperations))
            {
                seeGeolocalisation();
            }
            else if (Titre.equals(TitreDialogGeoloc))
            {
                deleteSession();
            }
            else if(Titre.equals(TitreDeleteSession))
            {
                myWebView.setVisibility(View.GONE);
            }
            dialog.dismiss();
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SESSION))
            {
                session();
            }
        }
    }

}

