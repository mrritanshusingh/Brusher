package com.mrrit.brusher

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class BookmarksFrag(val bookmarksList : ArrayList<BookmarksDS>, val mainActivity: MainActivity) : Fragment() {

    

    lateinit var newBMTV : TextView
    lateinit var recycler: RecyclerView
    val dbCenter : DBCenter by lazy { DBCenter.getDBCenter(requireContext()) }
    val data : ArrayList<BookmarksDS> = bookmarksList
    val adapter : BookmarksFragRecyAdapter = BookmarksFragRecyAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_bookmarks, container, false)
        newBMTV = root.findViewById(R.id.frag_bookmarks_new_bm_tv)
        recycler = root.findViewById(R.id.frag_bookmarks_recy)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        return root
    }

    fun removeBtnClicked(pos : Int){
        if(dbCenter.deleteFromBookmarksViaID(data[pos].id)){
            data.removeAt(pos)
            adapter.notifyItemRemoved(pos)
            mainActivity.isThisWebPageBookMarked = false
        }
    }
    fun titleClicked(pos : Int){
        mainActivity.loadUrlRequestFromBookmarksFrag(data[pos].url)
    }
    fun subTitleClicked(pos : Int){
        mainActivity.loadUrlRequestFromBookmarksFrag(data[pos].url)
    }



    fun getDataFromDB(){
        data.clear()
        dbCenter.getAllBookmarks()?.let {
            data.addAll(it)
        }
    }

    inner class BookmarksFragRecyAdapter : RecyclerView.Adapter<BookmarksFragRecyAdapter.BookmarksFragRecyViewHolder>(){

        inner class BookmarksFragRecyViewHolder : ViewHolder{
            lateinit var title : TextView
            lateinit var subTitle : TextView
            lateinit var removeBtn : ImageButton
            constructor(item : View): super(item){
                title = item.findViewById(R.id.bmview_single_item_title)
                subTitle = item.findViewById(R.id.bmview_single_item_subtitle)
                removeBtn = item.findViewById(R.id.bmview_single_item_remove_btn)
                removeBtn.setOnClickListener {
                    removeBtnClicked(adapterPosition)
                }
                title.setOnClickListener {
                    titleClicked(adapterPosition)
                }
                subTitle.setOnClickListener {
                    subTitleClicked(adapterPosition)
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BookmarksFragRecyViewHolder {
            return BookmarksFragRecyViewHolder(layoutInflater.inflate(R.layout.bmview_single_item,parent,false))
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: BookmarksFragRecyViewHolder, position: Int) {
            holder.title.text = mainActivity.getFirstNCharString(data[position].title, 20)
            holder.subTitle.text = mainActivity.getFirstNCharString(data[position].url, 40)
        }
    }


}