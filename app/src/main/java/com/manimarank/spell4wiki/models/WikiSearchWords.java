package com.manimarank.spell4wiki.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class WikiSearchWords {

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

        @SerializedName("sroffset")
        @Expose
        private Integer nextOffset;

        public Integer getNextOffset() {
            return nextOffset;
        }

        public void setNextOffset(Integer nextOffset) {
            this.nextOffset = nextOffset;
        }
    }

    public static class Query {

        @SerializedName("search")
        @Expose
        private List<WikiWord> wikiTitleList = new ArrayList<>();

        public List<WikiWord> getWikiTitleList() {
            return wikiTitleList;
        }

        public void setWikiTitleList(List<WikiWord> wikiTitleList) {
            this.wikiTitleList = wikiTitleList;
        }
    }

    public static class WikiWord {

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







