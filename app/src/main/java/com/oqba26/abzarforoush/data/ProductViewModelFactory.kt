package com.oqba26.abzarforoush.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oqba26.abzarforoush.util.TimeProvider

class ProductViewModelFactory(
    private val repository: ProductRepository,
    private val timeProvider: TimeProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository, timeProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
