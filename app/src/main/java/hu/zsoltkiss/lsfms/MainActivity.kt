package hu.zsoltkiss.lsfms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.zsoltkiss.lsfms.adapter.ArtistAdapter
import retrofit2.Call
import hu.zsoltkiss.lsfms.apiclient.ApiClient
import hu.zsoltkiss.lsfms.apiclient.ApiInterface
import hu.zsoltkiss.lsfms.model.SimpleArtist
import hu.zsoltkiss.lsfms.parser.ArtistRegexParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val TAG = "Main"

    lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    private var artists = mutableListOf<SimpleArtist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.artistsRecyclerView)
        searchView = findViewById(R.id.artistsSearchView)

        recyclerView.adapter = ArtistAdapter(artists, this)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        searchView.queryHint = "Type search phrase"
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
        val closeButton = searchView.findViewById<ImageView>(closeButtonId)
        closeButton.setOnClickListener {
            clearResults()
            clearSearchString()
        }

        progressBar = findViewById(R.id.artistsProgressbar)
        progressBar.visibility = View.GONE

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
                        artists.clear()
                        artists.addAll(result)
                        recyclerView.adapter?.notifyDataSetChanged()
                        progressBar.visibility = View.GONE
                    })


                }

            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                progressBar.visibility = View.GONE
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


    private fun getArtists(searchExpression: String) {

        val queryOptions = mapOf(
            "method" to "artist.search",
            "artist" to searchExpression,
            "api_key" to ApiInterface.API_KEY,
            "format" to "json"
        )

        val call: Call<ResponseBody> = ApiClient.getClient.artistSearch(queryOptions)

        call.enqueue(object: Callback<ResponseBody> {

//            override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
//                val sr = response.body()
//                if (sr != null) {
//                    Log.d(TAG, "SearchResults: ${sr}")
//                } else {
//                    Log.d(TAG, "No search results???")
//                }
//            }
//
//            override fun onFailure(call: Call<SearchResults>, t: Throwable) {
//                t.printStackTrace()
//            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val rawJson = response.body()?.let {
                    it.string()
                }

                Log.d(TAG, "rawJson: $rawJson")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }


}
