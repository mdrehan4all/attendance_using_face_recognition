package in.ac.iitp.facedetection;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    Button btnlogin;
    EditText edusername,edpassword;
    ProgressBar pblogin;
    Functions f=new Functions();
    String username,password,jsonstring;
    String ipaddress = "http://192.168.43.117:5000";
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnlogin=(Button)findViewById(R.id.btnlogin);
        edusername=(EditText)findViewById(R.id.edusername);
        edpassword=(EditText)findViewById(R.id.edpassword);
        pblogin=(ProgressBar)findViewById(R.id.pblogin);

        pblogin.setVisibility(View.INVISIBLE);

        //SERVER URL
        db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS userconfig (id TEXT PRIMARY KEY, userid TEXT, password TEXT, ipaddress TEXT);");
        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=c.getString(0);
        }

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username=edusername.getText().toString();
                password=edpassword.getText().toString();
                if(username.length()==0 && password.length()==0){
                    printtoast("Username and Password can't be left empty");
                }else{
                    pblogin.setVisibility(View.VISIBLE);
                    new Thread(){
                        public void run(){
                            try{
                                //ipaddress = 192.168.4.132
                                jsonstring=f.ePost(ipaddress+"/apploginsubmit","username="+username+"&password="+password);
                                JSONArray jarr=new JSONArray(jsonstring);
                                for(int i=0;i<jarr.length();i++){
                                    JSONObject jobj=jarr.getJSONObject(i);

                                    String loginstatus=jobj.getString("loginstatus");

                                    if(loginstatus.equals("1")){
                                        printtoast("Logged");
                                        MenuActivity();
                                        db.execSQL("UPDATE userconfig SET userid='"+username+"',password='"+password+"' WHERE id='0'");
                                        finish();
                                    }else if(loginstatus.equals("2")){
                                        printtoast("Incurrect Password");
                                    }else{
                                        printtoast("Something went wrong or User not exists");
                                    }
                                }
                            }catch (final Exception e){
                                printtoast(e.toString());
                            }
                        }
                    }.start();
                }
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        try {
            Cursor c = db.rawQuery("SELECT ipaddress FROM userconfig", null);
            while (c.moveToNext()) {
                ipaddress = c.getString(0);
            }
        }catch (Exception e){

        }
    }

    public void printtoast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Login.this, ""+msg, Toast.LENGTH_SHORT).show();
                pblogin.setVisibility(View.INVISIBLE);
            }
        });
    }
    public void SettingsActivity(View view){
        Intent intent=new Intent(this,Settings.class);
        startActivity(intent);
    }
    public void MenuActivity(){
        Intent intent=new Intent(this,MenuActivity.class);
        startActivity(intent);
    }
    public void SkipToMenu(View view){
        Intent intent=new Intent(this,MenuActivity.class);
        startActivity(intent);
    }
}
