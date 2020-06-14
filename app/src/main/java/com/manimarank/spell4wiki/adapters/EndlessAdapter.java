package com.manimarank.spell4wiki.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.manimarank.spell4wiki.BuildConfig;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.CommonWebActivity;
import com.manimarank.spell4wiki.utils.ShowCasePref;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.PrefManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.manimarank.spell4wiki.utils.constants.EnumTypeDef.ListMode;

public class EndlessAdapter extends ArrayAdapter<String> {

    private List<String> itemList;
    private List<String> wordsAlreadyHaveAudio = new ArrayList<>();
    private Activity activity;

    private PrefManager pref;
    @ListMode
    private int mode;

    private View rootView;

    public EndlessAdapter(Context ctx, List<String> itemList, @ListMode int mode) {
        super(ctx, R.layout.item_result_row, itemList);
        this.itemList = itemList;
        this.activity = (Activity) ctx;
        this.pref = new PrefManager(ctx);
        this.mode = mode;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public String getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).hashCode();
    }

    @NotNull
    @Override
    public View getView(final int position, View convertView, @NotNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null)
                rootView = inflater.inflate(R.layout.item_result_row, parent, false);
        } else
            rootView = convertView;

        String word = itemList.get(position);
        boolean isHaveAudio = wordsAlreadyHaveAudio.contains(word);
        rootView.setBackgroundColor(ContextCompat.getColor(activity, isHaveAudio ? R.color.record_have_audio : R.color.record_normal));

        // We should use class holder pattern
        TextView txtWord = rootView.findViewById(R.id.txtWord);
        txtWord.setText(word);

        txtWord.setOnClickListener(view -> {

            switch (mode) {
                case ListMode.SPELL_4_WIKI:
                case ListMode.SPELL_4_WORD_LIST:
                case ListMode.SPELL_4_WORD:
                    if(ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI))
                        return;
                    if (!isHaveAudio) {
                        if (GeneralUtils.checkPermissionGranted(activity)) {
                            GeneralUtils.showRecordDialog(activity, word, getLanguageCode());
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            getPermissionToRecordAudio();
                        }
                    } else
                        SnackBarUtils.INSTANCE.showLong(rootView, String.format(activity.getString(R.string.audio_file_already_exist), word));
                    break;
                case ListMode.WIKTIONARY:
                default:
                    openWiktionaryWebView(position);
                    break;
                case ListMode.TEMP:
                    break;
            }
        });

        View btnWiki = rootView.findViewById(R.id.btnWikiMeaning);
        btnWiki.setVisibility(View.VISIBLE);
        btnWiki.setOnClickListener(v -> openWiktionaryWebView(position));

        return rootView;
    }

    private String getLanguageCode() {
        switch (mode) {
            case ListMode.SPELL_4_WIKI:
                return pref.getLanguageCodeSpell4Wiki();
            case ListMode.SPELL_4_WORD_LIST:
                return pref.getLanguageCodeSpell4WordList();
            case ListMode.SPELL_4_WORD:
                return pref.getLanguageCodeSpell4Word();
            case ListMode.WIKTIONARY:
                return pref.getLanguageCodeWiktionary();
            case ListMode.TEMP:
            default:
                return null;
        }
    }

    private void openWiktionaryWebView(int position) {
        Intent intent = new Intent(activity, CommonWebActivity.class);
        String word = itemList.get(position);
        String langCode = getLanguageCode();
        if (langCode == null)
            langCode = AppConstants.DEFAULT_LANGUAGE_CODE;

        String url = String.format(Urls.WIKTIONARY_WEB, langCode, word);
        intent.putExtra(AppConstants.TITLE, word);
        intent.putExtra(AppConstants.URL, url);
        intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true);
        intent.putExtra(AppConstants.LANGUAGE_CODE, langCode);
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissionToRecordAudio() {
        if (GeneralUtils.permissionDenied(activity))
            showAppSettingsPageHint();
        activity.requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
                , AppConstants.RC_PERMISSIONS);
    }

    private void showAppSettingsPageHint() {
        if (rootView != null) {
            Snackbar.make(rootView, activity.getString(R.string.permission_required), Snackbar.LENGTH_LONG)
                    .setAction(activity.getString(R.string.go_settings), view -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                    })
                    .show();
        }
    }

    public void setWordsHaveAudioList(List<String> wordsAlreadyHaveAudio) {
        this.wordsAlreadyHaveAudio = wordsAlreadyHaveAudio;
    }

    public void addWordInWordsHaveAudioList(String wordsAlreadyHaveAudio) {
        this.wordsAlreadyHaveAudio.add(wordsAlreadyHaveAudio);
    }

}

