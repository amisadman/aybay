package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Repo.BackupManager;
import com.amisadman.aybaylite.Repo.RestoreManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class ManageCloudActivity extends AppCompatActivity {
    LinearLayout btnDriveBackup, btnDriveRestore;
    ImageButton btnBack;

    @SuppressLint("MissingInflatedId")
    private com.google.api.services.drive.Drive googleDriveService;
    private static final int REQUEST_CODE_PICK_BACKUP_FILE = 1002;
    private BackupManager backupManager;
    private RestoreManager restoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cloud);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manage_cloud_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        btnDriveBackup = findViewById(R.id.btnDriveBackup);
        btnDriveRestore = findViewById(R.id.btnDriveRestore);
        restoreManager = new RestoreManager(this);

        btnBack.setOnClickListener(v -> onBackPressed());

        // Initialize Drive Service
        initializeDriveService();

        // Upload Button: Open File Picker
        btnDriveBackup.setOnClickListener(v -> {
            if (googleDriveService != null) {
                chooseBackupFile();
            } else {
                Toast.makeText(this, "Drive Service not initialized. Please sign in again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Restore Button: Direct Download
        btnDriveRestore.setOnClickListener(v -> {
            if (googleDriveService != null) {
                downloadBackupFile();
            } else {
                Toast.makeText(this, "Drive Service not initialized. Please sign in again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeDriveService() {
        com.google.android.gms.auth.api.signin.GoogleSignInAccount account = com.google.android.gms.auth.api.signin.GoogleSignIn
                .getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, java.util.Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            try {
                googleDriveService = new com.google.api.services.drive.Drive.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        new GsonFactory(), credential)
                        .setApplicationName("Aybay")
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Not signed in. Please go back and sign in.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Upload Logic ---

    private void chooseBackupFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_CODE_PICK_BACKUP_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_BACKUP_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                uploadFileToDrive(uri);
            }
        }
    }

    private void uploadFileToDrive(Uri fileUri) {
        new Thread(() -> {
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to open file!", Toast.LENGTH_SHORT).show());
                    return;
                }

                File fileMetadata = new File();
                fileMetadata.setName("database_backup.json");

                InputStreamContent mediaContent = new InputStreamContent(
                        "application/json", inputStream);

                Log.d("Upload", "Starting upload process...");
                runOnUiThread(() -> Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show());

                File uploadedFile = googleDriveService.files()
                        .create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                Log.d("Upload", "File ID: " + uploadedFile.getId());
                runOnUiThread(() -> Toast.makeText(this, "Upload completed successfully!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show());
            }
        }).start();
    }

    // --- Download Logic ---

    private void downloadBackupFile() {
        new Thread(() -> {
            try {
                // Find file
                FileList result = googleDriveService.files().list()
                        .setQ("name='database_backup.json' and trashed=false")
                        .setFields("files(id, name)")
                        .execute();

                if (result.getFiles().isEmpty()) {
                    runOnUiThread(
                            () -> Toast.makeText(this, "Backup file not found in Drive!", Toast.LENGTH_SHORT).show());
                    return;
                }

                File backupFile = result.getFiles().get(0);
                String fileId = backupFile.getId();

                java.io.InputStream inputStream = googleDriveService.files().get(fileId).executeMediaAsInputStream();
                java.io.FileOutputStream outputStream = openFileOutput("database_backup.json", MODE_PRIVATE);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Download complete! Restoring...", Toast.LENGTH_SHORT).show();
                    restoreDatabase();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(
                        () -> Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void restoreDatabase() {
        try {
            restoreManager.restoreFromJson(Uri.fromFile(getFileStreamPath("database_backup.json")));
            runOnUiThread(() -> Toast.makeText(this, "Database restored successfully!", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        android.widget.TextView tvDriveBackupText = findViewById(R.id.tvDriveBackupText);
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            tvDriveBackupText.setText("Upload Backup");
        } else {
            tvDriveBackupText.setText("Sign in and Upload Backup");
        }
    }
}
