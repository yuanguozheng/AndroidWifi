package com.coderyuan.androidwifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

public class WifiClientAdmin extends WifiAdminBase {

    public WifiClientAdmin(Context context) {
        super(context);
    }

    /** 断开指定id的wifi **/
    public void disconnectWifi(int paramInt) {
        this.mWifiManager.disableNetwork(paramInt);
    }

    public WifiConfiguration makeConfiguration(String ssid, String passawrd, int authAlogrithm) {
        return super.makeConfiguration(ssid, passawrd, authAlogrithm, WIFI_CLIENT_MODE);
    }

    /** 添加并连接指定网络 **/
    public void addNetwork(WifiConfiguration paramWifiConfiguration) {
        if (!super.getWifiState()) {
            return;
        }
        WifiConfiguration tempConfiguration = isExsits(paramWifiConfiguration.SSID);
        if (tempConfiguration != null) {
            mWifiManager.removeNetwork(tempConfiguration.networkId); // 从列表中删除指定的网络配置网络
        }
        int i = mWifiManager.addNetwork(paramWifiConfiguration);
        mWifiManager.enableNetwork(i, true);
    }

    /**
     * 连接指定配置好的网络
     * 
     * @param index 配置好网络的ID
     */
    public void connectConfiguration(int index) {
        super.openWifi();
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }

}
