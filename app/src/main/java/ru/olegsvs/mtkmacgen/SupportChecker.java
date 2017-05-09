package ru.olegsvs.mtkmacgen;

import android.util.Log;

import com.orhanobut.rootchecker.RootChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by olegsvs on 09.05.2017.
 */

class SupportChecker {

    boolean isSupported = false;
    public static final String[] CPU_SUPPORT_LIST = {
            "MT6735P" , "MT6735M" , "MT6577" , "MT6577T" , "MT6572" ,
            "MT6572M" , "MT6582" , "MT6582M" , "MT6753" , "MT6753P" ,
            "MT6753M" , "MT6795" , "MT6797"}; // cat /proc/cpuinfo | grep Hardware
    public static String CPU = null;
    public static boolean SUSTATUS = false;

    boolean checkDeviceSupport() throws IOException, InterruptedException {
        SUSTATUS = suIsGranted();
        if((Arrays.asList(CPU_SUPPORT_LIST).contains(getFieldFromCpuinfo("Hardware"))) && (SUSTATUS)) { isSupported = true; return true; }
        // сравниваем поле Hardware с нашим списком процессоров и проверяем root-доступ
        else { isSupported = false; return false; }
    }

    private boolean suIsGranted() throws IOException, InterruptedException {
        if (!RootChecker.isDeviceRooted()) return false;
        // SU installed?
        Process p = Runtime.getRuntime().exec(new String[] {"su", "-c", "touch /data/data/tempFile"});
        Log.i(MainPage.TAG, "suIsGranted: attempt to create file /data/data/tempFile");
        p.waitFor();
        File tempFile = new File("/data/data/tempFile");
        Log.i(MainPage.TAG,"suIsGranted: check file exists : "+String.valueOf(tempFile.exists()));
        if (tempFile.exists()) {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "rm /data/data/tempFile"});
            Log.i(MainPage.TAG, "suIsGranted: root granted! delete tempFile");
            SUSTATUS = true;
            return true;
        }
        return false;
    }
    private static String getFieldFromCpuinfo(String field) throws IOException {
        // получаем всю инфу из cpuinfo
        BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
        Pattern p = Pattern.compile(field + "\\s*:\\s*(.*)");
        try {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    CPU = m.group(1);
                    return m.group(1);
                }
            }
        } finally {
            br.close();
        }
        return null;
    }
}
