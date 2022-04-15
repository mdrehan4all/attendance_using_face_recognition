package in.ac.iitp.facedetection;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    EditText edipaddress;
    Button btnsave;
    SQLiteDatabase db;
    boolean isConfigSet=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        edipaddress=(EditText)findViewById(R.id.edsettings_ip);
        btnsave=(Button)findViewById(R.id.btnsettings_save);

        db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS userconfig (id TEXT PRIMARY KEY, userid TEXT, password TEXT, ipaddress TEXT);");

        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            String ipaddress=c.getString(0);
            edipaddress.setText(ipaddress);
            isConfigSet=true;
        }

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipaddress=edipaddress.getText().toString();
                if(isConfigSet){
                    db.execSQL("UPDATE userconfig SET ipaddress='"+ipaddress+"' WHERE id='0'");
                }else {
                    db.execSQL("INSERT INTO userconfig (id,userid,password,ipaddress)values('0','0','0','http://192.168.43.132:5000')");
                }
                Toast.makeText(Settings.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
