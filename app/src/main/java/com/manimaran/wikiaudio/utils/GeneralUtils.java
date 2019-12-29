package com.manimaran.wikiaudio.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.activity.CommonWebActivity;
import com.manimaran.wikiaudio.constant.Constants;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.fragment.RecordAudioDialogFragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeneralUtils {

    public GeneralUtils() {
    }

    public static Boolean checkPermissionGranted(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnack(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void logoutAlert(final Activity activity) {

        new AlertDialog.Builder(activity)
                //.setIcon(R.drawable.ic_logout)
                .setTitle("Confirm to Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Logout user
                        new PrefManager(activity).logoutUser();
                    }

                })
                .setNegativeButton("No", null)
                .show();
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

    public static List<String> getWordsWithoutAudioListOnly(String fileName, ArrayList<String> wordsList) {

        final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Spell4Wiki/WordsWithAudio");
        //Get the text file
        File file = new File(dir, fileName + ".txt");

        //Read text from file
        List<String> tempList = new ArrayList<>();

        if(file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    tempList.add(line);
                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
                e.printStackTrace();
            }
        }
        wordsList.removeAll(tempList);
        return wordsList;
    }

    public Integer getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public Integer getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void openUrl(Context context, String url, int urlType, String title){
        try {
            if (url != null && !url.isEmpty())
            {
                switch (urlType){
                    case UrlType.INTERNAL :
                        Intent intent  = new Intent(context, CommonWebActivity.class);
                        intent.putExtra(Constants.TITLE, title);
                        intent.putExtra(Constants.URL, url);
                        context.startActivity(intent);
                        break;
                    case UrlType.EXTERNAL:
                    default:
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void openUrlInBrowser(Context context, String url){
        try {
            if (url != null && !url.isEmpty())
            {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void showRecordDialog(Activity activity, String word) {
        RecordAudioDialogFragment dialogFragment = RecordAudioDialogFragment.newInstance(word);
        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        if(!dialogFragment.isVisible())
            dialogFragment.show(ft, "dialog");
    }
}
