package com.ragl.divide.data.repositories

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import kotlinx.coroutines.tasks.await
import java.util.Date
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
    suspend fun getExpense(id: String): Expense
    suspend fun getExpenses(): Map<String, Expense>
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
    suspend fun getExpensePayments(expenseId: String): Map<String, Payment>
    suspend fun addExpensePayment(payment: Payment, expenseId: String)
    suspend fun deleteExpensePayment(paymentId: String, expenseId: String)
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
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
                .await()
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

    override suspend fun getExpense(id: String): Expense {
        val expenses = getExpenses()
        return expenses[id] ?: Expense()
    }

    override suspend fun getExpenses(): Map<String, Expense> {
        val expenses = mutableMapOf<String, Expense>()
        val user = getFirebaseUser() ?: return expenses
        val userRef = database.getReference("users/${user.uid}/expenses")
        val snapshot = userRef.get().await()
        snapshot.children.forEach {
            it.getValue(Expense::class.java)
                ?.let { expense -> expenses[expense.id] = expense }
        }
        return expenses
    }

    override suspend fun addExpense(expense: Expense) {
        val user = getFirebaseUser() ?: return
        val id = "id${Date().time}"
        val expensesRef = database.getReference("users/${user.uid}/expenses")
        expensesRef.child(id).setValue(expense.copy(id = id)).await()
    }

    override suspend fun getExpensePayments(expenseId: String): Map<String, Payment> {
        val payments = mutableMapOf<String, Payment>()
        val user = getFirebaseUser() ?: return payments
        val userRef = database.getReference("users/${user.uid}/expenses/$expenseId/payments")
        val snapshot = userRef.get().await()
        snapshot.children.forEach {
            it.getValue(Payment::class.java)
                ?.let { payment -> payments[payment.id] = payment }
        }
        return payments
    }

    override suspend fun addExpensePayment(payment: Payment, expenseId: String) {
        val user = getFirebaseUser() ?: return
        val id = "id${Date().time}"

        val amountPaidRef = database.getReference("users/${user.uid}/expenses/$expenseId/amountPaid")
        amountPaidRef.setValue(amountPaidRef.get().await().value as Long + payment.amount).await()

        val paymentsRef = database.getReference("users/${user.uid}/expenses/$expenseId/payments")
        paymentsRef.child(id).setValue(payment.copy(id = id)).await()
    }

    override suspend fun deleteExpensePayment(paymentId: String, expenseId: String) {
        val user = getFirebaseUser() ?: return
        val paymentsRef = database.getReference("users/${user.uid}/expenses/$expenseId/payments")
        paymentsRef.child(paymentId).removeValue().await()
    }

    override suspend fun deleteExpense(id: String) {
        val user = getFirebaseUser() ?: return
        val expensesRef = database.getReference("users/${user.uid}/expenses")
        expensesRef.child(id).removeValue().await()
    }
}