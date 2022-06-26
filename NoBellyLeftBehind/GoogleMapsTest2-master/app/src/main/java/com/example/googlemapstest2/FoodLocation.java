package com.example.googlemapstest2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FoodLocation {
    private int id;
    private String address;
    private String agency;
    private String dateUpdated;
    private String dayHours;
    private String distribution;
    private String foodResourceType;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String operationalNotes;
    private String operationalStatus;
    private String phoneNumber;
    private String website;
    private String whoTheyServe;

    private boolean[] daysOpen;

    private Context context;

    public FoodLocation(int id,
                        String Address,
                        String Agency,
                        String DateUpdated,
                        String DayHours,
                        String Distribution,
                        String FoodResourceType,
                        double Latitude,
                        double Longitude,
                        String LocationName,
                        String OperationalNotes,
                        String OperationalStatus,
                        String PhoneNumber,
                        String Website,
                        String WhoTheyServe,
                        Context context) {
        this.id = id;
        this.address = Address;
        this.agency = Agency;
        this.dateUpdated = DateUpdated;
        this.dayHours = DayHours;
        this.distribution = Distribution;
        this.foodResourceType = FoodResourceType;
        this.latitude = Latitude;
        this.longitude = Longitude;
        this.locationName = LocationName;
        this.operationalNotes = OperationalNotes;
        this.operationalStatus = OperationalStatus;
        this.phoneNumber = PhoneNumber;
        this.website = Website;
        this.whoTheyServe = WhoTheyServe;
        this.context = context;
    }

    public FoodLocation(Object o, Context context) {
        DataSnapshot entry = (DataSnapshot) o;
        this.id = Integer.parseInt(entry.getKey());
        Map<String, Object> details = (Map<String, Object>) entry.getValue();
        this.address = details.get("Address") == null ? "" : (String) details.get("Address");
        this.agency = details.get("Agency") == null ? "" : (String) details.get("Agency");
        this.dateUpdated = details.get("DateUpdated") == null ? "" : (String) details.get("DateUpdates");
        this.dayHours = details.get("DaysHours") == null ? "" : (String) details.get("DaysHours");
        this.distribution = details.get("Distribution") == null ? "" : (String) details.get("Distribution");
        this.latitude = details.get("Latitude").toString().isEmpty() ? null : (Double) details.get("Latitude");
        this.longitude = details.get("Longitude").toString().isEmpty() ? null : (Double) details.get("Longitude");
        this.locationName = details.get("Location") == null ? "" : (String) details.get("Location");
        this.operationalNotes = details.get("OperationalNotes") == null ? "" : (String) details.get("OperationalNotes");
        this.operationalStatus = details.get("OperationalStatus") == null ? "" : (String) details.get("OperationalNotes");
        this.phoneNumber = details.get("PhoneNumber") == null ? "" : (String) details.get("PhoneNumber");
        this.website = details.get("Website") == null ? "" : (String) details.get("Website");
        this.whoTheyServe = details.get("WhoTheyServe") == null ? "" : (String) details.get("WhoTheyServe");
        this.context = context;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getAgency() {
        return agency;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public String getDayHours() {
        return dayHours;
    }

    public String getDistribution() {
        return distribution;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getFoodResourceType() {
        return foodResourceType;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getOperationalNotes() {
        return operationalNotes;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public String getWhoTheyServe() {

        return whoTheyServe;
    }

    public String getTitle() {
        if (!locationName.isEmpty() && !agency.isEmpty()) {
            return locationName + " (" + agency + ")";
        } else if (!agency.isEmpty()) {
            return agency;
        } else if (!locationName.isEmpty()) {
            return locationName;
        } else {
            return "";
        }
    }

    public LatLng getLatLng() {
        if (latitude == null || longitude == null) return getLocationFromAddress(address);
        return new LatLng(latitude, longitude);
    }

    private LatLng getLocationFromAddress(String address) {
        Geocoder geocoder = new Geocoder(context);
        List<android.location.Address> list;
        try {
            list = geocoder.getFromLocationName(address, 1);
            Address current = list.get(0);
            return new LatLng(current.getLatitude(), current.getLongitude());
        } catch (Exception e){
            System.out.println(e);
            return new LatLng(200, 200);
        }
    }

    public boolean[] getDaysOpen() {
        if (daysOpen != null) {
            return Arrays.copyOf(daysOpen, daysOpen.length);
        }
        /// this will do logic
        daysOpen = new boolean[7];
        Arrays.fill(daysOpen, Boolean.FALSE);
        if (dayHours.contains("Mon")) {
            daysOpen[0] = true;
        }
        if (dayHours.contains("Tues")) {
            daysOpen[1] = true;
        }
        if (dayHours.contains("Wed")) {
            daysOpen[2] = true;
        }
        if (dayHours.contains("Thurs")) {
            daysOpen[3] = true;
        }
        if (dayHours.contains("Fri")) {
            daysOpen[4] = true;
        }
        if (dayHours.contains("Sat")) {
            daysOpen[5] = true;
        }
        if (dayHours.contains("Sun")) {
            daysOpen[6] = true;
        }
        return daysOpen;
    }


}
