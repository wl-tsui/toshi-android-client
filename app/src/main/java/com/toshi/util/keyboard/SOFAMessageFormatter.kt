package com.toshi.util.keyboard

import com.toshi.model.local.User
import com.toshi.model.sofa.SofaAdapters
import com.toshi.model.sofa.SofaMessage
import com.toshi.model.sofa.SofaType
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import java.io.IOException

/**
 * Convenience class for formatting SOFA messages in a single place with the current local user.
 */
class SOFAMessageFormatter() {

    private var localUser = BaseApplication
                .get()
                .userManager
                .getCurrentUser()
                .toBlocking()
                .value()

    /**
     * Constructor for tests so an arbitrary local user can be passed in
     *
     * @param user: The user to treat as the local user
     */
    constructor(user: User) : this() {
        this.localUser = user
    }

    fun formatMessage(sofaMessage: SofaMessage?): String {
        if (sofaMessage == null) return ""
        val sentByLocal = sofaMessage.isSentBy(localUser)

        try {
            when (sofaMessage.type) {
                SofaType.PLAIN_TEXT -> {
                    val message = SofaAdapters.get().messageFrom(sofaMessage.payload)
                    return message.toUserVisibleString(sentByLocal, sofaMessage.hasAttachment())
                }
                SofaType.PAYMENT -> {
                    val payment = SofaAdapters.get().paymentFrom(sofaMessage.payload)
                    return payment.toUserVisibleString(sentByLocal, sofaMessage.sendState)
                }
                SofaType.PAYMENT_REQUEST -> {
                    val request = SofaAdapters.get().txRequestFrom(sofaMessage.payload)
                    return request.toUserVisibleString(sentByLocal, sofaMessage.sendState)
                }
                SofaType.LOCAL_STATUS_MESSAGE -> {
                    val localStatusMessage = SofaAdapters.get().localStatusMessageRequestFrom(sofaMessage.payload)
                    val sender = localStatusMessage.sender
                    val isSenderLocalUser = sender != null && localUser.toshiId == sender.toshiId
                    return localStatusMessage.loadString(isSenderLocalUser)
                }
                SofaType.COMMAND_REQUEST,
                SofaType.INIT_REQUEST,
                SofaType.INIT,
                SofaType.TIMESTAMP,
                SofaType.UNKNOWN -> return ""
                else -> return ""
            }
        } catch (ex: IOException) {
            LogUtil.w("Error parsing SofaMessage. $ex")

        }

        return ""
    }
}