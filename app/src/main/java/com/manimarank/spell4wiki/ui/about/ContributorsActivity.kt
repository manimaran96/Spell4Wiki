package com.manimarank.spell4wiki.ui.about

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupEdgeToEdgeWithToolbar
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.databinding.ActivityContributorsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.util.*

class ContributorsActivity : BaseActivity() {

    private lateinit var binding: ActivityContributorsBinding
    private val codeContributorsList: MutableList<CodeContributors> = ArrayList()
    private val coreContributorsList: MutableList<CoreContributors> = ArrayList()
    private lateinit var coreContributorsAdapter: CoreContributorsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContributorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup edge-to-edge display
        setupEdgeToEdgeWithToolbar(
            rootView = binding.root,
            toolbar = toolbar
        )

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.contributors)

        val contributorsAdapter = ContributorsAdapter(this, codeContributorsList)
        binding.recyclerViewCodeContributors.adapter = contributorsAdapter
        binding.recyclerViewCodeContributors.layoutManager = GridLayoutManager(applicationContext, 2)
        coreContributorsAdapter = CoreContributorsAdapter(this, coreContributorsList)
        binding.recyclerViewCoreContributors.adapter = coreContributorsAdapter
        binding.recyclerViewCoreContributors.layoutManager = LinearLayoutManager(applicationContext)

        // Setup tabs programmatically
        setupTabs()

        loadCoreContributorsAndHelpersFromApi()
    }

    private fun setupTabs() {
        val tabLayout = binding.tabLayout

        // Add tabs programmatically
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.core_contributors)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.code_contributors)))

        // Set up tab selection listener
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> loadCoreContributorsAndHelpersFromApi()
                    1 -> loadCodeContributorsFromApi()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadCoreContributorsAndHelpersFromApi() {
        binding.loadingContributors.root.makeVisible()
        binding.layoutCoreContributors.makeGone()
        binding.recyclerViewCodeContributors.makeGone()
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
                        binding.txtHelpers.text = wikiTechHelpers.toString()
                        binding.loadingContributors.root.makeGone()
                        binding.layoutCoreContributors.makeVisible()
                        Handler(Looper.getMainLooper()).postDelayed({ callShowCaseUI() }, 1000)
                    }
                }

                override fun onFailure(call: Call<ContributorData?>, t: Throwable) {
                    t.printStackTrace()
                    binding.loadingContributors.root.makeGone()
                    showLong(binding.recyclerViewCodeContributors, getString(R.string.something_went_wrong))
                }
            })
        } else {
            binding.loadingContributors.root.makeGone()
            showLong(binding.recyclerViewCodeContributors, getString(R.string.check_internet))
        }
    }

    private fun loadCodeContributorsFromApi() {
        binding.loadingContributors.root.makeVisible()
        binding.recyclerViewCodeContributors.makeGone()
        binding.layoutCoreContributors.makeGone()

        if (isConnected(applicationContext)) {
            val api = ApiClient.api.create(ApiInterface::class.java)
            val call = api.fetchCodeContributorsList()
            call.enqueue(object : Callback<List<CodeContributors?>?> {
                override fun onResponse(call: Call<List<CodeContributors?>?>, response: Response<List<CodeContributors?>?>) {
                    if (response.isSuccessful && response.body() != null) {
                        codeContributorsList.clear()
                        response.body()?.filterNotNull()?.forEach { cc ->
                            codeContributorsList.add(cc)
                        }
                        binding.recyclerViewCodeContributors.adapter?.notifyDataSetChanged()
                    }
                    binding.loadingContributors.root.makeGone()
                    binding.recyclerViewCodeContributors.makeVisible()
                }

                override fun onFailure(call: Call<List<CodeContributors?>?>, t: Throwable) {
                    t.printStackTrace()
                    binding.loadingContributors.root.makeGone()
                    showLong(binding.recyclerViewCodeContributors, getString(R.string.something_went_wrong))
                }
            })
        } else {
            binding.loadingContributors.root.makeGone()
            showLong(binding.recyclerViewCodeContributors, getString(R.string.check_internet))
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed && isNotShowed(ShowCasePref.CORE_CONTRIBUTORS_LIST_ITEM) && binding.recyclerViewCoreContributors.visibility == View.VISIBLE && binding.recyclerViewCoreContributors.getChildAt(0) != null) {
            val sequence = MaterialTapTargetSequence().setSequenceCompleteListener { showed(ShowCasePref.CORE_CONTRIBUTORS_LIST_ITEM) }
            sequence.addPrompt(GeneralUtils.getPromptBuilder(this)
                    .setTarget(binding.recyclerViewCoreContributors.getChildAt(0))
                    .setPrimaryText(R.string.sc_t_core_contributors_list_item)
                    .setSecondaryText(R.string.sc_d_core_contributors_list_item))
            sequence.show()
        }
    }
}