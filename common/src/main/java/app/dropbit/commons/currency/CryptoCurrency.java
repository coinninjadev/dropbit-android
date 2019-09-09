package app.dropbit.commons.currency;


import android.content.Context;
import android.graphics.drawable.Drawable;


public interface CryptoCurrency extends Currency {
    String NO_SYMBOL_FORMAT = "#,##0.########";

    Drawable getSymbolDrawable(Context context);

    FiatCurrency toFiat(FiatCurrency conversionFiat);
}