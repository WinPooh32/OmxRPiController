package tk.winpooh32.omxrpicontroller;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;


public class ControllerApp extends Application {
    public  Settings settings = Settings.getInstance();
    public OmxNetController playerController;
    public String serverIp = "";
    public String serverPort = "";
    public String magnet = "magnet:?xt=urn:btih:7c8ddca8526ea4690f0078c61e3b27e8286e1837&dn=rutor.info_%D0%92%D0%B8%D0%BA%D0%B8%D0%BD%D0%B3%D0%B8+%2F+Vikings+%5B04%D1%8501-17+%D0%B8%D0%B7+20%5D+%282016%29+WEB-DLRip+%D0%BE%D1%82+qqss44+%7C+LostFilm&tr=udp://opentor.org:2710&tr=udp://opentor.org:2710&tr=http://retracker.local/announce";
    public boolean orientationChanged = false;
    public ItemModel itemSelected;
    public ItemModel itemPlayed;
    //public FileInfoAdapter adapter;
    public ArrayList<OmxNetController.FileInfo> filesInfo;

    @Override
    public void onCreate() {
        super.onCreate();

        settings.init(this);
        loadValues();

        playerController = new OmxNetController();
    }

    public void loadValues(){
        serverIp = settings.getIp();
        serverPort = settings.getPort();
    }

    public void saveValues(){
        settings.setIp(serverIp);
        settings.setPort(serverPort);
    }
}
