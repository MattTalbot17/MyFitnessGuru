package com.MyFitnessGuru;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//class used for the user to upload images and to view their uploaded images

public class GalleryActivity extends AppCompatActivity
{
    //declarations of the components used on the layout
    FloatingActionButton addNewPictureButton;
    Button saveBtn;
    ImageView imgPreview;
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    //firebase storage
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri filePath;
    private static final int PICK_IMAGE_REQUEST = 71;

    //firebase database- receiving values
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef ;

    String email;
    Intent intent;
    List<Upload> mUploads = new ArrayList<Upload>();

    //Method used to define menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    //Method used to display actions to take place once a menu option is selected.
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
    @SuppressLint("WrongConstant")
    @Override

    //method for when the layout loads
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //declarations for values to be assigned to the components by an ID
        mRecyclerView = findViewById(R.id.recyclerView);
        imgPreview = findViewById(R.id.imagePreview);

        //Button for choosing a new image. This opens up all the images from the devices files
        addNewPictureButton = findViewById(R.id.addNewImageFAB);
        addNewPictureButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        //button used to save the image to firebase storage and database
        saveBtn = findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uploadImage();
            }
        });

        //method call to download images
        downloadImages();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    //this method is invoked once a user selects an image from their files,
    //the image is loaded into the preview image view
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

                imgPreview.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    //method to upload an image to firebase storage and realtime database
    private void uploadImage() {
        Intent in = getIntent();
        final Bundle b = in.getExtras();
        //retrieving the values from the bundle
        email = b.getString("email");
        if(filePath != null)
        {
            final String url =  UUID.randomUUID().toString();
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            final String finalFilePath = url +"." + getFileExtension(filePath);

            final StorageReference ref = storageReference.child("Gallery/" + email+ "/" + finalFilePath);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            //putting the file into storage
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    // adds file to realtime database as well
                                    DatabaseReference myRef = database.getReference("Details/"+EncodeString(b.getString("email")) + "/");
                                    myRef.child("Gallery").push().setValue(uri.toString());
                                }
                            });
                            progressDialog.dismiss();
                            Toast.makeText(GalleryActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GalleryActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    //method to download images from firebase Storage using Realtime database link
    private void downloadImages(){
        mUploads.clear();
        Intent in = getIntent();
        Bundle b = in.getExtras();
        //retrieving the values from the bundle
        email = EncodeString2(b.getString("email"));

        myRef = FirebaseDatabase.getInstance().getReference("Details/"+EncodeString(b.getString("email"))+"/Gallery");

        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Uri downloadURL;
                //runs through the references and adds each image to the image adapter where it is displayed into the recycler view
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    downloadURL = Uri.parse(postSnapshot.getValue().toString());
                    mUploads.add(new Upload(downloadURL));
                }
                mAdapter = new ImageAdapter(GalleryActivity.this, mUploads);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(GalleryActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    //method used to convert image into an image type.
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public static String EncodeString2(String string){return  string.replace("%40" , "@");}
    public static String EncodeString(String string){return  string.replace("." , "_");}
}
