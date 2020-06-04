package com.manimarank.spell4wiki.activities

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.utils.constants.AppConstants
import kotlinx.android.synthetic.main.activity_web_view_content.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class CommonWebContentActivity : AppCompatActivity() {
    private var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_content)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val bundle = intent.extras
        if (bundle != null) {
            var title: String? = ""
            if (bundle.containsKey(AppConstants.TITLE)) {
                title = bundle.getString(AppConstants.TITLE)
                setTitle(title)
            }
            if (bundle.containsKey(AppConstants.URL)) {
                url = bundle.getString(AppConstants.URL)
                loadWebPageContent()
            }
        }
    }


    private fun loadWebPageContent() {
        val htmlStringTask = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String {
                val stringBuffer = StringBuffer()
                try {
                    val urlData = URL(url)
                    val br = BufferedReader(InputStreamReader(urlData.openStream()))
                    var input: String?

                    while (br.readLine().also { input = it } != null) {
                        stringBuffer.append(input)
                    }
                    br.close()
                }catch (e : Exception){
                    e.printStackTrace()
                }
                return stringBuffer.toString()
            }

            override fun onPostExecute(result: String?) {
                if(!TextUtils.isEmpty(result)) {
                    webView.loadData(result!!, "text/html", "UTF-8")
                    loadingProgress.visibility = View.GONE
                }
            }
        }
        htmlStringTask.execute()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }
}