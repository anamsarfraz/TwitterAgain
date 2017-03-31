package com.codepath.apps.twitter.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.NavigableMap;
import java.util.TreeMap;


public class FormatUtil {

    final static StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
    final static StyleSpan nss = new StyleSpan(Typeface.NORMAL); //Span to make text normal


    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        TreeMap.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static SpannableStringBuilder buildSpan(String countStr, String suffix) {
        String formatString = String.format("%s %s", countStr, suffix);

        SpannableStringBuilder sb = new SpannableStringBuilder(formatString);
        int countLength = countStr.length();

        // make count characters Bold
        sb.setSpan(bss, 0, countLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(nss, countLength, formatString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return sb;
    }
}
