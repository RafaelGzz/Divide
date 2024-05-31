package com.ragl.divide.data.repositories

import com.google.firebase.database.FirebaseDatabase
import com.ragl.divide.data.models.Group
import kotlinx.coroutines.tasks.await

interface GroupRepository {
    suspend fun getGroups(groups: List<String>): List<Group>
    suspend fun addGroup(group: Group): Group
}

class GroupRepositoryImpl(
    private val database: FirebaseDatabase
) : GroupRepository {
    override suspend fun getGroups(groups: List<String>): List<Group> {
        return groups.mapNotNull {
            database.getReference("groups/$it").get().await().getValue(Group::class.java)
        }
    }

    override suspend fun addGroup(group: Group): Group {
        TODO()
    }
}