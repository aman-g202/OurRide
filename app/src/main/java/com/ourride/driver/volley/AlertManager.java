package com.ourride.driver.volley;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.ourride.driver.R;


public class AlertManager {

    public static void showToast(Context oContext, String message) {
        Toast.makeText(oContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void showAlertDialog(final Context oContext, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                oContext);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        message)
                .setCancelable(false)
                .setPositiveButton(oContext.
                                getResources().getString(R.string.Ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                                //((Activity)oContext).finish();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
