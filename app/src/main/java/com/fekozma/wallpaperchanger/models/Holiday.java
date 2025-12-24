package com.fekozma.wallpaperchanger.models;

public class Holiday {
	private String id;
	private String name;
	private String startDate;  // Format: YYYY-MM-DD
	private String endDate;    // Format: YYYY-MM-DD
	private String type;       // "Public", "Bank", "School", etc.
	private boolean nationwide;

	public Holiday(String id, String name, String startDate, String endDate, String type, boolean nationwide) {
		this.id = id;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.type = type;
		this.nationwide = nationwide;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getType() {
		return type;
	}

	public boolean isNationwide() {
		return nationwide;
	}

	/**
	 * Get a tag-friendly identifier for this holiday
	 * Format: HOLIDAY_<name in uppercase with spaces replaced by underscores>
	 */
	public String getTagId() {
		return "HOLIDAY_" + name.toUpperCase()
			.replace(" ", "_")
			.replace("-", "_")
			.replaceAll("[^A-Z0-9_]", "");
	}
}
