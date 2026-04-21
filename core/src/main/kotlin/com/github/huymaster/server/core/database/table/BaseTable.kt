package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.repository.Repository
import org.koin.core.component.KoinComponent
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import kotlin.reflect.KClass

abstract class BaseTable<E : Entity<E>>(
    tableName: String,
    alias: String? = null,
    catalog: String? = null,
    schema: String? = null,
    entityClass: KClass<E>? = null
) : Table<E>(tableName, alias, catalog, schema, entityClass), KoinComponent {
    fun getRepository() = Repository[this]
}