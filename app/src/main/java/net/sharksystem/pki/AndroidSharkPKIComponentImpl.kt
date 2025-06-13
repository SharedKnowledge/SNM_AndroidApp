package net.sharksystem.pki

import net.sharksystem.SharkPeer
import net.sharksystem.asap.crypto.InMemoASAPKeyStore
import net.sharksystem.asap.persons.PersonValues
import net.sharksystem.sharknetmessengerandroid.sharknet.AndroidASAPKeyStoreNew


internal class AndroidSharkPKIComponentImpl : SharkPKIComponentImpl {
    var asapKeyStore: InMemoASAPKeyStore? = null
    constructor(owner: SharkPeer) : super(owner)
    fun setASAPKeyStore(keyStore: AndroidASAPKeyStoreNew) {
        this.asapKeyStore = keyStore
    }
    fun getPersons() : Set<PersonValues> {
        val persons = mutableSetOf<PersonValues>()

        for(i in 0 until this.numberOfPersons) {
            persons.add(this.getPersonValuesByPosition(i))
        }
        return persons

    }
}
