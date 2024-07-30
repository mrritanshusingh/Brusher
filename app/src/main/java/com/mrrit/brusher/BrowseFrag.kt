package com.mrrit.brusher


import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar


class BrowseFrag: Fragment {
    var wasAutoInitialized = false
    lateinit var mainActivity : MainActivity
    lateinit var info: BrowseTabsInstanceInfo
    lateinit var browseFragWebView : WebView
    var chromeClient: CustomChromeClient = CustomChromeClient()
    var webViewClient : CustomWebViewClient = CustomWebViewClient()
    lateinit var webView : WebView
    var shouldBeInitializedViaWebView = false
    val FILE_CHOOSER_REQUEST_CODE = 1;
    var uploadMessage : ValueCallback<Array<Uri>>? = null
    val desktopUserAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"

    constructor(webView : WebView, mainActivity: MainActivity, info: BrowseTabsInstanceInfo) : super() {
        this.mainActivity = mainActivity
        this.info = info
        shouldBeInitializedViaWebView = true
        this.webView = webView
        browseFragWebView = webView
    }
    constructor(mainActivity: MainActivity,info: BrowseTabsInstanceInfo) : super() {
        this.mainActivity = mainActivity
        this.info = info
    }

    constructor() : super(){
        wasAutoInitialized = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        var root : View? = null


        if(wasAutoInitialized){
            root = layoutInflater.inflate(R.layout.fragment_browse,container,false)
            mainActivity = requireActivity() as MainActivity
            if(mainActivity.wereTabsRestoredFromStorage && mainActivity.currActiveTabPosition != -1){
                info = mainActivity.getTabInsAndSetCFrag(this)
            }else{
                info = BrowseTabsInstanceInfo("Google","https://www.google.com",null)
                mainActivity.restoreDataFromFragSide(info,this)
            }

            root?.let {
                browseFragWebView = it.findViewById(R.id.browse_frag_web_view)
                browseFragWebView.webChromeClient = chromeClient
                browseFragWebView.webViewClient = webViewClient
                setupWebViewInitialSettings(browseFragWebView)
                if(savedInstanceState != null){
                    browseFragWebView.restoreState(savedInstanceState)
                }else{
                    browseFragWebView.loadUrl("https://www.google.com")
                }
            }

        }else{
            if(shouldBeInitializedViaWebView){



                context?.let {
                    // Create FrameLayout
                    val frameLayout = FrameLayout(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    // Create WebView
                    browseFragWebView.apply {
                        id = View.generateViewId()
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }

                    // Add WebView to FrameLayout
                    frameLayout.addView(browseFragWebView)
                    browseFragWebView.webChromeClient = chromeClient
                    browseFragWebView.webViewClient = webViewClient
                    setupWebViewInitialSettings(browseFragWebView)
//                if(savedInstanceState != null){
//                    browseFragWebView.restoreState(savedInstanceState)
//                }else{
//                    browseFragWebView.loadUrl("https://www.google.com")
//                }
                    root = frameLayout

                }

            }else{
                root = layoutInflater.inflate(R.layout.fragment_browse,container,false)
                root?.let {
                    browseFragWebView = it.findViewById(R.id.browse_frag_web_view)
                    browseFragWebView.webChromeClient = chromeClient
                    browseFragWebView.webViewClient = webViewClient
                    setupWebViewInitialSettings(browseFragWebView)
                    if(savedInstanceState != null){
                        browseFragWebView.restoreState(savedInstanceState)
                    }else{
                        browseFragWebView.loadUrl("https://www.google.com")
                    }
                }

            }



        }




        return root ?: FrameLayout(container!!.context)
    }

    fun setupWebViewInitialSettings(webView: WebView){
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        if(mainActivity.shouldOpenInDesktopMode){
            browseFragWebView.settings.userAgentString = desktopUserAgent
        }else{
            browseFragWebView.settings.userAgentString = WebSettings.getDefaultUserAgent(mainActivity)
        }
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        WebView.setWebContentsDebuggingEnabled(true)

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->

            val request =  DownloadManager.Request(Uri.parse(url));

            // Configure the request
            request.setMimeType(mimetype);
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading file...");
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));



            // Set the download directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));


            // Make the download visible and manageable
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Get the DownloadManager service and enqueue the request
            val downloadManager =  mainActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request);

            Toast.makeText(mainActivity , "Downloading File", Toast.LENGTH_LONG).show();


        }

    }



    override fun onStop() {
        super.onStop()
        val cookieManager = CookieManager.getInstance()
        cookieManager.flush() // Ensure cookies are saved immediately

    }

    override fun onResume() {
        super.onResume()
        if (wasAutoInitialized){
            mainActivity = requireActivity() as MainActivity
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        browseFragWebView.saveState(outState)
    }

    fun printRequest(){
        val printManager = mainActivity.getSystemService(Context.PRINT_SERVICE) as PrintManager
        browseFragWebView.evaluateJavascript(
            "javascript:(function() { " +
                    "var elements = document.getElementsByTagName('*'); " +
                    "for (var i = 0; i < elements.length; i++) { " +
                    "  elements[i].style.backgroundColor = 'white';" +
                    "elements[i].style.color = 'black'; " +
                    "} " +
                    "})()" , null
        )
        val printAdapter = browseFragWebView.createPrintDocumentAdapter("Document")
        val jobName = getString(R.string.app_name) + " Print Test"
        printManager.print(
            jobName,
            printAdapter,
            PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME).build()
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (uploadMessage == null) {
                return
            }

            val result = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            uploadMessage!!.onReceiveValue(result)
            uploadMessage = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    fun getAssociatedWebView() : WebView = browseFragWebView

    fun loadUrl(url : String){
        if(this.isResumed){
            browseFragWebView.loadUrl(url)
            info.url = url
        }
    }

    fun goBack() : Boolean{
        if(this.isResumed){
           if(browseFragWebView.canGoBack()){
               browseFragWebView.goBack()
               return true
           }else{
               return false
           }
        }else{
            return false
        }
    }

    fun goForward() : Boolean{
        if (this.isResumed){
            if(browseFragWebView.canGoForward()){
                browseFragWebView.goForward()
                return true
            }
        }
        return false
    }
    fun reload() : Boolean{
        if(this.isResumed){
            if(mainActivity.shouldOpenInDesktopMode){
                browseFragWebView.settings.userAgentString = desktopUserAgent
            }else{
                browseFragWebView.settings.userAgentString = WebSettings.getDefaultUserAgent(mainActivity)
            }
            browseFragWebView.reload()
            return true
        }
        return false
    }

     inner class CustomChromeClient: WebChromeClient(){
         var mCustomView : View? = null
         private var mCustomViewCallback: CustomViewCallback? = null
         var addedFrameLayout : FrameLayout? = null

         override fun onShowFileChooser(
             webView: WebView?,
             filePathCallback: ValueCallback<Array<Uri>>?,
             fileChooserParams: FileChooserParams?
         ): Boolean {

            uploadMessage?.let {
                    it.onReceiveValue(null);
                    uploadMessage = null
            }

             uploadMessage = filePathCallback;
             fileChooserParams?.let {
                 val intent = it.createIntent()
                 try {
                     this@BrowseFrag.startActivityForResult(intent,FILE_CHOOSER_REQUEST_CODE)
                 } catch (e : Exception) {
                     uploadMessage = null;
                     Toast.makeText(mainActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                     return false;
                 }

                 return true

             }

             return false
         }

         override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
             super.onShowCustomView(view, callback)
             if(mCustomView != null){
                 callback?.onCustomViewHidden()
             }

             // Set up the custom view container
             val mFullScreenContainer = FrameLayout(mainActivity)
             mFullScreenContainer.addView(
                 view, FrameLayout.LayoutParams(
                     ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.MATCH_PARENT
                 )
             )

             mainActivity.bind.activityMainFirstFrameLayout.addView(
                 mFullScreenContainer, FrameLayout.LayoutParams(
                     ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.MATCH_PARENT
                 )
             )

             mCustomView = view;
             mCustomViewCallback = callback;
             addedFrameLayout = mFullScreenContainer
             mainActivity.bind.activityMainRootFrameLayout.visibility = View.GONE
             mFullScreenContainer.visibility = View.VISIBLE


         }

         override fun onHideCustomView() {
             super.onHideCustomView()

             if (mCustomView == null) {
                 return;
             }

             // Remove the custom view container
             addedFrameLayout?.setVisibility(View.GONE);
             mainActivity.bind.activityMainFirstFrameLayout.removeView(addedFrameLayout);
             mCustomView = null;
             addedFrameLayout = null;
             mCustomViewCallback?.onCustomViewHidden();

             // Show the main content
             mainActivity.bind.activityMainRootFrameLayout.setVisibility(View.VISIBLE);

         }

         override fun onReceivedTitle(view: WebView?, title: String?) {
             super.onReceivedTitle(view, title)

             view?.let {
                 if(it.url != info.url){
                     it.url?.let {
                         info.url = it
                         title?.let {
                             info.title = it
                         }
                         mainActivity.tabMetaDataUpdated(info)

                         if(!(info.url.lowercase() == "https://www.google.com" || info.url.lowercase() == "https://www.google.com/" || info.url == "")){

                             val cal = Calendar.getInstance()
                             val date = cal.time
                             val dateFormat = SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a")
                             val dateStr = dateFormat.format(date)
                             mainActivity.dbCenter.addToHistory(info.title,info.url, dateStr)
                         }


                     }
                 }
             }




         }

         override fun onProgressChanged(view: WebView?, newProgress: Int) {
             super.onProgressChanged(view, newProgress)
             mainActivity.updateProgressBar(newProgress)
         }

         override fun onCreateWindow(
             view: WebView?,
             isDialog: Boolean,
             isUserGesture: Boolean,
             resultMsg: Message?
         ): Boolean {
             if(isUserGesture){
                 val newWebView = WebView(mainActivity)
                 newWebView.webChromeClient = WebChromeClient()
                 newWebView.webViewClient = WebViewClient()
                 newWebView.settings.javaScriptEnabled = true
                 // Setup the new WebView
                 val transport = resultMsg?.obj as WebView.WebViewTransport
                 transport.webView = newWebView
                 resultMsg.sendToTarget()
                 mainActivity.initiatePopUpWindowRequest(newWebView)
                 return true
             }

             return false
         }
     }



    inner class CustomWebViewClient() : WebViewClient(){
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            mainActivity.setProgressBarVisible()

            url?.let {
                if(it.lowercase() == "https://www.google.com" || it.lowercase() == "https://www.google.com/" ){
                    mainActivity.setHomeBtnInTopBarInvisible()
                }else{
                    mainActivity.setHomeBtnInTopBarVisible()
                }
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

            return false
        }



        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            mainActivity.setProgressBarInvisible()
            url?.let {
                info.url = it
                view?.title?.let {

                    info.title = it

                    mainActivity.tabMetaDataUpdated(info)

                    if(!(info.url.lowercase() == "https://www.google.com" || info.url.lowercase() == "https://www.google.com/" || info.url == "")){

                        val cal = Calendar.getInstance()
                        val date = cal.time
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a")
                        val dateStr = dateFormat.format(date)
                        mainActivity.dbCenter.addToHistory(info.title,info.url, dateStr)
                    }

                }


            }
        }
    }




}