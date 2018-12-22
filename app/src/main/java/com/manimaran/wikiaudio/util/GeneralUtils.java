package com.manimaran.wikiaudio.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.acticity.LoginActivity;
import com.manimaran.wikiaudio.wiki.ServiceGenerator;

public class GeneralUtils {

    public GeneralUtils() {
    }

    public static Boolean checkPermissionGranted(Activity activity)
    {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void exitAlert(final Activity activity)
    {
        new AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Confirm to Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public static void showToast(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void logoutAlert(final Activity activity) {

        new AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_logout)
                .setTitle("Confirm to Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  Write to shared preferences
                        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(activity.getString(R.string.pref_is_logged_in), false);
                        editor.apply();
                        ServiceGenerator.clearCookies();
                        Intent intent = new Intent(activity.getApplicationContext(), LoginActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}
