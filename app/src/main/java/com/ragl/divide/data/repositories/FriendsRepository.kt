package com.ragl.divide.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ragl.divide.data.models.User
import kotlinx.coroutines.tasks.await

interface FriendsRepository {
    suspend fun getFriends(friends: List<String>): Map<String, User>
    suspend fun searchUsers(query: String): Map<String, User>
    suspend fun addFriend(friend: User): User
}

class FriendsRepositoryImpl(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : FriendsRepository {

    companion object {
        const val LIMIT = 8
    }

    override suspend fun getFriends(friends: List<String>): Map<String, User> {
        TODO("Not yet implemented")
    }

    override suspend fun searchUsers(query: String): Map<String, User> {
        val uuid = auth.currentUser!!.uid
        val map = mutableMapOf<String, User>()
        val usersRef =
            database.getReference("users").orderByChild("name").endAt(query).limitToFirst(LIMIT)
        usersRef.get().await().children.forEach {
            it.getValue(User::class.java)?.let { user -> map[user.uuid] = user }
        }
        return map.apply { remove(uuid) }
    }

    override suspend fun addFriend(friend: User): User {
        TODO("Not yet implemented")
    }

}