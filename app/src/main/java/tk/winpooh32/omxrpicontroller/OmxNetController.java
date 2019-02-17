package tk.winpooh32.omxrpicontroller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

interface Callback {
    void  call(Object... args);
}

public class OmxNetController {
    private final String CMD_START          = "start";
    private final String CMD_DISCONNECT     = "disconnect";
    private final String CMD_PLAY           = "play";
    private final String CMD_PAUSE          = "pause";
    private final String CMD_STOP           = "stop";
    private final String CMD_SEEK           = "seek";
    private final String CMD_SEEK_TO        = "seek_to";
    private final String CMD_VOLUME_SET     = "volume_set";
    private final String CMD_VOLUME_UP      = "volume_up";
    private final String CMD_VOLUME_DOWN    = "volume_down";

    private final String EVENT_STOPPED       = "stopped";
    private final String EVENT_FILES_INFO    = "info";
    private final String EVENT_CHANGE_STATUS = "changeStatus";
    private final String EVENT_FINISH        = "finish";

    private final String ERROR_CONNECTION_FAILED = "Connection failed";

    private String _address;
    private int _duration;

    private boolean _isConnected = false;
    private boolean _isPlaying = false;
    private boolean _isSeekable = true;

    private Socket _socket;
    private Callback _onConnected;
    private Callback _onError;
    private Callback _onStatusChange;
    private Callback _onFilesInfo;
    private Callback _onStopped;

    public class FileInfo{
        public int index;
        public String name;
        public String size;
    }

    public OmxNetController(){

    }

    public OmxNetController(String address){
        _address = address;
    }

    private void reset(){
        _address = null;
        _duration = 0;

        _isConnected = false;
        _isPlaying = false;
        _isSeekable = true;

        _socket = null;
        _onConnected = null;
        _onError = null;
        _onStatusChange = null;
        _onFilesInfo = null;
        _onStopped = null;
    }

    private void registerEvents(){
        _socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                _isConnected = _socket.connected();

                if(_onConnected != null){
                    _onConnected.call();
                }
            }
        });

        _socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onConnectionError();
            }
        });

        _socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onConnectionError();
            }
        });

        _socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onConnectionError();
            }
        });


        _socket.on(EVENT_FILES_INFO, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    ArrayList<FileInfo> filesInfo = new ArrayList();

                    JSONArray arr = (JSONArray)args[0];
                    Integer file_index = (Integer) args[1];

                    for(int i = 0; i < arr.length(); i++){
                        final JSONObject obj = arr.getJSONObject(i);
                        filesInfo.add(new FileInfo(){{
                            index = obj.getInt("index");
                            name = obj.getString("name");
                            size = obj.getString("size");
                        }});
                    }

                    onFilesInfo(filesInfo, file_index);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        _socket.on(EVENT_CHANGE_STATUS, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onStatusChange((JSONObject)args[0]);
            }
        });
        _socket.on(EVENT_FINISH, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onFinish();
            }
        });

        _socket.on(EVENT_STOPPED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onStopped();
            }
        });


    }

    private void onConnectionError(){
        if(_onError != null){
            _onError.call(ERROR_CONNECTION_FAILED);
        }

        reset();
    }

    private void onStatusChange(JSONObject obj){
        if(_onStatusChange != null){
            //do
        }
    }

    private void onFilesInfo(ArrayList<FileInfo> filesInfo, Integer file_index){
        if(_onFilesInfo != null){
            _onFilesInfo.call(filesInfo, file_index);
        }
    }

    private void onStopped(){
        if(_onStopped != null){
            _onStopped.call();
        }
    }

    private void onFinish(){

    }

    public boolean isConnected(){
        return _isConnected;
    }

    public void setCallbackOnFilesInfo(Callback onFilesInfo){
        _onFilesInfo = onFilesInfo;
    }

    public void setCallbackOnStatusChange(Callback onStatusChange){
        _onStatusChange = onStatusChange;
    }

    public void setCallbackOnStopped(Callback onStopped){
        _onStopped = onStopped;
    }

    public void connect(String address, Callback onConnected, Callback onError){
        _address = address;
        connect(onConnected, onError);
    }

    public void connect(Callback onConnected, Callback onError){
        _onConnected = onConnected;
        _onError = onError;

        disconnect();

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = false;

        try {
            _socket = IO.socket(_address, opts);
            registerEvents();
            _socket.connect();
        } catch (Exception e){

            if(_onError != null){
                _onError.call(ERROR_CONNECTION_FAILED);
            }
            e.printStackTrace();
        }
    }

    public void disconnect(){
        if(_socket != null){
            if(_socket.connected()){
                _socket.disconnect();
                _socket = null;
            }
        }

        _isConnected = false;
    }

    public void sendStart(String magnet, int connections, int uploads, boolean verify, String path){
        if(!_isConnected) return;

        JSONObject json = new JSONObject();

        try {
            json.put("connections", connections);
            json.put("uploads", uploads);
            json.put("verify", verify);
            json.put("path", path);

            _socket.emit(CMD_START, magnet, json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendPlay(int file_index){
        if(!_isConnected) return;
        _socket.emit(CMD_PLAY, file_index);
    }

    public void sendPause(){
        if(!_isConnected) return;
        _socket.emit(CMD_PAUSE);
    }

    public void sendStop(){
        if(!_isConnected) return;
        _socket.emit(CMD_STOP);
    }

    public void sendSeek(long offset, int side){
        if(!_isConnected) return;

        JSONObject json = new JSONObject();

        try {
            json.put("offset", offset);
            json.put("side", side);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        _socket.emit(CMD_SEEK, json);
    }

    public void sendSeekTo(int msec){
        if(!_isConnected) return;
        _socket.emit(CMD_SEEK_TO, new Integer(msec));
    }
}
