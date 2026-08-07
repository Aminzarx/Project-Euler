package com.realestate.app.data.contact

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY lastCaseAt DESC, fullName ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getContactById(id: Long): Flow<Contact?>

    /** Backs the contact picker's search box — matches on name or either phone number. */
    @Query(
        """
        SELECT * FROM contacts
        WHERE fullName LIKE '%' || :query || '%'
           OR primaryPhone LIKE '%' || :query || '%'
           OR secondaryPhone LIKE '%' || :query || '%'
        ORDER BY lastCaseAt DESC, fullName ASC
        LIMIT 20
        """
    )
    fun search(query: String): Flow<List<Contact>>

    /** How many cases currently reference this contact — shown in the picker ("۲ پرونده دیگر")
     *  so attaching an existing contact instead of creating a duplicate is the obviously easier
     *  choice. */
    @Query("SELECT COUNT(*) FROM properties WHERE contactId = :contactId")
    suspend fun caseCountFor(contactId: Long): Int

    @Insert
    suspend fun insert(contact: Contact): Long

    @Insert
    suspend fun insertAll(contacts: List<Contact>)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()
}
