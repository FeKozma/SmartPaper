package com.fekozma.wallpaperchanger.api;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NominatimService {

	@GET("reverse")
	Call<NominatimResponse> reverseGeocode(
		@Query("lat") double lat,
		@Query("lon") double lon,
		@Query("format") String format,
		@Query("addressdetails") int addressDetails
	);

	public class NominatimResponse {
		@SerializedName("address")
		public Address address;

		public static class Address {
			@SerializedName("city")
			public String city;

			@SerializedName("town")
			public String town;

			@SerializedName("village")
			public String village;

			@SerializedName("county")
			public String county;
		}

		public String getCityName() {
			if (address.city != null) return address.city;
			if (address.town != null) return address.town;
			if (address.village != null) return address.village;
			return null;
		}

		public String getCountyName() {
			return address.county;
		}
	}
}