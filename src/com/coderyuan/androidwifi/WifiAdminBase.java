package com.coderyuan.androidwifi;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public abstract class WifiAdminBase {
    // 模式常量
    public final static int WIFI_CLIENT_MODE = 0; // 客户端模式
    public final static int WIFI_AP_MODE = 1;// 热点模式

    // 验证模式常量
    public final static int KEY_NONE = 0;
    public final static int KEY_WEP = 1;
    public final static int KEY_WPA = 2;

    protected List<WifiConfiguration> mWifiConfiguration; // 无线网络配置信息类集合(网络连接列表)
    protected WifiManager mWifiManager;
    protected WifiInfo mWifiInfo; // 描述任何Wifi连接状态
    protected WifiManager.WifiLock mWifilock; // 能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    protected Context context;

    public WifiAdminBase(Context context) {
        this.context = context;
        mWifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    /** 获取Wifi状态 **/
    public boolean getWifiState() {
        return this.mWifiManager.isWifiEnabled();
    }

    /** 打开Wifi **/
    public void openWifi() {
        if (!this.mWifiManager.isWifiEnabled()) { // 当前wifi不可用
            this.mWifiManager.setWifiEnabled(true);
        }
    }

    /** 关闭Wifi **/
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 配置Wifi信息
     * 
     * @param ssid Wifi热点名称
     * @param passawrd 密码
     * @param authAlogrithm KEY_NONE是无密码，KEY_WEP是共享密钥WEP模式，KEY_WPA是WPA_PSK加密，暂不支持EAP
     * @param type AP模式或Client模式,WIFI_CLIENT_MODE,WIFI_AP_MODE
     * @return WifiConfiguration WifiConfiguration对象
     */
    public WifiConfiguration makeConfiguration(String ssid, String passawrd, int authAlogrithm, int type) {
        // 配置网络信息类
        WifiConfiguration customerWifiConfig = new WifiConfiguration();
        // 清空配置网络属性
        customerWifiConfig.allowedAuthAlgorithms.clear();
        customerWifiConfig.allowedGroupCiphers.clear();
        customerWifiConfig.allowedKeyManagement.clear();
        customerWifiConfig.allowedPairwiseCiphers.clear();
        customerWifiConfig.allowedProtocols.clear();

        if (type == WIFI_CLIENT_MODE) { // wifi连接
            customerWifiConfig.SSID = ("\"" + ssid + "\"");
            // 检测热点是否已存在
            WifiConfiguration tempConfiguration = isExsits(ssid);
            if (tempConfiguration != null) {
                mWifiManager.removeNetwork(tempConfiguration.networkId); // 从列表中删除指定的网络配置网络
            }
            if (authAlogrithm == KEY_NONE) { // 没有密码
                customerWifiConfig.wepKeys[0] = "";
                customerWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                customerWifiConfig.wepTxKeyIndex = 0;
            } else if (authAlogrithm == KEY_WEP) { // WEP密码
                customerWifiConfig.hiddenSSID = true;
                customerWifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                customerWifiConfig.wepKeys[0] = ("\"" + passawrd + "\"");
            } else { // WPA_PSK加密
                customerWifiConfig.preSharedKey = ("\"" + passawrd + "\"");
                customerWifiConfig.hiddenSSID = true;
                customerWifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                customerWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                customerWifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                customerWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                customerWifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                customerWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            }
        } else {// "ap" wifi热点
            customerWifiConfig.SSID = ssid;
            customerWifiConfig.allowedAuthAlgorithms.set(1);

            customerWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            customerWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            customerWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            customerWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            if (authAlogrithm == KEY_NONE) { // 没有密码
                customerWifiConfig.wepKeys[0] = "";
                customerWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                customerWifiConfig.wepTxKeyIndex = 0;
            } else if (authAlogrithm == KEY_WEP) { // WEP密码
                customerWifiConfig.hiddenSSID = true;// 网络上不广播ssid
                customerWifiConfig.wepKeys[0] = passawrd;
                customerWifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            } else if (authAlogrithm == KEY_WPA) {// WPA加密
                customerWifiConfig.hiddenSSID = true;// 网络上不广播ssid
                customerWifiConfig.preSharedKey = passawrd;
                customerWifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                customerWifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                customerWifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                customerWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                customerWifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                customerWifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            }
        }
        return customerWifiConfig;
    }

    /**
     * 已配置的无线热点中，是否存在网络信息
     * 
     * @param str 热点名称
     * @return
     */
    protected WifiConfiguration isExsits(String str) {
        if (this.mWifiManager.getConfiguredNetworks() == null) {
            return null;
        }
        Iterator<WifiConfiguration> localIterator = this.mWifiManager.getConfiguredNetworks().iterator();
        WifiConfiguration localWifiConfiguration;
        do {
            if (!localIterator.hasNext()) {
                return null;
            }
            localWifiConfiguration = (WifiConfiguration) localIterator.next();
        } while (!localWifiConfiguration.SSID.equals("\"" + str + "\""));
        return localWifiConfiguration;
    }

    /** 得到配置好的网络 **/
    public List<WifiConfiguration> getConfiguration() {
        return this.mWifiConfiguration;
    }

    /** 锁定WifiLock，当下载大文件时需要锁定 **/
    public void AcquireWifiLock() {
        this.mWifilock.acquire();
    }

    /** 创建一个WifiLock **/
    public void CreateWifiLock() {
        this.mWifilock = this.mWifiManager.createWifiLock("Test");
    }

    /** 解锁WifiLock **/
    public void ReleaseWifilock() {
        if (mWifilock.isHeld()) { // 判断时候锁定
            mWifilock.acquire();
        }
    }

    /** 获取wifi SSID **/
    public String getSSID() {
        if (this.mWifiInfo == null) {
            return null;
        }
        return this.mWifiInfo.getSSID();
    }

    /** 获取wifi BSSID **/
    public String getBSSID() {
        if (this.mWifiInfo == null) {
            return null;
        }
        return this.mWifiInfo.getBSSID();
    }

    /** 获取ip地址 **/
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    /** 获取网关IP **/
    public int getGatewayIP() {
        return (this.mWifiManager == null) ? 0 : this.mWifiManager.getDhcpInfo().gateway;
    }

    /** 获取物理地址(Mac) **/
    public String getMacAddress() {
        return (mWifiInfo == null) ? null : mWifiInfo.getMacAddress();
    }

    /** 获取网络id **/
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /** 获取wifi连接信息 **/
    public WifiInfo getWifiInfo() {
        return this.mWifiManager.getConnectionInfo();
    }

    public WifiManager getWifiManager() {
        return this.mWifiManager;
    }
}
