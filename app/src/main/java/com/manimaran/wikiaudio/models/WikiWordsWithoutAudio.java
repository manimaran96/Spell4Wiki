package com.manimaran.wikiaudio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class WikiWordsWithoutAudio {

    @SerializedName("continue")
    @Expose
    private Offset offset;
    @SerializedName("query")
    @Expose
    private Query query;

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Offset {

        @SerializedName("cmcontinue")
        @Expose
        private String nextOffset;

        public String getNextOffset() {
            return nextOffset;
        }

        public void setNextOffset(String nextOffset) {
            this.nextOffset = nextOffset;
        }
    }

    public static class Query {

        @SerializedName("categorymembers")
        @Expose
        private List<WikiTitle> wikiTitleList = new ArrayList<>();

        public List<WikiTitle> getWikiTitleList() {
            return wikiTitleList;
        }

        public void setWikiTitleList(List<WikiTitle> wikiTitleList) {
            this.wikiTitleList = wikiTitleList;
        }
    }

    public static class WikiTitle {

        @SerializedName("pageid")
        @Expose
        private Integer pageId;
        @SerializedName("title")
        @Expose
        private String title;

        public Integer getPageId() {
            return pageId;
        }

        public void setPageId(Integer pageId) {
            this.pageId = pageId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

}







