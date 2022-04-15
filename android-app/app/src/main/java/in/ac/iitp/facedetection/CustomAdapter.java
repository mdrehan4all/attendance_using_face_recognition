package in.ac.iitp.facedetection;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String> {
    Activity context;
    ArrayList<String> id;
    ArrayList<String> name;
    ArrayList<String> icon;

    public CustomAdapter(Activity context, ArrayList<String> id, ArrayList<String> name, ArrayList<String> icon){
        super(context,R.layout.users_row,id);
        this.context=context;
        this.id=id;
        this.name=name;
        this.icon=icon;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView;

        rowView = inflater.inflate(R.layout.users_row, null, true);

        TextView tvid=(TextView)rowView.findViewById(R.id.tvusersrow_id);
        TextView tvname=(TextView)rowView.findViewById(R.id.tvusersrow_name);
        ImageView ivicon=(ImageView)rowView.findViewById(R.id.ivusersrow_icon);

        tvid.setText(id.get(position));
        tvname.setText(name.get(position));
        //ivicon.setImageResource(R.mipmap.ic_launcher);
        //new ImageDownloaderTask(ivicon).execute(icon.get(position));

        return rowView;
    }
}
