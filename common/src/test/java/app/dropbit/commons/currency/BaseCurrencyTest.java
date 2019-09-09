package app.dropbit.commons.currency;

import android.os.Parcel;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BaseCurrencyTest {

    String initialValue = "6,840.018265513351";

    @Test
    public void bigDecimalZeroCheck_WithOut_initialValue() throws Exception {
        AnyCurrency anyCurrency = new AnyCurrency();

        boolean isZero = anyCurrency.isZero();

        assertThat(isZero, equalTo(true));
    }

    @Test
    public void bigDecimalZeroCheck_With_initialValue() throws Exception {
        AnyCurrency anyCurrency = new AnyCurrency(initialValue);

        boolean isZero = anyCurrency.isZero();

        assertThat(isZero, equalTo(false));
    }

    class AnyCurrency extends BaseCurrency {
        String currencyFormat = "#,##0.00";

        public AnyCurrency() {
            super();
        }

        public AnyCurrency(String initialValue) {
            super(initialValue);
        }

        @Override
        public void setCurrencyFormat(String format) {
            currencyFormat = format;
        }

        @Override
        public int getMaxNumSubValues() {
            return 0;
        }

        @Override
        public int getMaxNumWholeValues() {
            return 0;
        }

        @Override
        public String getIncrementalFormat() {
            return currencyFormat;
        }

        @Override
        public boolean update(String formattedValue) {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean validate(String amount) {
            return false;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }

        @Override
        protected long getMaxLongValue() {
            return 0;
        }
    }
}