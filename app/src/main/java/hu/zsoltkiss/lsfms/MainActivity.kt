package hu.zsoltkiss.lsfms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.zsoltkiss.lsfms.adapter.ArtistAdapter
import hu.zsoltkiss.lsfms.apiclient.ApiInterface
import hu.zsoltkiss.lsfms.model.SimpleArtist
import hu.zsoltkiss.lsfms.parser.ArtistRegexParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val TAG = "Main"

    lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var infoTextView: TextView

    private var artists = mutableListOf<SimpleArtist>()

    private val networkListener = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkConnection()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.artistsRecyclerView)
        searchView = findViewById(R.id.artistsSearchView)

        recyclerView.adapter = ArtistAdapter(artists, this)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        searchView.queryHint = resources.getString(R.string.query_hint)
        searchView.isIconified = false
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty()) {
                    callAPISearchXML(query)
                } else {
                    clearResults()
                    clearSearchString()
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val closeButtonId = searchView.context.resources
            .getIdentifier("android:id/search_close_btn", null, null)
        val editTextId = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val closeButton = searchView.findViewById<ImageView>(closeButtonId)
        closeButton.setOnClickListener {
            clearResults()
            clearSearchString()
        }
        val searchTextView = searchView.findViewById<EditText>(editTextId)
        searchTextView.setTextColor(Color.WHITE)
        searchTextView.setHintTextColor(Color.WHITE)

        progressBar = findViewById(R.id.artistsProgressbar)
        progressBar.visibility = View.GONE

        infoTextView = findViewById(R.id.tvInfo)

    }

    override fun onResume() {
        super.onResume()

        checkConnection()

        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkListener, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkListener);
    }

    private fun callAPISearchXML(searchExpression: String) {
        progressBar.visibility = View.VISIBLE
        val client  = OkHttpClient()
        val url = "http://ws.audioscrobbler.com/2.0/?method=artist.search&artist=$searchExpression&api_key=${ApiInterface.API_KEY}&format=xml"

        val searchRequest = Request.Builder()
            .url(url)
            .build()

        val searchCallback = object: okhttp3.Callback {


            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val rawResponse = response.body()?.let {
                    it.string()
                }

                if (rawResponse != null) {
                    val result = ArtistRegexParser.retrieveArtistNodes(rawResponse)

                    this@MainActivity.runOnUiThread(Runnable {
                        dismissInfo()
                        artists.clear()
                        artists.addAll(result)
                        recyclerView.adapter?.notifyDataSetChanged()
                        progressBar.visibility = View.GONE
                        if (result.isEmpty()) {
                            displayInfo(resources.getString(R.string.no_results))
                        } else {
                            dismissInfo()
                        }
                    })
                }

            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                progressBar.visibility = View.GONE
                displayInfo(resources.getString(R.string.fetch_error))
                e.printStackTrace()
            }
        }


        client.newCall(searchRequest).enqueue(searchCallback)
    }

    private fun clearResults() {
        artists.clear()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun clearSearchString() {
        searchView.setQuery("", false)
    }

    private fun displayInfo(info: String) {
        infoTextView.text = info
        infoTextView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun dismissInfo() {
        infoTextView.text = ""
        infoTextView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun checkConnection() {
        val isConnected = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            isNetworkConnectedBeforeM()
        } else {
            isNetworkConnectedSinceM()
        }

        if (isConnected) {
            dismissInfo()
            if (searchView.query.isNotEmpty()) {
                callAPISearchXML(searchView.query.toString())
            }
        } else {
            displayInfo(resources.getString(R.string.no_network))
        }
    }


    private fun isNetworkConnectedBeforeM(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkConnectedSinceM(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.let {
            (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } ?: false
    }

}
