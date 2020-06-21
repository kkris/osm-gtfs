package org.kkris.osmgtfs.agency

import org.kkris.osmgtfs.common.model.RouteData

interface RouteProvider {
    fun getRoutes(): RouteData
}