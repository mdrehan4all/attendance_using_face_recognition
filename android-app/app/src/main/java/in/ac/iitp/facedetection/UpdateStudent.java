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

public class UpdateStudent extends AppCompatActivity {

    Button btnupdatedetails,btndelete;
    EditText edstudentid, edname,edrollno,edsemester,edcourse,edsession;
    ProgressBar pbloading;
    SQLiteDatabase db;
    String ipaddress;
    Functions fn=new Functions();
    String studentid,name,rollno,semester,course,session;
    String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_student);

        Bundle extra = getIntent().getExtras();
        studentid = extra.getString("studentid");

        edstudentid=(EditText)findViewById(R.id.edusstudentid);
        edname=(EditText)findViewById(R.id.edusname);
        edrollno=(EditText)findViewById(R.id.edusrollno);
        edsemester=(EditText)findViewById(R.id.edussemester);
        edcourse=(EditText)findViewById(R.id.eduscourse);
        edsession=(EditText)findViewById(R.id.edussession);
        btndelete=(Button)findViewById(R.id.btnusdelete);
        btnupdatedetails=(Button)findViewById(R.id.btnusupdatedetails);
        pbloading=(ProgressBar)findViewById(R.id.pbusloading);

        pbloading.setVisibility(View.INVISIBLE);

        edstudentid.setKeyListener(null);
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
                        btnupdatedetails.setEnabled(false);
                        btnupdatedetails.setText("Invalid Session");
                    } else if (s.length() < 9){
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnupdatedetails.setEnabled(false);
                        btnupdatedetails.setText("Invalid Session");
                    }else if (s.charAt(0) == '-' || s.charAt(1) == '-' || s.charAt(2) == '-' || s.charAt(3) == '-' || s.charAt(5) == '-' || s.charAt(6) == '-' || s.charAt(7) == '-' || s.charAt(8) == '-') {
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnupdatedetails.setEnabled(false);
                        btnupdatedetails.setText("Invalid Session");
                    }else if(Integer.parseInt(s.substring(0,4)) >= Integer.parseInt(s.substring(5,9))){
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnupdatedetails.setEnabled(false);
                        btnupdatedetails.setText("Invalid Session");
                    }else{
                        btnupdatedetails.setEnabled(true);
                        btnupdatedetails.setText("Update Student");
                    }
                    /*
                    if (s.charAt(4) != '-') {
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnupdatedetails.setEnabled(false);
                        btnupdatedetails.setText("Invalid Session");
                    }else{
                        btnupdatedetails.setEnabled(true);
                        btnupdatedetails.setText("Update");
                    }

                    if (s.charAt(0) == '-' || s.charAt(1) == '-' || s.charAt(2) == '-' || s.charAt(3) == '-' || s.charAt(5) == '-' || s.charAt(6) == '-' || s.charAt(7) == '-' || s.charAt(8) == '-') {
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnupdatedetails.setEnabled(false);
                        btnupdatedetails.setText("Invalid Session");
                    }else {
                        btnupdatedetails.setEnabled(true);
                        btnupdatedetails.setText("Update");
                    }
                    if (s.length() < 9) {
                        edsession.setError("Enter Valid Session YYYY-YYYY");
                        btnupdatedetails.setEnabled(false);
                        btnupdatedetails.setText("Invalid Session");
                    }else{
                        if(Integer.parseInt(s.substring(0,4)) >= Integer.parseInt(s.substring(5,9))){
                            edsession.setError("Enter Valid Session YYYY-YYYY");
                            btnupdatedetails.setEnabled(false);
                            btnupdatedetails.setText("Invalid Session");
                        }else{
                            btnupdatedetails.setEnabled(true);
                            btnupdatedetails.setText("Update");
                        }
                    }
                    */
                }catch (Exception e){
                    edsession.setError("Enter Valid Session YYYY-YYYY");
                    btnupdatedetails.setEnabled(false);
                    btnupdatedetails.setText("Invalid Session");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //SERVER URL
        db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS userconfig (id TEXT PRIMARY KEY, userid TEXT, password TEXT, ipaddress TEXT);");
        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=c.getString(0);
        }
        //SERVER URL END

        btnupdatedetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                studentid = edstudentid.getText().toString();
                name = edname.getText().toString();
                rollno = edrollno.getText().toString();
                semester = edsemester.getText().toString();
                course =  edcourse.getText().toString();
                session = edsession.getText().toString();

                if(name.length()==0 || rollno.length()==0 || semester.length()==0 || course.length()==0 || session.length()==0){
                    Toast.makeText(UpdateStudent.this, "Fill all fields before adding", Toast.LENGTH_SHORT).show();
                }else {
                    pbloading.setVisibility(View.VISIBLE);
                    new Thread() {
                        public void run() {
                            try {
                                studentid = URLEncoder.encode(studentid, "UTF-8");
                                name = URLEncoder.encode(name, "UTF-8");
                                rollno = URLEncoder.encode(rollno, "UTF-8");
                                semester = URLEncoder.encode(semester, "UTF-8");
                                course = URLEncoder.encode(course, "UTF-8");
                                session = URLEncoder.encode(session, "UTF-8");
                                final String response = fn.ePost(ipaddress + "/appupdatestudentdetails", "studentid=" + studentid + "&name=" + name + "&rollno=" + rollno + "&semester=" + semester + "&course=" + course + "&session=" + session);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Toast.makeText(UpdateStudent.this, ""+response, Toast.LENGTH_SHORT).show();
                                        try {
                                            pbloading.setVisibility(View.INVISIBLE);
                                            String jsonstring = response;

                                            JSONArray jarr = new JSONArray(jsonstring);
                                            for (int i = 0; i < jarr.length(); i++) {
                                                JSONObject jobj = jarr.getJSONObject(i);
                                                String success = jobj.getString("success");
                                                String error = jobj.getString("error");
                                                if (success.equals("0")) {
                                                    Toast.makeText(UpdateStudent.this, "Not Updated, Error : " + error, Toast.LENGTH_SHORT).show();
                                                } else if (success.equals("1")) {
                                                    Toast.makeText(UpdateStudent.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } catch (Exception e) {

                                        }
                                    }
                                });
                            } catch (Exception e) {

                            } finally {
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

        new Thread(){
            public void run(){
                try {
                    final String jsonstring=fn.downloadUrl(ipaddress+"/appgetstudentinfo?q="+ URLEncoder.encode(studentid,"UTF-8"));

                    JSONArray jarr=new JSONArray(jsonstring);
                    for(int i=0;i<jarr.length();i++){
                        JSONObject jobj=jarr.getJSONObject(i);

                        //jobj.getString("studentid");
                        name=jobj.getString("name");
                        rollno=jobj.getString("rollno");
                        semester=jobj.getString("semester");
                        course=jobj.getString("course");
                        session=jobj.getString("session");
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edstudentid.setText(studentid);
                            edname.setText(name);
                            edrollno.setText(rollno);
                            edsemester.setText(semester);
                            edcourse.setText(course);
                            edsession.setText(session);
                            //new ImageDownloaderTask(ivpreview).execute(ipaddress+"/static/faces/"+studentid+".jpg");
                        }
                    });
                }catch (final Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UpdateStudent.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();

        //Delete User
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UpdateStudent.this, "Long Press to Delete", Toast.LENGTH_SHORT).show();
            }
        });
        btndelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(){
                    public void run(){
                        try {
                            final String response=fn.downloadUrl(ipaddress+"/appdeletestudent?studentid="+studentid).trim();
                            if(response.equals("1")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UpdateStudent.this, "Deleted", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                            }
                        }catch (Exception e){}
                    }
                }.start();
                return false;
            }
        });
    }
}
