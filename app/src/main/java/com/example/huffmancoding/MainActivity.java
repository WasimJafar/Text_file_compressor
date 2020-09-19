package com.example.huffmancoding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    Button btnSelect;
    Button write;
    TextView textView;
    EditText txtKey;

    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int READ_REQUEST_CODE = 42;
    private static String readFromFile = "";
    String TAG = "From MainActivity";
    boolean choice = true;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelect = findViewById(R.id.btnSelect);
        textView = findViewById(R.id.textView);
        write = findViewById(R.id.btnWrite);
        txtKey = findViewById(R.id.txtKey);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        //request permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , PERMISSION_REQUEST_STORAGE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_REQUEST_STORAGE);
        }

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtKey.getText().toString().length() == 0) {

                    Toast.makeText(MainActivity.this, "Please enter encryption key" , Toast.LENGTH_LONG).show();

                }else {
                    textView.setText("");
                    compressor();

                }
            }
        });
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtKey.getText().toString().length() == 0) {
                    Toast.makeText(MainActivity.this , "Please enter encryption key" , Toast.LENGTH_LONG).show();
                }else {
                    choice = false; // decoding

                    textView.setText("");
                    performFileSearch();
                }
            }
        });
    }

    private void compressor() {

        performFileSearch();
//        Log.d(TAG , "In Compressor  " + readFromFile);

    }

    private void encode(){

        Log.d(TAG , "In Encode : " + readFromFile);

        HuffmanManager huffmanManager = new HuffmanManager(readFromFile);

        huffmanManager.createFreqMap();
        huffmanManager.constructHuffmanTree();
        huffmanManager.generateCode(huffmanManager.root , "");

        String encoded = huffmanManager.encodeFinally();

        Log.d(TAG , "Encoded String :-> " + encoded);

        long newFileSize = encoded.length() / (8 * 1024);
        long oldFileSize = ((long)readFromFile.length() * (long)Math.ceil(Math.log(readFromFile.length()))) / (8 * 1024);
        float reduction = (float) ((oldFileSize - newFileSize) * 1.0 / oldFileSize) * 100 ;

        Log.d(TAG , reduction + "%");

        String s = "<<<--------RESULT-------->>>";
        String s1 = "Original file size = " + (int) oldFileSize + " KBs";
        String s2 = "Encoded  file size = "+ (int) newFileSize + " KBs";
        String s3 = "Reduction = " + reduction + " %";

        textView.setText("\n"+s + "\n\n" +s1 + "\n\n" + s2 + "\n\n" + s3);

        HashMap hashMap = new HashMap();

        for(String key : huffmanManager.codeForDecoding.keySet()){
            hashMap.put(key , huffmanManager.codeForDecoding.get(key) +"");
        }

        String user = txtKey.getText().toString();
        FirebaseDatabase.getInstance().getReference().child("Huffman").child(user).setValue(hashMap);

        saveTextAsFile("encodedFile" ,encoded);

    }


    private void decode() {
        Log.d(TAG , "Decode : ");
        Log.d(TAG , "Decode : " + readFromFile);

        recoverCodes();
        String s = "<<<--------RESULT-------->>>";
        String s1 = "File successfully decoded";

        textView.setText("\n"+s + "\n\n" +s1 );
    }

    private void recoverCodes(){

        String key = txtKey.getText().toString();

        databaseReference.child("Huffman").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot == null) {
                    Toast.makeText(MainActivity.this , "Please provide correct encryption key" , Toast.LENGTH_LONG).show();
                }

                else {
                    Log.d(TAG, dataSnapshot.getValue().toString());

                    HashMap<String, String> retrieveMap = new HashMap<>();

                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Log.d(TAG, dataSnapshot1.getKey() + "---" + dataSnapshot1.getValue().toString());
                        retrieveMap.put(dataSnapshot1.getKey(), dataSnapshot1.getValue().toString());
                    }

                    decodeFinally(retrieveMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this , "Error encountered !! Check the encryption key" , Toast.LENGTH_LONG).show();
            }
        });

    }


    public void decodeFinally(HashMap<String , String> map) {

        StringBuilder sb = new StringBuilder();
        String str = "";
        Log.d(TAG , readFromFile);
        String arr[] = readFromFile.split(":");

        readFromFile = arr[1];

        for (int i = 0 ;  i < readFromFile.length(); i++) {
            str += readFromFile.charAt(i);
//            Log.d(TAG , "t----------" + i + "   " + t);
            if (map.containsKey(str)) {
                sb.append(map.get(str));
                str = "";
            }
        }

        Log.d(TAG , sb.toString());
        saveTextAsFile("decoded" , sb.toString());

    }
    private String readText(String input) {

        File file = new File(input);
        StringBuilder text = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }

            br.close();

        }catch (Exception e) {
            e.printStackTrace();
        }

        return text.toString();
    }


    private String readTextForDecode(String input) {

        File file = new File(input);
        StringBuilder text = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            boolean isFirst = true;
            while((line = br.readLine()) != null) {
                text.append(line);
                if(isFirst) {
                    text.append(":");
                    isFirst = false;
                }else text.append("\n");
            }

            br.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private void performFileSearch(){

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent , READ_REQUEST_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = uri.getPath();
                Log.d(TAG , "File name1 :-->> " + path);

//                String arr[] = path.split("/");
//                fileNameForDB = arr[arr.length - 1].substring(0 , arr[arr.length - 1].indexOf("."));
                path = path.substring(path.indexOf(":") + 1);

//                makeText(this, path, Toast.LENGTH_LONG).show();
//                textView.setText(readText(path));
                Log.d(TAG , "read file :-->>" + readFromFile);
                Log.d(TAG , "File name :-->> " + path);

                if(choice) {
                    readFromFile = readText(path);
                    encode();
                } else {
                    readFromFile = readTextForDecode(path);
                    choice = true;
                    decode();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_STORAGE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeText(this , "Permission Granted" , Toast.LENGTH_LONG).show();
            }else{
                makeText(this , "Permission not granted" , Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



    // write

    private void saveTextAsFile(String filename , String content) {

        String fileName = filename + ".txt";
        //create file
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);

        //write
        try {
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(content.getBytes());
            fout.close();
            makeText(this, "Saved", Toast.LENGTH_LONG).show();
        }catch (Exception e) {
            e.printStackTrace();
            makeText(this , "File not found" , Toast.LENGTH_LONG).show();
        }

    }


}
