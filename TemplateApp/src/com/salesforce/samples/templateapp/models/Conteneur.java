package com.salesforce.samples.templateapp.models;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.smartstore.app.SalesforceSDKManagerWithSmartStore;
import com.salesforce.androidsdk.smartstore.store.IndexSpec;
import com.salesforce.androidsdk.smartstore.store.QuerySpec;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsdk.smartstore.store.SmartStore.Type;
import com.salesforce.samples.templateapp.entities.Conteneur__c;
import com.salesforce.samples.templateapp.utils.Util;

public class Conteneur {

	private final String API_VERSION = "v29.0";
	private final String OBJECT_TYPE = "Conteneur__c";
	private SalesforceSDKManagerWithSmartStore sdkManager;
	private SmartStore smartStore;
	private RestClient client;
	private Context context;
	private int nbCp;
	private int cptCp = 0;
	private boolean endOfSync = false;
	private int errorNumber = 1;
	private StringBuilder errorRepport = new StringBuilder();
	private final static String SOUP_CONTENEUR = "Conteneur";
	private static IndexSpec[] CONTENEUR_INDEX_SPEC = {
			new IndexSpec("Id", Type.string),
			new IndexSpec("Name", Type.string),
			new IndexSpec("NomCP__c", Type.string),
			new IndexSpec("Description__c", Type.string) };

	public Conteneur(RestClient client, Context context) {
		// API_VERSION = context.getString(R.string.api_version);
		this.client = client;
		this.context = context;

		errorRepport.append("Rapport : " + new Date() + "\n");
		errorRepport.append("Site de production : " + "Site \n");
		errorRepport.append("Utilisateur : " + "User \n");

		// initConteneurSmartStore();
	}

	public void getConteneurById(String id) {
		/*
		 * QuerySpec querySpec = QuerySpec.buildExactQuerySpec(SOUP_CONTENEUR,
		 * "Name", id, 1);
		 */
		QuerySpec querySpec = QuerySpec.buildAllQuerySpec(SOUP_CONTENEUR,
				"Name", QuerySpec.Order.ascending, 100);
		JSONArray records;
		try {
			records = smartStore.query(querySpec, 0);
			System.out.println(records.toString());
			if (records.length() > 0) {
				System.out.println("Conteneur exists" + records.length());
			} else {
				System.out.println("Conteneur doesn't exist");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JSONArray find() {
		// QuerySpec querySpec = QuerySpec.buildSmartQuerySpec("select * from {"
		// + SOUP_CONTENEUR + "}", 1);

		QuerySpec querySpec = QuerySpec.buildAllQuerySpec(SOUP_CONTENEUR,
				"Name", QuerySpec.Order.ascending, 100);
		JSONArray records = null;

		try {
			records = smartStore.query(querySpec, 0);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return records;
	}

	/*
	 * Cette methode prend comme paramètre la liste des conteneurs a synchroniser
	 * 
	 */
	
	public void sync(JSONArray conteneurs) throws JSONException,
			UnsupportedEncodingException {
		
		// nbCp est la variable qui contient le nombre total de conteneurs a synchroniser
		nbCp = conteneurs.length();
		
		// Synchroniser les conteneurs un par un en utilisant la methode syncOne
		for (int i = 0; i < conteneurs.length(); i++) {
			JSONObject conteneur = conteneurs.getJSONObject(i);
			Toast.makeText(context, "Sync cp : " + conteneur.getString("Name"),
					Toast.LENGTH_LONG).show();
			syncOne(conteneur);
		}

	}

	/*
	 * Cette methode permet d'upserter le conteneur donné en paramètre
	 * En cas d'erreur un rapport est créé
	 * A la fin de la synchronisation un Objet Task est créé sur salesforce
	 */
	public void syncOne(JSONObject conteneur) throws JSONException,
			UnsupportedEncodingException {
		if (conteneur != null) {

			Map<String, Object> fields = new HashMap<String, Object>();
			fields.put("NomCP__c", conteneur.getString("NomCP__c"));
			fields.put("Description__c", conteneur.getString("Description__c"));
			RestRequest request = null;
			try {
				request = RestRequest.getRequestForUpsert(API_VERSION,
						OBJECT_TYPE, "Name", conteneur.getString("Name"),
						fields);

			} catch (Exception e) {
				return;
			}

			executeQuery(request, conteneur);
		}
	}

	private void executeQuery(RestRequest restRequest,
			final JSONObject conteneur) throws NumberFormatException, JSONException {
		final Long cpId = Long.valueOf(conteneur.getString("_soupEntryId"));
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {

				Toast.makeText(context, "Success !! ", Toast.LENGTH_LONG)
						.show();
				cptCp++;
				// A chaque synchronisation on teste si c'est le dernier conteneur
				// Si oui on  crée l'objet task
				if (cptCp == nbCp) {
					CreateTask();
					Toast.makeText(context, "Creating task", Toast.LENGTH_LONG)
							.show();
					Toast.makeText(context, errorRepport.toString(),
							Toast.LENGTH_LONG).show();
				}
				
				// Supprimer le conteneur du Soup
				deleteCP(cpId);

			}

			@Override
			public void onError(Exception exception) {
				VolleyError volleyError = (VolleyError) exception;
				NetworkResponse response = volleyError.networkResponse;
				String json = new String(response.data);

				System.out.println(json);
				Toast.makeText(context,
						"Error !!! Envoi du rapport ... " + json,
						Toast.LENGTH_LONG).show();
				cptCp++;

				// En cas d'erreur on crée un rapport d'erreur
				CreateError(json, conteneur);

				// A chaque synchronisation on teste si c'est le dernier conteneur
				// Si oui on  crée l'objet task
				if (cptCp == nbCp) {
					Toast.makeText(context, "Creating task ... ",
							Toast.LENGTH_LONG).show();
					Toast.makeText(context, errorRepport.toString(),
							Toast.LENGTH_LONG).show();
					CreateTask();
				}
				
				// Supprimer le conteneur du Soup 
				deleteCP(cpId);
			}
		});
	}

	private void executeQuery(RestRequest restRequest) {
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {

				Toast.makeText(context, result.toString(), Toast.LENGTH_LONG)
						.show();

			}

			@Override
			public void onError(Exception exception) {
				VolleyError volleyError = (VolleyError) exception;
				NetworkResponse response = volleyError.networkResponse;
				String json = new String(response.data);
				Toast.makeText(context, json, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void CreateTask() {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("Description", errorRepport.toString());
		fields.put("Status", "En cours");
		fields.put("Subject", "Rapport d'erreur offline");
		fields.put("OwnerId", "005b0000000y2lN");

		RestRequest request = null;
		try {
			request = RestRequest.getRequestForCreate(API_VERSION, "Task",
					fields);

		} catch (Exception e) {
			return;
		}

		executeQuery(request);
	}

	private void CreateError(String error, JSONObject conteneur) {
		JSONArray ja;
		try {
			errorRepport.append("\n");
			errorRepport.append("Erreur " + errorNumber + " : \n");
			errorNumber++;
			errorRepport.append("   Name : ");
			errorRepport.append(conteneur.get("Name") + "\n");

			ja = new JSONArray(error);
			for (int i = 0; i < ja.length(); ++i) {
				JSONObject jo = ja.getJSONObject(i);
				if (!Util.isNull(jo)) {
					if (!Util.isNull(Util.safeGet(jo, "message"))) {
						errorRepport.append("   Description : ");
						errorRepport
								.append(Util.safeGet(jo, "message") + " \n");
					}
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void add(String vignette, String nom, String description) {
		
		initConteneurSmartStore();
		Gson gson = new Gson();
		Conteneur__c conteneur__c = new Conteneur__c();

		conteneur__c.setName(vignette);
		conteneur__c
				.setDescription__c(description);
		conteneur__c.setNomCP__c(nom);

		String strCp = gson.toJson(conteneur__c);
		JSONObject jsonCP;
		try {
			jsonCP = new JSONObject(strCp);
			JSONObject j = smartStore.upsert(SOUP_CONTENEUR, jsonCP);

			System.out.println(j);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void initConteneurSmartStore() {
		sdkManager = SalesforceSDKManagerWithSmartStore.getInstance();
		smartStore = sdkManager.getSmartStore();
		initConteneurSoup();
	}

	public void initConteneurSoup() {
		if (!smartStore.hasSoup(SOUP_CONTENEUR)) {
			smartStore.registerSoup(SOUP_CONTENEUR, CONTENEUR_INDEX_SPEC);
			System.out.println("SOUP created ... " + SOUP_CONTENEUR);
		} else {
			System.out.println("SOUP initialized ... " + SOUP_CONTENEUR);
		}
	}

	public static SmartStore getSmartStore(String soupName,
			IndexSpec[] soupIndexSpecs) {
		SalesforceSDKManagerWithSmartStore sdkManager = SalesforceSDKManagerWithSmartStore
				.getInstance();
		SmartStore smartStore = sdkManager.getSmartStore();
		if (!smartStore.hasSoup(soupName))
			smartStore.registerSoup(soupName, soupIndexSpecs);
		return smartStore;
	}

	public void deleteAllCPs() throws JSONException {
		JSONArray conteneurs = find();
		Long[] soupIdsToDelete = new Long[conteneurs.length()];
		for (int i = 0; i < conteneurs.length(); i++) {
			JSONObject conteneur = conteneurs.getJSONObject(i);
			soupIdsToDelete[i] = Long.valueOf(conteneur
					.getString("_soupEntryId"));
		}
		smartStore.delete(SOUP_CONTENEUR, soupIdsToDelete);
	}

	public void deleteCP(Long cpId) {
		smartStore.delete(SOUP_CONTENEUR, cpId);
	}

}
