package com.mrrit.brusher

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcel
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrrit.brusher.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.MalformedURLException
import java.net.URL


class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding
    var wereTabsRestoredFromStorage = false
    var data: ArrayList<BrowseTabsInstanceInfo> = ArrayList<BrowseTabsInstanceInfo>()
    var oldActiveTabPosition = -1
    var currActiveTabPosition: Int = -1
        get() {
            return field ?: -1
        }
        set(value) {
            oldActiveTabPosition = field
            field = value
        }
    lateinit var currActiveFrag: Fragment
    lateinit var currActiveTab: BrowseTabsInstanceInfo
    var popUpFragList: ArrayList<Fragment> = ArrayList<Fragment>()
    var popUpFragInfoObjList: ArrayList<BrowseTabsInstanceInfo> =
        ArrayList<BrowseTabsInstanceInfo>()
    var adapter: TabsRecyAdapter = TabsRecyAdapter(data, this)
    val dbCenter: DBCenter by lazy { DBCenter.getDBCenter(this) }
    val bookmarksList : ArrayList<BookmarksDS> = ArrayList<BookmarksDS>()
    var isThisWebPageBookMarked = false
    var bookmarkingStatusLastCheckedForUrl = ""
    lateinit var srdprfs : SharedPreferences
    // Following state var is for category of currently visible fragment
    // -1 -> Not Initialized
    // 0 -> Browse Frag
    // 1 -> Bookmarks Frag
    // 2 -> History Frag
    // 3 -> Settings Frag
    var currentVisibleFragCategory = -1
    var bookmarksFragInstance : BookmarksFrag? = null
    var historyFragInstance : HistoryFrag? = null
    var settingsFragInstance : SettingsFrag? = null
    var visibleFragCategoryOnLastClose = -1
    var shouldOpenInDesktopMode = false
    val EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        val fi = File(filesDir, "TABS_INFO_ARRAY_LIST")
         srdprfs = getSharedPreferences("SAVE_FILE", MODE_PRIVATE)

        if (fi.exists()) {

            val fis: FileInputStream = FileInputStream(fi)
            val ois = ObjectInputStream(fis)
            val saveData: ByteArray = ois.readObject() as (ByteArray)
            val pcl = Parcel.obtain()
            pcl.unmarshall(saveData, 0, saveData.size)
            pcl.setDataPosition(0)
            pcl.createTypedArrayList(BrowseTabsInstanceInfo.CREATOR)?.let {
                data = it
                wereTabsRestoredFromStorage = true
                adapter = TabsRecyAdapter(data, this)

            }
            pcl.recycle()
            ois.close()
            fis.close()



            currActiveTabPosition = srdprfs.getInt("ACTIVE_TAB_POSITION", -1)
            if (currActiveTabPosition != -1) {
                currActiveTab = data[currActiveTabPosition]
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        setUpInitials()

        if (wereTabsRestoredFromStorage && savedInstanceState != null) {
            bind.activityMainTabBtnTv.text = data.size.toString()
            currentVisibleFragCategory = 0
        } else if (wereTabsRestoredFromStorage) {
            bind.activityMainTabBtnTv.text = data.size.toString()
            setUpInitialsIfSaveFilePresentAndNoSavedState()

        } else {
            setUpInitialTabs()
        }


//        if(savedInstanceState != null){
//
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//                currActiveTab = savedInstanceState.getParcelable("CURRENT_FRAG",BrowseTabsInstanceInfo::class.java) ?: BrowseTabsInstanceInfo("Google","https://www.google.com", null)
//            }else{
//                currActiveTab = savedInstanceState.getParcelable("CURRENT_FRAG") ?: BrowseTabsInstanceInfo("Google","https://www.google.com", null)
//            }
//            data.add(currActiveTab)
//            adapter.notifyItemInserted(0)
//            currActiveTabPosition = 0
//            currActiveFrag = BrowseFrag(this, currActiveTab)
//            currActiveFrag.setInitialSavedState(currActiveTab.savedState)
//            showFrag(currActiveFrag)
//
//
//        }else{
//
//        }


    }


    fun setUpInitials() {



        shouldOpenInDesktopMode = srdprfs.getBoolean("shouldOpenInDesktopMode", false)
        getLatestBookmarks()
        bind.activityMainHomeBtn.visibility = View.GONE
        bind.activityMainHomeBtn.setOnClickListener {
            (currActiveFrag as BrowseFrag).loadUrl("https://www.google.com")
            bind.activityMainHomeBtn.visibility = View.GONE
        }
        bind.topToolBarMenuBtn.setOnClickListener { v ->

            if(!bind.activityMainTabOverlay.isVisible)  showPopUp(v)

        }

        bind.activityMainTabBtn.setOnClickListener {
            toggleVisibility(bind.activityMainTabOverlay)
        }

        bind.newTabBtnTabUi.setOnClickListener {
            toggleVisibility(bind.activityMainTabOverlay)
            newTabBtnClickedTabUI()
        }



        bind.activityMainUrlBar.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(bind.activityMainUrlBar.windowToken, 0)
                bind.activityMainUrlBar.clearFocus()
                if (isStringURI(bind.activityMainUrlBar.text.toString())) {
                    searchBarUrlInputRecieved(bind.activityMainUrlBar.text.toString())
                } else {
                    if (bind.activityMainUrlBar.text.toString().trim() != "") {
                        searchBarSearchInputRecieved(bind.activityMainUrlBar.text.toString())
                    }
                }
                true
            } else {
                false
            }

        }

        bind.activityMainRecy.layoutManager = LinearLayoutManager(this)
        bind.activityMainRecy.adapter = adapter
        onBackPressedDispatcher.addCallback {
            backPressed()
        }
    }

    fun setUpInitialsIfSaveFilePresentAndNoSavedState() {
        currActiveFrag = BrowseFrag(this, currActiveTab)
        currActiveFrag.setInitialSavedState(currActiveTab.savedState)
        showFrag(currActiveFrag)
    }


    fun setUpInitialTabs() {
        currActiveTab = BrowseTabsInstanceInfo("Google", "https://www.google.com", null)
        data.add(currActiveTab)
        currActiveTabPosition = 0
        loadFragOnScreen(currActiveTab)
    }

    private fun showPopUp(v: View) {
        if(bookmarkingStatusLastCheckedForUrl != currActiveTab.url){
            if (isUrlInBookmarksList(currActiveTab.url)){
                isThisWebPageBookMarked = true
                bookmarkingStatusLastCheckedForUrl = currActiveTab.url
            }else{
                isThisWebPageBookMarked = false
                bookmarkingStatusLastCheckedForUrl = currActiveTab.url
            }
        }
        val vi: View = layoutInflater.inflate(R.layout.custom_main_menu, bind.root, false)
        val p: PopupWindow = PopupWindow(
            vi,
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true
        )
        p.showAsDropDown(v, 0, -1 * bind.topToolBarMenuBtn.height)
        vi.findViewById<TextView>(R.id.new_tab_btn_main_menu).setOnClickListener {
            p.dismiss()
            newTabBtnClickedMainMenu()
        }

        vi.findViewById<ImageButton>(R.id.info_btn_main_menu).setOnClickListener {
            p.dismiss()
            val par = Parcel.obtain()
            data[0].writeToParcel(par, 0)
            makeToast("Parcel Size is : ${par.dataSize()}")
            par.recycle()
        }

        vi.findViewById<ImageButton>(R.id.forward_btn_main_menu).setOnClickListener {
            p.dismiss()
            (currActiveFrag as BrowseFrag).goForward()
        }

        vi.findViewById<ImageButton>(R.id.reload_btn_main_menu).setOnClickListener {
            p.dismiss()
            (currActiveFrag as BrowseFrag).reload()
        }

        vi.findViewById<TextView>(R.id.bookmarks_text_btn_main_menu).setOnClickListener {
            p.dismiss()
            bookmarkTextBtnClicked()
        }
        vi.findViewById<TextView>(R.id.history_text_btn_main_menu).setOnClickListener {
            p.dismiss()
            historyTextBtnClicked()
        }

        vi.findViewById<TextView>(R.id.settings_text_btn_main_menu).setOnClickListener {
            p.dismiss()
            settingsTextBtnClicked()
        }

        vi.findViewById<TextView>(R.id.print_text_btn_main_menu).setOnClickListener {
            p.dismiss()
            (currActiveFrag as BrowseFrag).printRequest()
        }
        vi.findViewById<ImageButton>(R.id.downloads_btn_main_menu).setOnClickListener {
            p.dismiss()
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        }

        val desktopBtn = vi.findViewById<TextView>(R.id.desktop_text_btn_main_menu)
        if(shouldOpenInDesktopMode){
            desktopBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.checked_box), null, null, null)
        }else{
            desktopBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.empty_check_box), null, null, null)
        }

        desktopBtn.setOnClickListener {
            p.dismiss()
            shouldOpenInDesktopMode = !shouldOpenInDesktopMode
            (currActiveFrag as BrowseFrag).reload()
        }

        val bookmarkBtn = vi.findViewById<ImageButton>(R.id.bookmark_btn_main_menu)

        if(isThisWebPageBookMarked){
            bookmarkBtn.setImageResource(R.drawable.bookmark_final)
        }
        bookmarkBtn.setOnClickListener {
            p.dismiss()
            bookmarkBtnClicked()
        }


    }

    // Used to toggle visibility of any given view
    private fun toggleVisibility(v: View) {
        if (v.visibility == View.VISIBLE) {
            v.visibility = View.GONE
        } else {
            v.visibility = View.VISIBLE
        }
    }

    fun itemClickedInTabsUI(position: Int) {
        if (currActiveTabPosition != position) {
            loadInactiveTab(position)
            highlightCurrentActiveTab()
        }
    }

    fun removeBtnClickedInTabsUI(position: Int) {
        removeTab(position)

    }

    private fun searchBarSearchInputRecieved(searchTerm: String) {
        val url = "https://www.google.com/search?q=" + searchTerm
        (currActiveFrag as BrowseFrag).loadUrl(url)
    }

    private fun searchBarUrlInputRecieved(url: String) {
        (currActiveFrag as BrowseFrag).loadUrl(url)
    }

    fun backPressed() {
        if(currentVisibleFragCategory == 0){
            if(!(currActiveFrag as BrowseFrag).goBack()){
                finish()
            }
        }else if(currentVisibleFragCategory == 1 && bookmarksFragInstance != null){
            removeBookmarksFrag()
        }else if(currentVisibleFragCategory == 2 && historyFragInstance != null){
            removeHistoryFrag()
        }else if(currentVisibleFragCategory == 3&& settingsFragInstance != null){
            removeSettingsFrag()
        }

    }

    // Before calling bookMarkedBtnClicked function , set the correct state of variables isThisWebPageBookMarked  and bookmarkingStatusLastCheckedForUrl

    fun bookmarkBtnClicked(){
        if(isThisWebPageBookMarked){
            dbCenter.deleteFromBookmarksViaUrl(currActiveTab.url)
            getLatestBookmarks()
            isThisWebPageBookMarked = false
        }else{
            dbCenter.addToBookmarks(currActiveTab.title,currActiveTab.url)
            getLatestBookmarks()
            isThisWebPageBookMarked = true
        }
    }

    fun bookmarkTextBtnClicked(){



        if(currentVisibleFragCategory == 0 && bookmarksFragInstance == null){
            showBookmarksFrag()
        }else if(currentVisibleFragCategory == 1 && bookmarksFragInstance != null){
            removeBookmarksFrag()
        }
    }

    fun historyTextBtnClicked(){

        if(currentVisibleFragCategory == 0 && historyFragInstance == null){
            showHistoryFrag()
        }else if(currentVisibleFragCategory == 2 && historyFragInstance != null){
            removeBookmarksFrag()
        }

    }

    fun settingsTextBtnClicked(){
        if(currentVisibleFragCategory == 0 && settingsFragInstance == null){
            showSettingsFrag()
        }else if(currentVisibleFragCategory == 3 && settingsFragInstance != null){
            removeSettingsFrag()
        }

    }




    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)
        outState.putParcelable("CURRENT_FRAG", currActiveTab)

    }


    override fun onPause() {
        super.onPause()
        currActiveTab.savedState = supportFragmentManager.saveFragmentInstanceState(currActiveFrag)
        val srdprfs = getSharedPreferences("SAVE_FILE", MODE_PRIVATE)
        val srdprfsEditor = srdprfs.edit()


        if(currentVisibleFragCategory == 1 && bookmarksFragInstance != null){
            removeBookmarksFrag()
            srdprfsEditor.putInt("VISIBLE_FRAG_CATEGORY", 1)
        }else if(currentVisibleFragCategory == 2 && historyFragInstance != null){
            removeHistoryFrag()
            srdprfsEditor.putInt("VISIBLE_FRAG_CATEGORY", 2)
        }else if(currentVisibleFragCategory == 3 && settingsFragInstance != null){
            removeSettingsFrag()
            srdprfsEditor.putInt("VISIBLE_FRAG_CATEGORY", 3)
        }else if(currentVisibleFragCategory == 0 ){
            srdprfsEditor.putInt("VISIBLE_FRAG_CATEGORY", 0)
        }

        srdprfsEditor.putBoolean("shouldOpenInDesktopMode", shouldOpenInDesktopMode)

        srdprfsEditor.apply()

    }

    override fun onResume() {
        super.onResume()
        if(visibleFragCategoryOnLastClose == 1){
            showBookmarksFrag()
        }else if(visibleFragCategoryOnLastClose == 2){
            showHistoryFrag()
        }else if(visibleFragCategoryOnLastClose == 3){
            showSettingsFrag()
        }


    }

    override fun onStop() {
        super.onStop()
        val fos: FileOutputStream = FileOutputStream(File(filesDir, "TABS_INFO_ARRAY_LIST"))
        val pcl = Parcel.obtain()
        pcl.writeTypedList(data)
        val saveBytes: ByteArray = pcl.marshall()
        pcl.recycle()
        val oos: ObjectOutputStream = ObjectOutputStream(fos)
        oos.writeObject(saveBytes)
        oos.close()
        fos.close()
        val srdprfs = getSharedPreferences("SAVE_FILE", MODE_PRIVATE)
        val srdprfsEditor = srdprfs.edit()
        srdprfsEditor.putInt("ACTIVE_TAB_POSITION", currActiveTabPosition)

        srdprfsEditor.apply()

    }

    override fun onStart() {
        super.onStart()
        visibleFragCategoryOnLastClose = srdprfs.getInt("VISIBLE_FRAG_CATEGORY", -1)

    }

    fun makeToast(msg: String = "DEFAULT_MSG") = Toast.makeText(
        this, msg,
        Toast.LENGTH_SHORT
    ).show()

    fun showFragRemovingPrevious(frag: Fragment) {
        supportFragmentManager.findFragmentById(R.id.parent_frag_container)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        supportFragmentManager.beginTransaction().add(R.id.parent_frag_container, frag).commit()
        bind.activityMainTabBtnTv.text = data.size.toString()
        currentVisibleFragCategory = 0

    }


    fun showFrag(frag: Fragment) {
        supportFragmentManager.beginTransaction().add(R.id.parent_frag_container, frag).commit()
        bind.activityMainTabBtnTv.text = data.size.toString()
        currentVisibleFragCategory = 0

    }

    fun removePreviousFrag() {
        supportFragmentManager.findFragmentById(R.id.parent_frag_container)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        bind.activityMainTabBtnTv.text = data.size.toString()

    }

    fun removeFrag(frag: Fragment) {
        supportFragmentManager.beginTransaction().remove(frag).commit()
        bind.activityMainTabBtnTv.text = data.size.toString()

    }

    fun loadFragOnScreen(inf: BrowseTabsInstanceInfo) {

        val f: Fragment = BrowseFrag(this, inf)
        if (inf.savedState != null) {
            f.setInitialSavedState(inf.savedState)
        }
        currActiveFrag = f
        showFragRemovingPrevious(f)
    }

    fun loadFragOnScreenWithoutRemovingPrevious(inf: BrowseTabsInstanceInfo) {
        var f: Fragment = BrowseFrag(this, inf)
        if (inf.savedState != null) {
            f.setInitialSavedState(inf.savedState)
        }
        currActiveFrag = f
        showFrag(f)
    }

    fun loadFragOnScreenWithoutRemovingPreviousWithWV(
        inf: BrowseTabsInstanceInfo,
        webView: WebView
    ) {
        var f: Fragment = BrowseFrag(webView, this, inf)
        if (inf.savedState != null) {
            f.setInitialSavedState(inf.savedState)
        }
        currActiveFrag = f
        showFrag(f)
    }

    fun addNewTabAndLoad() {
        bind.activityMainUrlBar.setText("Home")
        currActiveTab = BrowseTabsInstanceInfo("Google", "https://www.google.com", null)
        data.add(currActiveTab)
        currActiveTabPosition = data.size - 1
        adapter.notifyItemInserted(currActiveTabPosition)
        loadFragOnScreen(currActiveTab)
        highlightCurrentActiveTab()
    }

    fun addNewTabAndLoadWithoutRemovingPrevious() {
        bind.activityMainUrlBar.setText("Home")
        currActiveTab = BrowseTabsInstanceInfo("Google", "https://www.google.com", null)
        data.add(currActiveTab)
        currActiveTabPosition = data.size - 1
        adapter.notifyItemInserted(currActiveTabPosition)
        loadFragOnScreenWithoutRemovingPrevious(currActiveTab)
        highlightCurrentActiveTab()
    }

    fun addNewTabAndLoadWithoutRemovingPreviousWithWV(webView: WebView) {
        bind.activityMainUrlBar.setText("Home")
        currActiveTab = BrowseTabsInstanceInfo("Google", "https://www.google.com", null)
        data.add(currActiveTab)
        currActiveTabPosition = data.size - 1
        adapter.notifyItemInserted(currActiveTabPosition)
        loadFragOnScreenWithoutRemovingPreviousWithWV(currActiveTab, webView)
        highlightCurrentActiveTab()

    }

    fun removeTab(pos: Int) {
        if (pos >= 0 && pos < data.size) {
            if (pos == currActiveTabPosition) {
                if (pos == 0 && data.size > 1) {
                    loadInactiveTab(1)
                    data.removeAt(0)
                    adapter.notifyItemRemoved(0)
                    currActiveTabPosition = 0

                } else if (pos == 0 && data.size == 1) {
                    addNewTabAndLoad()
                    data.removeAt(0)
                    adapter.notifyItemRemoved(0)
                    currActiveTabPosition = 0
                } else {
                    loadInactiveTab(pos - 1)
                    data.removeAt(pos)
                    adapter.notifyItemRemoved(pos)
                }
            } else {
                data.removeAt(pos)
                adapter.notifyItemRemoved(pos)
                if (currActiveTabPosition > pos) {
                    currActiveTabPosition--
                }
            }
        }
        bind.activityMainTabBtnTv.text = data.size.toString()
        highlightCurrentActiveTab()

    }

    fun saveTabState() {
        if (currActiveTabPosition != -1) {

            currActiveTab.savedState =
                supportFragmentManager.saveFragmentInstanceState(currActiveFrag)
        }
        bind.activityMainTabBtnTv.text = data.size.toString()


    }

    fun loadInactiveTab(tabPosition: Int) {
        saveTabState()
        if (tabPosition < data.size && tabPosition >= 0) {
            currActiveTabPosition = tabPosition
            currActiveTab = data[tabPosition]
            loadFragOnScreen(currActiveTab)
        }
        bind.activityMainTabBtnTv.text = data.size.toString()


    }

    fun newTabBtnClickedTabUI() {
        saveTabState()
        addNewTabAndLoad()
    }

    fun newTabBtnClickedMainMenu() {
        saveTabState()
        addNewTabAndLoad()
    }

    fun initiatePopUpWindowRequest(webView: WebView) {
        popUpFragList.add(currActiveFrag)
        popUpFragInfoObjList.add(currActiveTab)
        addNewTabAndLoadWithoutRemovingPreviousWithWV(webView)
        finalizePopUpWindowRequest()
        highlightCurrentActiveTab()
    }

    fun finalizePopUpWindowRequest() {
        for ((index, element) in popUpFragList.withIndex()) {
            saveFragmentInstanceStateInTabsInfoObj(element, popUpFragInfoObjList[index])
            removeFrag(element)
            popUpFragList.removeAt(index)
            popUpFragInfoObjList.removeAt(index)
        }
    }

    fun saveFragmentInstanceStateInTabsInfoObj(frag: Fragment, infoObj: BrowseTabsInstanceInfo) {
        infoObj.savedState = supportFragmentManager.saveFragmentInstanceState(frag)
    }

    fun findIndexOfTabInfoObj(item: BrowseTabsInstanceInfo): Int {
        var counter = 0
        while (data[counter] != item) {
            if (counter < data.size - 1) {
                counter++
            } else {
                return -1
            }
        }
        return counter
    }

    fun tabMetaDataUpdated(tab: BrowseTabsInstanceInfo) {
        var index = findIndexOfTabInfoObj(tab)
        if (index >= 0 && index < data.size) {
            adapter.notifyItemChanged(index)
            bind.activityMainUrlBar.setText(tab.url)
        }
    }

    fun getFirstNCharString(str: String, maxLen: Int): String {
        if (str.length <= maxLen) {
            return str
        }
        return str.take(maxLen) + "..."
    }

    fun isStringURI(str: String): Boolean {
        return try {
            URL(str)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    fun setProgressBarVisible() {
        bind.loadingProgressBar.visibility = View.VISIBLE
    }

    fun setProgressBarInvisible() {
        bind.loadingProgressBar.visibility = View.GONE
    }

    fun updateProgressBar(progress: Int) {
        bind.loadingProgressBar.progress = progress
    }

    fun restoreDataFromFragSide(info: BrowseTabsInstanceInfo, frag: Fragment) {

        currActiveTab = info
        data.add(currActiveTab)

        currActiveTabPosition = data.size - 1
        adapter.notifyItemInserted(currActiveTabPosition)
        currActiveFrag = frag
        highlightCurrentActiveTab()

    }

    fun getTabInsAndSetCFrag(frag: Fragment): BrowseTabsInstanceInfo {
        currActiveFrag = frag
        if (currActiveTabPosition != -1) {

            currActiveTab = data[currActiveTabPosition]
            highlightCurrentActiveTab()
            return currActiveTab
        } else {
            return BrowseTabsInstanceInfo("Google", "https://www.google.com", null)
        }
    }

    fun setHomeBtnInTopBarVisible() {
        bind.activityMainHomeBtn.visibility = View.VISIBLE
    }

    fun setHomeBtnInTopBarInvisible() {
        bind.activityMainHomeBtn.visibility = View.GONE
    }

    fun highlightCurrentActiveTab() {
        if (oldActiveTabPosition >= 0 && oldActiveTabPosition < data.size) {
            adapter.notifyItemChanged(oldActiveTabPosition)
        }
        adapter.notifyItemChanged(currActiveTabPosition)
    }

    fun getLatestBookmarks(){
        bookmarksList.clear()
        dbCenter.getAllBookmarks()?.let { bookmarksList.addAll(it) }
    }

    fun isUrlInBookmarksList(url : String) : Boolean{
        for (item in bookmarksList){
            if (item.url == url) return true
        }
        return false
    }

    fun showBookmarksFrag(){
        if(bookmarksFragInstance != null) return
        bind.topToolBar.visibility = View.GONE
        bind.activityMainTabOverlay.visibility = View.GONE
        bookmarksFragInstance = BookmarksFrag(bookmarksList, this)
        supportFragmentManager.beginTransaction().add(R.id.parent_frag_container,bookmarksFragInstance!!).commit()
        supportFragmentManager.beginTransaction().hide(currActiveFrag).commit()
        currentVisibleFragCategory = 1
    }

    fun removeBookmarksFrag(){
        bookmarksFragInstance?.let {
            bind.topToolBar.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().remove(it).commit()
            supportFragmentManager.beginTransaction().show(currActiveFrag).commit()
            bookmarksFragInstance = null
            currentVisibleFragCategory = 0
        }


    }

    fun loadUrlRequestFromBookmarksFrag(url : String){
        removeBookmarksFrag()
        (currActiveFrag as BrowseFrag).loadUrl(url)
    }

    fun showHistoryFrag(){
        if(historyFragInstance != null) return
        bind.topToolBar.visibility = View.GONE
        bind.activityMainTabOverlay.visibility = View.GONE
        historyFragInstance = HistoryFrag(this)
        supportFragmentManager.beginTransaction().add(R.id.parent_frag_container,historyFragInstance!!).commit()
        supportFragmentManager.beginTransaction().hide(currActiveFrag).commit()
        currentVisibleFragCategory = 2
    }

    fun removeHistoryFrag(){
        historyFragInstance?.let {
            bind.topToolBar.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().remove(it).commit()
            supportFragmentManager.beginTransaction().show(currActiveFrag).commit()
            historyFragInstance = null
            currentVisibleFragCategory = 0
        }
    }

    fun showSettingsFrag(){
        if(settingsFragInstance != null) return
        bind.topToolBar.visibility = View.GONE
        bind.activityMainTabOverlay.visibility = View.GONE
        settingsFragInstance = SettingsFrag(this)
        supportFragmentManager.beginTransaction().add(R.id.parent_frag_container,settingsFragInstance!!).commit()
        supportFragmentManager.beginTransaction().hide(currActiveFrag).commit()
        currentVisibleFragCategory = 3
    }

    fun removeSettingsFrag(){
        settingsFragInstance?.let {
            bind.topToolBar.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().remove(it).commit()
            supportFragmentManager.beginTransaction().show(currActiveFrag).commit()
            settingsFragInstance = null
            currentVisibleFragCategory = 0
        }
    }

    fun loadUrlRequestFromHistoryFrag(url: String){
        removeHistoryFrag()
        (currActiveFrag as BrowseFrag).loadUrl(url)
    }


}