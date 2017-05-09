package ru.olegsvs.mtkmacgen;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

public class MainPage extends AppCompatActivity implements WorkerTask.AsyncResponse {

    final static int GETMAC = 1;
    final static int SETRANDOMMAC = 2;
    final static int SETUSERMAC = 3;
    public static String TAG = "<<MTKMACGEN :>>";
    public static String dataPath;
    public Context context;
    EditText mMacEdit;
    public MacTools mTools;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        dataPath = new StringBuilder(String.valueOf(getBaseContext().getFilesDir().getAbsolutePath())).append("/WIFI").toString();
        context = getApplicationContext();

        mMacEdit = (EditText) findViewById(R.id.editMACaddress);

        mTools = new MacTools();
        final SupportChecker sc = new SupportChecker();

        try {
            if(sc.checkDeviceSupport()) {
                Log.i(TAG, "device support : " + sc.isSupported);
                testFunc(null);

            } else {
                notSupportedDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        registerAfterMacTextChangedCallback();
    }

    public void notSupportedDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                String msg = null;
                if(SupportChecker.SUSTATUS) msg = "Your CPU is " + SupportChecker.CPU + "\nSupported CPU : " + Arrays.asList(SupportChecker.CPU_SUPPORT_LIST).toString();
                else msg = "ROOT not granted!";

                new AlertDialog.Builder(MainPage.this)
                        .setTitle("Your Device not supported")
                        .setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Log.i(MainPage.TAG, "onClick: device_not_supported_exit");
                                System.exit(0);
                            }
                        }).show();
            }
        });
    }

    public void testFunc(View view) throws Exception {
//        mMacEdit.setText(mTools.getMAC());
        Log.i(MainPage.TAG, "testFunc: get MAC button clicked!");
        WorkerTask workTask = new WorkerTask(GETMAC , this);
        workTask.delegate = this;
        workTask.execute();
//        Log.i(MainPage.TAG, "testFunc: " + workTask.execute().toString());
    }

    public void testFunc3(View view) throws Exception {
        Log.i(MainPage.TAG, "testFunc3: set user MAC button clicked!");
        WorkerTask workTask = new WorkerTask(SETUSERMAC , mMacEdit.getText().toString() , this);
        workTask.delegate = this;
        workTask.execute();
    }

    public void testFunc2(View view) throws Exception {
        Log.i(MainPage.TAG, "testFunc2: set random MAC button clicked!");
        WorkerTask workTask = new WorkerTask(SETRANDOMMAC , this);
        workTask.delegate = this;
        workTask.execute();
    }

    /**
     * Registers TextWatcher for MAC EditText field. Automatically adds colons,
     * switches the MAC to upper case and handles the cursor position.
     */
    private void registerAfterMacTextChangedCallback() {
        mMacEdit.addTextChangedListener(new TextWatcher() {
            String mPreviousMac = null;

            /* (non-Javadoc)
             * Does nothing.
             * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
             */
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            /* (non-Javadoc)
             * Does nothing.
             * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
             */
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            /* (non-Javadoc)
             * Formats the MAC address and handles the cursor position.
             * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
             */
            @SuppressLint("DefaultLocale")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String enteredMac = mMacEdit.getText().toString().toUpperCase();
                String cleanMac = clearNonMacCharacters(enteredMac);
                String formattedMac = formatMacAddress(cleanMac);

                int selectionStart = mMacEdit.getSelectionStart();
                formattedMac = handleColonDeletion(enteredMac, formattedMac, selectionStart);
                int lengthDiff = formattedMac.length() - enteredMac.length();

                setMacEdit(cleanMac, formattedMac, selectionStart, lengthDiff);
            }

            /**
             * Strips all characters from a string except A-F and 0-9.
             * @param mac       User input string.
             * @return          String containing MAC-allowed characters.
             */
            private String clearNonMacCharacters(String mac) {
                return mac.toString().replaceAll("[^A-Fa-f0-9]", "");
            }

            /**
             * Adds a colon character to an unformatted MAC address after
             * every second character (strips full MAC trailing colon)
             * @param cleanMac      Unformatted MAC address.
             * @return              Properly formatted MAC address.
             */
            private String formatMacAddress(String cleanMac) {
                int grouppedCharacters = 0;
                String formattedMac = "";

                for (int i = 0; i < cleanMac.length(); ++i) {
                    formattedMac += cleanMac.charAt(i);
                    ++grouppedCharacters;

                    if (grouppedCharacters == 2) {
                        formattedMac += ":";
                        grouppedCharacters = 0;
                    }
                }

                // Removes trailing colon for complete MAC address
                if (cleanMac.length() == 12)
                    formattedMac = formattedMac.substring(0, formattedMac.length() - 1);

                return formattedMac;
            }

            /**
             * Upon users colon deletion, deletes MAC character preceding deleted colon as well.
             * @param enteredMac            User input MAC.
             * @param formattedMac          Formatted MAC address.
             * @param selectionStart        MAC EditText field cursor position.
             * @return                      Formatted MAC address.
             */
            private String handleColonDeletion(String enteredMac, String formattedMac, int selectionStart) {
                if (mPreviousMac != null && mPreviousMac.length() > 1) {
                    int previousColonCount = colonCount(mPreviousMac);
                    int currentColonCount = colonCount(enteredMac);

                    if (currentColonCount < previousColonCount) {
                        formattedMac = formattedMac.substring(0, selectionStart - 1) + formattedMac.substring(selectionStart);
                        String cleanMac = clearNonMacCharacters(formattedMac);
                        formattedMac = formatMacAddress(cleanMac);
                    }
                }
                return formattedMac;
            }

            /**
             * Gets MAC address current colon count.
             * @param formattedMac      Formatted MAC address.
             * @return                  Current number of colons in MAC address.
             */
            private int colonCount(String formattedMac) {
                return formattedMac.replaceAll("[^:]", "").length();
            }

            /**
             * Removes TextChange listener, sets MAC EditText field value,
             * sets new cursor position and re-initiates the listener.
             * @param cleanMac          Clean MAC address.
             * @param formattedMac      Formatted MAC address.
             * @param selectionStart    MAC EditText field cursor position.
             * @param lengthDiff        Formatted/Entered MAC number of characters difference.
             */
            private void setMacEdit(String cleanMac, String formattedMac, int selectionStart, int lengthDiff) {
                mMacEdit.removeTextChangedListener(this);
                if (cleanMac.length() <= 12) {
                    mMacEdit.setText(formattedMac);
                    mMacEdit.setSelection(selectionStart + lengthDiff);
                    mPreviousMac = formattedMac;
                } else {
                    mMacEdit.setText(mPreviousMac);
                    mMacEdit.setSelection(mPreviousMac.length());
                }
                mMacEdit.addTextChangedListener(this);
            }
        });
    }

    @Override
    public void processFinish(String output){
        if (output != null) {
            mMacEdit.setText(output);
        }

    }
}
