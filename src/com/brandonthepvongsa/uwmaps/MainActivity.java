package com.brandonthepvongsa.uwmaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;

public class MainActivity extends Activity {
	private GoogleMap map;

	// Default map location
	private LatLng DEFAULTCORDS = new LatLng(47.654930, -122.308681);
	private float DEFAULTZOOM = 15.9f;

	private List<LatLng> bikeLocks;
	private Map<String, LatLng> buildings;
	private boolean bikeLocksShown = false;
	
	private Menu menu;

	// // Drop Down Menu
	// SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
	// R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
	// Set overlay map
	// BitmapDescriptor image =
	// BitmapDescriptorFactory.fromAsset("overlay.png");
	// LatLngBounds bounds = new LatLngBounds(
	// new LatLng(
	//

	/**
	 * Setup the layout and content and instantiate map. Move map to the default
	 * location and zoom.
	 */
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Grab a reference to the ActionBar
		final ActionBar actionBar = getActionBar();

		// Set ActionBar background to semi-transparent
		actionBar
				.setBackgroundDrawable(new ColorDrawable(Color.rgb(43, 4, 65)));

		// Get a handle for the map
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		// map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULTCORDS,
				DEFAULTZOOM));

		// Allow user to see their current location
		map.setMyLocationEnabled(true);

		// Handle search from the actionBar
		handleIntent(getIntent());
		// Load coordinates of various provided amenities from files
		try {
			bikeLocks = readCordFile("bike-locks.txt");
			buildings = readMapFile("building-cords.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		// Start up the database tables
		DatabaseTable databaseTable = new DatabaseTable(this);
		
		String[] cols = {"TITLE", "LAT", "LNG"};
		String query = "kane";
		Log.e("app", "what in the fuck...");
		Cursor cursor = databaseTable.getBuildingMatches(query, null);
		
		if(cursor == null) {
			Log.e("app", "cursor is actually null tbh");
		} else {
			Log.e("App", "titlesss: " + cursor.getString(cursor.getColumnIndex("TITLE")));
			while(cursor.moveToNext()) {
				Log.e("App", "title: " + cursor.getString(cursor.getColumnIndex("TITLE"))); 
			}
			Log.e("app", "oh so maybe there's no results...");
			cursor.close();
		}
		
		
		
	}

	/**
	 * onStart method. Reads in coordinate files and stores data in fields
	 */
	protected void onStart() {
		super.onStart();

		
		
		
		

	}

	/**
	 * Handles a newIntent search from the actionBar
	 */
	protected void onNewIntent(Intent intent) {
		// Handles a search from the actioBar
		handleIntent(intent);
	}

	/**
	 * Helper method to handle the Intent for search
	 * 
	 * @param intent
	 *            the intent to search
	 */
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			for(String key: buildings.keySet()) {
				if(key.toLowerCase().contains(query.toLowerCase())) {
					// Clear out any other markers
					map.clear();
					// Add the new marker
					Marker marker = map.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromAsset("house_pin.png"))
					.title(key)
                    .snippet(buildings.get(key).toString())
					.position(buildings.get(key)));
					
					// Display the info window
					marker.showInfoWindow();
				}
			}
		}
	}

	/**
	 * Adds a parker to the given LatLng
	 * 
	 * @param latlng
	 *            the LatLng that contains the coordinates to place the marker
	 */
	private void addMarker(LatLng latlng) {
		map.addMarker(new MarkerOptions().icon(
				BitmapDescriptorFactory.fromAsset("lock_pin_svg.png"))
				.snippet(latlng.toString())
				.position(latlng));
	}

	/**
	 * Check to see if map is not null, set up if it is
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (map != null) {
				// The Map is verified. It is now safe to manipulate the map.

			}
		}
	}

	/**
	 * Helper method to request new places from the api
	 * 
	 * @param lat
	 *            requested focus latitude
	 * @param lng
	 *            requested focus longitude
	 */
	private void updatePlaces(double lat, double lng) {
		String placesSearch = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
				+ "json?location="
				+ lat
				+ ","
				+ lng
				+ "&radius=1000&sensor=true"
				+ "&types=food|bar|store|museum|art_gallery"
				+ "&AIzaSyA076Z_ATRir_DKoh2RnN9aaiMPjCzmmK4";
	}

	/**
	 * Shows bikes locks on the map by adding markers to the coordinates.
	 * Removes the bike locks from the screen if they are already being shown.
	 */
	private void showBikeLocks() {
		// Check if bike lock icons are currently being shown
		if (bikeLocksShown) {
			// Bike locks are already being show, clear the map
			map.clear();
			bikeLocksShown = false;
		} else {
			for (LatLng latlng : bikeLocks) {
				addMarker(latlng);
			}

			bikeLocksShown = true;
		}
	}

	/**
	 * Reads in the coordinates from a file and places them into a list that is
	 * returned.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private List<LatLng> readCordFile(String fileName) throws IOException {
		List<LatLng> results = new ArrayList<LatLng>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getAssets().open(
					fileName), "UTF-8"));

			// do reading, usually loop until end of file reading
			String mLine = reader.readLine();
			while (mLine != null) {
				// Split the strings on the space to grab each coordinate
				String split[] = mLine.split(", ");
				double lat = Double.parseDouble(split[0]);
				double lng = Double.parseDouble(split[1]);

				// Create the LatLng from our strings
				LatLng latlng = new LatLng(lat, lng);

				// Add the new LatLng to the list
				results.add(latlng);


				mLine = reader.readLine();
			}
		} catch (IOException e) {
			// log the exception
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// log the exception
				}
			}
		}

		return results;
	}

	/**
	 * Reads in the coordinates from a file and places them into a list that is
	 * returned.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private Map<String, LatLng> readMapFile(String fileName) throws IOException {
		Map<String, LatLng> results = new HashMap<String, LatLng>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getAssets().open(
					fileName), "UTF-8"));

			// do reading, usually loop until end of file reading
			String mLine = reader.readLine();
			while (mLine != null) {
				// Split the strings on the space to grab each coordinate
				String split[] = mLine.split(", ");
				String name = split[0];
				double lat = Double.parseDouble(split[1]);
				double lng = Double.parseDouble(split[2]);

				// Create the LatLng from our strings
				LatLng latlng = new LatLng(lat, lng);

				// Add the new LatLng to the list
				results.put(name, latlng);

				mLine = reader.readLine();
			}
		} catch (IOException e) {
			// log the exception
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// log the exception
				}
			}
		}

		return results;
	}

	private void loadHistory(String query) {
		
	}
	/**
	 * Inflate the ActionBar and set the search bar to be open by default
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		
		this.menu = menu;
		
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);

		
		return true;
	}

	/**
	 * Handle the event of clicking ActionBar menu items. Upon clicking on the
	 * bike lock, adds or removes bike lock markers according to their current
	 * state.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle actionBar clicks
		switch (item.getItemId()) {
		case R.id.bikes:
			showBikeLocks();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Requests the url for a call to the google maps api
	 * 
	 * @author UW_STUDENT_Virtual
	 * 
	 */
	private class GetPlaces extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... placesURL) {
			StringBuilder placesBuilder = new StringBuilder();

			// process search parameter string(s)
			for (String placeSearchURL : placesURL) {
				HttpClient placesClient = new DefaultHttpClient();

				try {
					HttpGet placesGet = new HttpGet(placeSearchURL);
					HttpResponse placesResponse = placesClient
							.execute(placesGet);
					StatusLine placeSearchStatus = placesResponse
							.getStatusLine();

					// Only continue if the we don't get an error
					if (placeSearchStatus.getStatusCode() == 200) {
						HttpEntity placesEntity = placesResponse.getEntity();
						InputStream placesContent = placesEntity.getContent();
						InputStreamReader placesInput = new InputStreamReader(
								placesContent);
						BufferedReader placesReader = new BufferedReader(
								placesInput);

						String lineIn;
						while ((lineIn = placesReader.readLine()) != null) {
							placesBuilder.append(lineIn);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return placesBuilder.toString();
		}

	}

}