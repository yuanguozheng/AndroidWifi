package com.coderyuan.androidwifi;

import java.lang.reflect.Method;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

public class WifiAPAdmin extends WifiAdminBase {

    public WifiAPAdmin(Context context) {
        super(context);
    }

    /**
     * 配置Wifi AP
     * 
     * @param wifiConfig 配置
     * @param enable 是否开启， {@code true}为开启， {@code false}为关闭
     * @return {@code true} 操作成功, {@code false} 出现异常
     */
    private boolean setWifiAp(WifiConfiguration wifiConfig, boolean enable) {
        try {
            if (enable) {
                closeWifi();// 开启热点需要关闭Wifi
            }
            Method method =
                    mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig, enable);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    /**
     * 按配置信息，开启AP模式
     * 
     * @param wifiConfig SSID、密码、加密等WifiConfiguration信息
     * @return {@code true} 操作成功, {@code false} 出现异常
     */
    public boolean startAp(WifiConfiguration wifiConfig) {
        if (isWifiApEnabled()) {
            return false;
        }
        return setWifiAp(wifiConfig, true);
    }

    /**
     * 开启AP模式
     */
    public boolean startAp() {
        return startAp(getWifiApConfiguration());
    }

    /**
     * 停止Wifi AP模式
     * 
     * @return {@code true} 操作成功, {@code false} 出现异常
     */
    public boolean stopAp() {
        if (!isWifiApEnabled()) {
            return true;
        }
        boolean result = setWifiAp(null, false);
        openWifi(); // 关闭热点，打开wifi
        return result;
    }

    /**
     * 获取Wifi AP状态
     * 
     * @return {@link WIFI_AP_STATE}
     * @see #isWifiApEnabled()
     */
    public WIFI_AP_STATE getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");

            int tmp = ((Integer) method.invoke(mWifiManager));

            // Fix for Android 4
            if (tmp >= 10) {
                tmp = tmp - 10;
            }

            return WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    /**
     * 检测 Wi-Fi AP 是否被开启
     * 
     * @return {@code true} 如果 Wi-Fi AP 开启
     * @see #getWifiApState()
     *
     * @hide Dont open yet
     */
    public boolean isWifiApEnabled() {
        return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    /**
     * 获取Wi-Fi AP 配置信息.
     * 
     * @return AP配置 {@link WifiConfiguration}
     */
    public WifiConfiguration getWifiApConfiguration() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return null;
        }
    }

    /**
     * 设置 Wi-Fi AP 配置.
     * 
     * @param {@link WifiConfiguration} 配置信息
     * @return {@code true} 操作成功, {@code false} 出现异常
     */
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    /**
     * 设置 Wi-Fi AP 配置.
     * 
     * @param ssid 无线热点名
     * @param passawrd 密码，长度>=8位
     * @return {@code true} 操作成功, {@code false} 出现异常
     */
    public boolean setWifiApConfiguration(String ssid, String passawrd) {
        WifiConfiguration apConfig = makeConfiguration(ssid, passawrd, KEY_WPA, WIFI_AP_MODE);
        return setWifiApConfiguration(apConfig);
    }

    public WifiConfiguration makeConfiguration(String ssid, String passawrd, int authAlogrithm) {
        return super.makeConfiguration(ssid, passawrd, authAlogrithm, WIFI_AP_MODE);

    }
}