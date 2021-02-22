package com.picpay.desafio.android

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: UserListAdapter
    var layoutManager = LinearLayoutManager(this)
    var USER_LIST_KEY = "USER_LIST"

    private val url = "http://careers.picpay.com/tests/mobdev/"

    private val gson: Gson by lazy { GsonBuilder().create() }

    var cacheSize = 10 * 1024 * 1024

    var cache = Cache(cacheDir, cacheSize.toLong())

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cache(cache)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(url)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val service: PicPayService by lazy {
        retrofit.create(PicPayService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.user_list_progress_bar)

        adapter = UserListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        if (savedInstanceState != null){
            val users : ArrayList<User> = savedInstanceState.getParcelableArrayList(USER_LIST_KEY)
            adapter.users = users.toList();
        } else {
            searchUserList()
        }
    }

    private fun searchUserList() {
        progressBar.visibility = View.VISIBLE
        service.getUsers()
            .enqueue(object : Callback<List<User>> {
                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    val message = getString(R.string.error)

                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE

                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onResponse(
                    call: Call<List<User>>,
                    response: Response<List<User>>
                ) {
                    progressBar.visibility = View.GONE

                    adapter.users = response.body()!!
                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(USER_LIST_KEY, ArrayList(adapter.users))
    }
}