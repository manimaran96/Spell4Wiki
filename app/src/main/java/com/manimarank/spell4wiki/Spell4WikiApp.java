package com.manimarank.spell4wiki;

import android.app.Application;

public class Spell4WikiApp extends Application {

    private static Spell4WikiApp INSTANCE;

    public Spell4WikiApp() {
        INSTANCE = this;
    }

    public static Spell4WikiApp getInstance(){
        return INSTANCE;
    }
}
