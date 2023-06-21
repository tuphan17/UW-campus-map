package com.brandonthepvongsa.uwmaps;

import android.content.SearchRecentSuggestionsProvider;

public class CustomSuggestionsProvider extends SearchRecentSuggestionsProvider{
	public final static String AUTHORITY = "com.brandonthepvongsa.CustomSuggestionsProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;
	
	public CustomSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
