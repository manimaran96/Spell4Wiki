package com.manimarank.spell4wiki.ui.compose.contributors

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.model.CodeContributors
import com.manimarank.spell4wiki.data.model.ContributorData
import com.manimarank.spell4wiki.data.model.CoreContributors
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel for ContributorsActivity
 * Manages contributors data and API calls
 */
class ContributorsViewModel(private val context: Context) : ViewModel() {
    
    private val _contributorsState = MutableStateFlow(ContributorsState())
    val contributorsState: StateFlow<ContributorsState> = _contributorsState.asStateFlow()
    
    /**
     * Load core contributors and wiki tech helpers
     */
    fun loadCoreContributors() {
        if (!NetworkUtils.isConnected(context)) {
            _contributorsState.value = _contributorsState.value.copy(
                isLoadingCore = false,
                errorMessage = context.getString(R.string.check_internet)
            )
            return
        }
        
        _contributorsState.value = _contributorsState.value.copy(
            isLoadingCore = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            val api = ApiClient.api.create(ApiInterface::class.java)
            val call = api.fetchContributorData()
            
            call.enqueue(object : Callback<ContributorData?> {
                override fun onResponse(call: Call<ContributorData?>, response: Response<ContributorData?>) {
                    if (response.isSuccessful && response.body() != null) {
                        val resBody = response.body()!!
                        _contributorsState.value = _contributorsState.value.copy(
                            isLoadingCore = false,
                            coreContributors = resBody.core_contributors,
                            wikiTechHelpers = resBody.wiki_tech_helpers,
                            errorMessage = null
                        )
                    } else {
                        _contributorsState.value = _contributorsState.value.copy(
                            isLoadingCore = false,
                            errorMessage = context.getString(R.string.something_went_wrong)
                        )
                    }
                }
                
                override fun onFailure(call: Call<ContributorData?>, t: Throwable) {
                    t.printStackTrace()
                    _contributorsState.value = _contributorsState.value.copy(
                        isLoadingCore = false,
                        errorMessage = context.getString(R.string.something_went_wrong)
                    )
                }
            })
        }
    }
    
    /**
     * Load code contributors from GitHub API
     */
    fun loadCodeContributors() {
        if (!NetworkUtils.isConnected(context)) {
            _contributorsState.value = _contributorsState.value.copy(
                isLoadingCode = false,
                errorMessage = context.getString(R.string.check_internet)
            )
            return
        }
        
        _contributorsState.value = _contributorsState.value.copy(
            isLoadingCode = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            val api = ApiClient.api.create(ApiInterface::class.java)
            val call = api.fetchCodeContributorsList()
            
            call.enqueue(object : Callback<List<CodeContributors?>?> {
                override fun onResponse(call: Call<List<CodeContributors?>?>, response: Response<List<CodeContributors?>?>) {
                    if (response.isSuccessful && response.body() != null) {
                        val contributors = response.body()?.filterNotNull() ?: emptyList()
                        _contributorsState.value = _contributorsState.value.copy(
                            isLoadingCode = false,
                            codeContributors = contributors,
                            errorMessage = null
                        )
                    } else {
                        _contributorsState.value = _contributorsState.value.copy(
                            isLoadingCode = false,
                            errorMessage = context.getString(R.string.something_went_wrong)
                        )
                    }
                }
                
                override fun onFailure(call: Call<List<CodeContributors?>?>, t: Throwable) {
                    t.printStackTrace()
                    _contributorsState.value = _contributorsState.value.copy(
                        isLoadingCode = false,
                        errorMessage = context.getString(R.string.something_went_wrong)
                    )
                }
            })
        }
    }
    
    /**
     * Open contributor link in browser
     */
    fun openContributorLink(context: Context, url: String) {
        if (url.isNotEmpty()) {
            GeneralUtils.openUrlInBrowser(context, url)
        }
    }
}

/**
 * Data class representing contributors screen state
 */
data class ContributorsState(
    val isLoadingCore: Boolean = false,
    val isLoadingCode: Boolean = false,
    val coreContributors: List<CoreContributors> = emptyList(),
    val codeContributors: List<CodeContributors> = emptyList(),
    val wikiTechHelpers: List<String> = emptyList(),
    val errorMessage: String? = null
)
