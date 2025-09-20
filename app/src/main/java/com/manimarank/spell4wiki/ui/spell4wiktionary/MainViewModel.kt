package com.manimarank.spell4wiki.ui.spell4wiktionary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

class MainViewModel: ViewModel() {

    val wordsWithoutAudioList: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val wordAlreadyHaveAudio: MutableLiveData<String> = MutableLiveData()
    val progressForFilter: MutableLiveData<Int> = MutableLiveData() // 0 to 100
    val filterCancelled = MutableLiveData<Boolean>()
    val networkError = MutableLiveData<Int>() // Resource ID for error message

    private var filterJob: Job? = null
    private var isCancelled = false

    fun checkWordsAvailability(
        wordsList: List<String>,
        langCode: String,
        runFilterNoOfWordsCheckCount: Int
    ) {
        isCancelled = false
        filterJob = viewModelScope.launch {
            val resultList = ArrayList<String>()
            wordsList.forEachIndexed { index, word ->
                // Check if operation was cancelled
                if (isCancelled) {
                    filterCancelled.value = true
                    return@launch
                }

                progressForFilter.value = index
                var fileExist = false
                withContext(Dispatchers.IO) {
                    try {
                        val url = String.format(Urls.AUDIO_FILE_IN_COMMONS, langCode, word)
                        val u = URL(url)
                        val huc: HttpURLConnection = u.openConnection() as HttpURLConnection
                        huc.requestMethod = "HEAD"
                        huc.connectTimeout = 10000 // 10 seconds timeout
                        huc.readTimeout = 10000 // 10 seconds timeout
                        huc.connect()
                        fileExist = huc.responseCode == 200
                        huc.disconnect()
                    } catch (e: UnknownHostException) {
                        // Network connectivity issue
                        networkError.postValue(R.string.no_internet_connection)
                        return@withContext
                    } catch (e: SocketTimeoutException) {
                        // Timeout issue
                        networkError.postValue(R.string.network_timeout_error)
                        return@withContext
                    } catch (e: IOException) {
                        // Other network issues
                        networkError.postValue(R.string.network_error_general)
                        return@withContext
                    } catch (e: Exception) {
                        // Other unexpected errors
                        e.printStackTrace()
                        networkError.postValue(R.string.network_error_general)
                        return@withContext
                    }
                }
                if (!fileExist)
                    resultList.add(word)
                else
                    wordAlreadyHaveAudio.value = word

                if (index >= wordsList.size -1 || resultList.size >= runFilterNoOfWordsCheckCount) {
                    wordsWithoutAudioList.value = resultList
                }
            }
        }
    }

    fun cancelFilter() {
        isCancelled = true
        filterJob?.cancel()
        filterCancelled.value = true
    }

}