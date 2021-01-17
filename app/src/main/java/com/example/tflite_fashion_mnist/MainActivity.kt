package com.example.tflite_fashion_mnist


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.AssetManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.io.InputStream



class MainActivity : AppCompatActivity(), View.OnClickListener{
    private val mInputSize = 28
    private val mModelPath = "model_opt_fashion_mnist.tflite"
    private val mLabelPath = "labels.txt"
    private val mImagesPath = "fmnist_test_images"
    private lateinit var classifier: Classifier

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
        val images : List<Drawable> = loadGridImages(assets, mImagesPath)
        recyclerView.adapter = ListAdapter(images, this, {
//            Log.d("DRAWABLE INFO", it.)
            val bitmap = (it as BitmapDrawable).bitmap
            val result = classifier.recognizeImage(bitmap)
            runOnUiThread { Toast.makeText(this, result[0].title, Toast.LENGTH_SHORT).show()}
        } )
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

    }

    private fun loadGridImages(assetManager : AssetManager,
                               imagesPath: String) : MutableList<Drawable>{
        var imagesNames : Array<out String>? = assetManager.list(imagesPath)
        var imagesList : MutableList<Drawable> = mutableListOf()

        if (imagesNames != null) {
            for (image in imagesNames){
                var inputStream : InputStream = assetManager.open("$imagesPath/$image")
                var drawable : Drawable = Drawable.createFromStream(inputStream, null)
                imagesList.add(drawable)
            }
        }
        return imagesList
    }

    override fun onClick(view: View?) {
        val bitmap = ((view as ImageView).drawable as BitmapDrawable).bitmap

        val result = classifier.recognizeImage(bitmap)

        runOnUiThread { Toast.makeText(this, result[0].title, Toast.LENGTH_SHORT).show() }
    }
}
