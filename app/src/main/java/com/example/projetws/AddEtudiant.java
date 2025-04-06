package com.example.projetws;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.projetws.classes.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddEtudiant extends AppCompatActivity {

    private static final String TAG = "AddEtudiant";
    private static final int PICK_IMAGE_REQUEST = 2;

    private EditText nom;
    private EditText prenom;
    private Spinner ville;
    private RadioButton m;
    private RadioButton f;
    private Button add;
    private ImageView photoImageView;
    private Button choosePhotoButton;
    private TextView dateNaissanceTextView;
    private Button selectDateButton;

    private String encodedImage = null;
    private String selectedDate = null;

    RequestQueue requestQueue;
    String insertUrl = "http://192.168.1.9/projet/ws/createEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);

        try {
            // Initialize views
            nom = findViewById(R.id.nomEditText);
            prenom = findViewById(R.id.prenomEditText);
            ville = findViewById(R.id.villeSpinner);
            add = findViewById(R.id.ajouterButton);
            m = findViewById(R.id.hommeRadioButton);
            f = findViewById(R.id.femmeRadioButton);
            photoImageView = findViewById(R.id.photoImageView);
            choosePhotoButton = findViewById(R.id.choosePhotoButton); // Choisir une photo depuis la galerie
            dateNaissanceTextView = findViewById(R.id.dateNaissanceTextView);
            selectDateButton = findViewById(R.id.selectDateButton);

            // Setup click listeners
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateInputs()) {
                        addEtudiant();
                    }
                }
            });

            choosePhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGallery();
                }
            });

            selectDateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog();
                }
            });

            // Setup ville spinner
            setupVilleSpinner();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupVilleSpinner() {
        // List of Moroccan cities
        String[] villes = new String[]{
                "Marrakech", "Rabat", "Casablanca",  "Agadir", "Fès", "Tanger"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, villes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ville.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        // Show date picker dialog
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddEtudiant.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        selectedDate = sdf.format(calendar.getTime());
                        SimpleDateFormat displaySdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                        dateNaissanceTextView.setText(displaySdf.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImageUri = data.getData();
                photoImageView.setImageURI(selectedImageUri);

                String imagePath = getPathFromURI(selectedImageUri);
                Bitmap bitmap = decodeImageFromPath(imagePath);
                encodedImage = encodeImage(bitmap);
            }
        }
    }

    private String getPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private Bitmap decodeImageFromPath(String path) {
        return BitmapFactory.decodeFile(path);
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private boolean validateInputs() {
        String nomText = nom.getText().toString().trim();
        String prenomText = prenom.getText().toString().trim();

        if (nomText.isEmpty()) {
            nom.setError("Veuillez entrer un nom");
            return false;
        }

        if (prenomText.isEmpty()) {
            prenom.setError("Veuillez entrer un prénom");
            return false;
        }

        if (!m.isChecked() && !f.isChecked()) {
            Toast.makeText(this, "Veuillez sélectionner un sexe", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addEtudiant() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST,
                insertUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    Type type = new TypeToken<Collection<Etudiant>>(){}.getType();
                    Collection<Etudiant> etudiants = new Gson().fromJson(response, type);
                    for(Etudiant e : etudiants){
                        Log.d(TAG, e.toString());
                    }

                    // Clear form after successful submission
                    nom.setText("");
                    prenom.setText("");
                    m.setChecked(false);
                    f.setChecked(false);
                    ville.setSelection(0);
                    photoImageView.setImageResource(R.drawable.ic_person_placeholder);
                    encodedImage = null;
                    dateNaissanceTextView.setText("Sélectionner une date");
                    selectedDate = null;

                    Toast.makeText(AddEtudiant.this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    Toast.makeText(AddEtudiant.this, "Erreur lors de l'ajout de l'étudiant", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(AddEtudiant.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nom", nom.getText().toString().trim());
                params.put("prenom", prenom.getText().toString().trim());
                params.put("ville", ville.getSelectedItem().toString());
                params.put("sexe", m.isChecked() ? "M" : "F");
                params.put("date_naissance", selectedDate);

                // Add photo only if it is selected
                if (encodedImage != null) {
                    params.put("photo", encodedImage);
                }

                return params;
            }
        };

        requestQueue.add(request);
    }
}