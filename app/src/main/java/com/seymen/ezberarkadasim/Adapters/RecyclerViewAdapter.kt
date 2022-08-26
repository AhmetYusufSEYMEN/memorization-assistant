package com.seymen.ezberarkadasim.Adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seymen.ezberarkadasim.R
import com.seymen.ezberarkadasim.model.WordsModel
import kotlinx.android.synthetic.main.recycler_design.view.*

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>()  {
    private var klmList : ArrayList<WordsModel> = ArrayList()
    private var onClickItem : ((WordsModel) -> Unit)? = null
    private var onClickDeleteItem : ((WordsModel) -> Unit)? = null


    fun addItems(item : ArrayList<WordsModel>){
        this.klmList = item
        notifyDataSetChanged()
    }

    class ViewHolder(itemview: View): RecyclerView.ViewHolder(itemview){

        fun bindview(klm : WordsModel){
           itemView.textviewRecycler.text = klm.wordModel
        }

    }
    fun setOnClickItem(callback: ((WordsModel) -> Unit)?){
        this.onClickItem =callback
    }
    fun setOnClickDeleteItem(callback : ((WordsModel) -> Unit)?){
        this.onClickDeleteItem =callback
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_design, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int = klmList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val klm = klmList[position]
        holder.bindview(klm)
        holder.itemView.setOnClickListener { onClickItem?.invoke(klm) }
        holder.itemView.btnDelete.setOnClickListener { onClickDeleteItem?.invoke(klm) }
    }

}