package com.trufflear.trufflear.views

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.trufflear.trufflear.R
import com.trufflear.trufflear.viewmodels.AttachmentViewLinkModel
import java.lang.Exception
import javax.inject.Inject

class ArtistLinkAdapter @Inject constructor(
    private val resources: Resources,
    private val inflater: LayoutInflater
): ListAdapter<AttachmentViewLinkModel, ArtistLinkViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistLinkViewHolder {
        return ArtistLinkViewHolder(
            resources,
            inflater.inflate(R.layout.artist_link_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArtistLinkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ArtistLinkViewHolder(
    private val resources: Resources,
    itemView: View
): RecyclerView.ViewHolder(itemView) {

    private val artistLinkButton = itemView.findViewById<ArtistLinkButton>(R.id.artist_link_button)

    internal fun bind(viewModel: AttachmentViewLinkModel) {

        val target = object: Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                artistLinkButton.setIcon(BitmapDrawable(resources, bitmap))
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }

        Picasso.get()
            .load(viewModel.imageUrl)
            .into(target)

        artistLinkButton.text = viewModel.text
        artistLinkButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(viewModel.colorCode))

        artistLinkButton.setOnClickListener {
            viewModel.onClick(viewModel.webLink)
        }
    }
}

internal object DiffCallback: DiffUtil.ItemCallback<AttachmentViewLinkModel>() {
    override fun areItemsTheSame(oldItem: AttachmentViewLinkModel, newItem: AttachmentViewLinkModel) =
        oldItem.imageUrl == newItem.imageUrl &&
                oldItem.text == newItem.text &&
                oldItem.webLink == newItem.webLink

    override fun areContentsTheSame(oldItem: AttachmentViewLinkModel, newItem: AttachmentViewLinkModel) =
        oldItem == newItem
}