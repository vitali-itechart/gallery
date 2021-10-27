package com.example.gallery.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.example.gallery.data.entity.Image
import com.example.gallery.databinding.PreviewsFragmentItemBinding


class PreviewsRecyclerViewAdapter(
    private val values: List<Image>,
    val onClick: (Image) -> Unit
) : RecyclerView.Adapter<PreviewsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            PreviewsFragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = values[position]
        holder.preview.setImageBitmap(image.preview)
        holder.preview.setOnClickListener { onClick(image) }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: PreviewsFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val preview: ImageView = binding.preview
    }
}