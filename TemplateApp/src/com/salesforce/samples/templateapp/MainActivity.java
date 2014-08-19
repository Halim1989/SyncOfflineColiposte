/*
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.samples.templateapp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.smartstore.app.SalesforceSDKManagerWithSmartStore;
import com.salesforce.androidsdk.smartstore.store.IndexSpec;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsdk.smartstore.store.SmartStore.Type;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;
import com.salesforce.samples.templateapp.models.Conteneur;
import com.salesforce.samples.templateapp.utils.Loading;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity {

	private final String OBJECT_TYPE = "Conteneur__c";
	private RestClient client;
	private final String API_VERSION = "v29.0";
	private ArrayAdapter<String> conteneurAdapter;
	private static final int CREATE_CONTAINER = 1;
	private String name;
	private Context context = this;

	private EditText txtVignette;
	private EditText txtNom;
	private EditText txtDescription;
	private ListView listConteneurs;

	private Conteneur conteneur;

	private SalesforceSDKManagerWithSmartStore sdkManager;
	private SmartStore smartStore;

	private final String USERS_SOUP = "Conteneur";
	private final IndexSpec[] USERS_INDEX_SPEC = {
			new IndexSpec("Id", Type.string),
			new IndexSpec("Name", Type.string),
			new IndexSpec("ProfileName__c", Type.string),
			new IndexSpec("Site_de_travail__c", Type.string) };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup view
		setContentView(R.layout.main);

		txtVignette = (EditText) findViewById(R.id.txtVignette);
		txtNom = (EditText) findViewById(R.id.txtNom);
		txtDescription = (EditText) findViewById(R.id.txtDescription);
		listConteneurs = (ListView) findViewById(R.id.listConteneurs);

	}

	@Override
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);
		super.onResume();

	}

	@Override
	public void onResume(RestClient client) {
		// Keeping reference to rest client
		this.client = client;

		conteneur = new Conteneur(client, context);
		conteneur.initConteneurSmartStore();
		// Show everything
		findViewById(R.id.root).setVisibility(View.VISIBLE);
		updateListView();
	}

	public void updateListView() {
		JSONArray conteneurs = conteneur.find();
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < conteneurs.length(); i++) {
			try {
				JSONObject c = conteneurs.getJSONObject(i);
				values.add(c.getString("Name"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_list_item_1,
				values);
		listConteneurs.setAdapter(adapter);
	}

	/**
	 * Called when "Logout" button is clicked.
	 * 
	 * @param v
	 */
	public void onLogoutClick(View v) {
		SalesforceSDKManager.getInstance().logout(this);
	}

	/**
	 * Called when "Clear" button is clicked.
	 * 
	 * @param v
	 */
	public void onClearClick(View v) {
	}

	/**
	 * Called when "Fetch Contacts" button is clicked
	 * 
	 * @param v
	 * @throws UnsupportedEncodingException
	 */
	public void onFetchContactsClick(View v)
			throws UnsupportedEncodingException {
		sendRequest("SELECT Name FROM Contact");
	}

	/**
	 * Called when "Fetch Accounts" button is clicked
	 * 
	 * @param v
	 * @throws UnsupportedEncodingException
	 */
	public void onFetchAccountsClick(View v)
			throws UnsupportedEncodingException {
		sendRequest("SELECT Name FROM Account");
	}

	private void sendRequest(String soql) throws UnsupportedEncodingException {
		RestRequest restRequest = RestRequest.getRequestForQuery(
				getString(R.string.api_version), soql);
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {
				Loading.hide();
				try {
					JSONArray records = result.asJSONObject().getJSONArray(
							"records");
					// Create
					if (records.length() == 0) {
						showDialog(CREATE_CONTAINER);
					}
					// Update
					else {
						JSONObject conteneur = records.getJSONObject(0);
						String NomCP_c = conteneur.getString("NomCP__c");
						String Description__c = conteneur
								.getString("Description__c");
						String Id = conteneur.getString("Id");

						Intent intent = new Intent(getApplicationContext(),
								ConteneurForm.class);
						intent.putExtra("Name", name);
						intent.putExtra("Id", Id);
						intent.putExtra("NomCP__c", NomCP_c);
						intent.putExtra("Description__c", Description__c);
						intent.putExtra("action", "update");
						startActivity(intent);
					}
				} catch (Exception e) {
					Loading.hide();
					onError(e);
				}
			}

			@Override
			public void onError(Exception exception) {
				/*
				 * Toast.makeText( MainActivity.this,
				 * MainActivity.this.getString(SalesforceSDKManager
				 * .getInstance().getSalesforceR() .stringGenericError(),
				 * exception.toString()), Toast.LENGTH_LONG).show();
				 */
			}
		});
	}

	/*
	 * Called when Scan vignette is called
	 */

	public void onSynch(View v) throws UnsupportedEncodingException,
			JSONException {

		Conteneur conteneur = new Conteneur(client, context);
		conteneur.initConteneurSmartStore();

		// Ajouter un conteneur
		// conteneur.add();

		// Récupérer la liste des conteneurs dans le soup
		JSONArray conteneurs = conteneur.find();
		Toast.makeText(context,
				"Nombre de conteneur dans la file : " + conteneurs.length(),
				Toast.LENGTH_SHORT).show();
		Toast.makeText(context, "Synchronisation ... ", Toast.LENGTH_SHORT)
				.show();

		// Effectuer la synchronisation
		conteneur.sync(conteneurs);

		sendNotification();
	}

	public void sendNotification() {
		NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification note = new Notification(R.drawable.logocse, "New E-mail",
				System.currentTimeMillis());
		PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(
				this, MainActivity.class), 0);
		note.setLatestEventInfo(
				this,
				"Rapport d'erreur offline",
				"Un rapport contenant les erreurs de synchronisation vous a été envoyé à l'admin",
				intent);
		notifManager.notify(1234, note);
	}

	public void onAdd(View v) throws JSONException {
		conteneur.add(txtVignette.getText().toString(), txtNom.getText()
				.toString(), txtDescription.getText().toString());
		updateListView();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch (id) {
		case CREATE_CONTAINER:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Ce conteneur n'existe pas. Voulez vous le créer ?")
					.setCancelable(true)
					.setPositiveButton("Oui",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent intent = new Intent(
											getApplicationContext(),
											ConteneurForm.class);
									intent.putExtra("Name", name);
									intent.putExtra("action", "add");
									startActivity(intent);
								}
							})
					.setNegativeButton("Non",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	public void initSoupConteneur() {
		if (!smartStore.hasSoup(USERS_SOUP)) {
			smartStore.registerSoup(USERS_SOUP, USERS_INDEX_SPEC);
			System.out.println("Soup created ");
		} else {
			System.out.println("Init user soup ... ");
		}

	}

	public void initConteneurSmartStore() {
		sdkManager = SalesforceSDKManagerWithSmartStore.getInstance();
		smartStore = sdkManager.getSmartStore();
		// initSoupConteneur();
	}

	private void executeQuery(RestRequest restRequest) {
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {
				try {
					Toast.makeText(getApplicationContext(), "Success !!! ",
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					// System.err.println(e.getMessage());
					VolleyError volleyError = (VolleyError) e;
					NetworkResponse response = volleyError.networkResponse;
					String json = new String(response.data);
					System.out.println(json);
					Toast.makeText(context, "Error !!! " + name + json,
							Toast.LENGTH_LONG).show();

				}

			}

			@Override
			public void onError(Exception exception) {
				System.err.println(exception.getMessage());
			}
		});
	}

}
