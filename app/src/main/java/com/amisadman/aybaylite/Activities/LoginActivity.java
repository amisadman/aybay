package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Controllers.AuthFacade;

public class LoginActivity extends AppCompatActivity {
    EditText login_password;
    Button login_button;
    TextView welcomeText;

    LinearLayout signupLayout;
    AuthFacade authFacade;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);
        welcomeText = findViewById(R.id.welcomeText);

        authFacade = new AuthFacade(this);

        String storedName = authFacade.getCurrentUser();
        if (storedName != null) {
            welcomeText.setText("Hello, " + storedName + "!");
        } else {
            welcomeText.setText("No user found! Please register.");
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        }

        login_button.setOnClickListener(v -> {
            String password = login_password.getText().toString();

            if (password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter PIN!", Toast.LENGTH_SHORT).show();
            } else {
                boolean valid = authFacade.login(password);
                if (valid) {
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to main app screen
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", storedName); // Name pass kore
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
