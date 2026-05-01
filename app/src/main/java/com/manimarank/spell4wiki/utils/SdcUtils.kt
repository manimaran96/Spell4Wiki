package com.manimarank.spell4wiki.utils

import com.manimarank.spell4wiki.utils.DateUtils.DF_YYYY_MM_DD
import com.manimarank.spell4wiki.utils.DateUtils.getDateToString
import com.manimarank.spell4wiki.utils.WikiLicense.LicensePrefs
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility class for building Structured Data on Commons (SDC) data
 */
object SdcUtils {

    /**
     * Build complete SDC data JSON for an audio file
     * 
     * @param username Wikimedia Commons username
     * @param licensePref License preference from app settings
     * @param uploadDate Date of upload (ISO format)
     * @return JSON string for SDC data parameter
     *
     * More details: https://commons.wikimedia.org/wiki/Commons:Structured_data/Modeling/Visual_artworks
     */
    fun buildSdcDataForAudioFile(
        username: String,
        licensePref: String,
        uploadDate: String = getDateToString(DF_YYYY_MM_DD)
    ): String {
        val statementsJson = JSONObject()

        // P170 - Creator (with qualifiers for username and URL)
        statementsJson.put("P170", JSONArray().apply {
            put(createCreatorStatement(username))
        })

        // P6216 - Copyright status
        val copyrightStatusQId = getCopyrightStatusQId(licensePref)
        statementsJson.put("P6216", JSONArray().apply {
            put(createWikibaseItemStatement("P6216", copyrightStatusQId))
        })

        // P275 - Copyright license
        val licenseQId = getLicenseQId(licensePref)
        statementsJson.put("P275", JSONArray().apply {
            put(createWikibaseItemStatement("P275", licenseQId))
        })

        // P571 - Inception (date created)
        statementsJson.put("P571", JSONArray().apply {
            put(createTimeStatement("P571", uploadDate))
        })

        // P1163 - Media type
        statementsJson.put("P1163", JSONArray().apply {
            put(createStringStatement("P1163", "application/ogg", "string"))
        })

        // P7482 - Source of file (original creation by uploader - https://www.wikidata.org/wiki/Q66458942)
        statementsJson.put("P7482", JSONArray().apply {
            put(createWikibaseItemStatement("P7482", "Q66458942"))
        })

        val dataJson = JSONObject()
        // Note: wbeditentity API expects 'claims' key in the data parameter
        // even though MediaInfo entities internally use 'statements'
        dataJson.put("claims", statementsJson)

        return dataJson.toString()
    }


    /**
     * Create creator statement with qualifiers
     * More details: https://www.wikidata.org/wiki/Property:P170
     */
    private fun createCreatorStatement(username: String): JSONObject {
        val mainsnak = JSONObject().apply {
            put("snaktype", "somevalue")
            put("property", "P170")
        }

        val qualifiers = JSONObject()

        // P2093 - Author name string
        qualifiers.put("P2093", JSONArray().apply {
            put(JSONObject().apply {
                put("snaktype", "value")
                put("property", "P2093")
                put("datavalue", JSONObject().apply {
                    put("value", username)
                    put("type", "string")
                })
            })
        })

        // P4174 - Wikimedia username
        qualifiers.put("P4174", JSONArray().apply {
            put(JSONObject().apply {
                put("snaktype", "value")
                put("property", "P4174")
                put("datavalue", JSONObject().apply {
                    put("value", username)
                    put("type", "string")
                })
            })
        })

        // P2699 - URL
        val userUrl = "https://commons.wikimedia.org/wiki/User:$username"
        qualifiers.put("P2699", JSONArray().apply {
            put(JSONObject().apply {
                put("snaktype", "value")
                put("property", "P2699")
                put("datavalue", JSONObject().apply {
                    put("value", userUrl)
                    put("type", "string")
                })
            })
        })

        return JSONObject().apply {
            put("mainsnak", mainsnak)
            put("type", "statement")
            put("rank", "normal")
            put("qualifiers", qualifiers)
        }
    }


    /**
     * Get copyright status Q-ID based on license
     * More details:
     * https://www.wikidata.org/wiki/Property:P6216
     * https://www.wikidata.org/wiki/Wikidata:Property_proposal/copyright_status
     */
    fun getCopyrightStatusQId(licensePref: String): String {
        return when (licensePref) {
            LicensePrefs.CC_0 -> "Q88088423"  // Copyrighted, dedicated to the public domain by copyright holder
            else -> "Q50423863"                // Copyrighted
        }
    }

    /**
     * Map app license preference to Wikibase Q-ID
     * More details:
     * https://www.wikidata.org/wiki/Help:Copyrights
     * https://www.wikidata.org/wiki/Property:P275
     */
    fun getLicenseQId(licensePref: String): String {
        return when (licensePref) {
            LicensePrefs.CC_0 -> "Q6938433"           // CC0 1.0
            LicensePrefs.CC_BY_3 -> "Q14947546"       // CC BY 3.0
            LicensePrefs.CC_BY_4 -> "Q20007257"       // CC BY 4.0
            LicensePrefs.CC_BY_SA_3 -> "Q14946043"    // CC BY-SA 3.0
            LicensePrefs.CC_BY_SA_4 -> "Q18199165"    // CC BY-SA 4.0
            else -> "Q6938433"                         // Default to CC0
        }
    }

    /**
     * Create a wikibase-item statement (entity reference)
     */
    private fun createWikibaseItemStatement(property: String, qId: String): JSONObject {
        val numericId = qId.substring(1).toLong() // Remove 'Q' prefix

        val entityValue = JSONObject().apply {
            put("entity-type", "item")
            put("numeric-id", numericId)
            put("id", qId)
        }

        val datavalue = JSONObject().apply {
            put("value", entityValue)
            put("type", "wikibase-entityid")
        }

        val mainsnak = JSONObject().apply {
            put("snaktype", "value")
            put("property", property)
            put("datavalue", datavalue)
        }

        return JSONObject().apply {
            put("mainsnak", mainsnak)
            put("type", "statement")
            put("rank", "normal")
        }
    }


    /**
     * Create a string-based statement
     */
    private fun createStringStatement(property: String, value: String, datatype: String = "string"): JSONObject {
        val datavalue = JSONObject().apply {
            put("value", value)
            put("type", datatype)
        }

        val mainsnak = JSONObject().apply {
            put("snaktype", "value")
            put("property", property)
            put("datavalue", datavalue)
        }

        return JSONObject().apply {
            put("mainsnak", mainsnak)
            put("type", "statement")
            put("rank", "normal")
        }
    }


    /**
     * Create a time-based statement
     * More details: https://www.wikidata.org/wiki/Property:P571
     */
    private fun createTimeStatement(property: String, date: String): JSONObject {
        // Convert date to Wikibase time format: +YYYY-MM-DDT00:00:00Z
        val wikibaseTime = "+${date}T00:00:00Z"
        
        val timeValue = JSONObject().apply {
            put("time", wikibaseTime)
            put("timezone", 0)
            put("before", 0)
            put("after", 0)
            put("precision", 11) // Day precision
            put("calendarmodel", "http://www.wikidata.org/entity/Q1985727")
        }

        val datavalue = JSONObject().apply {
            put("value", timeValue)
            put("type", "time")
        }

        val mainsnak = JSONObject().apply {
            put("snaktype", "value")
            put("property", property)
            put("datavalue", datavalue)
        }

        return JSONObject().apply {
            put("mainsnak", mainsnak)
            put("type", "statement")
            put("rank", "normal")
        }
    }
}
