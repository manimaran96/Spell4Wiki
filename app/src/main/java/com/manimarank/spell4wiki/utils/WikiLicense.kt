package com.manimarank.spell4wiki.utils

import com.manimarank.spell4wiki.R.string

object WikiLicense {
    /**
     * Generates licence name with given ID
     *
     * @param licensePref License ID
     * @return Name of license
     */
    @JvmStatic
    fun licenseNameId(licensePref: String?): Int {
        return when (licensePref) {
            LicensePrefs.CC_BY_3 -> string.license_name_cc_by_three
            LicensePrefs.CC_BY_4 -> string.license_name_cc_by_four
            LicensePrefs.CC_BY_SA_3 -> string.license_name_cc_by_sa_three
            LicensePrefs.CC_BY_SA_4 -> string.license_name_cc_by_sa_four
            LicensePrefs.CC_0 -> string.license_name_cc_zero
            else -> string.license_name_cc_zero
        }
    }

    /**
     * Generates license url with given ID
     *
     * @param licensePref License ID
     * @return Url of license
     */
    @JvmStatic
    fun licenseUrlFor(licensePref: String?): String {
        return when (licensePref) {
            LicensePrefs.CC_BY_3 -> "https://creativecommons.org/licenses/by/3.0/"
            LicensePrefs.CC_BY_4 -> "https://creativecommons.org/licenses/by/4.0/"
            LicensePrefs.CC_BY_SA_3 -> "https://creativecommons.org/licenses/by-sa/3.0/"
            LicensePrefs.CC_BY_SA_4 -> "https://creativecommons.org/licenses/by-sa/4.0/"
            LicensePrefs.CC_0 -> "https://creativecommons.org/publicdomain/zero/1.0/"
            else -> "https://creativecommons.org/publicdomain/zero/1.0/"
        }
    }

    @JvmStatic
    fun getLicenseTemplateInWiki(licensePref: String?): String {
        return when (licensePref) {
            LicensePrefs.CC_BY_3 -> LicenseTemplateInWiki.CC_BY_3
            LicensePrefs.CC_BY_4 -> LicenseTemplateInWiki.CC_BY_4
            LicensePrefs.CC_BY_SA_3 -> LicenseTemplateInWiki.CC_BY_SA_3
            LicensePrefs.CC_BY_SA_4 -> LicenseTemplateInWiki.CC_BY_SA_4
            LicensePrefs.CC_0 -> LicenseTemplateInWiki.CC_0
            else -> LicenseTemplateInWiki.CC_0
        }
    }

    /**
     * Creative commons license pref type
     */
    object LicensePrefs {
        const val CC_BY_4 = "CC_BY_4"
        const val CC_BY_SA_3 = "CC_BY_SA_3"
        const val CC_BY_3 = "CC_BY_3"
        const val CC_BY_SA_4 = "CC_BY_SA_4"
        const val CC_0 = "CC0"
    }

    private object LicenseTemplateInWiki {
        // {{CC-Zero}}"), License type ->  PD-self, CC-Zero, CC-BY-SA-4.0, CC-BY-SA-3.0
        const val CC_BY_3 = "{{self|cc-by-3.0}}"
        const val CC_BY_4 = "{{self|cc-by-4.0}}"
        const val CC_BY_SA_3 = "{{self|cc-by-sa-3.0}}"
        const val CC_BY_SA_4 = "{{self|cc-by-sa-4.0}}"
        const val CC_0 = "{{self|cc-zero}}"
        const val PUBLIC_DOMAIN = "{{self|pd-self}}"
    }
}