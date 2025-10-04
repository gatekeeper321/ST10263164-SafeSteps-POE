package com.fake.safesteps.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fake.safesteps.models.TrustedContact
import kotlinx.coroutines.tasks.await

class ContactRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val contactsCollection = db.collection("trustedContacts")

    suspend fun addContact(
        contactUserId: String,
        contactName: String,
        contactEmail: String,
        contactPhone: String
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val contact = hashMapOf(
                "userId" to userId,
                "contactUserId" to contactUserId,
                "contactName" to contactName,
                "contactEmail" to contactEmail,
                "contactPhone" to contactPhone,
                "isActive" to true,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            val documentRef = contactsCollection.add(contact).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error adding contact", e)
            Result.failure(e)
        }
    }

    suspend fun getTrustedContacts(): Result<List<TrustedContact>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = contactsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val contacts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TrustedContact::class.java)
            }

            Result.success(contacts)
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error getting contacts", e)
            Result.failure(e)
        }
    }

    suspend fun removeContact(contactId: String): Result<Unit> {
        return try {
            contactsCollection.document(contactId)
                .update("isActive", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error removing contact", e)
            Result.failure(e)
        }
    }
}