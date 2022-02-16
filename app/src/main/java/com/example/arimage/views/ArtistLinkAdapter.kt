package com.example.arimage.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.arimage.R

internal class ArtistLinkAdapter(
    private val inflater: LayoutInflater
): ListAdapter<ArtistLinkViewModel, ArtistLinkViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistLinkViewHolder {
        return ArtistLinkViewHolder(inflater.inflate(R.layout.artist_link_item, parent, false))
    }

    override fun onBindViewHolder(holder: ArtistLinkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

internal data class ArtistLinkViewModel(
    @DrawableRes val image: Int,
    val onClick: () -> Unit
)

internal class ArtistLinkViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val imageButton = itemView.findViewById<ImageView>(R.id.image_button)

    internal fun bind(viewModel: ArtistLinkViewModel) {
        imageButton.setImageResource(viewModel.image)
    }
}

internal object DiffCallback: DiffUtil.ItemCallback<ArtistLinkViewModel>() {
    override fun areItemsTheSame(oldItem: ArtistLinkViewModel, newItem: ArtistLinkViewModel) =
        oldItem.image == newItem.image

    override fun areContentsTheSame(oldItem: ArtistLinkViewModel, newItem: ArtistLinkViewModel) =
        oldItem == newItem
}