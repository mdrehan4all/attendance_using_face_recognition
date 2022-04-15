package in.ac.iitp.facedetection;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Rehan on 1/26/2018.
 */
public class Functions {
    //Download Url
    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while((line=br.readLine())!= null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            //Log.d("Exception while fetching data", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    public String ePost(String targetURL, String urlParameters)
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url=new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", ""+Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //Send request
            DataOutputStream wr=new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            //Get Response
            InputStream is=connection.getInputStream();
            BufferedReader rd=new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch(Exception e) {
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public String readin(String filename,Context context)
    {
        File infile=new File(context.getFilesDir()+"/",filename);
        try {
            FileReader reader = new FileReader(infile);
            int c;
            String temp="";
            while((c=reader.read())!=-1) {
                temp = temp + Character.toString((char) c);
            }
            return temp;
        }catch (IOException e){}

        return "";
    }

    public int writein(String filename,String data,Context context)
    {
        File infile=new File(context.getFilesDir()+"/",filename);
        try {
            FileWriter writer = new FileWriter(infile);
            writer.write(data);
            writer.close();
        } catch (IOException e){}
        return 0;
    }
}