package in.ac.iitp.facedetection;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class ViewStudentInfo extends AppCompatActivity {
    Button btnupdateinfo,btnviewattendance,btnviewimages;
    TextView tvstudentid,tvname,tvrollno,tvsemester,tvcourse,tvsession;
    String studentid,name,rollno,semester,course,session;

    SQLiteDatabase db;
    String ipaddress;
    Functions fn=new Functions();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_student_info);

        Bundle extra = getIntent().getExtras();
        studentid = extra.getString("studentid");

        //SERVER URL
        SQLiteDatabase db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=""+c.getString(0);
        }

        tvstudentid=(TextView)findViewById(R.id.tv_viewinfo_studentid);
        tvname=(TextView)findViewById(R.id.tv_viewinfo_name);
        tvrollno=(TextView)findViewById(R.id.tv_viewinfo_rollno);
        tvsemester=(TextView)findViewById(R.id.tv_viewinfo_semester);
        tvcourse=(TextView)findViewById(R.id.tv_viewinfo_course);
        tvsession=(TextView)findViewById(R.id.tv_viewinfo_session);
        btnupdateinfo=(Button)findViewById(R.id.btn_viewinfo_updateinfo);
        btnviewattendance=(Button)findViewById(R.id.btn_viewinfo_viewattendance);
        btnviewimages=(Button)findViewById(R.id.btn_viewinfo_viewimages);
        tvstudentid.setText(studentid);

        new Thread(){
            public void run(){
                try {
                    final String jsonstring=fn.downloadUrl(ipaddress+"/appgetstudentinfo?q="+ URLEncoder.encode(studentid,"UTF-8"));
                    JSONArray jarr=new JSONArray(jsonstring);
                    for(int i=0;i<jarr.length();i++){
                        JSONObject jobj=jarr.getJSONObject(i);
                         name=jobj.getString("name");
                        rollno=jobj.getString("rollno");
                        semester=jobj.getString("semester");
                        course=jobj.getString("course");
                        session=jobj.getString("session");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvname.setText(name);
                            tvrollno.setText(rollno);
                            tvsemester.setText(semester);
                            tvcourse.setText(course);
                            tvsession.setText(session);
                            //new ImageDownloaderTask(ivstudentimage).execute(ipaddress+"/static/faces/"+studentid+".jpg");
                        }
                    });
                }catch (final Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewStudentInfo.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();

        btnupdateinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateinfo(studentid);
            }
        });

        btnviewattendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewattendance(studentid);
            }
        });

        btnviewimages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewimages(studentid);
            }
        });
    }
    public void updateinfo(String studentid){
        Intent intent=new Intent(this, UpdateStudent.class);
        intent.putExtra("studentid",""+studentid);
        startActivity(intent);
    }
    public void viewattendance(String studentid){
        Intent intent=new Intent(this, ViewAttendance.class);
        intent.putExtra("studentid",""+studentid);
        startActivity(intent);
    }
    public void viewimages(String studentid){
        Intent intent=new Intent(this, Images.class);
        intent.putExtra("studentid",""+studentid);
        startActivity(intent);
    }
}
