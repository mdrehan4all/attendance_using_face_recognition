package in.ac.iitp.facedetection;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity {

    Spinner spinner;
    EditText edq;
    Button btnsearch;
    String q,with="name";
    ListView listView;
    CustomAdapter customAdapter;
    ArrayList<String> id=new ArrayList<>();
    ArrayList<String> name=new ArrayList<>();
    ArrayList<String> icon=new ArrayList<>();
    SQLiteDatabase db;
    String ipaddress;
    Functions fn=new Functions();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        spinner=(Spinner)findViewById(R.id.spinner_search);
        listView=(ListView)findViewById(R.id.listview_search);
        edq=(EditText)findViewById(R.id.ed_search);
        btnsearch=(Button)findViewById(R.id.btn_search);

        //SERVER URL
        SQLiteDatabase db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=""+c.getString(0);
        }

        final ArrayList<String> list=new ArrayList<>();
        list.add("Student ID");
        list.add("Name");
        list.add("Roll no.");
        list.add("Semester");
        list.add("Course");
        list.add("Session");
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,list);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(Search.this, ""+list.get(i), Toast.LENGTH_SHORT).show();
                if(i==0){
                    with = "studentid";
                }else if(i==1){
                    with = "name";
                }else if(i==2){
                    with = "rollno";
                }else if(i==3){
                    with = "semester";
                }else if(i==4){
                    with = "course";
                }else if(i==5){
                    with = "session";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //id.add("1");
        //name.add("Rehan");
        //icon.add(ipaddress+"static/faces/1.jpg");

        customAdapter=new CustomAdapter(this,id,name,icon);
        listView.setAdapter(customAdapter);

        btnsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                q = edq.getText().toString();
                find(q,with);
                //Toast.makeText(Search.this, "q="+q+" and w="+with, Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                //Toast.makeText(Search.this, ""+i, Toast.LENGTH_SHORT).show();
                viewinfo(id.get(i));
                /*
                PopupMenu popupMenu=new PopupMenu(Search.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.searchaction,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.item_viewdetails){
                            viewinfo(id.get(i));
                        }else if(menuItem.getItemId() == R.id.item_editdetails){
                            updateinfo(id.get(i));
                        }else if(menuItem.getItemId() == R.id.item_viewattendance){
                            viewattendance(id.get(i));
                        }else if(menuItem.getItemId() == R.id.item_images){
                            images(id.get(i));
                        }
                        return false;
                    }
                });
                popupMenu.show();
                */
            }
        });
    }

    private void find(final String q, final String with){
        customAdapter.clear();
        id.clear();
        name.clear();
        icon.clear();
        new Thread(){
            public void run(){
                try {
                    final String jsonstring=fn.downloadUrl(ipaddress+"/appsearchstudent?q="+ URLEncoder.encode(q,"UTF-8")+"&with="+with);
                    JSONArray jarr=new JSONArray(jsonstring);
                    for(int i=0;i<jarr.length();i++){
                        JSONObject jobj=jarr.getJSONObject(i);
                        id.add(jobj.getString("studentid"));
                        name.add(jobj.getString("name"));
                        icon.add(ipaddress+"/static/faces/"+jobj.getString("studentid")+".jpg");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            customAdapter.notifyDataSetChanged();
                        }
                    });
                }catch (final Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(Search.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            q = edq.getText().toString();
            find(q,with);
        }catch (Exception e){

        }
    }
    public void updateinfo(String studentid){
        Intent intent=new Intent(this, UpdateStudent.class);
        intent.putExtra("studentid",""+studentid);
        startActivity(intent);
    }
    public void viewinfo(String studentid){
        Intent intent=new Intent(this, ViewStudentInfo.class);
        intent.putExtra("studentid",""+studentid);
        startActivity(intent);
    }
    public void viewattendance(String studentid){
        Intent intent=new Intent(this, ViewAttendance.class);
        intent.putExtra("studentid",""+studentid);
        startActivity(intent);
    }
    public void images(String studentid){
        Intent intent=new Intent(this, Images.class);
        intent.putExtra("studentid",""+studentid);
        startActivity(intent);
    }
}
