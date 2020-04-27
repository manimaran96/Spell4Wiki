package com.manimaran.wikiaudio.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.Spell4WikiApp;
import com.manimaran.wikiaudio.activities.CommonWebActivity;
import com.manimaran.wikiaudio.activities.RecordAudioActivity;
import com.manimaran.wikiaudio.constants.AppConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GeneralUtils {

    public static Boolean checkPermissionGranted(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static Boolean permissionDenied(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;
    }

    public static void exitAlert(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Confirm to Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public static void showLongToast(String msg) {
        Toast.makeText(Spell4WikiApp.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnack(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void writeAudioWordsToFile(String fileName, List<String> wordsList) {

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Spell4Wiki/WordsWithAudio");

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("TAG", "could not create the directories");
                }
            }

            final File myFile = new File(dir, fileName + ".txt");
            if (!myFile.exists())
                myFile.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(myFile, true));
            for (String str : wordsList) {
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void openUrl(Context context, String url, String title) {
        try {
            View view = ((Activity) context).findViewById(android.R.id.content);
            if (GeneralUtils.isNetworkConnected(context)) {
                if (url != null && !url.isEmpty()) {
                    Intent intent = new Intent(context, CommonWebActivity.class);
                    intent.putExtra(AppConstants.TITLE, title);
                    intent.putExtra(AppConstants.URL, url);
                    context.startActivity(intent);
                } else
                    GeneralUtils.showSnack(view, context.getString(R.string.check_url));
            } else
                GeneralUtils.showSnack(view, context.getString(R.string.check_internet));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openUrlInBrowser(Context context, String url) {
        try {
            if (url != null && !url.isEmpty()) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showRecordDialog(Activity activity, String word, String langCode) {
        Intent intent = new Intent(activity, RecordAudioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(AppConstants.WORD, word);
        intent.putExtra(AppConstants.LANGUAGE_CODE, langCode);
        activity.startActivityForResult(intent, AppConstants.RC_UPLOAD_DIALOG);
    }
}
