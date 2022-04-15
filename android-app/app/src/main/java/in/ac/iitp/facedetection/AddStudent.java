package in.ac.iitp.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class AddStudent extends AppCompatActivity {

    Button btnadd;
    EditText edname, edrollno, edsemester, edcourse, edsession;
    ProgressBar pbloading;
    SQLiteDatabase db;
    String ipaddress, responsemsg;
    private static int RESULT_LOAD_IMAGE = 1;
    Functions fn = new Functions();
    String name, rollno, semester, course, session;
    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        btnadd = (Button) findViewById(R.id.btnasadd);
        edname = (EditText) findViewById(R.id.edasname);
        edrollno = (EditText) findViewById(R.id.edasroll);
        edsemester = (EditText) findViewById(R.id.edassemester);
        edcourse = (EditText) findViewById(R.id.edascourse);
        edsession = (EditText) findViewById(R.id.edassession);
        pbloading = (ProgressBar) findViewById(R.id.pbasloading);

        pbloading.setVisibility(View.INVISIBLE);

        //Session validation
        edsession.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = charSequence.toString();
                try {
                    if (s.length() >= 5 && s.charAt(4) != '-') {
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnadd.setEnabled(false);
                        btnadd.setText("Invalid Session");
                    } else if (s.length() < 9){
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnadd.setEnabled(false);
                        btnadd.setText("Invalid Session");
                    }else if (s.charAt(0) == '-' || s.charAt(1) == '-' || s.charAt(2) == '-' || s.charAt(3) == '-' || s.charAt(5) == '-' || s.charAt(6) == '-' || s.charAt(7) == '-' || s.charAt(8) == '-') {
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnadd.setEnabled(false);
                        btnadd.setText("Invalid Session");
                    }else if(Integer.parseInt(s.substring(0,4)) >= Integer.parseInt(s.substring(5,9))){
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnadd.setEnabled(false);
                        btnadd.setText("Invalid Session");
                    }else{
                        btnadd.setEnabled(true);
                        btnadd.setText("Add");
                    }
                        /*
                        if (s.charAt(4) != '-') {
                            edsession.setError("Enter Valid Session YYYY-YYYY");
                            btnadd.setEnabled(false);
                            btnadd.setText("Invalid Session");
                        }else{
                            btnadd.setEnabled(true);
                            btnadd.setText("Add");
                        }

                        if (s.charAt(0) == '-' || s.charAt(1) == '-' || s.charAt(2) == '-' || s.charAt(3) == '-' || s.charAt(5) == '-' || s.charAt(6) == '-' || s.charAt(7) == '-' || s.charAt(8) == '-') {
                            edsession.setError("Enter Valid Session YYYY-YYYY");
                            btnadd.setEnabled(false);
                            btnadd.setText("Invalid Session");
                        }else {
                            btnadd.setEnabled(true);
                            btnadd.setText("Add");
                        }
                    if (s.length() < 9) {
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnadd.setEnabled(false);
                        btnadd.setText("Invalid Session");
                    }else{
                        //btnadd.setEnabled(true);
                        //btnadd.setText("Add");
                        if(Integer.parseInt(s.substring(0,4)) >= Integer.parseInt(s.substring(5,9))){
                            Toast.makeText(AddStudent.this, Integer.parseInt(s.substring(0,4))+"-"+Integer.parseInt(s.substring(5,9)), Toast.LENGTH_SHORT).show();
                            edsession.setError("Enter Valid Session YYYY-YYYY");
                            btnadd.setEnabled(false);
                            btnadd.setText("Invalid Session");
                        }else{
                            btnadd.setEnabled(true);
                            btnadd.setText("Add");
                        }
                    }
                    */
                }catch (Exception e){
                    edsession.setError("Enter Valid Session YYYY-YYYY");
                    btnadd.setEnabled(false);
                    btnadd.setText("Invalid Session");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //SERVER URL
        db = openOrCreateDatabase("db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS userconfig (id TEXT PRIMARY KEY, userid TEXT, password TEXT, ipaddress TEXT);");
        Cursor c = db.rawQuery("SELECT ipaddress FROM userconfig", null);
        while (c.moveToNext()) {
            ipaddress = c.getString(0);
        }
        //SERVER URL END

        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = edname.getText().toString();
                rollno = edrollno.getText().toString();
                semester = edsemester.getText().toString();
                course = edcourse.getText().toString();
                session = edsession.getText().toString();

                if(name.length()==0 || rollno.length()==0 || semester.length()==0 || course.length()==0 || session.length()==0){
                    Toast.makeText(AddStudent.this, "Fill all fields before adding", Toast.LENGTH_SHORT).show();
                }else {
                    pbloading.setVisibility(View.VISIBLE);
                    new Thread() {
                        public void run() {
                            try {
                                name = URLEncoder.encode(name, "UTF-8");
                                rollno = URLEncoder.encode(rollno, "UTF-8");
                                semester = URLEncoder.encode(semester, "UTF-8");
                                course = URLEncoder.encode(course, "UTF-8");
                                session = URLEncoder.encode(session, "UTF-8");
                                String jsonstring = fn.downloadUrl(ipaddress + "/appaddstudent?name=" + name + "&rollno=" + rollno + "&semester=" + semester + "&course=" + course + "&session=" + session).trim();
                                JSONArray jarr = new JSONArray(jsonstring);
                                for (int i = 0; i < jarr.length(); i++) {
                                    JSONObject jobj = jarr.getJSONObject(i);
                                    String success = jobj.getString("success");
                                    String error = jobj.getString("error");
                                    if (success.equals("0")) {
                                        printToast("Not Updated, Error : " + error);
                                    } else if (success.equals("1")) {
                                        printToast("Successfully Updated");

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pbloading.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                    }
                                }
                            } catch (Exception e) {
                                printToast(e.toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pbloading.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }
                    }.start();
                }
            }
        });
    }

    public void printToast(final String m) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AddStudent.this, "" + m, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String working = s.toString();
            boolean isValid = true;
            if (working.length() == 4 && before == 0) {
                if (Integer.parseInt(working) < 1 || Integer.parseInt(working) > 12) {
                    isValid = false;
                } else {
                    working = working + "-";
                    //edsession.setText(working);
                    //edsession.setSelection(working.length());
                }
            } else if (working.length() != 9) {
                isValid = false;
            }

            if (!isValid) {
                edsession.setError("Enter a valid session: YYYY-YYYY");
            } else {
                edsession.setError(null);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
    };
}
