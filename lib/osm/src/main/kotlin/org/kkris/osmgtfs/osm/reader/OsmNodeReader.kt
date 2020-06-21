package org.kkris.osmgtfs.osm.reader

import de.topobyte.osm4j.core.model.iface.EntityType
import de.topobyte.osm4j.core.model.impl.Node

internal class OsmNodeReader(path: String, filter: (Node) -> Boolean): OsmEntityReader<Node>(path, EntityType.Node, filter)