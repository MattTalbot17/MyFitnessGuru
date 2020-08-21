package com.MyFitnessGuru;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//login activity

public class MainActivity extends AppCompatActivity
{
    Button register, logIn;
    TextView emailText, passwordText;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailText = findViewById(R.id.emailAddressTextView);
        passwordText = findViewById(R.id.passwordTextView);

        register = findViewById(R.id.registerButton);
        logIn = findViewById(R.id.logInButton);

        //login button that compares input to values in database
        //if successful the user is taken to their profile page
        logIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                authenticate();
            }
        });
        register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                intent= new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //method used to check if the users email and password match a user saved in the database
    public void authenticate(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Boolean check = false;
                List<UserObject> uo = new ArrayList<UserObject>();

                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    uo.add(new UserObject(ds.child("emailAddress").getValue().toString(), ds.child("password").getValue().toString()));
                }
                for (UserObject item:uo)
                {
                    if(emailText.getText().toString().trim().equals(item.emailAddress) && passwordText.getText().toString().trim().equals(item.password))
                    {
                        intent= new Intent(MainActivity.this, MyProfile.class);
                        intent.putExtra("email", emailText.getText().toString().trim());
                        startActivity(intent);
                        check = true;
                    }
                }
                if (check == false)

                {
                    Toast.makeText(MainActivity.this, "No Match", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Cant connect to Database", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
