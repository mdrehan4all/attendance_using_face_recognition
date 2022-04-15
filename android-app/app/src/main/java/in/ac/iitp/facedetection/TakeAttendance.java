package in.ac.iitp.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class TakeAttendance extends AppCompatActivity {
    private static int RESULT_LOAD_IMAGE = 1;
    ImageView ivtakeimage,ivpreview;
    Button btnfindstudent,btncofirm;
    ProgressBar pbloading;
    TextView tvinfo;
    String ipaddress,responsemsg;
    SQLiteDatabase db;
    String sid,name,rollno,semester,course,session;
    Functions fn=new Functions();
    String filename = Environment.getExternalStorageDirectory()+"/tempfile.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);
        ivtakeimage=(ImageView)findViewById(R.id.iv_takeattendance_takeimage);
        ivpreview=(ImageView)findViewById(R.id.iv_takeattendance_preview);

        btncofirm=(Button)findViewById(R.id.btntaconfirm);
        btnfindstudent=(Button)findViewById(R.id.btntafind);
        tvinfo=(TextView)findViewById(R.id.tvtainfo);
        pbloading=(ProgressBar)findViewById(R.id.pbta);
        pbloading.setVisibility(View.GONE);

        //SERVER URL
        db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS userconfig (id TEXT PRIMARY KEY, userid TEXT, password TEXT, ipaddress TEXT);");
        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=c.getString(0);
        }

        ivtakeimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                String newPicFile = "tempfile.jpg";
                String outPath = Environment.getExternalStorageDirectory()+ "/" + newPicFile;
                File outFile = new File(outPath);

                filename = outPath;
                Uri outuri = Uri.fromFile(outFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        btnfindstudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadTask().execute();
                pbloading.setVisibility(View.VISIBLE);
            }
        });

        btncofirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbloading.setVisibility(View.VISIBLE);
                new Thread(){
                    public void run(){
                        try{
                            final String response = fn.downloadUrl(ipaddress+"/appinsertattendance?studentid="+sid).trim();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pbloading.setVisibility(View.INVISIBLE);
                                }
                            });
                            if(response.equals("1")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btncofirm.setBackgroundColor(Color.rgb(0,255,0));
                                        btncofirm.setText("Confirmed");
                                    }
                                });
                            }else if(response.equals("2")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btncofirm.setBackgroundColor(Color.rgb(255,0,0));
                                        btncofirm.setText("Already Taken");
                                    }
                                });
                            }
                        }catch (Exception e){

                        }
                    }
                }.start();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                Bitmap bitmap = BitmapFactory.decodeFile(filename);
                try{
                    ExifInterface ei = new ExifInterface(filename);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Bitmap rotatedBitmap = null;
                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(bitmap, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = bitmap;
                    }
                    Bitmap b=getResizedBitmap(rotatedBitmap,500);
                    ivpreview.setImageBitmap(getResizedBitmap(rotatedBitmap,300));

                    File file = new File(Environment.getExternalStorageDirectory(), "file.jpg");
                    FileOutputStream fOut = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                }catch (Exception e){
                    Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        long transfered = 0;
        long totalsize = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            uploadFile();
            return null;
        }
        private String uploadFile() {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            final File sourceFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "file.jpg");
            totalsize = sourceFile.length();
            String serverResponseMessage = null;
            final String responce = null;
            if (!sourceFile.isFile()) {
                return "no file";
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile.getPath());
                    URL url = new URL( ipaddress+"/appstudentinfofromimage");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    //conn.setRequestProperty(POST_FIELD, sourceFile.getName());
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=" + URLEncoder.encode(sourceFile.getName(), "UTF-8") + lineEnd);
                    dos.writeBytes(lineEnd);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    int serverResponseCode = conn.getResponseCode();
                    serverResponseMessage = conn.getResponseMessage();
                    if (serverResponseCode <= 200) {

                    }
                    fileInputStream.close();

                    //Get Response
                    InputStream is=conn.getInputStream();
                    BufferedReader rd=new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer response = new StringBuffer();
                    while((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    //End Get Response
                    dos.flush();
                    dos.close();
                    //Return Response
                    responsemsg=response.toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(TakeAttendance.this, ""+responsemsg, Toast.LENGTH_SHORT).show();
                            //tvinfo.setText(responsemsg);
                            pbloading.setVisibility(View.INVISIBLE);
                            try{
                                String jsonstring=responsemsg;
                                JSONArray jarr=new JSONArray(jsonstring);
                                for(int i=0;i<jarr.length();i++){
                                    JSONObject jobj=jarr.getJSONObject(i);
                                    sid = jobj.getString("id");
                                    name = jobj.getString("name");
                                    rollno = jobj.getString("rollno");
                                    semester = jobj.getString("semester");
                                    course = jobj.getString("course");
                                    session = jobj.getString("session");
                                    //edid.setText(jobj.getString("id"));
                                    //edname.setText(jobj.getString("name"));
                                }
                                String info="Name : "+name+"\nRollno : "+rollno+"\nSemester : "+semester+"\nCourse : "+course+"\nSession : "+session;
                                tvinfo.setText(info);
                            }catch (Exception e){}
                        }
                    });
                    return response.toString();
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TakeAttendance.this, ""+e, Toast.LENGTH_SHORT).show();
                            pbloading.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
            return responce;
        }
    }
}
