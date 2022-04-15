package in.ac.iitp.facedetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    String ipaddress = "";
    boolean isConfigSet = false;
    TextView tvmainheading;
    ProgressBar pbmain;
    boolean logged=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvmainheading=(TextView)findViewById(R.id.tvmainheading);
        pbmain=(ProgressBar) findViewById(R.id.pbmain);
        //SERVER URL
        SQLiteDatabase db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS userconfig (id TEXT PRIMARY KEY, userid TEXT, password TEXT, ipaddress TEXT);");
        Cursor c=db.rawQuery("SELECT userid,password,ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            String userid=c.getString(0);
            String password=c.getString(1);
            ipaddress = c.getString(2);
            isConfigSet = true;
            if(userid.equals("0") && password.equals("0")){
                //Not Logged in
            }else{
                logged=true;
                //MenuActivity();
                finish();
            }
        }

        if(isConfigSet){
            db.execSQL("UPDATE userconfig SET ipaddress='"+ipaddress+"' WHERE id='0'");
        }else {
            db.execSQL("INSERT INTO userconfig (id,userid,password,ipaddress)values('0','0','0','http://192.168.43.132:5000')");
        }

        //Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            // Permission has already been granted
        }
        //End

        //GET SCREEN SIZE
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        final int width = size. x;
        final int height = size. y;

        if(logged){
            MenuActivity();
        }else{
            //Login();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void LoginActivity(View view){
        Intent intent=new Intent(this,Login.class);
        startActivity(intent);
    }
    public void Login(){
        Intent intent=new Intent(this,Login.class);
        startActivity(intent);
    }
    public void MenuActivity(){
        Intent intent=new Intent(this,MenuActivity.class);
        startActivity(intent);
    }
}
