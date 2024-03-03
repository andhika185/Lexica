package com.serwylo.lexica.view;

import android.graphics.Typeface;

/**
 * Avoid problems on older devices by caching required fonts.
 * See https://stackoverflow.com/questions/2376250/custom-fonts-and-xml-layouts-android#comment11263047_7197867
 */
public class Fonts {

    private static Fonts instance;

    public static Fonts get() {
        if (instance == null) {
            instance = new Fonts();
        }

        return instance;
    }

    private final Typeface sansSerifCondensed;
    private final Typeface sansSerifBold;

    private Fonts() {
        this.sansSerifCondensed = Typeface.create("sans-serif-light", Typeface.NORMAL);
        this.sansSerifBold = Typeface.create("sans-serif-bold", Typeface.BOLD);
    }

    public Typeface getSansSerifCondensed() {
        return sansSerifCondensed;
    }

    public Typeface getSansSerifBold() {
        return sansSerifBold;
    }

}
