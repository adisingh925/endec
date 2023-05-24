package app.adreal.endec.RecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.adreal.endec.Model.File
import app.adreal.endec.databinding.RecycleritemBinding

class MainAdapter(private val context: Context, private val onItemClickListener : OnItemClickListener) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    lateinit var binding: RecycleritemBinding

    private var filesList = emptyList<File>()

    interface OnItemClickListener
    {
        fun onItemClick()
    }

    class MyViewHolder(binding: RecycleritemBinding) : RecyclerView.ViewHolder(binding.root) {
        val fileName = binding.recyclerItemText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = RecycleritemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.fileName.text = filesList[position].fileName

        holder.itemView.setOnClickListener{
            onItemClickListener.onItemClick()
        }
    }

    fun setData(data: List<File>)
    {
        this.filesList = data
        notifyItemRangeInserted(0,data.size - 1)
    }

}