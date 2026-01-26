package com.example.stellarstocks.viewmodel

import androidx.lifecycle.ViewModel
import com.example.stellarstocks.data.db.models.StockMaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StockViewModel: ViewModel() {
    private val _stock = MutableStateFlow<List<StockMaster>>(emptyList())
    val stock: StateFlow<List<StockMaster>> = _stock

}