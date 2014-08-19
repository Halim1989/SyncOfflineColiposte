package com.salesforce.samples.templateapp;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;
import com.salesforce.androidsdk.util.EventsObservable;
import com.salesforce.androidsdk.util.EventsObservable.EventType;
import com.salesforce.samples.templateapp.utils.Loading;

public class ConteneurForm extends SalesforceActivity {

	private RestClient client;
	private final String API_VERSION = "v29.0";
	private final String OBJECT_TYPE = "Conteneur__c";
	private String Name;
	private String Id;
	private EditText description;
	private EditText nom;
	private Button btnValider;
	private String action;

	private final int CREATE_CONTAINER_SUCCESS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup view
		setContentView(R.layout.conteneur_from);

		description = (EditText) findViewById(R.id.txtDescription);
		nom = (EditText) findViewById(R.id.txtNom);
		btnValider = (Button) findViewById(R.id.btnValider);

		Bundle bundle = getIntent().getExtras();
		Name = bundle.getString("Name");
		Id = bundle.getString("Id");
		action = bundle.getString("action");

		if (action.equals("update")) {
			btnValider.setText(R.string.modifier_button);

			String NomCP__c = bundle.getString("NomCP__c");
			String Description__c = bundle.getString("Description__c");
			if (!Description__c.equals("null")) {
				description.setText(Description__c);

			}
			if (!NomCP__c.equals("null")) {
				nom.setText(NomCP__c);
			}

		}

	}

	@Override
	public void onResume(RestClient client) {
		this.client = client;

	}

	public void onValider(View v) {
		Loading.show(ConteneurForm.this);
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("Name", Name);
		fields.put("NomCP__c", nom.getText().toString());
		fields.put("Description__c", description.getText().toString());
		RestRequest request = null;
		try {
			if (action.equals("add")) {
				request = RestRequest.getRequestForCreate(API_VERSION,
						OBJECT_TYPE, fields);
			} else {
				System.out.println("action ========= " + Id);
				request = RestRequest.getRequestForUpdate(API_VERSION,
						OBJECT_TYPE, Id, fields);
			}

		} catch (Exception e) {
			return;
		}
		executeQuery(request);

	}

	/**
	 * Send restRequest using RestClient's sendAsync method. Note: Synchronous
	 * calls are not allowed from code running on the UI thread.
	 * 
	 * @param restRequest
	 */
	private void executeQuery(RestRequest restRequest) {
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {
				try {
					Loading.hide();
					String msg;
					if (action.equals("add")) {
						msg = getResources().getString(
								R.string.msg_add_conteneur_success);
					} else {
						msg = getResources().getString(
								R.string.msg_update_conteneur_success);
					}
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}

				EventsObservable.get().notifyEvent(EventType.RenditionComplete);
			}

			@Override
			public void onError(Exception exception) {
				System.err.println(exception.getMessage());
			}
		});
	}
}
