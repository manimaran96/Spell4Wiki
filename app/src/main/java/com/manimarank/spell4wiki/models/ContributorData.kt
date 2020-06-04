package com.manimarank.spell4wiki.models

import java.io.Serializable

data class ContributorData(
        val core_contributors: List<CoreContributors>,
        val wiki_tech_helpers: List<String>
) : Serializable