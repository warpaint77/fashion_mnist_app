package com.example.tflite_fashion_mnist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(private val images: MutableList<MainActivity.Image>,
                  private val context: Context, private val onItemClicked: (MainActivity.Image) -> Unit) : RecyclerView.Adapter<ViewHolder>(){

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