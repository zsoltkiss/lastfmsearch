package hu.zsoltkiss.lsfms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import hu.zsoltkiss.lsfms.R
import hu.zsoltkiss.lsfms.model.SimpleArtist

class ArtistAdapter(private var artists: List<SimpleArtist>, private val context: Context): RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.artist_item, parent, false))
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anArtist = artists.get(position)
        holder.tvArtistName.text = anArtist.name
        holder.tvArtistListeners.text = "${anArtist.listeners}"

        Picasso.get().load(anArtist.mediumImage).into(holder.ivArtistArtwork);
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var tvArtistName: TextView = view.findViewById(R.id.tvName)
        var tvArtistListeners: TextView = view.findViewById(R.id.tvListeners)
        var ivArtistArtwork: ImageView = view.findViewById(R.id.ivArtwork)


    }
}