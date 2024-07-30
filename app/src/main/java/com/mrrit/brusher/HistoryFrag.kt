package com.mrrit.brusher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HistoryFrag(val mainActivity: MainActivity) : Fragment() {
    private val data = mainActivity.dbCenter.getAllHistory() ?: ArrayList<HistoryDS>()
    private val dbCenter by lazy { DBCenter.getDBCenter(requireContext()) }
    private val adapter = HistoryRecyViewAdapter()
    lateinit var recyView : RecyclerView
    lateinit var clearHistoryBtn : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_history, container, false)
        clearHistoryBtn = v.findViewById(R.id.frag_history_clear_hs_tv)
        clearHistoryBtn.setOnClickListener {
            clearHistoryBtnClicked()
        }
        recyView = v.findViewById(R.id.frag_history_recy)
        recyView.layoutManager = LinearLayoutManager(requireContext())
        recyView.adapter = adapter
        return v
    }

    fun getAllHistory(){
        data.clear()
        dbCenter.getAllHistory()?.let {
            data.addAll(it)
        }
    }

    fun titleClicked(pos: Int){
        mainActivity.loadUrlRequestFromHistoryFrag(data[pos].url)
    }

    fun subtitleClicked(pos: Int){
        mainActivity.loadUrlRequestFromHistoryFrag(data[pos].url)
    }

    fun dateClicked(pos: Int){

    }

    fun removeBtnClicked(pos: Int){
        if(dbCenter.deleteFromHistoryViaID(data[pos].id)){
            data.removeAt(pos)
            adapter.notifyItemRemoved(pos)
        }
    }

    fun clearHistory(){
        if(mainActivity.dbCenter.clearHistory()){
            data.clear()
            adapter.notifyDataSetChanged()
        }
    }

    fun clearHistoryBtnClicked(){
        clearHistory()
    }


    private inner class HistoryRecyViewAdapter() : RecyclerView.Adapter<HistoryRecyViewAdapter.CustomHistoryRecyVH>(){

        private inner class CustomHistoryRecyVH(v : View) : RecyclerView.ViewHolder(v){
            val title : TextView = v.findViewById(R.id.hsview_single_item_title)
            val subtitle: TextView = v.findViewById(R.id.hsview_single_item_subtitle)
            val date: TextView = v.findViewById(R.id.hsview_single_item_date)
            val removeBtn: ImageButton = v.findViewById(R.id.hsview_single_item_remove_btn)

            init {
                removeBtn.setOnClickListener {
                    removeBtnClicked(adapterPosition)
                }
                title.setOnClickListener {
                    titleClicked(adapterPosition)
                }
                subtitle.setOnClickListener {
                    subtitleClicked(adapterPosition)
                }
                date.setOnClickListener {
                    dateClicked(adapterPosition)
                }
            }


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomHistoryRecyVH =  CustomHistoryRecyVH(layoutInflater.inflate(R.layout.hsview_single_item, parent,false))

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: CustomHistoryRecyVH, position: Int) {
            holder.title.text = mainActivity.getFirstNCharString(data[position].title, 20)
            holder.subtitle.text = mainActivity.getFirstNCharString(data[position].url, 40)
            holder.date.text = data[position].date
        }
    }


}