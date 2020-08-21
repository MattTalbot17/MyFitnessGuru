package com.MyFitnessGuru;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyProfile extends AppCompatActivity implements SensorEventListener, StepListener
{
    //declarations for Step counter
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private int numSteps;

    //declarations for Layout components
    TextView genderText, ageText, heightText, weightText, stepCounterText, weightGoalText, stepGoalText, weightLabelText, weightGoalLabelText, heightLabelText;
    Button saveBtn, addImageBtn;
    ImageView profileImage;
    Switch simpleSwitch;

    //declarations to be used globally
    String email = "", gender, age, height;
    int stepGoal;
    double  weightGoal, weight;
    boolean test = false;
    //conversions
    double toPounds = 2.20462, toKgs = 0.45359, toInches = 0.39370, toCm = 2.54;

    //Firebase storage
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference myRef;

    //firebase- receiving values
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Uri filePath;
    private static final int PICK_IMAGE_REQUEST = 71;
    Intent intent;

    //method used to define menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    //method used to display actions to take place once a menu option is selected.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.Profile:
                intent = new Intent(this, MyProfile.class);
                intent.putExtra("email", email);
                startActivity(intent);
                return true;
            case R.id.Steps:
                intent = new Intent(this, StepsActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                return  true;
            case  R.id.Weight :
                intent = new Intent(this, WeightActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                return  true;
            case  R.id.Gallery :
                intent = new Intent(this, GalleryActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        //code to do with Setting Profile image
        //declaration of Image view and Add Image Button
        profileImage = findViewById(R.id.profileImage);
        addImageBtn = findViewById(R.id.addImageButton);
        addImageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chooseImage();
            }
        });

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        sensorManager.registerListener(MyProfile.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

        //declaration of text views
        genderText = findViewById(R.id.genderTextView);
        ageText = findViewById(R.id.ageTextView);
        heightText = findViewById(R.id.heightTextView);
        weightText = findViewById(R.id.weightTextView);
        stepCounterText = findViewById(R.id.stepCounterTextView);
        stepGoalText = findViewById(R.id.stepGoalTextView);
        weightGoalText = findViewById(R.id.weightGoalTextView);

        //when the step counter sensor adds a new step, the step counter text views text is changed
        //when this text views text changed, this value is pushed to the realtime database
        stepCounterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s){
                numSteps = Integer.parseInt(stepCounterText.getText().toString());
                Intent in = getIntent();
                Bundle b = in.getExtras();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(new Date());

                myRef = database.getReference("Details" );
                //pushes data to firebase database with the reference of the users email address

                if(test==true)
                {
                    // Write a message to the database
                    myRef = database.getReference("Details/"+EncodeString(b.getString("email")) + "/");

                    //pushes data to firebase database with the reference of the users email address
                    myRef.child("Date").child(date).child("Steps").setValue(numSteps);
                    myRef.child("Date").child(date).child("Date").setValue(date);
                }

            }
        });

        //Save Button Click to save all the users details to the database
        saveBtn = findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(ageText.getText().toString().isEmpty() || heightText.getText().toString().isEmpty() || weightText.getText().toString().isEmpty() || genderText.getText().toString().isEmpty() || stepGoalText.getText().toString().isEmpty() || weightGoalText.getText().toString().isEmpty())
                {
                    Toast.makeText(MyProfile.this, "Please enter in all fields", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(genderText.getText().toString().equalsIgnoreCase("male") || genderText.getText().toString().equalsIgnoreCase("female"))
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String date = sdf.format(new Date());

                        Intent in = getIntent();
                        Bundle b = in.getExtras();

                        //retrieving the values from the bundle
                        email = EncodeString(b.getString("email"));

                        //setting the string variables to the text views text
                        age = ageText.getText().toString();
                        height = heightText.getText().toString();
                        weight = Double.parseDouble(weightText.getText().toString());
                        gender = genderText.getText().toString();
                        weightGoal= Double.parseDouble(weightGoalText.getText().toString());
                        stepGoal = Integer.parseInt(stepGoalText.getText().toString());

                        // Write a message to the database
                        myRef = database.getReference("Details" );

                        //pushes data to firebase database with the reference of the users email address
                        myRef.child(email).child("emailAddress").setValue(email);
                        myRef.child(email).child("age").setValue(age);
                        myRef.child(email).child("height").setValue(height);
                        myRef.child(email).child("weight").setValue(weight);
                        myRef.child(email).child("gender").setValue(gender);
                        myRef.child(email).child("weightGoal").setValue(weightGoal);
                        myRef.child(email).child("stepGoal").setValue(stepGoal);
                        if(simpleSwitch.isChecked())
                            myRef.child(email).child("Setting").setValue("Metric");
                        else
                            myRef.child(email).child("Setting").setValue("Imperial");
                        if(numSteps == 0){
                            myRef.child(email).child("Date").child(date).child("Steps").setValue(numSteps);
                            myRef.child(email).child("Date").child(date).child("Date").setValue(date);
                        }
                        uploadImage();
                        Toast.makeText(MyProfile.this, "Updates Made", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(MyProfile.this, "Please input a valid Gender", Toast.LENGTH_SHORT).show();
                }
                //code to reload the activity with new values
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
        //code dealing with the "Switch"
        simpleSwitch = findViewById(R.id.metricImperialSwitch);
        weightLabelText = findViewById(R.id.weightLabelTxt);
        weightGoalLabelText = findViewById(R.id.weightGoalLabletxt);
        heightLabelText = findViewById(R.id.heightLabelText);
        simpleSwitch.setOnClickListener(new View.OnClickListener()
        {
            //switch button to change between the two systems
            @Override
            public void onClick(View v)
            {
                Intent in = getIntent();
                Bundle b = in.getExtras();
                //retrieving the values from the bundle
                email = EncodeString(b.getString("email"));
                // Write a message to the database
                myRef = database.getReference("Details" );

                //if the switch is checked it changes all the labels and values to match the Metric system
                if (simpleSwitch.isChecked())
                {
                    weightLabelText.setText("Weight(Kg)");
                    weightGoalLabelText.setText("Weight(Kg)");
                    heightLabelText.setText("Height(cm)");
                    if (!weightText.getText().toString().isEmpty() && !heightText.getText().toString().isEmpty() && !weightGoalText.getText().toString().isEmpty())
                    {
                        //changing the values to the Metric system
                        weightText.setText(String.valueOf(Math.round(Double.parseDouble(weightText.getText().toString()) * toKgs)));
                        weightGoalText.setText(String.valueOf(Math.round(Double.parseDouble(weightGoalText.getText().toString()) * toKgs)));
                        heightText.setText(String.valueOf(Math.round(Double.parseDouble(heightText.getText().toString()) * toCm)));
                        //pushes data to firebase database with the reference of the users email address
                        myRef.child(email).child("weight").setValue(weightText.getText().toString());
                        myRef.child(email).child("height").setValue(heightText.getText().toString());
                        myRef.child(email).child("weightGoal").setValue(weightGoalText.getText().toString());
                        myRef.child(email).child("Setting").setValue("Metric");
                    }
                }
                //if the switch is switched off it changes all the labels and values to match the Imperial system
                else
                {
                    weightLabelText.setText("Weight(lbs)");
                    weightGoalLabelText.setText("Weight(lbs)");
                    heightLabelText.setText("Height(Inches)");
                    if (!weightText.getText().toString().isEmpty() && !heightText.getText().toString().isEmpty() && !weightGoalText.getText().toString().isEmpty())
                    {
                        //changing the values to the Imperial system
                        weightText.setText(String.valueOf(Math.round(Double.parseDouble(weightText.getText().toString()) * toPounds)));
                        weightGoalText.setText(String.valueOf(Math.round(Double.parseDouble(weightGoalText.getText().toString()) *  toPounds)));
                        heightText.setText(String.valueOf(Math.round(Double.parseDouble(heightText.getText().toString()) * toInches)));
                        //pushes data to firebase database with the reference of the users email address
                        myRef.child(email).child("weight").setValue(weightText.getText().toString());
                        myRef.child(email).child("height").setValue(heightText.getText().toString());
                        myRef.child(email).child("weightGoal").setValue(weightGoalText.getText().toString());
                        myRef.child(email).child("Setting").setValue("Imperial");
                    }
                }
            }
        });

        //method calls to download data
        downloadFromStorage();
        downloadFromDatabase();

    }
    //Methods for Step COunter
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }
    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }
    @Override
    public void step(long timeNs){
        numSteps++;
        stepCounterText.setText(String.valueOf(numSteps));
    }

    //method to download information from database
    public void downloadFromDatabase(){
        Intent in = getIntent();
        Bundle b = in.getExtras();

        //retrieving the values from the bundle
        email = EncodeString(b.getString("email"));

        //getting todays date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String date = sdf.format(new Date());

        // Read from the database
        DatabaseReference myRef = database.getReference("Details");
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                List<UserObject> uo = new ArrayList<UserObject>();
                //this loops through all the users in the database and checks if they have saved details or not.
                //if they have details, they will be displayed in the various text views
                //if they have no detials an empty activity will be displayed for them to ful out
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if(ds.child("emailAddress").getValue().toString().equals(EncodeString(email)))
                    {
                        test = true;
                        if (!ds.child("Date").child(date).child("Steps").exists())
                            numSteps = 0;
                        else
                            numSteps = Integer.parseInt(ds.child("Date").child(date).child("Steps").getValue().toString());
                        //populates the object with the users details
                        uo.add(new UserObject(ds.child("emailAddress").getValue().toString(), ds.child("age").getValue().toString() ,Double.parseDouble(ds.child("height").getValue().toString()) ,Double.parseDouble(ds.child("weight").getValue().toString()) ,ds.child("gender").getValue().toString() ,Double.parseDouble(ds.child("weightGoal").getValue().toString()) , Integer.parseInt(ds.child("stepGoal").getValue().toString()), numSteps,ds.child("Setting").getValue().toString()) );
                    }
                }
                //used to display the details of the user
                for (UserObject item:uo)
                {
                    genderText.setText(item.gender);
                    weightText.setText(String.valueOf(item.weight));
                    heightText.setText(String.valueOf(item.height));
                    ageText.setText(String.valueOf(item.age));
                    stepCounterText.setText((String.valueOf(item.steps)));
                    stepGoalText.setText(String.valueOf(item.stepGoal));
                    weightGoalText.setText(String.valueOf(item.weightGoal));
                    if (item.setting.equals("Metric"))
                        simpleSwitch.setChecked(true);
                    else
                        simpleSwitch.setChecked(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyProfile.this, "Cant connect to Database", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //method to download the image from the databasee
    public void downloadFromStorage() {
        Intent in = getIntent();
        Bundle b = in.getExtras();
        //retrieving the values from the bundle
        email = EncodeString2(b.getString("email"));

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        final StorageReference Ref = storageReference.child("images/" + email);

        final long ONE_MEGABYTE = 1024 * 1024;

        Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL).into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profileImage.setImageResource(R.drawable.editimage);
            }
        });
    }
    //method to choose an image
    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = null;
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                profileImage.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    //method to upload an image to firebase
    private void uploadImage() {

        Intent in = getIntent();
        Bundle b = in.getExtras();
        //retrieving the values from the bundle
        email = b.getString("email").toString();
        if(filePath != null)
        {
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + email);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MyProfile.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MyProfile.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
    //methods to convert email address in order to upload and download the image
    public static String EncodeString(String string){return  string.replace("." , "_");}
    public static String EncodeString2(String string){return  string.replace("%40" , "@");}
}


