package org.kkris.osmgtfs.gtfs.model

import org.kkris.osmgtfs.common.model.fare.PaymentMethod
import org.onebusaway.gtfs.model.AgencyAndId
import org.onebusaway.gtfs.model.FareAttribute
import java.util.*

data class GtfsFare(
    val id: String,
    val price: Float,
    val currency: Currency,
    val paymentMethod: PaymentMethod

): GtfsEntity<FareAttribute>() {

    override fun toEntity(agency: GtfsAgency): FareAttribute {
        val entity = FareAttribute()

        entity.id = AgencyAndId(agency.id, id)
        entity.price = price
        entity.currencyType = currency.currencyCode
        entity.paymentMethod = when (paymentMethod) {
            PaymentMethod.PAY_ON_BOARD -> 0
            PaymentMethod.PAY_BEFORE_BOARDING -> 1
        }

        return entity
    }

    override fun id(): String {
        return "fare:$id"
    }
}