/*
Mangadex@Home
Copyright (c) 2020, MangaDex Network
This file is part of MangaDex@Home.

MangaDex@Home is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MangaDex@Home is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this MangaDex@Home.  If not, see <http://www.gnu.org/licenses/>.
 */
package mdnet.base.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object ImageData : IdTable<String>() {
    override val id = varchar("id", 32).entityId()
    override val primaryKey = PrimaryKey(id)

    val contentType = varchar("contentType", 20)
    val lastModified = varchar("lastModified", 29)
}

class ImageDatum(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ImageDatum>(ImageData)
    var contentType by ImageData.contentType
    var lastModified by ImageData.lastModified
}
