package com.ragl.divide.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ragl.divide.data.models.User
import kotlinx.coroutines.tasks.await

interface FriendsRepository {
    suspend fun getFriends(friends: Map<String, String>): Map<String, User>
    suspend fun searchUsers(query: String, existing: List<User>): Map<String, User>
    suspend fun addFriend(friend: User): User
}

class FriendsRepositoryImpl(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : FriendsRepository {

    private val limit = 8

    override suspend fun getFriends(friends: Map<String, String>): Map<String, User> {
        val map = mutableMapOf<String, User>()
        friends.forEach {
            database.getReference("users").child(it.value).get().await().getValue(User::class.java)
                ?.let { user -> map[user.uuid] = user }
        }
        return map
    }

    override suspend fun searchUsers(query: String, existing: List<User>): Map<String, User> {
        val uuid = auth.currentUser!!.uid
        val map = mutableMapOf<String, User>()
        val usersRef =
            database.getReference("users").orderByChild("email").startAt(query).limitToFirst(limit)
        usersRef.get().await().children.forEach {
            it.getValue(User::class.java)?.let { user -> map[user.uuid] = user }
        }
        return map.apply {
            remove(uuid)
            existing.forEach {
                remove(it.uuid)
            }
        }
    }

    override suspend fun addFriend(friend: User): User {
        val uuid = auth.currentUser!!.uid
        database.getReference("users/$uuid/friends/${friend.uuid}").setValue(friend.uuid).await()
        return friend
    }

}