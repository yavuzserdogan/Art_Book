package com.yavuzselimerdogan.artbook


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.yavuzselimerdogan.artbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private  lateinit var artList : ArrayList<Art>
    private  lateinit var artAdapter : ArtAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        artList = ArrayList<Art>()

        artAdapter = ArtAdapter(artList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter

        try {

            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM arts",null)
            val artNameIndex = cursor.getColumnIndex("artname")
            val idIndex = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {
                val name = cursor.getString(artNameIndex)
                val id = cursor.getInt(idIndex)
                val art = Art(name,id)
                artList.add(art)
            }

            artAdapter.notifyDataSetChanged()

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    //inflater -> connect code with XML
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //when clicked , move the ArtActivity
    //There ara may be more than one option in the menu so check it!
        if(item.itemId == R.id.add_art_item) {
            val intent = Intent(this@MainActivity, ArtActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}