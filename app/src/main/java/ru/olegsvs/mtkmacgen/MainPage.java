package ru.olegsvs.mtkmacgen;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class MainPage extends AppCompatActivity {

    public static String TAG = "olegsvs_macgen";
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        context = getApplicationContext();
    }

    public void testFunc(View view) throws IOException, InterruptedException {
        SupportChecker sc = new SupportChecker();
        sc.checkDeviceSupport();
        Log.i("device support : ", "testFunc: " + sc.isSupported);
        Log.i("extract assets ", "testFunc: " + extract_dd_binary());
        for (int i = 0; i < 5; i++) {
            MacTools.randomMACAddress();
        }
    }

    public boolean extract_dd_binary() {
        try
        {
            InputStream stream = this.getAssets().open("dd");
            OutputStream output = new BufferedOutputStream(new FileOutputStream(this.getFilesDir() + "/dd"));

            byte data[] = new byte[1024];
            int count;

            while((count = stream.read(data)) != -1)
            {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            stream.close();

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        File testFile = new File(this.getFilesDir() + "/dd");
        Log.i(MainPage.TAG, "extract_dd_binary: " + testFile.exists());
        return testFile.exists();
    }
}
