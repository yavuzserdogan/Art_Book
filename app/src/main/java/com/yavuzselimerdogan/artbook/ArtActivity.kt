package com.yavuzselimerdogan.artbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.yavuzselimerdogan.artbook.databinding.ActivityArtBinding
import com.yavuzselimerdogan.artbook.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding

    //go to gallery
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    //for permission
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var selectedBitmap:Bitmap? = null

    private var counter : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerLauncher()
        counter = 0

    }

    fun save (view : View){

        val artName = binding.nameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()


            try {
                val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }

    private  fun makeSmallerBitmap(image : Bitmap, maximumSize:Int) : Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio>1){
            //landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else{
            //portrait
            height = maximumSize
            val scaledWidth = height*bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    fun selectPhoto (view : View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Android 33+ -> READ_MEDIA_IMAGES

            //(context , Permission kind) ! Manifest --> Android
            //PackageManager --> the class that permission return
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    counter++
                    Snackbar.make(view,"Permission needed gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }else{
                    counter++
                    //request permission
                    if(counter > 3){
                        Toast.makeText(this,"ArtBook->Permissions->Not allowed You should change this",Toast.LENGTH_LONG).show()
                    }else{
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            else{
                val intentGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGallery)
            }
        }else{
            ////Android 33- ->READ_EXTERNAL_STORAGE
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                val intentGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGallery)
            }
        }
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if(result.resultCode== RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageUri = intentFromResult.data
                    if(imageUri != null) {
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver, imageUri)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap =MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result){
                //permission granted
                val intentGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGallery)
            }else{
                //permission denied
                Toast.makeText(this@ArtActivity,"Permission Needed",Toast.LENGTH_LONG).show()
            }
        }

    }

}