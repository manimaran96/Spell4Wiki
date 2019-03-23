package com.manimaran.wikiaudio.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UrlType {
  // ... type definitions
  // Describes when the annotation will be discarded
  @Retention(RetentionPolicy.SOURCE)
  // Enumerate valid values for this interface
  @IntDef({COMMONS, WIKTIONARY})
  // Create an interface for validating int types
  public @interface ItemTypeDef {}
  // Declare the constants
  public static final int COMMONS = 0;
  public static final int WIKTIONARY = 1;

  public final int itemType;

  // Mark the argument as restricted to these enumerated types
  public UrlType(@ItemTypeDef int itemType) {
    this.itemType = itemType;
  }
}