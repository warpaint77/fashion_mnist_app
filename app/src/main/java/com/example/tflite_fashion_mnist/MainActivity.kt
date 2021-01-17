package com.example.tflite_fashion_mnist


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.AssetManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.io.InputStream



class MainActivity : AppCompatActivity(){
    private val mInputSize = 28
    private val mModelPath = "model_opt_fashion_mnist.tflite"
    private val mLabelPath = "labels.txt"
    private val mImagesPath = "fmnist_test_images"
    private lateinit var classifier: Classifier

    data class Image(var drawable: Drawable, var name : String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initClassifier()
        initViews()
    }

    private fun initClassifier(){
        classifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
    }

    private fun initViews(){
        val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
        val images : MutableList<Image> = loadGridImages(assets, mImagesPath)
        recyclerView.adapter = ListAdapter(images, this, {
            Log.d("DRAWABLE INFO", it.name.split('.'
            ).get(0))
            val bitmap = (it.drawable as BitmapDrawable).bitmap
            val result = classifier.recognizeImage(bitmap)
            runOnUiThread { Toast.makeText(this,
                    "Image="+it.name+'\n'+"Inference result="+result?.title+'\n'+
                            "Inference confidence="+result?.confidence,
                    Toast.LENGTH_LONG).show()}
        } )
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

    }

    private fun loadGridImages(assetManager : AssetManager,
                               imagesPath: String) : MutableList<Image> {
        var imagesNames : Array<out String>? = assetManager.list(imagesPath)
        var imagesList  = mutableListOf<Image>()

        if (imagesNames != null) {
            for (image in imagesNames){
                var inputStream : InputStream = assetManager.open("$imagesPath/$image")
                var drawable : Drawable = Drawable.createFromStream(inputStream, null)
                imagesList.add(Image(drawable, image))
                Log.d("IMAGES INFO", image)
            }
        }
        return imagesList
    }
}
