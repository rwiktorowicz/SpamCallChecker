package com.rwiktorowicz.spamcallchecker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.provider.CallLog;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getIntent().getDataString() != null) {
            try {
                String phonenumber = URLDecoder.decode(getIntent().getDataString(),"UTF-8");
                phonenumber = phonenumber.replace("tel:","");
                phonenumber = removeNonNumericCharacters(phonenumber);
                phonenumber = "1" + phonenumber;

                EditText phonetextbox = (EditText) findViewById(R.id.phoneTextBox);
                phonetextbox.setText(phonenumber);
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(this,"There was an error processing the phone number.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void searchButtonClick(View view) {
        EditText phonetextbox = (EditText) findViewById(R.id.phoneTextBox);
        String phonenumber = removeNonNumericCharacters(phonetextbox.getText().toString());

        if (phonenumber.length() != 11) {
            Toast.makeText(this,"The phone number is not valid. Must be 11 digits including a prefix of 1!",Toast.LENGTH_SHORT).show();
        }
        else if (phonenumber.length() == 11 && phonenumber.startsWith("1") == false) {
            Toast.makeText(this,"The phone number must start with 1!",Toast.LENGTH_SHORT).show();
        }
        else {
            Intent openurl = new Intent(Intent.ACTION_VIEW, Uri.parse(getURL(phonenumber)));
            startActivity(openurl);
        }
    }

    public void recentCallsButtonClick(View view) {
        ArrayList<String> recentcalls = new ArrayList<String>();

        /* Grab last 20 calls and add them to an array list to pass into recent call dialog */
        Cursor callcursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int phonenumbercolumnindex = callcursor.getColumnIndex(CallLog.Calls.NUMBER);
        while (callcursor.moveToNext()) {
            String phonenumber = getFormattedPhoneNumber(callcursor.getString(phonenumbercolumnindex),true);
            recentcalls.add(phonenumber);

            if (recentcalls.size() == 20) {
                break;
            }
        }
        callcursor.close();
        final CharSequence[] recentcallssequence = recentcalls.toArray(new CharSequence[recentcalls.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose a phone number:")
                .setItems(recentcallssequence, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText phonetextbox = (EditText) findViewById(R.id.phoneTextBox);
                        phonetextbox.setText(removeNonNumericCharacters(recentcallssequence[i].toString()));
                    }
                });
        AlertDialog recentcalldialog = builder.create();
        recentcalldialog.show();
    }

    /* Returns properly formatted url, depending on service choice */
    private String getURL(String phonenumber) {
        String url = "";

        RadioGroup servicegroup = (RadioGroup) findViewById(R.id.serviceRadios);
        int selectedid = servicegroup.getCheckedRadioButtonId();
        RadioButton selectedservice = (RadioButton) findViewById(selectedid);

        switch (selectedservice.getText().toString()) {
            case "800Notes.com":
                phonenumber = getFormattedPhoneNumber(phonenumber,false);
                url = "http://www.800notes.com/Phone.aspx/" + phonenumber;
                break;
            case "Mr. Number":
                phonenumber = getFormattedPhoneNumber(phonenumber,false);
                url = "http://www.mrnumber.com/" + phonenumber;
                break;
            default:
                phonenumber = getFormattedPhoneNumber(phonenumber,false);
                url = "http://www.800notes.com/Phone.aspx/" + phonenumber;
                break;
        }
        return url;
    }

    /* Returns a phone number in either #-###-###-#### or #-(###)-###-#### format.
     * Returns null if phone number is invalid. */
    private String getFormattedPhoneNumber(String phonenumber, boolean includeparenthesis) {
        String formattedphonenumber;
        phonenumber = removeNonNumericCharacters(phonenumber);

        /*
        If the phone number's length is not 10 or 11, it is invalid. Return null.
        otherwise if it has a length of 10, it is missing the 1 prefix, so add it for proper formatting.
        */
        if (phonenumber.length() < 10 || phonenumber.length() > 11) {
            return null;
        }
        else if (phonenumber.length() == 10) {
            phonenumber = "1" + phonenumber;
        }

        String[] phonearray = {phonenumber.substring(1, 4), phonenumber.substring(4, 7),
                 phonenumber.substring(7)};

        if (includeparenthesis) {
            formattedphonenumber = String.format("1-(%s)-%s-%s", phonearray[0], phonearray[1], phonearray[2]);
        } else {
            formattedphonenumber = String.format("1-%s-%s-%s", phonearray[0], phonearray[1], phonearray[2]);
        }

        return formattedphonenumber;
    }

    /* Strips input of all non-numeric characters.  If null, return null */
    private String removeNonNumericCharacters(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\D+","");
    }

}
