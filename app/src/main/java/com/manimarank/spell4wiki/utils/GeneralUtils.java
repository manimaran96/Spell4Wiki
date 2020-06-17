package com.manimarank.spell4wiki.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.CommonWebActivity;
import com.manimarank.spell4wiki.activities.CommonWebContentActivity;
import com.manimarank.spell4wiki.activities.RecordAudioActivity;
import com.manimarank.spell4wiki.activities.Spell4Wiktionary;
import com.manimarank.spell4wiki.activities.Spell4WordActivity;
import com.manimarank.spell4wiki.activities.Spell4WordListActivity;
import com.manimarank.spell4wiki.apis.WikimediaCommonsUtils;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.databases.entities.WordsHaveAudio;
import com.manimarank.spell4wiki.utils.constants.AppConstants;

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


    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            if (inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openUrl(Context context, String url, String title) {
        try {
            if (NetworkUtils.INSTANCE.isConnected(context)) {
                if (url != null && !url.isEmpty()) {
                    Intent intent = new Intent(context, CommonWebActivity.class);
                    intent.putExtra(AppConstants.TITLE, title);
                    intent.putExtra(AppConstants.URL, url);
                    context.startActivity(intent);
                } else
                    ToastUtils.INSTANCE.showLong(context.getString(R.string.check_url));
            } else
                ToastUtils.INSTANCE.showLong(context.getString(R.string.check_internet));

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
        if (NetworkUtils.INSTANCE.isConnected(activity)) {
            WikimediaCommonsUtils.INSTANCE.checkFileAvailability(activity, word, langCode, fileExist -> {
                if (!activity.isDestroyed() && !activity.isFinishing()) {
                    if (fileExist) {
                        WordsHaveAudioDao wordsHaveAudioDao = DBHelper.getInstance(activity).getAppDatabase().getWordsHaveAudioDao();
                        wordsHaveAudioDao.insert(new WordsHaveAudio(word, langCode));
                        if (activity instanceof Spell4Wiktionary)
                            ((Spell4Wiktionary) activity).updateList(word);
                        else if (activity instanceof Spell4WordListActivity)
                            ((Spell4WordListActivity) activity).updateList(word);
                        else if (activity instanceof Spell4WordActivity)
                            ((Spell4WordActivity) activity).updateList(word);
                        else if (activity instanceof CommonWebActivity)
                            ((CommonWebActivity) activity).updateList(word);

                        ToastUtils.INSTANCE.showLong(String.format(activity.getString(R.string.audio_file_already_exist), word));
                    } else {
                        Intent intent = new Intent(activity, RecordAudioActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(AppConstants.WORD, word);
                        intent.putExtra(AppConstants.LANGUAGE_CODE, langCode);
                        activity.startActivityForResult(intent, AppConstants.RC_UPLOAD_DIALOG);
                    }
                }
            });
        } else
            ToastUtils.INSTANCE.showLong(activity.getString(R.string.check_internet));
    }

    public static void openMarkdownUrl(Activity activity, String url, String title) {
        Intent intent = new Intent(activity, CommonWebContentActivity.class);
        intent.putExtra(AppConstants.TITLE, title);
        intent.putExtra(AppConstants.URL, url);
        activity.startActivityForResult(intent, AppConstants.RC_UPLOAD_DIALOG);
    }
}
