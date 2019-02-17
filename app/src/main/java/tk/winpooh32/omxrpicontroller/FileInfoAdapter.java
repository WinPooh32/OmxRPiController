package tk.winpooh32.omxrpicontroller;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class FileInfoAdapter extends BaseAdapter {

    Context context;
    ItemModel[] data;
    private static LayoutInflater inflater = null;

    public FileInfoAdapter(Context context,  ArrayList<OmxNetController.FileInfo> files, int current_index) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = new ItemModel[files.size()];

        for (int i = 0; i < files.size(); i++) {
            this.data[i] = new ItemModel(files.get(i), i);
        }

        if(current_index >= 0){
            ((ControllerApp)context.getApplicationContext()).itemPlayed =  data[current_index];
        }else{
            ((ControllerApp)context.getApplicationContext()).itemPlayed = null;
        }

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void selectItem(int position){
        for (ItemModel item: data) {
            TextView text = item._textview;

            if(item._position == position){
                item.select(true);
                text.setBackgroundColor(ContextCompat.getColor(context, R.color.pressed_color));
            }else if(item.isSelected()){
                item.select(false);
                text.setBackgroundColor(ContextCompat.getColor(context, R.color.default_color));
            }
        }

        ((ControllerApp)context.getApplicationContext()).itemSelected = (ItemModel) getItem(position);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.row_file_info, null);

        final ItemModel item = (ItemModel) getItem(position);

        TextView text = (TextView) vi.findViewById(R.id.text);
        text.setText(item.getName());

        //vi.setSelected(true);
        //text.setSelected(true);

        text.setMovementMethod(new ScrollingMovementMethod());
        item._checkbox = (CheckBox)vi.findViewById(R.id.item_checkbox);
        item._textview = text;

        ControllerApp app = (ControllerApp) context.getApplicationContext();

        if(item.isSelected() || (app.itemSelected != null && item._position == app.itemSelected._position)){
            text.setBackgroundColor(ContextCompat.getColor(context, R.color.pressed_color));
        }else{
            text.setBackgroundColor(ContextCompat.getColor(context, R.color.default_color));
        }

        if(item == app.itemPlayed || (app.itemPlayed != null && item._position == app.itemPlayed._position)){
            item.showCheckBox(true);
        }else{
            item.showCheckBox(false);
        }

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(position);
            }
        });

//        vi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.wtf("@@@", "VI PRESSED!");
//            }
//        });

        return vi;
    }
}