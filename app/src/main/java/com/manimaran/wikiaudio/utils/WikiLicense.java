package com.manimaran.wikiaudio.utils;

import androidx.annotation.NonNull;

import com.manimaran.wikiaudio.R;

import static com.manimaran.wikiaudio.R.string.license_name_cc_by_sa_four;


public class WikiLicense {

    /**
     * Generates licence name with given ID
     *
     * @param licensePref License ID
     * @return Name of license
     */
    public static int licenseNameId(String licensePref) {
        switch (licensePref) {
            case LicensePrefs.CC_BY_3:
                return R.string.license_name_cc_by_three;
            case LicensePrefs.CC_BY_4:
                return R.string.license_name_cc_by_four;
            case LicensePrefs.CC_BY_SA_3:
                return R.string.license_name_cc_by_sa_three;
            case LicensePrefs.CC_BY_SA_4:
                return license_name_cc_by_sa_four;
            case LicensePrefs.CC_0:
            default:
                return R.string.license_name_cc_zero;
        }
    }

    /**
     * Generates license url with given ID
     *
     * @param licensePref License ID
     * @return Url of license
     */

    @NonNull
    public static String licenseUrlFor(String licensePref) {
        switch (licensePref) {
            case LicensePrefs.CC_BY_3:
                return "https://creativecommons.org/licenses/by/3.0/";
            case LicensePrefs.CC_BY_4:
                return "https://creativecommons.org/licenses/by/4.0/";
            case LicensePrefs.CC_BY_SA_3:
                return "https://creativecommons.org/licenses/by-sa/3.0/";
            case LicensePrefs.CC_BY_SA_4:
                return "https://creativecommons.org/licenses/by-sa/4.0/";
            case LicensePrefs.CC_0:
            default:
                return "https://creativecommons.org/publicdomain/zero/1.0/";
        }
    }

    public static String getLicenseTemplateInWiki(String licensePref) {
        switch (licensePref) {
            case LicensePrefs.CC_BY_3:
                return LicenseTemplateInWiki.CC_BY_3;
            case LicensePrefs.CC_BY_4:
                return LicenseTemplateInWiki.CC_BY_4;
            case LicensePrefs.CC_BY_SA_3:
                return LicenseTemplateInWiki.CC_BY_SA_3;
            case LicensePrefs.CC_BY_SA_4:
                return LicenseTemplateInWiki.CC_BY_SA_4;
            case LicensePrefs.CC_0:
            default:
                return LicenseTemplateInWiki.CC_0;
        }

    }

    public static class LicensePrefs {
        public static final String CC_BY_4 = "CC_BY_4";
        public static final String CC_BY_SA_3 = "CC_BY_SA_3";
        public static final String CC_BY_3 = "CC_BY_3";
        public static final String CC_BY_SA_4 = "CC_BY_SA_4";
        public static final String CC_0 = "CC0";
    }

    private static class LicenseTemplateInWiki {
        //{{CC-Zero}}"), License type ->  PD-self, CC-Zero, CC-BY-SA-4.0, CC-BY-SA-3.0
        static final String CC_BY_3 = "{{CC-BY-3.0}}";
        static final String CC_BY_4 = "{{CC-BY-4.0}}";
        static final String CC_BY_SA_3 = "{{CC-BY-SA-3.0}}";
        static final String CC_BY_SA_4 = "{{CC-BY-SA-4.0}}";
        static final String CC_0 = "{{CC-Zero}}";
        static final String PUBLIC_DOMAIN = "{{PD-self}}";
    }


}
