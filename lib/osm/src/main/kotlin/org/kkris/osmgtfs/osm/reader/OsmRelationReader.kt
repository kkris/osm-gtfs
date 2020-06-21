package org.kkris.osmgtfs.osm.reader

import de.topobyte.osm4j.core.model.iface.EntityType
import de.topobyte.osm4j.core.model.impl.Relation
import org.kkris.osmgtfs.osm.OsmEntityCache

internal class OsmRelationReader(path: String, filter: (Relation) -> Boolean): OsmEntityReader<Relation>(path, EntityType.Relation, filter) {
    override fun read(): List<Relation> {
        return super.read().map { relation ->
            relation.members.forEach { OsmEntityCache.get().cache(it.id) }

            relation
        }
    }
}