package ru.olegsvs.mtkmacgen;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by user on 09.05.2017.
 */

public class WorkerTask extends AsyncTask<Void, Void, String>
{
    private Context ctx;
    private int operation;
    private ProgressDialog p;
    private String MAC;
    public AsyncResponse delegate = null;

    public interface AsyncResponse {
        void processFinish(String output);
    }
    /*
         Constructor
     */
    public WorkerTask(int operation, String MAC, Context ctx)
    {
        Log.i(MainPage.TAG, "WorkerTask: constructor with MAC : " + operation);
        this.operation=operation;
        this.ctx=ctx;
        this.MAC=MAC;
        this.p=new ProgressDialog(ctx);
    }

    public WorkerTask(int operation, Context ctx)
    {
        Log.i(MainPage.TAG, "WorkerTask: constructor without MAC: " + operation);
        this.operation=operation;
        this.ctx=ctx;
        this.p=new ProgressDialog(ctx);
    }

    /*
        Runs on the UI thread before doInBackground
     */
    @Override
    protected void onPreExecute() {
        switch(operation) {
            case MainPage.GETMAC:
                Log.i(MainPage.TAG, "onPreExecute: MainPage.GETMAC ");
                p.setMessage("Get MAC address");
                break;
            case MainPage.SETRANDOMMAC:
                Log.i(MainPage.TAG, "onPreExecute: MainPage.SETRANDOMMAC ");
                p.setMessage("Get RANDOM MAC address");
                break;
            case MainPage.SETUSERMAC:
                Log.i(MainPage.TAG, "onPreExecute: MainPage.SETUSERMAC " + this.MAC);
                p.setMessage("Set CUSTOM MAC address!");
                break;
        }
        super.onPreExecute();

        p.setIndeterminate(false);
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.setCancelable(false);
        p.show();
    }

    /*
         This method to perform a computation on a background thread.
     */
    @Override
    protected String doInBackground(Void... voids) {
        MacTools mTools = new MacTools();
        switch (operation) {
            case MainPage.GETMAC :
                try {
                    Log.i(MainPage.TAG, "doInBackground: getMAC operation");
                    return mTools.getMAC();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MainPage.SETRANDOMMAC :
                Log.i(MainPage.TAG, "doInBackground: randomMACAddress operation");
                this.MAC = mTools.randomMACAddress();
                try {
                    if (mTools.setUserMAC(this.MAC)) {
                        Log.i(MainPage.TAG, "doInBackground: SUCCESS mTools.SETRANDOMMAC(this.MAC)");
                    } else { Log.i(MainPage.TAG, "doInBackground: ERROR mTools.SETRANDOMMAC(this.MAC)"); return "ERROR"; }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    return mTools.getMAC();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MainPage.SETUSERMAC :
                Log.i(MainPage.TAG, "doInBackground: setUserMAC operation");
                try {
                    if (mTools.setUserMAC(this.MAC)) {
                        Log.i(MainPage.TAG, "doInBackground: SUCCESS mTools.setUserMAC(this.MAC)");
                    } else { Log.i(MainPage.TAG, "doInBackground: ERROR mTools.setUserMAC(this.MAC)"); return "ERROR"; }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    return mTools.getMAC();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    /*
        Runs on the UI thread after doInBackground
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        p.dismiss();
        if(result != null && result!="ERROR")
        {
            delegate.processFinish(result);
        } else if(result != null && result=="ERROR") {
            Toast.makeText(ctx,"Произошла ошибка!", Toast.LENGTH_LONG).show();
        }
    }

}

