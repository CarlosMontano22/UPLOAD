package com.example.uploadfile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    Button selectfile,upload;
    TextView notification;
    Uri pdfUri;

    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private StorageReference tasksnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = FirebaseStorage.getInstance();//Retorna objeto desde firebase Storage
        database=FirebaseDatabase.getInstance();//Retorna objeto desde firebase Database

        selectfile=findViewById(R.id.selectfile);
        upload=findViewById(R.id.upload);
        notification=findViewById(R.id.notificar);


        selectfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    Selectpdf();
                }
                else
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri!=null)
                uploadfile(pdfUri);
                else
                    Toast.makeText(MainActivity.this,"selecione un archivo",Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void uploadfile(Uri pdfUri) {
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Carcagando archivo");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String fileName=System.currentTimeMillis()+"";
        StorageReference storageReference = storage.getReference();
        storageReference.child("Uploads").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                String Url=tasksnapshot.getDownloadUrl().toString();

                DatabaseReference reference =database.getReference();
                reference.child(fileName).setValue(Url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            Toast.makeText(MainActivity.this, "archivo cargado con éxito",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(MainActivity.this, "el archivo no se cargó correctamente",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this, "el archivo no se cargó correctamente",Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentProgress=(int)(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);

            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==9 && grantResults[0]==  PackageManager.PERMISSION_GRANTED)
        {
            Selectpdf();
        }
        else
            Toast.makeText(MainActivity.this,"Please provide permission..",Toast.LENGTH_SHORT).show();
    }

    private void Selectpdf() {
        // to offer user to select a file using file manager.
        // we will be using an Intent.
        // para ofrecer al usuario que seleccione un archivo usando el administrador de archivos.
        // usaremos una intención.

        Intent intent=new Intent();
        intent.setAction("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent ,86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // verifica si el usuario ha seleccionado un archivo o no (ej.": pdf)
        if (requestCode==86 && requestCode ==RESULT_OK && data!=null)
        {
            pdfUri=data.getData();
            notification.setText("A file is select :"+ data.getData().getLastPathSegment());
        }
        else{
            Toast.makeText(MainActivity.this,"Porfavor selecione un archivo",Toast.LENGTH_SHORT).show();
        }

    }
}
