package com.MyFitnessGuru;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StepsActivity extends AppCompatActivity
{
    TextView stepsTakenText, resultText;
    ProgressBar stepProgress;

    //firebase- receiving values
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Details");

    //declaration of global values
    int stepStart = 0, steps, stepGoal;
    String email = "";

    //class used to define menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }
    //class used to display actions to take place once a menu option is selected.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent act;
        int id = item.getItemId();
        switch (id){
            case R.id.Profile:
                act = new Intent(this, MyProfile.class);
                act.putExtra("email", email);
                startActivity(act);
                return true;
            case R.id.Steps:
                act = new Intent(this, StepsActivity.class);
                act.putExtra("email", email);
                startActivity(act);
                return  true;
            case  R.id.Weight :
                act = new Intent(this, WeightActivity.class);
                act.putExtra("email", email);
                startActivity(act);
                return  true;
            case  R.id.Gallery :
                act = new Intent(this, GalleryActivity.class);
                act.putExtra("email", email);
                startActivity(act);
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        //downloads the users steps takn from the database
        downloadFromDatabase();

        stepsTakenText = findViewById(R.id.stepsTakenTextView);
        stepProgress = findViewById(R.id.stepsProgressBar);
        resultText = findViewById(R.id.resultTextView);

    }
    public void downloadFromDatabase(){
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Intent in = getIntent();
                Bundle b = in.getExtras();
                //retrieving the values from the bundle
                email = EncodeString(b.getString("email"));
                List<UserObject> uo = new ArrayList<UserObject>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(new Date());


                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if(!ds.child("Date").child(date + "/").child("Steps").exists())
                        steps = 0;
                    else
                        steps = Integer.parseInt(ds.child("Date").child(date + "/").child("Steps").getValue().toString());
                    uo.add(new UserObject(ds.child("emailAddress").getValue().toString(),ds.child("age").getValue().toString(), Double.parseDouble(ds.child("height").getValue().toString()), Double.parseDouble(ds.child("weight").getValue().toString()), ds.child("gender").getValue().toString(),Double.parseDouble(ds.child("weightGoal").getValue().toString()), Integer.parseInt(ds.child("stepGoal").getValue().toString()), steps, ds.child("Setting").getValue().toString() ));

                }
                for (UserObject item:uo)
                {
                    if((item.emailAddress.equals(EncodeString(email))))
                    {
                        stepsTakenText.setText(String.valueOf(item.steps));
                        stepGoal = item.stepGoal;
                        steps = item.steps;
                        stepProgress.setMax(stepGoal);
                        stepProgress.setMin(stepStart);
                        stepProgress.setProgress(item.steps);
                    }
                }
                if(steps >= stepGoal)
                    resultText.setText("Congratulations, you have reached your goal");
                else
                        resultText.setText("You have " + (stepGoal - steps) + " steps to Go");
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StepsActivity.this, "Cant connect to Database", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static String EncodeString(String string){return  string.replace("." , "_");}
}
