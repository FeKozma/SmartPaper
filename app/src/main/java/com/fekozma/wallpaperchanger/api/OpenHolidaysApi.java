package com.fekozma.wallpaperchanger.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenHolidaysApi {
	@GET("PublicHolidays")
	Call<List<HolidayResponse>> getPublicHolidays(
		@Query("countryIsoCode") String countryIsoCode,
		@Query("languageIsoCode") String languageIsoCode,
		@Query("validFrom") String validFrom,  // Format: YYYY-MM-DD
		@Query("validTo") String validTo       // Format: YYYY-MM-DD
	);

	class HolidayResponse {
		@SerializedName("id")
		public String id;
		
		@SerializedName("startDate")
		public String startDate;  // Format: YYYY-MM-DD
		
		@SerializedName("endDate")
		public String endDate;    // Format: YYYY-MM-DD
		
		@SerializedName("type")
		public String type;       // "Public", "Bank", "School", "Authorities", "Optional", "Observance"
		
		@SerializedName("name")
		public List<LocalizedName> name;
		
		@SerializedName("nationwide")
		public boolean nationwide;
		
		public static class LocalizedName {
			@SerializedName("language")
			public String language;
			
			@SerializedName("text")
			public String text;
		}
		
		/**
		 * Get the holiday name in the specified language, or fallback to first available
		 */
		public String getLocalizedName(String languageCode) {
			if (name == null || name.isEmpty()) {
				return "Holiday";
			}
			
			// Try to find exact language match
			for (LocalizedName ln : name) {
				if (ln.language.equalsIgnoreCase(languageCode)) {
					return ln.text;
				}
			}
			
			// Fallback to first available name
			return name.get(0).text;
		}
	}
}
