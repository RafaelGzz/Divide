package com.ragl.divide.data

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.ragl.divide.data.models.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {

    fun isUserSignedIn(): Boolean

    suspend fun createUserInDatabase(): User

    suspend fun getDatabaseUser(): User?

    fun getFirebaseUser(): FirebaseUser?

    suspend fun signInWithCredential(credential: AuthCredential): User?

    suspend fun signInWithEmailAndPassword(email: String, password: String): User?
    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User?
    fun signOut()
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : UserRepository {

    override fun isUserSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        name: String
    ): User? {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
        return if (user != null) {
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
            createUserInDatabase()
        } else {
            null
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): User? {
        val user = auth.signInWithEmailAndPassword(email, password).await().user
        return if (user != null) {
            getDatabaseUser()
        } else {
            null
        }
    }

    override suspend fun createUserInDatabase(): User {
        val user = getFirebaseUser()!!
        val userRef = database.getReference("users/${user.uid}")

        // Get the first provider data (assuming there is only one)
        val providerData = user.providerData[1]

        val userData = User(
            user.uid,
            providerData.email ?: user.email.orEmpty(),
            providerData.displayName ?: user.displayName.orEmpty(),
            (providerData.photoUrl ?: "").toString()
        )

        userRef.setValue(userData).await()
        return userData
    }

    override suspend fun getDatabaseUser(): User? {
        val user = getFirebaseUser() ?: return null
        val userRef = database.getReference("users/${user.uid}")
        val snapshot = userRef.get().await()
        return snapshot.getValue(User::class.java)
    }

    override fun getFirebaseUser(): FirebaseUser? = auth.currentUser

    override suspend fun signInWithCredential(
        credential: AuthCredential
    ): User? {
        val res = auth.signInWithCredential(credential).await()
        return if (res.user != null) {
            if (res.additionalUserInfo!!.isNewUser) {
                createUserInDatabase()
            } else
                getDatabaseUser()
        } else {
            null
        }
    }

    override fun signOut() {
        auth.signOut()
    }

}