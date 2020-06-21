package org.kkris.osmgtfs.common.model.fare

import java.util.*

data class Fare(
    val id: String,
    val price: Float,
    val currency: Currency,
    val paymentMethod: PaymentMethod
)