package com.MyFitnessGuru;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//activity used to register a new user with an email address and a password

public class RegisterActivity extends AppCompatActivity
{
    Button back, register;
    TextView emailText, passwordText;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailText = findViewById(R.id.emailAddressTextView);
        passwordText = findViewById(R.id.passwordTextView);

        back = findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        //button used to register a new user
        //pushes the users email and password to the database
        register = findViewById(R.id.registerButton);
        register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String passwordString, emailString;

                emailString = emailText.getText().toString();
                passwordString = passwordText.getText().toString();

                UserObject uo = new UserObject(emailString, passwordString);

                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users");


                myRef.child(EncodeString(emailString)).setValue(uo);

                Toast.makeText(RegisterActivity.this, "Registered", Toast.LENGTH_SHORT).show();

                intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }
    public static String EncodeString(String string){return  string.replace("." , "_");}
}
