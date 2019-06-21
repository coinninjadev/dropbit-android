package com.coinninja.coinkeeper.service

import android.Manifest
import android.content.Intent
import androidx.core.app.JobIntentService
import app.dropbit.twitter.Twitter
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.UserIdentityHelper
import com.coinninja.coinkeeper.util.LocalContactQueryUtil
import com.coinninja.coinkeeper.util.android.PermissionsUtil
import dagger.android.AndroidInjection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactLookupService : JobIntentService() {

    @Inject
    internal lateinit var userIdentityHelper: UserIdentityHelper

    @Inject
    internal lateinit var localContactQueryUtil: LocalContactQueryUtil

    @Inject
    internal lateinit var permissionsUtil: PermissionsUtil

    @Inject
    internal lateinit var dropbitAccountHelper: DropbitAccountHelper

    @Inject
    lateinit var twitter: Twitter

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleWork(intent: Intent) {
        if (permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS)) {
            updatePhoneIdentities()
        }

        if (dropbitAccountHelper.isTwitterVerified) updateTwitterIdentities()
    }

    internal fun updateTwitterIdentities() {
        GlobalScope.launch {
            userIdentityHelper.twitterIdentities.forEach { identity ->
                twitter.getUser(identity.identity.toLong(), identity.handle)?.let { twitterUser ->
                    identity.displayName = twitterUser.displayScreenName()
                    identity.avatar = twitterUser.profileImage?.replace("_normal", "_bigger")
                    identity.update()
                }
            }
        }
    }

    internal fun updatePhoneIdentities() {
        val contacts = localContactQueryUtil.contacts
        val theNameless = userIdentityHelper.namelessPhoneIdentities
        theNameless.forEach { identity ->
            contacts.forEach contactLoop@{ contact ->
                if (compare(contact.phoneNumber, PhoneNumber(identity.identity))) {
                    identity.displayName = contact.displayName
                    identity.update()
                    return@contactLoop
                }
            }
        }
    }

    internal fun compare(contactNumber: PhoneNumber?, identityNumber: PhoneNumber): Boolean {
        if (contactNumber == null || contactNumber.nationalNumber <= 1) return false
        val contactNational = contactNumber.nationalNumber.toString()
        val identityNational = identityNumber.nationalNumber.toString()
        return identityNational.endsWith(contactNational)
    }

}