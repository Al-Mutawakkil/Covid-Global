package com.tugas.covid_19


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.tugas.covid_19.adapter.AdapterCountry
import com.tugas.covid_19.model.CountriesItem
import com.tugas.covid_19.model.ResponseCountry
import com.tugas.covid_19.network.ApiService
import com.tugas.covid_19.network.RetrofitBuilder.retrofit
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private var ascending = true
    companion object{
        lateinit var adapterNegara: AdapterCountry
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapterNegara.filter.filter(newText)
                return false
            }

        })

        swipe_refresh.setOnRefreshListener {
            getNegara()
            swipe_refresh.isRefreshing = false
        }
        initializedView()
        getNegara()
    }

    private fun initializedView() {
        btnSequence.setOnClickListener {
            sequenceWithoutInternet(ascending)
        }
    }

    private fun sequenceWithoutInternet(ascending: Boolean) {
        rv_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (ascending){
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd = true
                Toast.makeText(this@MainActivity, "Z-A", Toast.LENGTH_SHORT).show()
            }else {
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd = false
                Toast.makeText(this@MainActivity, "A-Z", Toast.LENGTH_SHORT).show()
            }
            adapter = adapterNegara
        }
    }

    private fun getNegara() {
        val api = retrofit.create(ApiService::class.java)
        api.getAllNegara().enqueue(object : Callback<ResponseCountry> {
            override fun onFailure(call: Call<ResponseCountry>, t: Throwable) {
                progress_bar.visibility = View.GONE
            }

            override fun onResponse(call: Call<ResponseCountry>, response: Response<ResponseCountry>) {
                if (response.isSuccessful){
                    val getListDataCorona = response.body()!!.global
                    val formatter: NumberFormat = DecimalFormat("#,###")
                    txt_confirmed_globe.text = formatter.format(getListDataCorona?.totalConfirmed?.toDouble())
                    txt_recovered_globe.text = formatter.format(getListDataCorona?.totalRecovered?.toDouble())
                    txt_death_globe.text = formatter.format(getListDataCorona?.totalDeaths?.toDouble())
                    rv_country.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        progress_bar?.visibility = View.GONE
                        adapterNegara = AdapterCountry(response.body()!!.countries as ArrayList<CountriesItem>)
                        {negara -> itemClicked(negara)}
                            adapter = adapterNegara

                    }
                }else {
                    progress_bar?.visibility = View.GONE
                }
            }

        })
    }

    private fun itemClicked(negara: CountriesItem) {
        val moveWithData = Intent(this@MainActivity, DetailActivity::class.java)
        moveWithData.putExtra(DetailActivity.EXTRA_COUNTRY, negara)
        startActivity(moveWithData)
    }
}