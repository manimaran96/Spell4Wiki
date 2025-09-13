package com.manimarank.spell4wiki.ui.spell4wiktionary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel: ViewModel() {

    val wordsWithoutAudioList: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val wordAlreadyHaveAudio: MutableLiveData<String> = MutableLiveData()
    val progressForFilter: MutableLiveData<Int> = MutableLiveData() // 0 to 100
    val filterCancelled = MutableLiveData<Boolean>()

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
                        huc.connect()
                        fileExist = huc.responseCode == 200
                        huc.disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
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