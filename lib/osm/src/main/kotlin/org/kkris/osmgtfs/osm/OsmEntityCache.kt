package org.kkris.osmgtfs.osm

import de.topobyte.osm4j.core.model.iface.OsmEntity
import de.topobyte.osm4j.core.model.impl.Node
import de.topobyte.osm4j.core.model.impl.Relation
import de.topobyte.osm4j.core.model.impl.Way
import org.kkris.osmgtfs.osm.reader.OsmEntityReader

internal class OsmEntityCache private constructor(private val osmPath: String) {

    private val ways: MutableMap<Long, Way> = mutableMapOf()
    private val nodes: MutableMap<Long, Node> = mutableMapOf()
    private val relations: MutableMap<Long, Relation> = mutableMapOf()

    private val resolvedIds = mutableSetOf<Long>()
    private val unresolvedIds = mutableListOf<Long>()

    companion object {
        private lateinit var instance: OsmEntityCache

        fun init(osmPath: String) {
            instance = OsmEntityCache(osmPath)
        }

        fun get(): OsmEntityCache {
            return instance
        }
    }

    fun getWay(id: Long): Way? {
        resolveIds()
        return ways[id]
    }

    fun getNode(id: Long): Node? {
        resolveIds()
        return nodes[id]
    }

    fun getRelation(id: Long): Relation? {
        resolveIds()
        return relations[id]
    }

    fun cache(id: Long) {
        if (!resolvedIds.contains(id)) {
            unresolvedIds.add(id)
        }
    }

    private fun resolveIds() {
        if (unresolvedIds.isEmpty()) return

        val additionalNodeIds = mutableListOf<Long>()

        OsmEntityReader<OsmEntity>(osmPath, null) {
            unresolvedIds.contains(it.id)
        }.read().forEach{ cache(it, additionalNodeIds) }

        OsmEntityReader<OsmEntity>(osmPath, null) {
            additionalNodeIds.contains(it.id)
        }.read().forEach{ cache(it, additionalNodeIds) }

        resolvedIds.addAll(unresolvedIds)
        unresolvedIds.clear()
    }

    private fun cache(entity: OsmEntity, additionalNodeIds: MutableList<Long>) {
        when {
            Way::class.java.isAssignableFrom(entity.javaClass) -> {
                val way = entity as Way
                ways[entity.id] = way

                way.nodes.forEach { id -> additionalNodeIds.add(id) }
            }
            Node::class.java.isAssignableFrom(entity.javaClass) -> nodes[entity.id] = entity as Node
            Relation::class.java.isAssignableFrom(entity.javaClass) -> relations[entity.id] = entity as Relation
            else -> throw RuntimeException("Failed to cache entity $entity")
        }
    }
}