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
