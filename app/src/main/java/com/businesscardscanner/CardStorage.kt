package com.businesscardscanner

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CardStorage {
    
    private const val PREF_NAME = "BusinessCardPrefs"
    private const val KEY_CARDS = "cards"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveCard(context: Context, card: BusinessCard) {
        val cards = getAllCards(context).toMutableList()
        cards.add(card)
        saveCards(context, cards)
    }
    
    fun getAllCards(context: Context): List<BusinessCard> {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_CARDS, "[]") ?: "[]"
        val type = object : TypeToken<List<BusinessCard>>() {}.type
        return Gson().fromJson(json, type)
    }
    
    private fun saveCards(context: Context, cards: List<BusinessCard>) {
        val prefs = getPrefs(context)
        val json = Gson().toJson(cards)
        prefs.edit().putString(KEY_CARDS, json).apply()
    }
}
