package com.example.safetrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.Incident;
import com.example.safetrack.utils.SessionManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostIncidentActivity extends AppCompatActivity {

    // ✅ Fields declared at class level
    String editId = null;
    boolean isEditing = false;

    ImageView imgPreview;
    Button btnPickImage, btnGetLocation, btnSubmit;
    TextInputEditText etTitle, etDescription, etLocation;
    Spinner spinnerType;
    CheckBox cbAnonymous;
    SessionManager session;
    FusedLocationProviderClient locationClient;
    String imageBase64 = "";

    String[] incidentTypes = {
            "Safety Hazard", "Infrastructure",
            "Public Disturbance", "Fire", "Flood",
            "Crime", "Medical Emergency", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_incident);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        session        = new SessionManager(this);
        locationClient = LocationServices
                .getFusedLocationProviderClient(this);

        imgPreview     = findViewById(R.id.imgPreview);
        btnPickImage   = findViewById(R.id.btnPickImage);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSubmit      = findViewById(R.id.btnSubmit);
        etTitle        = findViewById(R.id.etTitle);
        etDescription  = findViewById(R.id.etDescription);
        etLocation     = findViewById(R.id.etLocation);
        spinnerType    = findViewById(R.id.spinnerType);
        cbAnonymous    = findViewById(R.id.cbAnonymous);

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        incidentTypes);
        typeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // ✅ Check if editing — INSIDE onCreate AFTER views setup
        Intent intent = getIntent();
        if (intent.hasExtra("edit_id")) {
            editId    = intent.getStringExtra("edit_id");
            isEditing = true;

            etTitle.setText(
                    intent.getStringExtra("edit_title"));
            etDescription.setText(
                    intent.getStringExtra("edit_description"));
            etLocation.setText(
                    intent.getStringExtra("edit_location"));
            btnSubmit.setText("Update Report");

            // Set spinner to correct type
            String editType =
                    intent.getStringExtra("edit_type");
            if (editType != null) {
                for (int i = 0; i < incidentTypes.length; i++) {
                    if (incidentTypes[i].equals(editType)) {
                        spinnerType.setSelection(i);
                        break;
                    }
                }
            }
        }

        btnPickImage.setOnClickListener(v ->
                ImagePicker.with(this)
                        .crop()
                        .compress(512)
                        .maxResultSize(1080, 1080)
                        .start());

        btnGetLocation.setOnClickListener(v -> getLocation());
        btnSubmit.setOnClickListener(v -> submitIncident());
    }

    @Override
    protected void onActivityResult(int req, int res,
                                    @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            imgPreview.setImageURI(uri);
            try {
                Bitmap bmp = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), uri);
                ByteArrayOutputStream baos =
                        new ByteArrayOutputStream();
                bmp.compress(
                        Bitmap.CompressFormat.JPEG, 70, baos);
                imageBase64 = Base64.encodeToString(
                        baos.toByteArray(), Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 100);
            return;
        }

        Toast.makeText(this,
                "Getting location...",
                Toast.LENGTH_SHORT).show();

        locationClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        getAddressFromOSM(
                                loc.getLatitude(),
                                loc.getLongitude());
                    } else {
                        Toast.makeText(
                                PostIncidentActivity.this,
                                "GPS not ready! Turn ON GPS " +
                                        "and try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getAddressFromOSM(double lat, double lon) {
        String url = "https://nominatim.openstreetmap.org/reverse"
                + "?format=json"
                + "&lat=" + lat
                + "&lon=" + lon
                + "&zoom=18"
                + "&addressdetails=1";

        okhttp3.OkHttpClient client =
                new okhttp3.OkHttpClient();

        okhttp3.Request request =
                new okhttp3.Request.Builder()
                        .url(url)
                        .header("User-Agent", "SafeTrackApp/1.0")
                        .build();

        client.newCall(request).enqueue(
                new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call,
                                          IOException e) {
                        runOnUiThread(() -> {
                            etLocation.setText(
                                    lat + ", " + lon);
                            Toast.makeText(
                                    PostIncidentActivity.this,
                                    "Could not get address",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(
                            okhttp3.Call call,
                            okhttp3.Response response)
                            throws IOException {
                        if (response.isSuccessful()) {
                            String body =
                                    response.body().string();
                            try {
                                org.json.JSONObject json =
                                        new org.json.JSONObject(
                                                body);
                                org.json.JSONObject address =
                                        json.getJSONObject(
                                                "address");

                                StringBuilder locationName =
                                        new StringBuilder();

                                // Barangay level
                                if (address.has("village")) {
                                    locationName.append(
                                                    address.getString(
                                                            "village"))
                                            .append(", ");
                                } else if (address.has(
                                        "suburb")) {
                                    locationName.append(
                                                    address.getString(
                                                            "suburb"))
                                            .append(", ");
                                } else if (address.has(
                                        "quarter")) {
                                    locationName.append(
                                                    address.getString(
                                                            "quarter"))
                                            .append(", ");
                                } else if (address.has(
                                        "neighbourhood")) {
                                    locationName.append(
                                                    address.getString(
                                                            "neighbourhood"))
                                            .append(", ");
                                }

                                // Municipality / City
                                if (address.has(
                                        "municipality")) {
                                    locationName.append(
                                                    address.getString(
                                                            "municipality"))
                                            .append(", ");
                                } else if (address.has(
                                        "city")) {
                                    locationName.append(
                                                    address.getString(
                                                            "city"))
                                            .append(", ");
                                } else if (address.has(
                                        "town")) {
                                    locationName.append(
                                                    address.getString(
                                                            "town"))
                                            .append(", ");
                                }

                                // Province
                                if (address.has("province")) {
                                    locationName.append(
                                            address.getString(
                                                    "province"));
                                } else if (address.has(
                                        "state")) {
                                    locationName.append(
                                            address.getString(
                                                    "state"));
                                }

                                String result = locationName
                                        .toString().trim();

                                // Remove trailing comma
                                if (result.endsWith(",")) {
                                    result = result.substring(
                                                    0, result.length() - 1)
                                            .trim();
                                }

                                final String finalResult =
                                        result.isEmpty()
                                                ? lat + ", " + lon
                                                : result;

                                runOnUiThread(() ->
                                        etLocation.setText(
                                                finalResult));

                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() ->
                                        etLocation.setText(
                                                lat + ", " + lon));
                            }
                        }
                    }
                });
    }

    private void submitIncident() {
        String title =
                etTitle.getText().toString().trim();
        String type =
                spinnerType.getSelectedItem().toString();
        String description =
                etDescription.getText().toString().trim();
        String location =
                etLocation.getText().toString().trim();
        boolean anonymous = cbAnonymous.isChecked();

        if (title.isEmpty() || description.isEmpty()
                || location.isEmpty()) {
            Toast.makeText(this,
                    "Fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Saving...");

        if (isEditing && editId != null) {
            // UPDATE existing report
            Incident update = new Incident();
            update.title       = title;
            update.type        = type;
            update.description = description;
            update.location    = location;
            update.isAnonymous = anonymous;

            RetrofitClient.getService()
                    .updateIncident("eq." + editId, update)
                    .enqueue(new Callback<List<Incident>>() {
                        @Override
                        public void onResponse(
                                Call<List<Incident>> call,
                                Response<List<Incident>> response) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Update Report");
                            if (response.isSuccessful()) {
                                Toast.makeText(
                                        PostIncidentActivity.this,
                                        "Report updated! ✅",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(
                                        PostIncidentActivity.this,
                                        "Failed to update",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(
                                Call<List<Incident>> call,
                                Throwable t) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Update Report");
                            Toast.makeText(
                                    PostIncidentActivity.this,
                                    "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // INSERT new report
            Incident incident = new Incident(
                    title, type, description, location,
                    imageBase64, session.getUserId(),
                    session.getUsername(), anonymous);

            RetrofitClient.getService()
                    .insertIncident(incident)
                    .enqueue(new Callback<List<Incident>>() {
                        @Override
                        public void onResponse(
                                Call<List<Incident>> call,
                                Response<List<Incident>> response) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Submit Report");
                            if (response.isSuccessful()) {
                                Toast.makeText(
                                        PostIncidentActivity.this,
                                        "Incident reported! ✅",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(
                                        PostIncidentActivity.this,
                                        "Failed to submit",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(
                                Call<List<Incident>> call,
                                Throwable t) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Submit Report");
                            Toast.makeText(
                                    PostIncidentActivity.this,
                                    "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}