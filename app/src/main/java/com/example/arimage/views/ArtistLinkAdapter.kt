package com.example.arimage.views

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.arimage.R
import com.example.arimage.viewmodels.ArtistLinkViewModel
import javax.inject.Inject

class ArtistLinkAdapter @Inject constructor(
    private val inflater: LayoutInflater
): ListAdapter<ArtistLinkViewModel, ArtistLinkViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistLinkViewHolder {
        return ArtistLinkViewHolder(inflater.inflate(R.layout.artist_link_item, parent, false))
    }

    override fun onBindViewHolder(holder: ArtistLinkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


class ArtistLinkViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val artistLinkButton = itemView.findViewById<ArtistLinkButton>(R.id.artist_link_button)

    internal fun bind(viewModel: ArtistLinkViewModel) {
        artistLinkButton.setIconResource(viewModel.image)
        artistLinkButton.text = viewModel.text

        artistLinkButton.setOnClickListener {
            viewModel.onClick(viewModel.webLink)
        }
    }
}

internal object DiffCallback: DiffUtil.ItemCallback<ArtistLinkViewModel>() {
    override fun areItemsTheSame(oldItem: ArtistLinkViewModel, newItem: ArtistLinkViewModel) =
        oldItem.image == newItem.image &&
                oldItem.text == newItem.text &&
                oldItem.webLink == newItem.webLink

    override fun areContentsTheSame(oldItem: ArtistLinkViewModel, newItem: ArtistLinkViewModel) =
        oldItem == newItem
}