package in.ac.iitp.facedetection;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {

    Button btnlogout,btntrain;
    ProgressBar pbloading;
    SQLiteDatabase db;
    String ipaddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnlogout=(Button)findViewById(R.id.btnlogout);
        btntrain=(Button)findViewById(R.id.btntrain);
        pbloading=(ProgressBar)findViewById(R.id.pb_menu_train);

        //SERVER URL
        db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=""+c.getString(0);
        }

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.execSQL("UPDATE userconfig SET userid='0',password='0' WHERE id='0'");
                LoginActivity();
                finish();
            }
        });

        pbloading.setVisibility(View.GONE);
        btntrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MenuActivity.this, "Training Started...", Toast.LENGTH_SHORT).show();
                pbloading.setVisibility(View.VISIBLE);
                new Thread(){
                    public void run(){
                        try{
                            Functions fn=new Functions();
                            String response = fn.downloadUrl(ipaddress+"/appknntrain").trim();
                            if(response.equals("1")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MenuActivity.this, "Training Successful", Toast.LENGTH_SHORT).show();
                                        pbloading.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }catch (Exception e){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MenuActivity.this, "Training Failed", Toast.LENGTH_SHORT).show();
                                    pbloading.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }.start();
            }
        });
    }

    public void LoginActivity(){
        Intent intent=new Intent(this,Login.class);
        startActivity(intent);
    }
    public void SettingsActivity(View view){
        Intent intent=new Intent(this,Settings.class);
        startActivity(intent);
    }
    public void MainPage(View view){
        Intent intent=new Intent(this,MainPage.class);
        startActivity(intent);
    }
    public void TakeAttendanceActivity(View view){
        Intent intent=new Intent(this,TakeAttendance.class);
        startActivity(intent);
    }
    public void AddStudentActivity(View view){
        Intent intent=new Intent(this,AddStudent.class);
        startActivity(intent);
    }
    public void SearchActivity(View view){
        Intent intent=new Intent(this,Search.class);
        startActivity(intent);
    }
}
