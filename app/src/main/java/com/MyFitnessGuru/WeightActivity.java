package com.MyFitnessGuru;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeightActivity extends AppCompatActivity
{
    String TAG = "Weight Activity";

    //firebase- receiving values
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    String email, todaysWeightText;

    //Used for Line Graph
    List<DateObject> dateList = new ArrayList<DateObject>();
    LineGraphSeries<DataPoint>  mSeries1;
    DataPoint[] values = new DataPoint[7];

    //components
    TextView todaysWeight;
    GraphView graphV;
    Button saveWeight;


    //class used to define menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }
    //class used to display actions to take place once a menu option is selected.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
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
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        //method call to download the users weight
        downloadFromDatabase();

        //declarations for the line graph view
        graphV = findViewById(R.id.graph);
        graphV.getViewport().setXAxisBoundsManual(true);
        GridLabelRenderer gridLabel = graphV.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Last 7 Days:");
        gridLabel.setVerticalAxisTitle("Weight");
        gridLabel.setVerticalAxisTitleTextSize(60);
        gridLabel.setHorizontalAxisTitleTextSize(60);

        Intent in = getIntent();
        Bundle b = in.getExtras();
        //retrieving the values from the bundle
        email = EncodeString(b.getString("email"));

        todaysWeight = findViewById(R.id.todaysWeightTextView);

        //button used to save the users current weight
        saveWeight = findViewById(R.id.saveWeightButton);
        saveWeight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                todaysWeightText = todaysWeight.getText().toString();
                //gets the current date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(new Date());

                // Write a message to the database
                DatabaseReference myRef = database.getReference("Details/"+email+"/");
                myRef.child("Date").child(date).child("WeightCurrent").setValue(todaysWeightText);

                //used to reset both lists in order to not over populate it
                mSeries1.resetData(values);
                dateList.clear();
                //used to re-load activity
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
    }

    //method used to download the users weight
    private void downloadFromDatabase(){
        Intent in = getIntent();
        Bundle b = in.getExtras();
        //retrieving the values from the bundle
        email = EncodeString(b.getString("email"));
        // Read from the database
        DatabaseReference myRef = database.getReference("Details/" + email + "/Date");
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if (ds.child("WeightCurrent").exists())
                    {
                        String[] splitArray = ds.child("Date").getValue().toString().split("-");
                        String dateNum = splitArray[2];
                        dateList.add(new DateObject(dateNum, ds.child("WeightCurrent").getValue().toString(), ds.child("Steps").getValue().toString()));
                    }
                }
                int i = 0;
                //x is used for the list index
                int x = 0;
                //these values need to be a double in order to be added to the Graph
                double y, xx=0;

                if(dateList.size() >= 7){
                    int size =  dateList.size() - 7;
                    for (DateObject item:dateList)
                    {
                        if(i >= size && i <= dateList.size())
                        {
                            //the x-axis has a range from 0-7
                            graphV.getViewport().setMinX(0);
                            graphV.getViewport().setXAxisBoundsManual(true);
                            graphV.getViewport().setMaxX(7);
                            //the y-values is set to what the user has input over the last 7 days
                            y = Double.parseDouble(item.weightCurrent);
                            DataPoint v = new DataPoint(xx, y);
                            values[x] = v;
                            x++;
                            xx++;
                        }
                        i++;
                    }
                    mSeries1 = new LineGraphSeries<>(values);
                    graphV.addSeries(mSeries1);
                }
                else{
                    Toast.makeText(WeightActivity.this, "Come back after 7 days to view your progress", Toast.LENGTH_LONG).show();
                    graphV.setVisibility(View.GONE);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(WeightActivity.this, "Cant connect to Database", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static String EncodeString(String string){return  string.replace("." , "_");}
}
