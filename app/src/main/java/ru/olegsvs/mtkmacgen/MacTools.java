package ru.olegsvs.mtkmacgen;

import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * Created by olegsvs on 09.05.2017.
 */

public class MacTools {
    public static String resultMACaddress;

    public boolean setUserMAC(String str) throws Exception {
        String tmp  = str;
        str = str.replaceAll("\\:", "");

        writeNewMAC(str);
        Process process = Runtime.getRuntime().exec("su"); //Generic SU Command
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        os.writeBytes("svc wifi disable\n");
        Log.i(MainPage.TAG, "setUserMAC: DISABLING WIFI SERVISE is STARTED");
        os.writeBytes("cp -rf  " + MainPage.dataPath + " /data/nvram/APCFG/APRDEB/WIFI\n");
        os.writeBytes("chmod 777 /data/nvram/APCFG/APRDEB/WIFI\n");
        Log.i(MainPage.TAG, "setUserMAC: rewrite WIFI in nvram");
        os.writeBytes("svc wifi enable\n");
        Log.i(MainPage.TAG, "setUserMAC: ENABLE WIFI");
        os.writeBytes("exit\n");
        os.flush();
        os.close();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //устанавливаем пользовательский MAC-адрес
        Log.i(MainPage.TAG, "setUserMAC: " + str.toString());
        Log.i(MainPage.TAG, "setUserMAC: getMAC :  " + getMAC());
        Log.i(MainPage.TAG, "setUserMAC: resultMACaddress.equalsIgnoreCase(tmp) : " + resultMACaddress.equalsIgnoreCase(tmp));
        if(resultMACaddress.equalsIgnoreCase(tmp))
        return true;
        else return false;
    }

    private boolean isFirstLaunch() {
        File fFile = new File(MainPage.dataPath + "backup");
        if(fFile.exists()) return false;
        return true;
    }

    private boolean backupMAC() throws IOException {
        if(isFirstLaunch()) {
            Log.i(MainPage.TAG, "backupMAC: isFirstLaunch!");
            Log.i(MainPage.TAG, "backupMAC: backingUp MAC address!");
            Process process = Runtime.getRuntime().exec("su"); //Generic SU Command
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("cp -rf  /data/nvram/APCFG/APRDEB/WIFI " + MainPage.dataPath + "backup\n");
            os.writeBytes("chmod 777 " + MainPage.dataPath  + "backup\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return isFirstLaunch();
        }
        else {
            Log.i(MainPage.TAG, "backupMAC: isNotFirstLaunch!");
            return true;
        }
    }

    public boolean restoreMAC() throws Exception {
        if(!isFirstLaunch()) {
            Log.i(MainPage.TAG, "restoreMAC: restoring backup");
            Process process = Runtime.getRuntime().exec("su"); //Generic SU Command
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("svc wifi disable\n");
            Log.i(MainPage.TAG, "restoreMAC: DISABLING WIFI SERVISE is STARTED");
            os.writeBytes("cp -rf  " + MainPage.dataPath + "backup /data/nvram/APCFG/APRDEB/WIFI\n");
            os.writeBytes("chmod 777 /data/nvram/APCFG/APRDEB/WIFI\n");
            Log.i(MainPage.TAG, "restoreMAC: rewrite WIFI in nvram");
            os.writeBytes("svc wifi enable\n");
            Log.i(MainPage.TAG, "restoreMAC: ENABLE WIFI");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        } else return false;
    }

    public String getMAC() throws Exception {
        if(backupMAC()) Log.i(MainPage.TAG, "getMAC: backup create success!");
        else Log.e(MainPage.TAG, "getMAC: buckup create not success!");

        Process process = Runtime.getRuntime().exec("su"); //Generic SU Command
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        os.writeBytes("cp -rf  /data/nvram/APCFG/APRDEB/WIFI " + MainPage.dataPath + "\n");
        os.writeBytes("chmod 777 " + MainPage.dataPath  + "\n");
        os.writeBytes("exit\n");
        os.flush();
        os.close();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        File wFile = new File(MainPage.dataPath);
        Log.i(MainPage.TAG, "getMAC: WIFI exists? " + wFile.exists());
        resultMACaddress = bytesToMAC(getBytesFromFile(wFile));
        Log.i(MainPage.TAG, "getMAC: " + resultMACaddress);

            return resultMACaddress;
    }


    public String randomMACAddress(){
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

    private static byte[] getBytesFromFile(File file) throws IOException {
        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large!");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        return bytes;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        Log.i(MainPage.TAG, "hexStringToByteArray: data = " + data.toString());
        return data;
    }

    private void writeNewMAC(String str) throws IOException {
        File fl = new File(MainPage.dataPath);
        RandomAccessFile raf = new RandomAccessFile(fl, "rw");
        try {
            raf.seek(4); // offset = 4 ( Начало MAC )
            raf.write(hexStringToByteArray(str)); // Пишем байты из hexString в файл
        } finally {
            raf.close(); // flush + close
        }
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToMAC(byte[] bytes) {
        //байты в formatted-MAC
        String tmp , mac;
        char divisionChar = ':';
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        tmp = new String(hexChars);
        Log.i(MainPage.TAG, "bytesToMAC: readed bytes : " + tmp);
        mac = tmp.substring(8,20);
        Log.i(MainPage.TAG, "bytesToMAC: substring : " + mac.toString());
        return mac.replaceAll("(.{2})", "$1"+divisionChar).substring(0,17);
    }
}
