package in.ac.iitp.facedetection;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by RomySonu on 2/20/2019.
 */

public class ImageCustomAdapter extends ArrayAdapter<String> {
    Activity context;
    ArrayList<String> id;
    ArrayList<String> name;
    ArrayList<String> link;

    public ImageCustomAdapter(Activity context, ArrayList<String> id, ArrayList<String> name, ArrayList<String> link){
        super(context,R.layout.images_row,id);
        this.context=context;
        this.id=id;
        this.name=name;
        this.link=link;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView;

        rowView = inflater.inflate(R.layout.images_row, null, true);

        ImageView ivicon=(ImageView)rowView.findViewById(R.id.iv_images_row_img);
        new ImageDownloaderTask(ivicon).execute(link.get(position));

        return rowView;
    }
}
