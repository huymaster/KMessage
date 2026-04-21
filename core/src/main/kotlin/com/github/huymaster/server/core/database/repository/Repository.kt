package com.github.huymaster.server.core.database.repository

import com.github.huymaster.server.core.database.table.BaseTable
import org.koin.core.component.KoinComponent
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.Query
import org.ktorm.dsl.QuerySource
import org.ktorm.dsl.UpdateStatementBuilder
import org.ktorm.entity.Entity
import org.ktorm.schema.ColumnDeclaring

interface Repository<E : Entity<E>, T : BaseTable<E>> : KoinComponent {
    companion object {
        operator fun <E : Entity<E>, T : BaseTable<E>> get(table: T): Repository<E, T> =
            object : RepositoryImpl<E, T>(table) {}
    }

    suspend fun add(vararg entities: E): Int
    suspend fun insert(block: AssignmentsBuilder.(T) -> Unit): Int
    suspend fun find(predicate: (T) -> ColumnDeclaring<Boolean>): List<E>
    suspend fun update(block: UpdateStatementBuilder.(T) -> Unit): Int
    suspend fun delete(predicate: (T) -> ColumnDeclaring<Boolean>): Int

    suspend fun count(predicate: (T) -> ColumnDeclaring<Boolean>): Int
    suspend fun exists(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean

    fun query(source: T.(QuerySource) -> Query): Query
}