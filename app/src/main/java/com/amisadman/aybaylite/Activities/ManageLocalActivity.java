package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Repo.BackupManager;
import com.amisadman.aybaylite.Repo.RestoreManager;
import com.amisadman.aybaylite.patterns.adapter.DateAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class ManageLocalActivity extends AppCompatActivity {

    private BackupManager backupManager;
    private RestoreManager restoreManager;
    private LinearLayout btnLocalBackup, btnLocalRestore;
    ImageButton btnBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_local);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manage_local_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnBack = findViewById(R.id.btnBack);
        backupManager = new BackupManager(this);
        btnLocalBackup = findViewById(R.id.btnLocalBackup);
        restoreManager = new RestoreManager(this);
        btnLocalRestore = findViewById(R.id.btnLocalRestore);
        LinearLayout btnCloudBackup = findViewById(R.id.btnCloudBackup);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnLocalBackup.setOnClickListener(v -> chooseBackupLocation());
        btnLocalRestore.setOnClickListener(v -> chooseRestoreFile());
        btnCloudBackup.setOnClickListener(v -> signInAndLaunchManager());

    }

    private void chooseBackupLocation() {
        DateAdapter dateAdapter = new DateAdapter();
        String time = dateAdapter.format(System.currentTimeMillis());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, time + ".json"); // Default filename

        backupLauncher.launch(intent);
    }

    // 🔹 Handle the file picker result
    private final ActivityResultLauncher<Intent> backupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        saveBackup(fileUri);
                    }
                }
            });

    // 🔹 Write backup JSON data to the file
    private void saveBackup(Uri uri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {

            String backupData = String.valueOf(backupManager.getBackupJson()); // Get JSON data
            writer.write(backupData);
            writer.flush();

            Toast.makeText(this, "Backup saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save backup!", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseRestoreFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");

        restoreLauncher.launch(intent);
    }

    // 🔹 Handle the file picker result
    private final ActivityResultLauncher<Intent> restoreLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        restoreBackup(fileUri);
                    }
                }
            });

    // Restore data from the selected JSON file
    private void restoreBackup(Uri uri) {
        if (restoreManager.restoreFromJson(uri)) {
            Toast.makeText(this, "Restore completed successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to restore backup!", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 Google Sign-In Logic
    private static final int REQUEST_CODE_SIGN_IN = 1001;

    private void signInAndLaunchManager() {
        GoogleSignInAccount account = GoogleSignIn
                .getLastSignedInAccount(this);
        if (account != null) {
            // Already signed in, proceed
            startActivity(new Intent(ManageLocalActivity.this, ManageCloudActivity.class));
        } else {
            // Not signed in, start sign-in flow
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(
                            DriveScopes.DRIVE_FILE))
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn
                    .getClient(this, gso);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task
                        .getResult(ApiException.class);
                // Sign-In Successful, proceed to ManageCloudActivity
                if (account != null) {
                    startActivity(new Intent(ManageLocalActivity.this, ManageCloudActivity.class));
                }
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Sign-In Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
