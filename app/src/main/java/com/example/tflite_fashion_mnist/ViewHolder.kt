package com.example.tflite_fashion_mnist

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ViewHolder(itemView : View, onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder(itemView){
    var image : ImageView = itemView?.findViewById(R.id.image_view)

    init{
        itemView.setOnClickListener{
            onItemClicked(adapterPosition)
        }
    }

    fun bindView(img: MainActivity.Image){
        image.setImageDrawable(img.drawable)
    }

}