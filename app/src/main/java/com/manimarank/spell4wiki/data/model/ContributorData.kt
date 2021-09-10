package com.manimarank.spell4wiki.data.model

/**
 * Model Class for Contributor Data Items
 * @property core_contributors List<CoreContributors>
 * @property wiki_tech_helpers List<String>
 * @constructor
 */
data class ContributorData(
    val core_contributors: List<CoreContributors>,
    val wiki_tech_helpers: List<String>
)
