package com.example.tflite_fashion_mnist


import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.AssetManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
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

class ListAdapter(private val images: List<Drawable>,
                  private val context: Context, private val onItemClicked: (Drawable) -> Unit) : Adapter<ViewHolder>(){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(images[position])
        holder.itemView.setOnClickListener({onItemClicked(images[position])})

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_view, parent, false)
        return ViewHolder(view) {
            onItemClicked(images[it])
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

}

class ViewHolder(itemView : View, onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder(itemView){
    var image : ImageView = itemView?.findViewById(R.id.image_view)

    init{
        itemView.setOnClickListener{
            onItemClicked(adapterPosition)
        }
    }

    fun bindView(drawable: Drawable){
        image.setImageDrawable(drawable)
    }

}