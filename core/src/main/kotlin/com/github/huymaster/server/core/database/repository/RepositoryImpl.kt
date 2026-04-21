package com.github.huymaster.server.core.database.repository

import com.github.huymaster.server.core.database.table.BaseTable
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.ColumnDeclaring

abstract class RepositoryImpl<E : Entity<E>, T : BaseTable<E>>(
    private val table: T
) : Repository<E, T> {
    private val database: Database by inject()
    protected val sequence get() = database.sequenceOf(table)

    override suspend fun add(vararg entities: E): Int {
        if (entities.isEmpty()) return 0
        return entities.sumOf { sequence.add(it) }
    }

    override suspend fun insert(block: AssignmentsBuilder.(T) -> Unit): Int = database.insert(table, block)
    override suspend fun find(predicate: (T) -> ColumnDeclaring<Boolean>) = sequence.filter(predicate).toList()
    override suspend fun update(block: UpdateStatementBuilder.(T) -> Unit) = database.update(table, block)
    override suspend fun delete(predicate: (T) -> ColumnDeclaring<Boolean>) = database.delete(table, predicate)

    override suspend fun count(predicate: (T) -> ColumnDeclaring<Boolean>): Int = sequence.filter(predicate).count()
    override suspend fun exists(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean =
        sequence.filter(predicate).take(1).isNotEmpty()


    override fun query(source: T.(QuerySource) -> Query): Query =
        source(table, database.from(table))
}