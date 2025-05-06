package net.sharksystem.sharknetmessengerandroid.components

import net.sharksystem.SharkComponent
import net.sharksystem.SharkComponentFactory
import net.sharksystem.SharkPeer
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.pki.SharkPKIComponent

class SharkNetMessengerComponentFactory(private val pkiComponent: SharkPKIComponent?) :
    SharkComponentFactory {
    override fun getComponent(sharkPeer: SharkPeer?): SharkComponent {
        return SharkNetMessengerComponentImpl(this.pkiComponent)
    }
}