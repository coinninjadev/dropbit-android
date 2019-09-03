package com.coinninja.coinkeeper.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.ui.util.OnItemClickListener
import java.util.*

class PickContactRecycleViewAdapter : RecycleViewAdapterFilterable<PickContactRecycleViewAdapter.ViewHolder>() {
    companion object {
        internal const val CONTACT = 0
        internal const val UNVERIFIED_HEADER = 2
    }

    internal var clickListener: OnItemClickListener? = null
    internal var dropbitClickHandler: View.OnClickListener? = null
    internal var unVerifiedHeaderIndex = -1

    private var contacts: MutableList<Contact> = mutableListOf()
    private var contactFilter = ContactFilter(contacts)

    fun setupOnClickListeners(clickListener: OnItemClickListener, whatIsDropbitClickListener: View.OnClickListener) {
        dropbitClickHandler = whatIsDropbitClickListener
        this.clickListener = clickListener
    }

    fun getContacts(): List<Contact> {
        return contacts
    }

    fun setContacts(contacts: List<Contact>) {
        setupDataSourceForContacts(contacts)
        contactFilter = ContactFilter(contacts)

        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): Contact? {
        if (position == unVerifiedHeaderIndex) {
            return null
        }

        val hasUnVerifiedHeader = unVerifiedHeaderIndex != -1
        val isBelowUnVerifiedHeader = hasUnVerifiedHeader && position > unVerifiedHeaderIndex

        val offset = if (isBelowUnVerifiedHeader) 1 else 0

        return if (contacts.isNotEmpty() && contacts.size > position - offset) {
            contacts[position - offset]
        } else {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            UNVERIFIED_HEADER -> ViewHolderHeader(
                    LayoutInflater.from(parent.context).inflate(R.layout.contacts_unverified_header, parent, false),
                    viewType, dropbitClickHandler)

            else -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.contact_layout, parent, false), clickListener, viewType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == CONTACT) {
            holder.bindTo(getItemAt(position), position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == unVerifiedHeaderIndex) {
            UNVERIFIED_HEADER
        } else {
            CONTACT
        }
    }

    override fun getItemCount(): Int {
        val offsetCount = if (unVerifiedHeaderIndex == -1) 0 else 1

        return contacts.size + offsetCount
    }

    override fun getFilter(): Filter {
        return contactFilter
    }

    private fun setupDataSourceForContacts(contacts: List<Contact>) {
        unVerifiedHeaderIndex = -1
        val unverifiedContacts = ArrayList<Contact>()
        val verifiedContacts = ArrayList<Contact>()

        for (i in contacts.indices) {
            val contact = contacts[i]
            if (contact.isVerified) {
                verifiedContacts.add(contact)
            } else {
                unverifiedContacts.add(contact)
            }
        }

        this.contacts = ArrayList()
        this.contacts.addAll(verifiedContacts)
        this.contacts.addAll(unverifiedContacts)
        if (unverifiedContacts.isNotEmpty())
            unVerifiedHeaderIndex = verifiedContacts.size
    }

    open class ViewHolder(var view: View, private val onItemClickListener: OnItemClickListener?, val viewType: Int) : RecyclerView.ViewHolder(view) {
        internal var position: Int = 0

        init {
            view.setOnClickListener { V -> onClick() }
        }

        fun bindTo(contact: Contact?, position: Int) {
            this.position = position
            view.findViewById<TextView>(R.id.contact_name).text = contact?.displayName ?: ""
            view.findViewById<TextView>(R.id.contact_phone).text = contact?.phoneNumber?.toInternationalDisplayText()
                    ?: ""
            val sendButton = view.findViewById<View>(R.id.contact_action_send)
            val inviteButton = view.findViewById<View>(R.id.contact_action_invite)

            sendButton.visibility = View.GONE
            inviteButton.visibility = View.GONE
            sendButton.setOnLongClickListener(null)
            inviteButton.setOnLongClickListener(null)

            contact?.let {
                val actionButton: Button = if (it.isVerified) {
                    sendButton as Button
                } else {
                    inviteButton as Button
                }

                actionButton.setOnClickListener { V -> onClick() }
                actionButton.visibility = View.VISIBLE
            }
        }

        private fun onClick() {
            onItemClickListener?.onItemClick(view, position)
        }

    }

    class ViewHolderHeader(itemView: View, viewType: Int, dropbitClickHandler: View.OnClickListener?) : ViewHolder(itemView, null, viewType) {

        init {
            itemView.findViewById<View>(R.id.what_is_dropbit).setOnClickListener(dropbitClickHandler)
        }

    }

    inner class ContactFilter(private val contacts: List<Contact>) : Filter() {

        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            val filterResults = Filter.FilterResults()

            val filteredVerifiedContacts = performContactFiltering(constraint, contacts)

            filterResults.count = filteredVerifiedContacts.size
            filterResults.values = filteredVerifiedContacts

            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            this@PickContactRecycleViewAdapter.contacts = results.values as ArrayList<Contact>
            notifyDataSetChanged()
        }

        private fun performContactFiltering(constraint: CharSequence?, contacts: List<Contact>): List<Contact> {
            if (constraint == null || constraint.length == 0) {
                return contacts
            }

            val tempList = ArrayList<Contact>()
            for (contact in contacts) {
                if (contact.displayName.toLowerCase().contains(constraint.toString().toLowerCase())) {
                    tempList.add(contact)
                }
            }
            return tempList
        }
    }
}