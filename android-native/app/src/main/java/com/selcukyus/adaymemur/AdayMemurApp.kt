package com.selcukyus.adaymemur

import android.app.Application
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.notify.IlanIscisi

class AdayMemurApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Depo.baslat(this)
        Pro.baslat(this)
        IlanIscisi.kanalOlustur(this)
        IlanIscisi.planla(this)
    }
}
