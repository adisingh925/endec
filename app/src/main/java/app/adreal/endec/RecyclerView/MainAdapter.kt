package app.adreal.endec.RecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.adreal.endec.Constants
import app.adreal.endec.Model.File
import app.adreal.endec.databinding.RecycleritemBinding
import com.bumptech.glide.Glide

class MainAdapter(
    private val context: Context,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    private lateinit var binding: RecycleritemBinding

    private var filesList = emptyList<File>()

    interface OnItemClickListener {
        fun onItemClick(fileData : File)
    }

    class MyViewHolder(binding: RecycleritemBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.recyclerItemImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = RecycleritemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(java.io.File(Constants.getFilesDirectoryPath(context), filesList[position].fileName)).centerCrop().into(holder.image)

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(filesList[position])
        }
    }

    fun setData(data: List<File>) {
        this.filesList = data
        notifyDataSetChanged()
    }
}