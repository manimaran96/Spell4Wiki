package com.manimarank.spell4wiki.ui.spell4wiktionary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.manimarank.spell4wiki.BuildConfig;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.ui.webui.CommonWebActivity;
import com.manimarank.spell4wiki.ui.common.BaseViewHolder;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.pref.PrefManager;
import com.manimarank.spell4wiki.utils.pref.ShowCasePref;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.ToastUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import com.manimarank.spell4wiki.utils.constants.ListMode;
import com.manimarank.spell4wiki.utils.constants.ListMode.Companion.EnumListMode;


public class EndlessRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;


    private List<String> mItems;
    private Context mContext;
    private Activity mActivity;
    private View rootView;

    private List<String> wordsAlreadyHaveAudio = new ArrayList<>();
    private PrefManager pref;
    @EnumListMode
    private int mode;

    public EndlessRecyclerAdapter(Context context, List<String> wordItems, @EnumListMode int mode) {
        this.mContext = context;
        this.mActivity = (Activity) context;
        this.mItems = wordItems;
        this.pref = new PrefManager(mContext);
        this.mode = mode;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result_row, parent, false));
            case VIEW_TYPE_LOADING:
            default:
                return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_row, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == mItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public void addItems(List<String> postItems) {
        if (postItems.size() > 0) {
            mItems.addAll(postItems);
            notifyDataSetChanged();
        }
    }

    public void remove(String word) {
        try {
            int pos = mItems.indexOf(word);
            if (pos >= 0 && mItems.size() > pos) {
                mItems.remove(word);
                notifyItemRemoved(pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addLoading() {
        if (!isLoaderVisible && mItems.size() > 0) {
            isLoaderVisible = true;
            mItems.add("");
            try {
                new Handler().post(() -> notifyItemInserted(mItems.size() - 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeLoading() {
        int position = mItems.size() - 1;
        if (position >= 0 && isLoaderVisible) {
            isLoaderVisible = false;
            String item = getItem(position);
            if (item != null) {
                mItems.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    String getItem(int position) {
        return mItems.get(position);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void setWordsHaveAudioList(List<String> wordsAlreadyHaveAudio) {
        this.wordsAlreadyHaveAudio = wordsAlreadyHaveAudio;
    }

    public void addWordInWordsHaveAudioList(String wordsAlreadyHaveAudio) {
        this.wordsAlreadyHaveAudio.add(wordsAlreadyHaveAudio);
    }

    private void showNetworkProblem() {
        if (rootView != null)
            SnackBarUtils.INSTANCE.showLong(rootView, mActivity.getString(R.string.check_internet));
        else
            ToastUtils.INSTANCE.showLong(mActivity.getString(R.string.check_internet));
    }

    private void openWiktionaryWebView(String word) {
        if (NetworkUtils.INSTANCE.isConnected(mActivity)) {
            Intent intent = new Intent(mContext, CommonWebActivity.class);
            String langCode = getLanguageCode();
            if (langCode == null)
                langCode = AppConstants.DEFAULT_LANGUAGE_CODE;

            String url = String.format(Urls.WIKTIONARY_WEB, langCode, word);
            intent.putExtra(AppConstants.TITLE, word);
            intent.putExtra(AppConstants.URL, url);
            intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true);
            intent.putExtra(AppConstants.LANGUAGE_CODE, langCode);
            mActivity.startActivity(intent);
        } else {
            showNetworkProblem();
        }
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

    static class ProgressViewHolder extends BaseViewHolder {
        ProgressViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void clear() {
        }
    }

    class DataViewHolder extends BaseViewHolder {
        private TextView txtWord;
        private View btnWikMeaning;

        DataViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            txtWord = itemView.findViewById(R.id.txtWord);
            btnWikMeaning = itemView.findViewById(R.id.btnWikiMeaning);
        }

        protected void clear() {
        }

        public void onBind(int position) {
            super.onBind(position);
            String word = mItems.get(position);

            txtWord.setText(word);
            btnWikMeaning.setVisibility(View.VISIBLE);
            btnWikMeaning.setOnClickListener(v -> openWiktionaryWebView(word));

            boolean isHaveAudio = wordsAlreadyHaveAudio.contains(word);
            itemView.setBackgroundColor(ContextCompat.getColor(mContext, isHaveAudio ? R.color.record_have_audio : R.color.record_normal));

            txtWord.setOnClickListener(view -> {

                switch (mode) {
                    case ListMode.SPELL_4_WIKI:
                    case ListMode.SPELL_4_WORD_LIST:
                    case ListMode.SPELL_4_WORD:
                        if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI))
                            return;
                        if (!isHaveAudio) {
                            if (GeneralUtils.checkPermissionGranted(mActivity)) {
                                if (NetworkUtils.INSTANCE.isConnected(mActivity))
                                    GeneralUtils.showRecordDialog(mActivity, word, getLanguageCode());
                                else
                                    showNetworkProblem();
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                getPermissionToRecordAudio();
                            }
                        } else
                            SnackBarUtils.INSTANCE.showLong(itemView, String.format(mActivity.getString(R.string.audio_file_already_exist), word));
                        break;
                    case ListMode.WIKTIONARY:
                    default:
                        openWiktionaryWebView(word);
                        break;
                    case ListMode.TEMP:
                        break;
                }
            });
        }

        private void getPermissionToRecordAudio() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && GeneralUtils.permissionDenied(mActivity)) {
                showAppSettingsPageHint();
                mActivity.requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }
                        , AppConstants.RC_PERMISSIONS);
            }
        }

        private void showAppSettingsPageHint() {
            Snackbar.make(itemView, mActivity.getString(R.string.permission_required), Snackbar.LENGTH_LONG)
                    .setAction(mActivity.getString(R.string.go_settings), view -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(intent);
                    })
                    .show();
        }
    }
}