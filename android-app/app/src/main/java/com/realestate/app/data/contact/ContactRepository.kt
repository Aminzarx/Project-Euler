package com.realestate.app.data.contact

import kotlinx.coroutines.flow.Flow

/** Thin pass-through over [ContactDao] — mirrors [com.realestate.app.data.PropertyRepository]'s
 *  shape so the two entities read the same way from callers. */
class ContactRepository(private val dao: ContactDao) {
    val allContacts: Flow<List<Contact>> = dao.getAllContacts()

    fun search(query: String): Flow<List<Contact>> = dao.search(query)

    fun getContactById(id: Long): Flow<Contact?> = dao.getContactById(id)

    suspend fun caseCountFor(contactId: Long): Int = dao.caseCountFor(contactId)

    suspend fun insert(contact: Contact): Long = dao.insert(contact)

    suspend fun update(contact: Contact) = dao.update(contact)

    suspend fun delete(contact: Contact) = dao.delete(contact)
}
