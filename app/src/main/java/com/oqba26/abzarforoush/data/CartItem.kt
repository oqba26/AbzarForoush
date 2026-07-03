package com.oqba26.abzarforoush.data

data class CartItem(
    val product: Product,
    val quantity: Double,
    val sellPrice: Double = product.price,
    val discount: Double = 0.0,
) {
    val totalPrice: Double get() = (sellPrice * quantity) - discount
}
