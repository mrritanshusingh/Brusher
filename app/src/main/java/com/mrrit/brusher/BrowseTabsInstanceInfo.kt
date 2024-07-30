package com.mrrit.brusher


import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable

class BrowseTabsInstanceInfo : Parcelable, Serializable{
    lateinit var title : String
    lateinit var url : String
    var savedState: Fragment.SavedState? = null
    constructor(title : String, url : String, savedState : Fragment.SavedState?){
        this.title = title
        this.url = url
        this.savedState = savedState
    }
    constructor(parcel: Parcel){
        parcel.readString()?.let {
            title = it
        }
        parcel.readString()?.let {
            url = it
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
           savedState = parcel.readParcelable(Fragment.SavedState::class.java.classLoader,Fragment.SavedState::class.java)
        }else{
           savedState = parcel.readParcelable<Fragment.SavedState>(Fragment.SavedState::class.java.classLoader)
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(url)
        parcel.writeParcelable(savedState, flags)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BrowseTabsInstanceInfo> {
        override fun createFromParcel(parcel: Parcel): BrowseTabsInstanceInfo {
            return BrowseTabsInstanceInfo(parcel)
        }

        override fun newArray(size: Int): Array<BrowseTabsInstanceInfo?> {
            return arrayOfNulls(size)
        }
    }




}