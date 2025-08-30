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

		public String getPrimaryPlaceName() {
			if (address.city != null) return address.city;
			if (address.town != null) return address.town;
			if (address.village != null) return address.village;
			if (address.municipality != null) return address.municipality;
			return null;
		}

		public String getRegionName() {
			if (address.county != null) return address.county;
			if (address.state != null) return address.state;
			return null;
		}

		public String getGeneralLocation() {
			String primary = getPrimaryPlaceName();
			String region = getRegionName();
			if (primary != null && region != null) {
				return primary + ", " + region;
			}
			return primary != null ? primary : region;
		}

		public static class Address {
			@SerializedName("city")
			public String city;

			@SerializedName("town")
			public String town;

			@SerializedName("village")
			public String village;

			@SerializedName("municipality")
			public String municipality;

			@SerializedName("county")
			public String county;

			@SerializedName("state")
			public String state;
		}
	}
}