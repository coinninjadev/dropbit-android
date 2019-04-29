package com.coinninja.coinkeeper.view.widget.phonenumber;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.text.PhoneNumberFormattingTextWatcher;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;
import java.util.Locale;

public class PhoneNumberInputView extends ConstraintLayout {

    PhoneNumberFormattingTextWatcher numberFormattingTextWatcher;
    private int textAppearanceStyle;
    private LinearLayout countryCodesSelctor;
    private List<CountryCodeLocale> countryCodeLocales;
    private Locale defaultLocale;
    private CountryCodeLocale currentCountryCodeLocale;
    private TextView flagView;
    private EditText phoneNumberInput;
    private PhoneNumberUtil phoneNumberUtil;
    private OnExamplePhoneNumberChangedObserver onExampleNumberChangeObserver;
    private String exampleFormattedNumber;
    private OnValidPhoneNumberObserver onValidPhoneNumberObserver;
    private OnInvalidPhoneNumberObserver onInvalidPhoneNumberObserver;
    private PhoneNumberFormattingTextWatcher.Callback phoneInputCallback = new PhoneNumberFormattingTextWatcher.Callback() {

        @Override
        public void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
            if (onValidPhoneNumberObserver != null)
                onValidPhoneNumberObserver.onValidPhoneNumber(phoneNumber);
        }

        @Override
        public void onPhoneNumberInValid(String text) {
            if (onInvalidPhoneNumberObserver != null)
                onInvalidPhoneNumberObserver.onInvalidPhoneNumber(text);
        }
    };
    private OnCountryCodeLocaleChangedObserver onCountryCodeLocaleChangedObserver;

    public PhoneNumberInputView(Context context) {
        super(context);
        init(context, null);
    }

    public PhoneNumberInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public PhoneNumberInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray styleAttributesArray = context.obtainStyledAttributes(attrs, R.styleable.PhoneNumberInputView);
        textAppearanceStyle = styleAttributesArray.getResourceId(R.styleable.PhoneNumberInputView_textAppearance,
                android.R.style.TextAppearance);
        render(context);
    }

    private void render(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.merge_component_phone_number_input_view, this, true);
        phoneNumberUtil = PhoneNumberUtil.getInstance();
        countryCodesSelctor = findViewById(R.id.phone_number_view_country_codes);
        flagView = findViewById(R.id.phone_number_view_flag);
        phoneNumberInput = findViewById(R.id.phone_number_view_number_input);
        setMinimumHeight(countryCodesSelctor.getHeight());
        setDefaults();
        numberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher(defaultLocale, phoneInputCallback);
        phoneNumberInput.addTextChangedListener(numberFormattingTextWatcher);
        setBackground(getResources().getDrawable(R.drawable.input_background));
    }

    private void setDefaults() {
        setDefaultLocale();
    }

    private void setDefaultLocale() {
        LocaleListCompat locales = ConfigurationCompat.getLocales(getContext().getResources().getConfiguration());
        defaultLocale = locales.get(0);
    }

    public void setCountryCodeLocals(List<CountryCodeLocale> countryCodeLocales) {
        this.countryCodeLocales = countryCodeLocales;
        invalidateSelectionFromList();
    }

    public Locale getCurrentLocale() {
        return defaultLocale;
    }

    public CountryCodeLocale getSelectedCountryCodeLocale() {
        return currentCountryCodeLocale;
    }

    private void invalidateSelectionFromList() {
        for (CountryCodeLocale countryCodeLocale : countryCodeLocales) {
            if (defaultLocale.getCountry().equals(countryCodeLocale.getLocale().getCountry())) {
                invalidateSelection(countryCodeLocale);
            }
        }
    }

    private void configurePhoneInput() {
        Phonenumber.PhoneNumber exampleNumber = phoneNumberUtil.getExampleNumberForType(
                currentCountryCodeLocale.getLocale().getCountry(),
                PhoneNumberUtil.PhoneNumberType.MOBILE);
        exampleFormattedNumber = phoneNumberUtil.format(exampleNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        setOnClickListener(v -> onClick());
        phoneNumberInput.setMinEms((exampleFormattedNumber.length() / 2));
        phoneNumberInput.setMaxEms((exampleFormattedNumber.length() / 2));
        countryCodesSelctor.setOnClickListener(v -> selectCountry());
        notifyOfExampleChanged();
    }

    private void notifyOfExampleChanged() {
        if (onExampleNumberChangeObserver != null)
            onExampleNumberChangeObserver.onExamplePhoneNumberChanged(exampleFormattedNumber);
    }

    private void selectCountry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.phone_number_input_widget_country_selection_title);
        builder.setAdapter(new PhoneNumberInputViewCountryListAdapter(getContext(), countryCodeLocales), this::onCountrySelected);
        AlertDialog dialog = builder.create();
        dialog.getContext().setTheme(R.style.WideDialogTheme);
        dialog.show();
    }

    private void onCountrySelected(DialogInterface dialogInterface, int which) {
        invalidateSelection(countryCodeLocales.get(which));
    }

    private void onInputClick() {
        phoneNumberInput.requestFocus();
        phoneNumberInput.setSelection(phoneNumberInput.getText().length());
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(phoneNumberInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void onClick() {
        onInputClick();
    }

    private void invalidateCountryCode() {
        phoneNumberInput.setText(currentCountryCodeLocale.getDisplayCountryCode());
    }

    private void invalidateSelection(CountryCodeLocale countryCodeLocale) {
        currentCountryCodeLocale = countryCodeLocale;
        flagView.setText(currentCountryCodeLocale.getEmoji());
        numberFormattingTextWatcher.updateLocale(currentCountryCodeLocale.getLocale());
        invalidateCountryCode();
        configurePhoneInput();

        if (getVisibility() == VISIBLE)
            onClick();

        if (onCountryCodeLocaleChangedObserver != null)
            onCountryCodeLocaleChangedObserver.onCountryCodeLocaleChanged(countryCodeLocale);
    }

    public void setOnExampleNumberChangeObserver(OnExamplePhoneNumberChangedObserver onExampleNumberChangeObserver) {
        this.onExampleNumberChangeObserver = onExampleNumberChangeObserver;
        if (null != exampleFormattedNumber && !"".equals(exampleFormattedNumber))
            onExampleNumberChangeObserver.onExamplePhoneNumberChanged(exampleFormattedNumber);
    }

    public void setOnValidPhoneNumberObserver(OnValidPhoneNumberObserver onValidPhoneNumberObserver) {
        this.onValidPhoneNumberObserver = onValidPhoneNumberObserver;
    }

    public void setOnInvalidPhoneNumberObserver(OnInvalidPhoneNumberObserver onInvalidPhoneNumberObserver) {
        this.onInvalidPhoneNumberObserver = onInvalidPhoneNumberObserver;
    }

    public String getText() {
        return phoneNumberInput.getText().toString();
    }

    public void setText(String text) {
        phoneNumberInput.setText(text);
    }

    public List<CountryCodeLocale> getCountryCodeLocales() {
        return countryCodeLocales;
    }

    public void setOnCountryCodeChangeObserver(OnCountryCodeLocaleChangedObserver onCountryCodeLocaleChangedObserver) {
        this.onCountryCodeLocaleChangedObserver = onCountryCodeLocaleChangedObserver;
    }

    public interface OnValidPhoneNumberObserver {
        void onValidPhoneNumber(Phonenumber.PhoneNumber phoneNumber);
    }

    public interface OnInvalidPhoneNumberObserver {
        void onInvalidPhoneNumber(String invalidEntry);
    }

    public interface OnExamplePhoneNumberChangedObserver {
        void onExamplePhoneNumberChanged(String exampleNumber);
    }

    public interface OnCountryCodeLocaleChangedObserver {
        void onCountryCodeLocaleChanged(CountryCodeLocale countryCodeLocale);
    }

}
