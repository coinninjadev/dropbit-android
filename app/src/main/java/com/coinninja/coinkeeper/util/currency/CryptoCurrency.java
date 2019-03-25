package com.coinninja.coinkeeper.util.currency;


import android.content.Context;
import android.graphics.drawable.Drawable;


public interface CryptoCurrency {
    String NO_SYMBOL_FORMAT = "#,##0.########";

    Drawable getSymbolDrawable(Context context);
}