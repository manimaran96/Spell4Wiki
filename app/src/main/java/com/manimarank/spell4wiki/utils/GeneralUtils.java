package com.manimarank.spell4wiki.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.Spell4WikiApp;
import com.manimarank.spell4wiki.activities.CommonWebActivity;
import com.manimarank.spell4wiki.activities.RecordAudioActivity;
import com.manimarank.spell4wiki.constants.AppConstants;

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

    public static void showLongToast(String msg) {
        Toast.makeText(Spell4WikiApp.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnack(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
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
