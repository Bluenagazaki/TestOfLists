package com.example.testoflists

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.list_item.*
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Station(id: Int, name: String, numBike: Int, coord: HashMap<String, Double>) {
    val id: Int = id
    val name: String = name
    var numBike: Int = numBike
    val coord: HashMap<String, Double> = coord
}

class StationAdapter(activity: MainActivity, ctx: Context, resid: Int) :
    ArrayAdapter<Station>(ctx, resid) {

    var act = activity

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: act.layoutInflater.inflate(R.layout.list_item, null)
        val station = this.getItem(position)
        v.findViewById<TextView>(R.id.name).text = station!!.name
        v.findViewById<TextView>(R.id.bike).text = "Vélos disponibles: " + this.getItem(position)?.numBike.toString()
        v.findViewById<TextView>(R.id.coord).text =
            this.getItem(position)!!.coord["lat"].toString() + " " + this.getItem(position)!!.coord["lon"].toString()
        return v
    }

}


class MainActivity : AppCompatActivity() {

    var stations: ArrayList<Station> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var adapter = StationAdapter(this, this, R.id.main_listView)
        getData(adapter)
    }


    private fun getData(adapter: StationAdapter) {
        val t = Thread(Runnable {
            try {
                val u =
                    URL("https://opendata.paris.fr/api/records/1.0/search/?dataset=velib-disponibilite-en-temps-reel&rows=15")
                val c = u.openConnection()

                val reader = JsonReader(InputStreamReader(c.getInputStream()))

                reader.beginObject()
                while (reader.hasNext()) {
                    val i = reader.nextName()
                    if (i == "records") {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                val j = reader.nextName()
                                if (j == "fields") {
                                    reader.beginObject()
                                    var id: Int = 0
                                    var name: String = ""
                                    var numBike: Int = 0
                                    var coord = HashMap<String, Double>()
                                    while (reader.hasNext()) {
                                        val k = reader.nextName()
                                        if (k == "stationcode") {
                                            id = reader.nextString().toInt()
                                        } else if (k == "name") {
                                            name = reader.nextString()
                                        } else if (k == "numbikesavailable") {
                                            numBike = reader.nextInt()
                                        } else if (k == "coordonnees_geo") {
                                            reader.beginArray()
                                            coord["lat"] = reader.nextDouble()
                                            coord["lon"] = reader.nextDouble()
                                            reader.endArray()
                                        } else {
                                            reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                    val station = Station(id, name, numBike, coord)
                                    stations.add(station)

                                } else {
                                    reader.skipValue()
                                }
                            }
                            reader.endObject()

                        }
                        reader.endArray()
                    } else {
                        reader.skipValue()
                    }

                }
                reader.endObject()
                runOnUiThread {
                    for (n in stations) {
                        adapter.add(n)
                    }
                    adapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e("error", e.message ?: "No message but error")
                e.printStackTrace()
            }
        })
        t.start()
        findViewById<TextView>(R.id.date).text = SimpleDateFormat ("dd/MM kk:mm\n").toString()

        findViewById<ListView>(R.id.main_listView).adapter = adapter

        findViewById<Button>(R.id.refresh).setOnClickListener { y ->
            adapter.clear()
            adapter.notifyDataSetChanged()
            getData(adapter)

        }

    }
}
