package com.coinninja.coinkeeper.view.widget.phonenumber;

import com.coinninja.coinkeeper.util.java.LocaleUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CountryCodeLocaleGeneratorTest {

    private Locale gb = new Locale("en", "GB");
    private Locale us = new Locale("en", "US");
    private Locale[] availableLocales = new Locale[]{gb, us};

    @Mock
    private LocaleUtil localeUtil;

    @Mock
    private PhoneNumberUtil phoneNumberUtil;

    @InjectMocks
    private CountryCodeLocaleGenerator countryCodeLocaleGenerator;

    @Before
    public void setUp() {
        when(localeUtil.getAvailableLocales()).thenReturn(availableLocales);
        when(phoneNumberUtil.getCountryCodeForRegion("US")).thenReturn(1);
        when(phoneNumberUtil.getCountryCodeForRegion("GB")).thenReturn(44);
    }

    @Test
    public void generates_locales_mapped_to_country_codes() {
        List<CountryCodeLocale> countryCodeLocales = countryCodeLocaleGenerator.generate();

        assertThat(countryCodeLocales.get(0).getDisplayCountryCode(), equalTo("+44"));
        assertThat(countryCodeLocales.get(0).getDisplayName(), equalTo("United Kingdom"));

        assertThat(countryCodeLocales.get(1).getDisplayCountryCode(), equalTo("+1"));
        assertThat(countryCodeLocales.get(1).getDisplayName(), equalTo("United States"));
    }

    @Test
    public void removes_unknown_countries() {
        Locale unknown = new Locale("en", "");
        availableLocales = new Locale[]{gb, us, unknown};
        when(localeUtil.getAvailableLocales()).thenReturn(availableLocales);

        List<CountryCodeLocale> countryCodeLocales = countryCodeLocaleGenerator.generate();

        assertThat(countryCodeLocales.size(), equalTo(2));
    }

    @Test
    public void removes_undefined_country_codes() {
        Locale unknown = new Locale("en", "CA");
        availableLocales = new Locale[]{gb, us, unknown};
        when(localeUtil.getAvailableLocales()).thenReturn(availableLocales);
        when(phoneNumberUtil.getCountryCodeForRegion("CA")).thenReturn(0);

        List<CountryCodeLocale> countryCodeLocales = countryCodeLocaleGenerator.generate();

        assertThat(countryCodeLocales.size(), equalTo(2));
    }

    private static final String locals = "[<>, <ar_AE>, <ar_JO>, <ar_SY>, <hr_HR>, <fr_BE>, <es_PA>, <mt_MT>, " +
            "<es_VE>, <bg>, <zh_TW>, <it>, <ko>, <uk>, <lv>, <da_DK>, <es_PR>, <vi_VN>, <en_US>, " +
            "<sr_ME>, <sv_SE>, <es_BO>, <en_SG>, <ar_BH>, <pt>, <ar_SA>, <sk>, <ar_YE>, <hi_IN>, " +
            "<ga>, <en_MT>, <fi_FI>, <et>, <sv>, <cs>, <sr_BA_#Latn>, <el>, <uk_UA>, <hu>, <fr_CH>, " +
            "<in>, <es_AR>, <ar_EG>, <ja_JP_JP_#u-ca-japanese>, <es_SV>, <pt_BR>, <be>, <is_IS>, " +
            "<cs_CZ>, <es>, <pl_PL>, <tr>, <ca_ES>, <sr_CS>, <ms_MY>, <hr>, <lt>, <es_ES>, <es_CO>, " +
            "<bg_BG>, <sq>, <fr>, <ja>, <sr_BA>, <is>, <es_PY>, <de>, <es_EC>, <es_US>, <ar_SD>, <en>, " +
            "<ro_RO>, <en_PH>, <ca>, <ar_TN>, <sr_ME_#Latn>, <es_GT>, <sl>, <ko_KR>, <el_CY>, <es_MX>, " +
            "<ru_RU>, <es_HN>, <zh_HK>, <no_NO_NY>, <hu_HU>, <th_TH>, <ar_IQ>, <es_CL>, <fi>, <ar_MA>, " +
            "<ga_IE>, <mk>, <tr_TR>, <et_EE>, <ar_QA>, <sr__#Latn>, <pt_PT>, <fr_LU>, <ar_OM>, <th>, " +
            "<sq_AL>, <es_DO>, <es_CU>, <ar>, <ru>, <en_NZ>, <sr_RS>, <de_CH>, <es_UY>, <ms>, <el_GR>, " +
            "<iw_IL>, <en_ZA>, <th_TH_TH_#u-nu-thai>, <hi>, <fr_FR>, <de_AT>, <nl>, <no_NO>, <en_AU>, " +
            "<vi>, <nl_NL>, <fr_CA>, <lv_LV>, <de_LU>, <es_CR>, <ar_KW>, <sr>, <ar_LY>, <mt>, <it_CH>, " +
            "<da>, <de_DE>, <ar_DZ>, <sk_SK>, <lt_LT>, <it_IT>, <en_IE>, <zh_SG>, <ro>, <en_CA>, " +
            "<nl_BE>, <no>, <pl>, <zh_CN>, <ja_JP>, <de_GR>, <sr_RS_#Latn>, <iw>, <en_IN>, <ar_LB>, " +
            "<es_NI>, <zh>, <mk_MK>, <be_BY>, <sl_SI>, <es_PE>, <in_ID>, <en_GB>]";

    private static final String country_codes = "[{AE=971}, {JO=962}, {SY=963}, {HR=385}, {BE=32}, " +
            "{PA=507}, {MT=356}, {VE=58}, {TW=886}, {DK=45}, {PR=1}, {VN=84}, {US=1}, {ME=382}, " +
            "{SE=46}, {BO=591}, {SG=65}, {BH=973}, {SA=966}, {YE=967}, {IN=91}, {MT=356}, {FI=358}, " +
            "{BA=387}, {UA=380}, {CH=41}, {AR=54}, {EG=20}, {JP=81}, {SV=503}, {BR=55}, {IS=354}, " +
            "{CZ=420}, {PL=48}, {ES=34}, {MY=60}, {ES=34}, {CO=57}, {BG=359}, {BA=387}, {PY=595}, " +
            "{EC=593}, {US=1}, {SD=249}, {RO=40}, {PH=63}, {TN=216}, {ME=382}, {GT=502}, {KR=82}, " +
            "{CY=357}, {MX=52}, {RU=7}, {HN=504}, {HK=852}, {NO=47}, {HU=36}, {TH=66}, {IQ=964}, " +
            "{CL=56}, {MA=212}, {IE=353}, {TR=90}, {EE=372}, {QA=974}, {PT=351}, {LU=352}, {OM=968}, " +
            "{AL=355}, {DO=1}, {CU=53}, {NZ=64}, {RS=381}, {CH=41}, {UY=598}, {GR=30}, {IL=972}, " +
            "{ZA=27}, {TH=66}, {FR=33}, {AT=43}, {NO=47}, {AU=61}, {NL=31}, {CA=1}, {LV=371}, " +
            "{LU=352}, {CR=506}, {KW=965}, {LY=218}, {CH=41}, {DE=49}, {DZ=213}, {SK=421}, {LT=370}, " +
            "{IT=39}, {IE=353}, {SG=65}, {CA=1}, {BE=32}, {CN=86}, {JP=81}, {GR=30}, {RS=381}, " +
            "{IN=91}, {LB=961}, {NI=505}, {MK=389}, {BY=375}, {SI=386}, {PE=51}, {ID=62}, {GB=44}]";

    private String stringify(Locale[] availableLocales) {
        String wrapper = "[%s]";
        StringBuilder localeStr = new StringBuilder();
        Locale locale;

        for (int i = 0; i < availableLocales.length; i++) {
            locale = availableLocales[i];
            localeStr.append(String.format("<%s>", locale.toString()));

            if (i != availableLocales.length - 1) {
                localeStr.append(", ");
            }
        }

        return String.format(wrapper, localeStr.toString());
    }

    @Test
    public void locales() {
        Locale[] availableLocales = Locale.getAvailableLocales();
        Locale locale = availableLocales[1];
        assertThat(stringify(availableLocales), IsEqual.equalTo(locals));
        assertThat(locale.toString(), IsEqual.equalTo("ar_AE"));
        assertThat(locale.getCountry(), IsEqual.equalTo("AE"));
        assertThat(locale.getDisplayCountry(), IsEqual.equalTo("United Arab Emirates"));
        assertThat(locale.getDisplayLanguage(), IsEqual.equalTo("Arabic"));
        assertThat(locale.getISO3Language(), IsEqual.equalTo("ara"));
        assertThat(locale.getLanguage(), IsEqual.equalTo("ar"));
        assertThat(locale.getDisplayName(), IsEqual.equalTo("Arabic (United Arab Emirates)"));
        assertThat(locale.getDisplayVariant(), IsEqual.equalTo(""));
        assertThat(locale.getDisplayScript(), IsEqual.equalTo(""));
    }

    @Test
    public void countryCodes() {
        Locale[] availableLocales = Locale.getAvailableLocales();
        assertThat(PhoneNumberUtil.getInstance()
                        .getCountryCodeForRegion(availableLocales[availableLocales.length - 1].getCountry()),
                IsEqual.equalTo(44));

        assertThat(generateRegionToCountryCode().toString(), IsEqual.equalTo(country_codes));
    }

    private List<Map<String, Integer>> generateRegionToCountryCode() {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        List<Map<String, Integer>> countryCodeList = new ArrayList<>();
        Locale[] availableLocales = Locale.getAvailableLocales();
        Locale locale;
        Map<String, Integer> map;
        int cc = 0;
        String country = "";

        for (int i = 0; i < availableLocales.length; i++) {
            locale = availableLocales[i];
            if (locale.getCountry().isEmpty()) {
                continue;
            }

            country = locale.getCountry();
            cc = phoneNumberUtil.getCountryCodeForRegion(locale.getCountry());

            if (cc == 0) {
                continue;
            }

            System.out.println(String.format("%s : %s", country, cc));
            map = new HashMap<>();
            map.put(country, cc);
            countryCodeList.add(map);
        }

        return countryCodeList;
    }
}