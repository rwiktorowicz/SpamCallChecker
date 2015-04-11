package com.rwiktorowicz.spamcallchecker;

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

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void searchButtonClick(View view) {
        EditText phoneTextBox = (EditText) findViewById(R.id.phoneTextBox);
        String phonenumber = phoneTextBox.getText().toString().replaceAll("\\D+","");

        if (phonenumber.length() != 10) {
            Toast.makeText(this,"The phone number is not valid. The length must be 10 with no special characters!",Toast.LENGTH_SHORT).show();
        }
        else {
            Intent openurl = new Intent(Intent.ACTION_VIEW, Uri.parse(getURL(phonenumber)));
            startActivity(openurl);
        }
    }

    public void recentCallsButtonClick(View view) {
        ArrayList<String> recentcalls = new ArrayList<String>();

        Cursor callCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,null,null,null,null);
        int phonenumberindex = callCursor.getColumnIndex(CallLog.Calls.NUMBER);
        while (callCursor.moveToNext()) {
            String phonenumber = callCursor.getString(phonenumberindex);
            recentcalls.add(phonenumber);
        }

        Intent showcalls = new Intent(this,RecentCallActivity.class);
        showcalls.putStringArrayListExtra("RecentCallList",recentcalls);
        startActivity(showcalls);
    }

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

    private String getFormattedPhoneNumber(String phonenumber, boolean includeparenthesis) {
        String formattedPhoneNumber;
        phonenumber = phonenumber.replaceAll("\\D+","");

        if (phonenumber.length() != 10) {
            return null;
        }
        String[] phonearray = {phonenumber.substring(0,3), phonenumber.substring(3,6),
                phonenumber.substring(6)};

        if (includeparenthesis) {
            formattedPhoneNumber = String.format("1-(%s)-%s-%s",phonearray[0],phonearray[1],phonearray[2]);
        }
        else {
            formattedPhoneNumber = String.format("1-%s-%s-%s",phonearray[0],phonearray[1],phonearray[2]);
        }

        return formattedPhoneNumber;
    }

}
