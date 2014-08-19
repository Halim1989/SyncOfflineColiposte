package com.salesforce.samples.templateapp.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.salesforce.androidsdk.rest.RestClient;

public class Util {
	
	/**
	 * Helper method
	 * checks substring in string
	 */
	
	public static boolean checkSubstring(String needle, String stack) {
		return stack.toLowerCase().contains(needle.toLowerCase());
	}
	
	public static void toastException(Activity act, Exception e) {
		try {
			VolleyError volleyError = (VolleyError) e;
	        NetworkResponse response = volleyError.networkResponse;
	        String json = new String(response.data);
	        JSONArray ja = new JSONArray(json);
	        Util.log("ORIGINAL  : "+ja.toString());
        	
	        for(int i = 0; i<ja.length(); ++i) {
	        	JSONObject jo = ja.getJSONObject(i);
	        	if(!Util.isNull(jo)) {
	        		if(!Util.isNull(Util.safeGet(jo, "message"))) {
	        			Util.toast(act, Util.safeGet(jo, "message"));
		        	}
	        	}
	        }
	    } catch (Exception e1) {
			Util.log("ERROR: ");
	    	e.printStackTrace();
	    	//e1.printStackTrace();
		}
    }
	
    public static String restNow() {
    	return String.format("%tFT%<tRZ", new Date()); 
    	//return DatatypeConverter.printDateTime(new GregorianCalendar());
    }
    
	
	/**
	 * Helper to read json string representing field name-value map
	 * 
	 * @param jsonTextField
	 * @return
	 */
	public static  Map<String, Object> parseFieldMapForUpsert(JSONObject fieldsJson) {
		try {
			Map<String, Object> fields = new HashMap<String, Object>();
			JSONArray names = fieldsJson.names();
			for (int i = 0; i < names.length(); i++) {
				String name = (String) names.get(i);
			    if(!name.equals("_soupEntryId") && !name.equals("_soupLastModifiedDate")) {
					if(!Util.isNull(fieldsJson.get(name)))
						fields.put(name, fieldsJson.get(name));
				} else {
					
				} 
			}
			return fields;
		} catch (Exception e) {
			return null;
		}
	}
	 /**
	  * Debug log
	  * @param msg
	  */
    public static void log(String msg) {
    	Log.d("Custom debug : ",msg);
    }
	
	/**
	 * helper toast
	 */
	public static void toast(Activity act, String text) {
		Toast.makeText(act, text, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * If the str is null, return empty
	 * @param str
	 * @return
	 */
	
	public static String correctNull(String str) {
		if(str == null || str.equalsIgnoreCase("null")) return "";
		else return str;
	}
	
	
	
	/**
	 * Log big messages
	 * @param msg
	 */
	public static void logBig(String msg) {
		int maxLogSize = 1000;
		for(int i = 0; i <= msg.length() / maxLogSize; i++) {
		    int start = i * maxLogSize;
		    int end = (i+1) * maxLogSize;
		    end = end > msg.length() ? msg.length() : end;
		    Log.d("Custom debug", msg.substring(start, end));
		}
	}
	
	/**
	 * Rturns a valid iso 18061 datetime from datepicker and timepicker
	 */
	
	@SuppressWarnings("deprecation")
	public static String getRestApiDateTime(DatePicker receiveDP,
		 TimePicker receiveTP) {
		 int YYYY = receiveDP.getYear()-1900;
	   	 int MM = receiveDP.getMonth();
	   	 int DD = receiveDP.getDayOfMonth();
	   	 int hh = receiveTP.getCurrentHour();
	   	 int mm = receiveTP.getCurrentMinute();
	   	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00Z'");
 	 	 return sdf.format(new Date(YYYY, MM, DD, hh, mm));
    }
	
	public static String nowAsIso() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    df.setTimeZone(tz);
	    String nowAsISO = df.format(new Date());
	    return nowAsISO;
    }
		
	/**
	 * Check null
	 */
	public static boolean isNull(Object o) {
		if(o == null || o.toString().equals("") || o.toString().equals("null")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check null
	 */
	public static boolean isStrictNull(Object o) {
		if(o == null || o.toString().equals("null")) {
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * get the current datetime for Rest Operation
	 */
	public static String getRestApiCurrentDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00Z'");
	 	return sdf.format(new Date());
	}	
	
	/**
	 * Adds quotes to String
	 */
	
	public static String quote(String str) {
		return "'"+str+"'";
	}
	
	

	/**
	 * Gets a dynamic soql in clause from jsonObject Ids
	 * @param ja
	 * @return
	 */
	public static String getDynamicInClause(JSONArray ja, String field) {
		ArrayList<String> ids = new ArrayList<String>();
		for(int i = 0; i< ja.length(); i++) {
			String id = null;	
			try {
				id = ja.getJSONObject(i).getString(field);
			} catch (JSONException e) {
				id = null;
			}
			if(!Util.isNull(id)) ids.add(Util.quote(id));
		}
		return Util.join(ids);
	}

	/**
	 * Convert a string array to comma separated String
	 */
	public static String join(ArrayList<String> array) {
		if(array.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String n : array) { 
			    if (sb.length() > 0) sb.append(',');
			    sb.append(n);
			}
			return sb.toString();
		} else {
			return "";
		}
			
	}

	public static Date getDateFromRest(String sfRecordCreationDateTime) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(sfRecordCreationDateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Util.log("LOG DATE RETURNED");
		Util.log(date.toGMTString());
		return date;
	}
	
	/**
	 * Safe get from json
	 * @param jo
	 * @param field
	 */
	public static String safeGet(JSONObject jo, String field) {
		String value = null;
		try {
			value = jo.getString(field);
		} catch (Exception e) {
		}
		return value;
	}
	
	public static String safeGet(Bundle b, String field) {
		String value = null;
		try {
			value = b.getString(field);
		} catch (Exception e) {
		}
		return value;
	}
	
	
	/**
	 * Encode to basr 64
	 */
	
	public static String encodeTobase64(Bitmap image) {
	    Bitmap immagex = Bitmap.createScaledBitmap(image, 300, 300, true);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	    byte[] b = baos.toByteArray();
	    String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);
	    return imageEncoded;
	}

	public static String getAttachmentName(String otId, String type) {
		Random rand = new Random();
		int r = rand.nextInt();
		return otId+"__"+r+"."+type;
	}
	/**
	 * Gets edit text TEXT
	 * @return
	 */
	public static String getET(Activity act, int rId) {
		EditText et = (EditText) act.findViewById(rId);
		//if(et != null) {
			String text = et.getText().toString();
			return text;
		//} else {
		//	return null;
		//}
	}
	
	
	public static EditText getEditText(Activity act, int rId) {
		EditText et = (EditText) act.findViewById(rId);
		if(Util.isNull(et))
			return null;
		else return et;
	}
	

	public static Button getButton(Activity act, int rId) {
		Button btn = (Button) act.findViewById(rId);
		if(Util.isNull(btn))
			return null;
		else return btn;
	}
	
	
	public static String getET(LinearLayout ll, int rId) {
		EditText et = (EditText) ll.findViewById(rId);
		String text = et.getText().toString();
		if(Util.isNull(text))
			return null;
		else return text;
	}
	/**
	 * Sets edit text
	 * @return
	 */
	public static void setET(RelativeLayout item, int rId, String value) {
		//if(item != null) {
			EditText et = (EditText) item.findViewById(rId);
			//if(!Util.isNull(et)) {
				if(!Util.isNull(value))
					et.setText(value);
				else 
					et.setText("");
			//} else return;
		//}
		return;
	}	
	
	/**
	 * Print exception
	 * @param act
	 * @param rId
	 * @param value
	 */
	public static void printException(Activity launcher, Exception e) {
		toast(launcher, "Error: " + e.getClass().getSimpleName()+"\n"+e.getMessage());
	}
	
	
	public static void setET(Activity act, int rId, String value) {
		if(act != null) {
			EditText et = (EditText) act.findViewById(rId);
			if(!Util.isNull(et)) {
				if(!Util.isNull(value))
					et.setText(value);
				else 
					et.setText("");
			} else return;
		}
		return;
	}	
	
	public static void setTV(Activity act, int rId, String value) {
		if(act != null) {
			TextView et = (TextView) act.findViewById(rId);
			if(!Util.isNull(et)) {
				if(!Util.isNull(value))
					et.setText(value);
				else 
					et.setText("");
			} else return;
		}
		return;
	}	
	
	
	/**
	 * Get bitmap from base 64 string
	 */
	public static Bitmap getBitmapFromBase64(String base64) {
		byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
	    return decodedByte;	    
	}
	
	
	public static Bitmap getBitmapFromByte(byte[] blob) {
		return BitmapFactory.decodeByteArray(blob,0,blob.length);
	}
	
	/**
	 * Get image from salesforce
	 * @param takePicture
	 * @param jsonObject
	 * @param sfClient
	 */

	public static boolean emptyObject(JSONObject jo) {
		if(jo.toString().equals("{}")) return true;
		return false;	
	}
	
	/**
	 * Open PDF
	 * @param path
	 */
	private static Bitmap openImage(String path){
		File file = new File(path);
		if(file.exists()){
		    return BitmapFactory.decodeFile(file.getAbsolutePath());
		} else {
			return null;
		}
	}

	public static boolean validateBarcode(String dwBarcode) {
		return !Util.isNull(dwBarcode);
	}
	
	public static String getSpinnerString(Activity currentActivity,
			int categoryspinner) {
		
		Spinner spin = (Spinner) currentActivity.findViewById(categoryspinner);
		if(spin != null) {
			String selected = (String) spin.getSelectedItem();
			if(Util.isNull(selected))
				return "";
			else return selected;	
		}
		return "";
	}
	
	public static String getSpinnerString(LinearLayout ll,
			int categoryspinner) {
		Spinner spin = (Spinner) ll.findViewById(categoryspinner);
		
		String selected = (String) spin.getSelectedItem();
		if(Util.isNull(selected))
			return "";
		else return selected;
	}
	
		
	public static void setSpinner(Spinner spinner, String value) {
		if(spinner != null) {
			ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
			if(adapter != null) {
				for(int i=0 ; i<adapter.getCount() ; i++){
					   String str =  adapter.getItem(i);
					   //Util.log(so.getValue()+" IS COMPARED TO : "+selectOption.getValue());
					   if(str.equals(value)) {
							spinner.setSelection(i);
					   }
				}	
			}	
		}
	}	
	
	
	
	/*
	 * Dumps intent
	 */
	 public static void dumpIntent(Intent i){

		    Bundle bundle = i.getExtras();
		    if (bundle != null) {
		        Set<String> keys = bundle.keySet();
		        Iterator<String> it = keys.iterator();
		        while (it.hasNext()) {
		            String key = it.next();
		            Util.log("[" + key + "=" + bundle.get(key)+"]");
		        }
		    }
		}
	 
	 
	 public static String getStringFromInputStream(InputStream is) {

			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();

			String line;
			try {

				br = new BufferedReader(new InputStreamReader(is));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return sb.toString();

		}
	
		
		public static void showLinearLayout(Activity act, int id) {
			LinearLayout ll = (LinearLayout) act.findViewById(id);
			ll.setVisibility(View.VISIBLE);
		}
		
		public static void hideLinearLayout(Activity act, int id) {
			LinearLayout ll = (LinearLayout) act.findViewById(id);
			ll.setVisibility(View.GONE);
		}
}	
