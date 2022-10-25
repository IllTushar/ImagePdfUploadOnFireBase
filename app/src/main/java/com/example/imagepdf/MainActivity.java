package com.example.imagepdf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.imagepdf.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
Button save;
EditText name,phone;
ImageView imageView;
FirebaseStorage storage;
StorageReference storageReference;
Uri filePath;
private int REQUEST_CODE=22;
FirebaseDatabase database;
DatabaseReference reference;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.photo);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        save = findViewById(R.id.save);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

       imageView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
              chooseImage();
           }
       });
       save.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               uploadImage();

           }
       });
    }

    private void uploadDetails(String uri)
    {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Details");
        String stringUri = uri.toString();
        String Name = name.getText().toString();
        String Phone = phone.getText().toString();
        UserModel userModel = new UserModel(Name,Phone,stringUri);
        reference.child(Name).setValue(userModel);
    }

    private void uploadImage()
    {
         if (filePath!=null)
         {
           final ProgressDialog progressDialog = new ProgressDialog(this);
           progressDialog.setMessage("Please wait..");
           progressDialog.show();
           StorageReference ref =storageReference.child("images/"+ UUID.randomUUID().toString());
           try {
               ref.putFile(filePath)
                       .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                               ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                       progressDialog.dismiss();
                                       String fileuri =uri.toString();
                                       uploadDetails(fileuri);
                                       Toast.makeText(MainActivity.this, "Upload Images", Toast.LENGTH_SHORT).show();
                                   }
                               });
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               progressDialog.dismiss();
                               Log.d("##Check",e.getMessage());
                               Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                           }
                       }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                               double progress = (100.0*snapshot.getBytesTransferred()/snapshot
                                       .getTotalByteCount());
                               progressDialog.setMessage("Uploaded "+(int)progress+"%");
                           }

                       });
           }catch (Exception e){
               Log.d("##Check1",e.getMessage());
           }

         }
    }



    private void chooseImage()
    {
      Intent i = new Intent();
     // i.setType("image/*");
        i.setType("application/pdf");
      i.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(i,"Select Picture"),REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE && resultCode ==RESULT_OK
        && data!=null && data.getData()!=null)
        {
          filePath = data.getData();
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
             imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}