package ru.olegsvs.mtkmacgen;

import android.util.Log;

import java.util.Random;

/**
 * Created by olegsvs on 09.05.2017.
 */

public class MacTools {

    private boolean setUserMAC(String str) {
        //устанавливаем пользовательский MAC-адрес
        if(getMAC().equals(str))
        return true;
        else return false;
    }

    private String getMAC() {
        //получаем MAC-адрес устройства
        return null;
    }

    static String randomMACAddress(){
        //генерируем случайный MAC-адрес
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

        StringBuilder sb = new StringBuilder(18);
        for(byte b : macAddr){

            if(sb.length() > 0)
                sb.append(":");

            sb.append(String.format("%02x", b));
        }

        Log.i(MainPage.TAG, "randomMACAddress: generated " + sb.toString());
        return sb.toString();
    }
}
