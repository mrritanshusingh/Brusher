package com.mrrit.brusher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

class TabsRecyAdapter(val data : ArrayList<BrowseTabsInstanceInfo>, val context: MainActivity) : RecyclerView.Adapter<TabsRecyAdapter.TabsRecyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabsRecyViewHolder =
        TabsRecyViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.recy_view_single_item,parent,false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: TabsRecyViewHolder, position: Int) {
        holder.titleTv.text = getFirstNCharString(data[position].title, 15 )
        holder.setOnClickCallBackOnText()
        holder.setOnClickCallBackOnImage()
        if(position == context.currActiveTabPosition){
            holder.highlightTab()
        }else{
            holder.unhighlightTab()
        }
    }

    fun getFirstNCharString(str : String, maxLen : Int) : String{
        if(str.length <= maxLen){
            return str
        }
        return str.take(maxLen) + "..."
    }


    inner class TabsRecyViewHolder(var v : View) : RecyclerView.ViewHolder(v) {
        lateinit var titleTv : TextView
        lateinit var removeBtn : ImageButton

        init {
            titleTv = v.findViewById(R.id.recy_single_item_title)
            removeBtn = v.findViewById(R.id.recy_single_item_remove_btn)
        }
       fun setOnClickCallBackOnText(){
           titleTv.setOnClickListener { this@TabsRecyAdapter.context.itemClickedInTabsUI(adapterPosition) }

       }
       fun setOnClickCallBackOnImage(){
           removeBtn.setOnClickListener { this@TabsRecyAdapter.context.removeBtnClickedInTabsUI(adapterPosition) }
       }
        fun highlightTab(){
            titleTv.background = ResourcesCompat.getDrawable(
                this@TabsRecyAdapter.context.resources,R.drawable.ripple_effect_highlighted,
                this@TabsRecyAdapter.context.theme
            )
        }
        fun unhighlightTab(){
            titleTv.background = ResourcesCompat.getDrawable(
                this@TabsRecyAdapter.context.resources,R.drawable.ripple_effect,
                this@TabsRecyAdapter.context.theme
            )
        }
    }


}

