package in.ac.iitp.facedetection;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ViewAttendance extends AppCompatActivity {

    String studentid;
    ListView listView;
    ArrayAdapter arrayAdapter;
    TextView tvtotalattended;
    ArrayList<String> arrayList=new ArrayList<>();
    SQLiteDatabase db;
    String ipaddress;
    Functions fn=new Functions();
    String id,sid,attended,attenddate;
    int totalattended=0;
    Button btnview;
    EditText edfromdate,edtodate;
    String fromdate,todate;
    final Calendar myCalendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        Bundle extra = getIntent().getExtras();
        studentid = extra.getString("studentid");

        //SERVER URL
        SQLiteDatabase db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=""+c.getString(0);
        }

        listView=(ListView)findViewById(R.id.listview_viewattendance);
        tvtotalattended=(TextView)findViewById(R.id.tv_viewattendance_totalattended);
        edfromdate=(EditText)findViewById(R.id.ed_viewattendance_fromdate);
        edtodate=(EditText)findViewById(R.id.ed_viewattendance_todate);
        btnview=(Button)findViewById(R.id.btn_viewattendance_view);

        //arrayList.add("Rehan");
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        listView.setAdapter(arrayAdapter);
        //Toast.makeText(this, ""+studentid, Toast.LENGTH_SHORT).show();
        getAttendance(studentid);


        final DatePickerDialog.OnDateSetListener date0 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updatefromdate();
            }

        };

        final DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updatetodate();
            }

        };

        edtodate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ViewAttendance.this, date1, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edfromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ViewAttendance.this, date0, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        btnview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fromdate=edfromdate.getText().toString();
                String todate=edtodate.getText().toString();
                getAttendanceWithDate(studentid,fromdate,todate);
            }
        });
    }
    public void updatetodate(){
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        edtodate.setText(sdf.format(myCalendar.getTime()));
    }
    public void updatefromdate(){
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        edfromdate.setText(sdf.format(myCalendar.getTime()));
    }

    public void getAttendance(final String studentid){
        new Thread(){
            public void run(){
                try {
                    final String jsonstring=fn.downloadUrl(ipaddress+"/appgetattendance?studentid="+ URLEncoder.encode(studentid,"UTF-8"));

                    JSONArray jarr=new JSONArray(jsonstring);
                    totalattended=jarr.length();
                    for(int i=0;i<jarr.length();i++){
                        JSONObject jobj=jarr.getJSONObject(i);
                        id=jobj.getString("id");
                        sid=jobj.getString("studentid");
                        attended=jobj.getString("attended");
                        attenddate=jobj.getString("attenddate");
                        arrayList.add(attenddate+"");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            arrayAdapter.notifyDataSetChanged();
                            tvtotalattended.setText("Total Attended : "+totalattended);
                        }
                    });
                }catch (final Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewAttendance.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();
    }

    public void getAttendanceWithDate(final String studentid,final String fromdate,final String todate){
        arrayList.clear();
        arrayAdapter.clear();
        new Thread(){
            public void run(){
                try {
                    final String jsonstring=fn.downloadUrl(ipaddress+"/appgetattendancewithdate?studentid="+ URLEncoder.encode(studentid,"UTF-8")+"&fromdate="+URLEncoder.encode(fromdate,"UTF-8")+"&todate="+URLEncoder.encode(todate,"UTF-8"));
                    JSONArray jarr=new JSONArray(jsonstring);
                    totalattended=jarr.length();
                    for(int i=0;i<jarr.length();i++){
                        JSONObject jobj=jarr.getJSONObject(i);
                        //jobj.getString("studentid");
                        id=jobj.getString("id");
                        sid=jobj.getString("studentid");
                        attended=jobj.getString("attended");
                        attenddate=jobj.getString("attenddate");
                        arrayList.add(attenddate+"");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            arrayAdapter.notifyDataSetChanged();
                            tvtotalattended.setText("Total Attended : "+totalattended);
                        }
                    });
                }catch (final Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewAttendance.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();
    }
}
