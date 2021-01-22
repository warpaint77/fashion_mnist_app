package com.example.tflite_fashion_mnist


import android.R.layout
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.AssetManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.io.InputStream



class MainActivity : AppCompatActivity() , AdapterView.OnItemSelectedListener{
    private val mInputSize = 28
    private val mModelPathList = listOf<String>("model_opt_fashion_mnist.tflite",
            "model_notopt_fashion_mnist.tflite")
    private var mModelPath = ""
    private val mLabelPath = "labels.txt"
    private val mImagesPath = "fmnist_test_images"
    private lateinit var classifier: Classifier

    data class Image(var drawable: Drawable, var name : String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner : Spinner = findViewById(R.id.spinner)
        ArrayAdapter.createFromResource(
                this,
                R.array.models_array,
                layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this
     }
    }


    private fun initClassifier(modelPath: String){
            classifier = Classifier(assets, modelPath, mLabelPath, mInputSize)
    }

    private fun initViews(){
        val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
        val images : MutableList<Image> = loadGridImages(assets, mImagesPath)
        recyclerView.adapter = ListAdapter(images, this, {
            val bitmap = (it.drawable as BitmapDrawable).bitmap
            val result = classifier.recognizeImage(bitmap)
            runOnUiThread { Toast.makeText(this,
                    "Image="+it.name+'\n'+"Inference result="+result?.title+'\n'+
                            "Inference confidence="+result?.confidence+'\n'+
                    "Inference duration="+result?.inferenceDuration,
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
                }
        }
        return imagesList
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        runOnUiThread { Toast.makeText(this, "Nothing Selected",
                Toast.LENGTH_SHORT).show()}
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        //Refatorar o codigo para melhorar a logica
        mModelPath = "teste"
        if (parent != null) {
            if(parent.getItemAtPosition(pos).equals("Selecione um modelo")){
                runOnUiThread { Toast.makeText(this, "Selecione um modelo!",
                        Toast.LENGTH_SHORT).show()}
                val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
                if (recyclerView?.isVisible){
                    recyclerView.visibility = android.view.View.INVISIBLE
                }
            }
            else{
                mModelPath = parent.getItemAtPosition(pos).toString()
                runOnUiThread { Toast.makeText(this, mModelPath,
                        Toast.LENGTH_SHORT).show()}
                initClassifier(mModelPath)
                initViews()
                val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
                if(!recyclerView?.isVisible){
                    recyclerView.visibility = android.view.View.VISIBLE
                }
            }
        }
        else{
            Log.d("SpinnerOnItemSelected", "parent is null")
        }
    }
}
