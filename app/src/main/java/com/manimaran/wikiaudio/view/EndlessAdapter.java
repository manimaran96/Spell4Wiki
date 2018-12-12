package com.manimaran.wikiaudio.view;

/*
 * Copyright (C) 2012 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.WebWikiActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EndlessAdapter extends ArrayAdapter<String> {

    private List<String> itemList;
    private Context ctx;
    private int layoutId;

    public EndlessAdapter(Context ctx, List<String> itemList, int layoutId) {
        super(ctx, layoutId, itemList);
        this.itemList = itemList;
        this.ctx = ctx;
        this.layoutId = layoutId;
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View result = convertView;

        if (result == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            result = inflater.inflate(layoutId, parent, false);
        }

        // We should use class holder pattern
        TextView tv = (TextView) result.findViewById(R.id.txt1);
        tv.setText(itemList.get(position));

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Activity activity1 = (Activity) ctx;
                Intent intent = new Intent(ctx, WebWikiActivity.class);
                intent.putExtra("word", itemList.get(position));
                activity1.startActivity(intent);
                /*MediaWikiClient mediaWikiClient = ServiceGenerator.createService(MediaWikiClient.class, ctx);
                //Call<ResponseBody> call = mediaWikiClient.search("query", "search", query, nextOffset, true);
                //Call<ResponseBody> call = mediaWikiClient.fetchRecords(query, nextOffset);
                Call<ResponseBody> call = mediaWikiClient.fetchWordInfo(itemList.get(position));

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseStr = response.body().string();
                                try {
                                    processResult(responseStr);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });*/
            }
        });

        return result;

    }

    private void processResult(String responseStr) throws JSONException {
        JSONObject reader = new JSONObject(responseStr);

        try {
            Toast.makeText(ctx, responseStr, Toast.LENGTH_SHORT).show();
            ArrayList<String> titleList = new ArrayList<>();
            Log.w("TAG", "WIKI api " + reader.toString());
            JSONArray searchResults = reader.getJSONObject("query").optJSONArray("categorymembers");
            for (int ii = 0; ii < searchResults.length(); ii++) {
                titleList.add(
                        searchResults.getJSONObject(ii).getString("title")
                        //+ " --> " + searchResults.getJSONObject(ii).getString("pageid")
                );
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}