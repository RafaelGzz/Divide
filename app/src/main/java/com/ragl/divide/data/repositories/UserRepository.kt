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

    suspend fun getUser(id: String): User

    fun getFirebaseUser(): FirebaseUser?

    suspend fun signInWithCredential(credential: AuthCredential): User?

    suspend fun signInWithEmailAndPassword(email: String, password: String): User?
    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User?
    fun signOut()
    suspend fun getExpense(id: String): Expense
    suspend fun getExpenses(): Map<String, Expense>
    suspend fun saveExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
    suspend fun getExpensePayments(expenseId: String): Map<String, Payment>
    suspend fun saveExpensePayment(payment: Payment, expenseId: String, expensePaid: Boolean)
    suspend fun deleteExpensePayment(paymentId: String, amount: Double, expenseId: String)
    suspend fun saveGroup(id: String)
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
            getUser(user.uid)
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

    override suspend fun getUser(id: String): User {
        val userRef = database.getReference("users/$id")
        val snapshot = userRef.get().await()
        return snapshot.getValue(User::class.java) ?: User()
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
                getUser(res.user!!.uid)
        } else {
            null
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override suspend fun getExpense(id: String): Expense {
        val user = getFirebaseUser() ?: return Expense()
        val expenseRef = database.getReference("users/${user.uid}/expenses/${id}")
        val snapshot = expenseRef.get().await()
        return snapshot.getValue(Expense::class.java) ?: Expense()
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

    override suspend fun saveExpense(expense: Expense) {
        val user = getFirebaseUser() ?: return
        val id = expense.id.ifEmpty { "id${Date().time}" }
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

    override suspend fun saveExpensePayment(payment: Payment, expenseId: String, expensePaid: Boolean) {
        val user = getFirebaseUser() ?: return
        val id = "id${Date().time}"

        val amountPaidRef =
            database.getReference("users/${user.uid}/expenses/$expenseId/amountPaid")
        val amountPaid = try {
            (amountPaidRef.get().await().value) as Double + payment.amount
        } catch (e: Exception) {
            (amountPaidRef.get().await().value) as Long + payment.amount
        }
        amountPaidRef.setValue(amountPaid).await()

        val paymentsRef = database.getReference("users/${user.uid}/expenses/$expenseId/payments")
        paymentsRef.child(id).setValue(payment.copy(id = id)).await()

        if(expensePaid)
            database.getReference("users/${user.uid}/expenses/$expenseId/paid").setValue(true).await()
    }

    override suspend fun deleteExpensePayment(paymentId: String, amount: Double, expenseId: String) {
        val user = getFirebaseUser() ?: return

        val amountPaidRef = database.getReference("users/${user.uid}/expenses/$expenseId/amountPaid")
        val amountPaid = try {
            (amountPaidRef.get().await().value) as Double - amount
        } catch (e: Exception) {
            (amountPaidRef.get().await().value) as Long - amount
        }
        amountPaidRef.setValue(amountPaid).await()

        val paymentsRef = database.getReference("users/${user.uid}/expenses/$expenseId/payments")
        paymentsRef.child(paymentId).removeValue().await()
    }

    override suspend fun deleteExpense(id: String) {
        val user = getFirebaseUser() ?: return
        val expensesRef = database.getReference("users/${user.uid}/expenses")
        expensesRef.child(id).removeValue().await()
    }

    override suspend fun saveGroup(id: String){
        val user = getFirebaseUser() ?: return
        val groupRef = database.getReference("users/${user.uid}/groups")
        groupRef.child(id).setValue(id).await()
    }
}