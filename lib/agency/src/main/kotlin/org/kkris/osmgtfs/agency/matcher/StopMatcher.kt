package org.kkris.osmgtfs.agency.matcher

import org.kkris.osmgtfs.common.model.stop.Stop

interface StopMatcher {
    fun match(stopName: String, stops: List<Stop>): Stop?
}