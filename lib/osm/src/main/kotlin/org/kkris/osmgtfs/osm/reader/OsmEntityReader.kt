package org.kkris.osmgtfs.osm.reader

import de.topobyte.osm4j.core.model.iface.EntityType
import de.topobyte.osm4j.core.model.iface.OsmEntity
import de.topobyte.osm4j.pbf.seq.PbfIterator
import org.kkris.osmgtfs.osm.OsmEntityCache
import java.io.FileInputStream

internal open class OsmEntityReader<E: OsmEntity>(private val path: String, private val type: EntityType?, private val filter: (E) -> Boolean) {
    open fun read(): List<E> {
        return PbfIterator(FileInputStream(path), true)
            .filter {
                type == null || it.type == type
            }
            .map {
                @Suppress("UNCHECKED_CAST")
                it.entity as E
            }
            .filter {
                filter(it)
            }
            .also {
                it.forEach { entity -> OsmEntityCache.get().cache(entity.id) }
            }
    }
}

