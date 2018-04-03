package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.model.local.Conversation
import com.toshi.view.adapter.viewholder.ConversationRequestsViewHolder


class ConversationRequestsAdapter(
        private val onRequestsClickListener: () -> Unit
): BaseCompoundableAdapter<ConversationRequestsViewHolder, Int>() {

    private var unacceptedConversations: List<Conversation>? = null

    fun setUnacceptedConversations(conversations: List<Conversation>) {
        unacceptedConversations = conversations
        setItemList(listOf(conversations.count()))
    }

    fun removeConversation(conversation: Conversation) {
        unacceptedConversations?.let { unaccepted ->
            if (!unaccepted.contains(conversation)) {
                return
            }

            val mutableConversations = unaccepted.toMutableList()
            mutableConversations.remove(conversation)
            setUnacceptedConversations(mutableConversations.toList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationRequestsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item__conversation_requests, parent, false);
        return ConversationRequestsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversationRequestsViewHolder, position: Int) {
        unacceptedConversations?.let {
            holder.setNumberOfConversationRequests(it.count())
                    .setOnItemClickListener(onRequestsClickListener)
                    .loadAvatar(it)
        }
    }

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        val typedHolder = viewHolder as? ConversationRequestsViewHolder ?: return
        onBindViewHolder(typedHolder, adapterIndex)
    }
}