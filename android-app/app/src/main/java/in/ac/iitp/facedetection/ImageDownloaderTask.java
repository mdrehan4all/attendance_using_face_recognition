package in.ac.iitp.facedetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by RomySoun on 6/8/2018.
 */

public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    File outputFile=null;
    String downloadFileName="temp.jpg";

    public ImageDownloaderTask(ImageView imageView) {
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return downloadBitmap(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    //Drawable placeholder = null;
                    //imageView.setImageDrawable(placeholder);
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }
    }

    private Bitmap downloadBitmap(String link) {

        try {
            int index = link.lastIndexOf("/");
            downloadFileName = link.substring(index);

            File statusupdatedir = new File(Environment.getExternalStorageDirectory(),"FaceDetectionIcons");
            if (!statusupdatedir.exists()) {
                statusupdatedir.mkdir();
            }
            outputFile = new File(statusupdatedir, downloadFileName);//Create Output file in Main File
            //Create New File if not present
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getPath());
                //return bitmap;
                //return null;
            }
        }catch (Exception e){}

        try {
            URL url = new URL(link);//Create Download URl
            HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
            c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
            c.connect();//connect the URL Connection
            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {

            }
            FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location
            InputStream is = c.getInputStream();//Get InputStream for connection
            byte[] buffer = new byte[1024];//Set buffer type
            int len1 = 0;//init length
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);//Write new file
            }
            fos.close();
            is.close();

            Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getPath());
            return bitmap;
        } catch (Exception e) {
            outputFile = null;
        }
        return null;
    }
}
