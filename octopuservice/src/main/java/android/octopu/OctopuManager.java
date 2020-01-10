package android.octopu;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.android.server.octopu.wifiextend.bean.WifiDeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: OctopuManager
 * Description:
 * Author: FlyZebra
 * Email:flycnzebra@gmail.com
 * Date: 20-1-8 下午5:44
 */
public class OctopuManager {
    private static final int NOTIFY_WIFIDEVICES = 1;

    private List<WifiDeviceListener> mWifiDeviceListeners = new ArrayList<>();
    private final Object mListenerLock = new Object();
    private List<WifiDeviceBean> mWifiDeviceBeans = new ArrayList<>() ;
    private final Object mWifiListLock = new Object();
    private IOctopuService mService;
    private Handler mHandler = new MainHandler(Looper.getMainLooper());
    private OctopuListener mOctopuListener = new OctopuListener.Stub() {
        @Override
        public void notifyWifiDevices(final List<WifiDeviceBean> wifiDeviceBeans) throws RemoteException {
            synchronized (mWifiListLock) {
                mWifiDeviceBeans.clear();
                mWifiDeviceBeans.addAll(wifiDeviceBeans);
            }
            mHandler.sendEmptyMessage(NOTIFY_WIFIDEVICES);
        }
    };

    public OctopuManager(Context context, IOctopuService octopuService) {
        mService = octopuService;
        try {
            mService.registerListener(mOctopuListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void flyWifiDevices(List<String> wifiBssids) {
        try {
            mService.flyWifiDevices(wifiBssids);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public interface WifiDeviceListener {
        void notifyWifiDevices(List<WifiDeviceBean> wifiDeviceBeans);
    }

    public void addWifiDeviceListener(WifiDeviceListener wifiDeviceListener) {
        synchronized (mListenerLock) {
            mWifiDeviceListeners.add(wifiDeviceListener);
        }
    }

    public void removeWifiDeviceListener(WifiDeviceListener wifiDeviceListener) {
        synchronized (mListenerLock) {
            mWifiDeviceListeners.remove(wifiDeviceListener);
        }
    }


    private class MainHandler extends Handler {
        public MainHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage( Message msg) {
            switch (msg.what){
                case NOTIFY_WIFIDEVICES:
                    synchronized (mListenerLock) {
                        for (WifiDeviceListener wifiDeviceListener: mWifiDeviceListeners) {
                            synchronized (mWifiListLock){
                                wifiDeviceListener.notifyWifiDevices(mWifiDeviceBeans);
                            }
                        }
                    }
                    break;
            }
        }
    }
}
