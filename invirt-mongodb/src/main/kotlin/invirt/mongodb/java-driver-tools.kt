package invirt.mongodb

import com.mongodb.kotlin.client.ClientSession

typealias JavaClientSession = com.mongodb.client.ClientSession

fun JavaClientSession.kotlin(): ClientSession = ClientSession(this)
