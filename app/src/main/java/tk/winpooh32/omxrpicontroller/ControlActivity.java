package tk.winpooh32.omxrpicontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ControlActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final String TAG_ERROR = "ERROR";
    final String TAG_WARNING = "WARN";
    final String TAG_VERBOSE = "INFO";

    View _headerLayout;

    int _orientation = -1;
    boolean _orientationChanged = false;

    FireMissilesDialogFragment _dialogSetAdress;
    MagnetDialogFragment _dialogSetMagnet;

    ControllerApp _application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _application = (ControllerApp)getApplication();

        setContentView(R.layout.activity_control);

        //Try to connect to server on previous ip and port
        if(!_application.playerController.isConnected()){
            connectToServer(_application.serverIp, _application.serverPort);
        }

        //Handling orientation change and check onPause
        _orientation = this.getResources().getConfiguration().orientation;


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        if(savedInstanceState == null){
           // _application.playerController = new OmxNetController();
        }


        //Setup buttons callbacks
        _dialogSetAdress = new FireMissilesDialogFragment();
        _dialogSetMagnet = new MagnetDialogFragment();

        ((ImageButton)findViewById(R.id.btn_play)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnStart(v);
            }
        });

        ((ImageButton)findViewById(R.id.btn_stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnStop(v);
            }
        });


        final long offset = 30000; // 30 sec

        ((ImageButton)findViewById(R.id.btn_rew_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSeek(v, offset, -1);
            }
        });

        ((ImageButton)findViewById(R.id.btn_rew_forward)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSeek(v, offset, 1);
            }
        });


        //Reset files list on orientation changed
        if(_application.orientationChanged){
            //orientation changed
            if(_application.filesInfo != null){
                int pos = -1;
                if(_application.itemPlayed != null){
                    pos = _application.itemPlayed._position;
                }
                FileInfoAdapter adapter = new FileInfoAdapter(ControlActivity.this, _application.filesInfo, pos);

                ListView list = (ListView)findViewById(R.id.listView);
                list.setAdapter(adapter);
            }

            _application.orientationChanged = false;
        }

        _headerLayout = navigationView.getHeaderView(0);
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        if(this.getResources().getConfiguration().orientation != _orientation){
            _orientation = this.getResources().getConfiguration().orientation;
            _application.orientationChanged = true;
        }
    }


    public void onBtnStop(View btn){
        _application.playerController.sendStop();
    }

    public void onBtnStart(View btn){
        if(_application.itemPlayed != null){
            _application.itemPlayed.showCheckBox(false);
        }

        if(_application.itemSelected != null){
            _application.itemPlayed = _application.itemSelected;
            _application.itemPlayed.showCheckBox(true);

            _application.playerController.sendPlay(_application.itemPlayed._position);
        }
    }

    public void onBtnSeek(View btn, long offset, int side){
        _application.playerController.sendSeek(offset, side);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        _application.saveValues();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        switch (id){
            case R.id.action_reconnect:
                connectToServer(_application.serverIp, _application.serverPort);
                //Log.wtf("@@@", "DO reconnect action here!");
                return true;

            case R.id.action_magnet:
                _dialogSetMagnet.show(getFragmentManager(), "_dialogSetMagnet");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action

            _dialogSetAdress.show(getFragmentManager(), "_dialogSetAdress");
        } else if (id == R.id.nav_gallery) {
            _dialogSetMagnet.show(getFragmentManager(), "_dialogSetMagnet");
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(ControlActivity.this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_share) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Send to ..."));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void startMagnet(){
        if(!_application.playerController.isConnected()){
            return;
        }

        String magnet = _application.magnet;
        int connections = 10;
        int uploads = 2;
        boolean verify = false;
        String path = "/media/usb/torrent-stream";

        _application.playerController.sendStart(magnet, connections, uploads, verify, path);
    }

    void connectToServer(String ip, String port){
        _application.serverIp = ip;
        _application.serverPort = port;

        String socketioAdress = "http://" + _application.serverIp + ":" + _application.serverPort + "/";

        Log.wtf("@@@", "Connecting to server " + socketioAdress);

        _application.playerController.connect(socketioAdress,
                //Success callback
                new Callback() {
                    @Override
                    public void call(Object... args) {
                        final String connected = "Connected";
                        Log.wtf(TAG_VERBOSE, connected);

                        ControlActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView tvIp = (TextView)(_headerLayout.findViewById(R.id.nav_header_ip));
                                tvIp.setText(_application.serverIp + ":" + _application.serverPort);

                                Toast.makeText(ControlActivity.this.getApplicationContext(), connected, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                },
                //Error callback
                new Callback() {
                    @Override
                    public void call(Object... args) {
                        final String error = (String)args[0];

                        Log.wtf(TAG_ERROR, error);

                        ControlActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView tvIp = (TextView)_headerLayout.findViewById(R.id.nav_header_ip);
                                tvIp.setText(ControlActivity.this.getResources().getText(R.string.disconnected));

                                Toast.makeText(ControlActivity.this.getApplicationContext(), error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

        _application.playerController.setCallbackOnFilesInfo(new Callback() {
            @Override
            public void call(Object... args) {
                final ArrayList<OmxNetController.FileInfo> filesInfo = (ArrayList<OmxNetController.FileInfo>) args[0];
                final Integer file_current = (Integer) args[1];

                ControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //ArrayAdapter<String> adapter = new ArrayAdapter<>(ControlActivity.this, android.R.layout.simple_list_item_activated_1, filesNames);

                        _application.filesInfo = filesInfo;
                        FileInfoAdapter adapter = new FileInfoAdapter(ControlActivity.this, filesInfo, file_current);

                        ListView list = (ListView)findViewById(R.id.listView);
                        list.setAdapter(adapter);
                        list.setItemsCanFocus(false);
                    }
                });
            }
        });

        _application.playerController.setCallbackOnStopped(new Callback() {
            @Override
            public void call(Object... args) {
                Log.wtf(TAG_VERBOSE, "File stopped");
                ControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ControlActivity.this.getApplicationContext(), "File stopped", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public static class FireMissilesDialogFragment extends DialogFragment {
        ControlActivity owner;
        View view;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            view = inflater.inflate(R.layout.dialog_set_ip, null);
            owner = (ControlActivity)getActivity();

            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           android.text.Spanned dest, int dstart, int dend) {
                    if (end > start) {
                        String destTxt = dest.toString();
                        String resultingTxt = destTxt.substring(0, dstart)
                                + source.subSequence(start, end)
                                + destTxt.substring(dend);
                        if (!resultingTxt
                                .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                            return "";
                        } else {
                            String[] splits = resultingTxt.split("\\.");
                            for (int i = 0; i < splits.length; i++) {
                                if (Integer.valueOf(splits[i]) > 255) {
                                    return "";
                                }
                            }
                        }
                    }
                    return null;
                }

            };
            ((EditText)view.findViewById(R.id.ip_input)).setFilters(filters);

            ((EditText)view.findViewById(R.id.ip_input)).setText(owner._application.serverIp);
            ((EditText)view.findViewById(R.id.port_input)).setText(owner._application.serverPort);



            // Inflate and set the layout for the _dialogSetAdress
            // Pass null as the parent view because its going in the _dialogSetAdress layout
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            EditText ip = (EditText)view.findViewById(R.id.ip_input);
                            EditText port = (EditText)view.findViewById(R.id.port_input);

                            owner.connectToServer(ip.getText().toString(), port.getText().toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            return builder.create();
        }
    }

    public static class MagnetDialogFragment extends DialogFragment {
        ControlActivity owner;
        View view;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            view = inflater.inflate(R.layout.dialog_set_magent, null);
            owner = (ControlActivity)getActivity();

            ((EditText)view.findViewById(R.id.magnet_input)).setText(owner._application.magnet);
            Log.wtf("@@@", owner._application.magnet);
            // Inflate and set the layout for the _dialogSetAdress
            // Pass null as the parent view because its going in the _dialogSetAdress layout
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton("Stream", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            EditText magnet = (EditText)view.findViewById(R.id.magnet_input);
                            owner._application.magnet = magnet.getText().toString();
                            owner.startMagnet();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            return builder.create();
        }
    }
}
