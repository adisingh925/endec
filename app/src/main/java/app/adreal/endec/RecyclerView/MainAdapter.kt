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
        val text = binding.recyclerItemText
        val size = binding.recyclerItemSize
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = RecycleritemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.text.text = filesList[position].fileName
        holder.size.text = app.adreal.endec.File.File().fileSize(filesList[position].size.toInt())
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