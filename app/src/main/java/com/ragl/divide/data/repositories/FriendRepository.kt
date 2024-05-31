package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.User

interface FriendRepository {
    suspend fun getFriends(friends: List<String>): List<User>
    suspend fun addFriend(friend: User): User
}

class FriendRepositoryImpl: FriendRepository {
    override suspend fun getFriends(friends: List<String>): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun addFriend(friend: User): User {
        TODO("Not yet implemented")
    }

}