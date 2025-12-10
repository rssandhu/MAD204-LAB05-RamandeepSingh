/**
 * Course: MOBILE APP DEVELOPMENT - Lab 5
 * Student: [Your Full Name] - [Student ID]
 * Date: December 09, 2025
 * Description: RecyclerView Adapter for displaying favorite media list.
 */

package com.example.medialibraryapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medialibraryapp.R

class FavoritesAdapter(private val onDelete: (FavoriteMedia) -> Unit) :
    ListAdapter<FavoriteMedia, FavoritesAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.mediaThumbnail)
        val typeText: TextView = view.findViewById(R.id.typeText)
        val deleteBtn: ImageView = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = getItem(position)
        Glide.with(holder.itemView.context)
            .load(Uri.parse(media.uri))
            .centerCrop()
            .placeholder(android.R.color.darker_gray)
            .into(holder.imageView)

        holder.typeText.text = media.type.uppercase()
        holder.deleteBtn.setOnClickListener { onDelete(media) }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FavoriteMedia>() {
            override fun areItemsTheSame(oldItem: FavoriteMedia, newItem: FavoriteMedia) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: FavoriteMedia, newItem: FavoriteMedia) =
                oldItem == newItem
        }
    }
}
