package com.ragl.divide.data.repositories

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.User
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

interface GroupRepository {
    suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group>
    suspend fun getGroup(id: String): Group
    suspend fun saveGroup(group: Group, photoUri: Uri): Group
    suspend fun uploadPhoto(photoUri: Uri, id: String): String
    suspend fun getPhoto(id: String): String
    suspend fun addUser(groupId: String, userId: String)
    suspend fun getUsers(userIds: List<String>): List<User>
    suspend fun leaveGroup(groupId: String)
    suspend fun deleteGroup(groupId: String, image: String)
    suspend fun saveExpense(groupId: String, expense: GroupExpense): GroupExpense
    suspend fun deleteExpense(groupId: String, expense: GroupExpense)
}

class GroupRepositoryImpl(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository
) : GroupRepository {
    init {
        database.getReference("groups").apply { keepSynced(true) }
    }

    override suspend fun getGroups(groupIds: Map<String, String>): Map<String, Group> {
        val groups = mutableMapOf<String, Group>()
        groupIds.values.mapNotNull {
            database.getReference("groups/$it").get().await().getValue(Group::class.java)
        }.forEach { groups[it.id] = it }
        return groups
    }

    override suspend fun getGroup(id: String): Group {
        val group = database.getReference("groups/$id").get().await().getValue(Group::class.java)
            ?: Group()
        return group.copy(image = if (group.image.isNotEmpty()) getPhoto(id) else "")
    }

    override suspend fun saveGroup(group: Group, photoUri: Uri): Group {
        val id = group.id.ifEmpty { "id${Date().time}" }
        val uid = userRepository.getFirebaseUser()!!.uid
        val savedGroup = group.copy(
            users = group.users + (uid to GroupUser(id = uid)),
            image = if (photoUri != Uri.EMPTY) uploadPhoto(photoUri, id) else group.image,
            id = id
        )
        database.getReference("groups/$id").setValue(savedGroup).await()
        coroutineScope {
            savedGroup.users.forEach {
                launch {
                    userRepository.saveGroup(id, it.key)
                }
            }
        }
        return savedGroup
    }

    override suspend fun uploadPhoto(photoUri: Uri, id: String): String {
        val photoRef = storage.getReference("groupPhotos/$id.jpg")
        photoRef.putFile(photoUri).await()
        return photoRef.downloadUrl.await().toString()
    }

    override suspend fun getPhoto(id: String): String {
        val storageRef = storage.getReference("groupPhotos/$id.jpg")
        return storageRef.downloadUrl.await().toString()
    }

    override suspend fun addUser(groupId: String, userId: String) {
        val groupRef = database.getReference("groups/$groupId/users")
        groupRef.child(userId).setValue(userId).await()
    }

    override suspend fun getUsers(userIds: List<String>): List<User> = userIds.map {
        userRepository.getUser(it)
    }

    override suspend fun leaveGroup(groupId: String) {
        val user = userRepository.getFirebaseUser() ?: return
        val groupRef = database.getReference("groups/$groupId/users")
        groupRef.child(user.uid).removeValue().await()
        val userRef = database.getReference("users/${user.uid}/groups")
        userRef.child(groupId).removeValue().await()
    }

    override suspend fun deleteGroup(groupId: String, image: String) {
        if (image.isNotEmpty()) {
            storage.getReference("groupPhotos/$image.jpg").delete().await()
        }

        val groupRef = database.getReference("groups/$groupId")
        val userIds = groupRef.child("users").get().await().children.mapNotNull {
            it.getValue(String::class.java)
        }
        coroutineScope {
            userIds.forEach { userId ->
                launch {
                    userRepository.leaveGroup(groupId, userId)
                }
            }
        }

        groupRef.removeValue().await()
    }

    override suspend fun saveExpense(groupId: String, expense: GroupExpense): GroupExpense {
        val id = expense.id.ifEmpty { "id${Date().time}" }
        val savedExpense = expense.copy(id = id)
        val groupRef = database.getReference("groups/$groupId")
        groupRef.child("expenses").child(id).setValue(savedExpense).await()

        coroutineScope { // Use coroutineScope for concurrent updates
            expense.paidBy.entries.forEach { (userId, amount) ->
                val userRef = groupRef.child("users").child(userId)
                val groupUser = userRef.get().await().getValue(GroupUser::class.java)!!
                groupUser.owed += amount
                userRef.setValue(groupUser) // Update user debt concurrently
            }
            expense.debtors.entries.forEach { (userId, amount) ->
                launch {
                    val userRef = groupRef.child("users").child(userId)
                    val groupUser = userRef.get().await().getValue(GroupUser::class.java)!!
                    groupUser.debt += amount
                    userRef.setValue(groupUser) // Update user debt concurrently
                }
            }
        }

        return savedExpense
    }

    override suspend fun deleteExpense(groupId: String, expense: GroupExpense) {
        val groupRef = database.getReference("groups/$groupId")
        groupRef.child("expenses").child(expense.id).removeValue().await()
        coroutineScope {
            expense.paidBy.entries.forEach { (userId, amount) ->
                val userRef = groupRef.child("users").child(userId)
                val groupUser = userRef.get().await().getValue(GroupUser::class.java)!!
                groupUser.owed -= amount
                userRef.setValue(groupUser)
            }
            expense.debtors.entries.forEach { (userId, amount) ->
                launch {
                    val userRef = groupRef.child("users").child(userId)
                    val groupUser = userRef.get().await().getValue(GroupUser::class.java)!!
                    groupUser.debt -= amount
                    userRef.setValue(groupUser)
                }
            }
        }
    }
}