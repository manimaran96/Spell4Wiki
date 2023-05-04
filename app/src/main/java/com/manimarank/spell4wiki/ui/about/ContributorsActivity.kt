package com.manimarank.spell4wiki.ui.about

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.model.CodeContributors
import com.manimarank.spell4wiki.data.model.ContributorData
import com.manimarank.spell4wiki.data.model.CoreContributors
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.activity_contributors.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.util.*

class ContributorsActivity : BaseActivity() {
    private val codeContributorsList: MutableList<CodeContributors> = ArrayList()
    private val coreContributorsList: MutableList<CoreContributors> = ArrayList()
    private lateinit var coreContributorsAdapter: CoreContributorsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contributors)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.contributors)

        val contributorsAdapter = ContributorsAdapter(this, codeContributorsList)
        recyclerViewCodeContributors.adapter = contributorsAdapter
        recyclerViewCodeContributors.layoutManager = GridLayoutManager(applicationContext, 2)
        coreContributorsAdapter = CoreContributorsAdapter(this, coreContributorsList)
        recyclerViewCoreContributors.adapter = coreContributorsAdapter
        recyclerViewCoreContributors.layoutManager = LinearLayoutManager(applicationContext)
        loadCoreContributorsAndHelpersFromApi()
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 1) loadCodeContributorsFromApi() else loadCoreContributorsAndHelpersFromApi()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadCoreContributorsAndHelpersFromApi() {
        loadingContributors.makeVisible()
        layoutCoreContributors.makeGone()
        recyclerViewCodeContributors.makeGone()
        if (isConnected(applicationContext)) {
            val api = ApiClient.api.create(ApiInterface::class.java)
            val call = api.fetchContributorData()
            call.enqueue(object : Callback<ContributorData?> {
                override fun onResponse(call: Call<ContributorData?>, response: Response<ContributorData?>) {
                    if (response.isSuccessful && response.body() != null) {
                        val resBody = response.body()
                        coreContributorsList.clear()
                        resBody?.core_contributors?.forEach { coreContributors ->
                            coreContributorsList.add(coreContributors)
                        }
                        coreContributorsAdapter.notifyDataSetChanged()
                        val wikiTechHelpers = StringBuilder()
                        resBody?.wiki_tech_helpers?.forEach { helper ->
                            wikiTechHelpers.append("- ").append(helper).append("\n")
                        }
                        txtHelpers.text = wikiTechHelpers.toString()
                        loadingContributors.makeGone()
                        layoutCoreContributors.makeVisible()
                        Handler().postDelayed({ callShowCaseUI() }, 1000)
                    }
                }

                override fun onFailure(call: Call<ContributorData?>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        } else {
            showLong(recyclerViewCodeContributors, getString(R.string.check_internet))
        }
    }

    private fun loadCodeContributorsFromApi() {
        loadingContributors.makeVisible()
        recyclerViewCodeContributors.makeGone()
        layoutCoreContributors.makeGone()
        val api = ApiClient.api.create(ApiInterface::class.java)
        val call = api.fetchCodeContributorsList()
        call.enqueue(object : Callback<List<CodeContributors?>?> {
            override fun onResponse(call: Call<List<CodeContributors?>?>, response: Response<List<CodeContributors?>?>) {
                if (response.isSuccessful && response.body() != null) {
                    codeContributorsList.clear()
                    response.body()?.filterNotNull()?.forEach { cc ->
                        codeContributorsList.add(cc)
                    }
                    recyclerViewCodeContributors.adapter?.notifyDataSetChanged()
                }
                loadingContributors.makeGone()
                recyclerViewCodeContributors.makeVisible()
            }

            override fun onFailure(call: Call<List<CodeContributors?>?>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed && isNotShowed(ShowCasePref.CORE_CONTRIBUTORS_LIST_ITEM) && recyclerViewCoreContributors.visibility == View.VISIBLE && recyclerViewCoreContributors.getChildAt(0) != null) {
            val sequence = MaterialTapTargetSequence().setSequenceCompleteListener { showed(ShowCasePref.CORE_CONTRIBUTORS_LIST_ITEM) }
            sequence.addPrompt(GeneralUtils.getPromptBuilder(this)
                    .setTarget(recyclerViewCoreContributors.getChildAt(0))
                    .setPrimaryText(R.string.sc_t_core_contributors_list_item)
                    .setSecondaryText(R.string.sc_d_core_contributors_list_item))
            sequence.show()
        }
    }
}