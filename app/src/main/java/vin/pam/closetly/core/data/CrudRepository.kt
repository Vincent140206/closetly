package vin.pam.closetly.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import vin.pam.closetly.core.model.ClosetlyEntity

interface CrudRepository<T : ClosetlyEntity> {
    fun observeAll(): Flow<List<T>>
    suspend fun findById(id: String): T?
    suspend fun save(item: T)
    suspend fun delete(id: String)
}

abstract class InMemoryCrudRepository<T : ClosetlyEntity>(initialItems: List<T>) : CrudRepository<T> {
    private val items = MutableStateFlow(initialItems)

    override fun observeAll(): Flow<List<T>> = items

    override suspend fun findById(id: String): T? = items.value.firstOrNull { it.id == id }

    override suspend fun save(item: T) {
        items.value = items.value.filterNot { it.id == item.id } + item
    }

    override suspend fun delete(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }
}
