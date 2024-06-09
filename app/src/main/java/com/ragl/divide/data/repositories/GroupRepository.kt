package com.ragl.divide.data.repositories

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ragl.divide.data.models.Group
import kotlinx.coroutines.tasks.await
import java.util.Date

interface GroupRepository {
    suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group>
    suspend fun getGroup(id: String): Group
    suspend fun saveGroup(group: Group, photoUri: Uri?)
    suspend fun uploadPhoto(photoUri: Uri, id: String): String
    suspend fun addUser(groupId: String, userId: String)
    suspend fun leaveGroup(groupId: String)
}

class GroupRepositoryImpl(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository
) : GroupRepository {
    override suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group> {
        val groups = mutableMapOf<String, Group>()
        groupIds.values.mapNotNull {
            database.getReference("groups/$it").get().await().getValue(Group::class.java)
        }.forEach { groups[it.id] = it }
        return groups
    }

    override suspend fun getGroup(id: String): Group {
        return database.getReference("groups/$id").get().await().getValue(Group::class.java)
            ?: Group()
    }

    override suspend fun saveGroup(group: Group, photoUri: Uri?) {
        val id = group.id.ifEmpty { "id${Date().time}" }
        val uid = userRepository.getFirebaseUser()!!.uid
        database.getReference("groups/$id").setValue(
            group.copy(
                image = if (photoUri != null) uploadPhoto(photoUri, id) else "",
                id = id,
                users = group.users.apply { set(uid, uid) }
            )
        ).await()
        userRepository.saveGroup(id)
    }

    override suspend fun uploadPhoto(photoUri: Uri, id: String): String {
        val photoRef = storage.getReference("groupPhotos/$id.jpg")
        photoRef.putFile(photoUri).await()
        return photoRef.downloadUrl.await().toString()
    }

    override suspend fun addUser(groupId: String, userId: String) {
        val groupRef = database.getReference("groups/$groupId/users")
        groupRef.child(userId).setValue(userId).await()
    }

    override suspend fun leaveGroup(groupId: String) {
        val user = userRepository.getFirebaseUser() ?: return
        val groupRef = database.getReference("groups/$groupId/users")
        groupRef.child(user.uid).removeValue().await()
    }
}