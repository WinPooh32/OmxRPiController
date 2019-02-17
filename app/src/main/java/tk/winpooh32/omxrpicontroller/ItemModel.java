package tk.winpooh32.omxrpicontroller;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ItemModel {
    public OmxNetController.FileInfo _info;
    public boolean _selected = false;
    public boolean _checkBoxShowed = false;
    public int _position;
    public CheckBox _checkbox;
    public TextView _textview;

    ItemModel(OmxNetController.FileInfo info, int position){
        _info = info;
        _position = position;
    }

    String getName(){
        return _info.name;
    }

    String getSize(){
        return _info.size;
    }

    boolean isSelected(){
        return _selected;
    }

    void select(boolean selected){
        _selected = selected;
    }

    void showCheckBox(boolean show){
        _checkBoxShowed = show;
        if(show){
            _checkbox.setChecked(true);
            _checkbox.setVisibility(View.VISIBLE);
        }else{
            _checkbox.setChecked(false);
            _checkbox.setVisibility(View.GONE);
        }
    }
}
