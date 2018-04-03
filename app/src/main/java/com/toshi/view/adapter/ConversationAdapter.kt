package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.view.adapter.viewholder.ThreadViewHolder
import com.toshi.model.local.Conversation
import com.toshi.util.keyboard.SOFAMessageFormatter

class ConversationAdapter(
        private val onItemClickListener: (Conversation) -> Unit,
        private val onItemLongClickListener: (Conversation) -> Unit
    ): BaseCompoundableAdapter<ThreadViewHolder, Conversation>() {

    private val messageFormatter: SOFAMessageFormatter by lazy { SOFAMessageFormatter() }

    // COMPOUNDABLE ADAPTER OVERRIDES

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        val typedHolder = viewHolder as? ThreadViewHolder
                ?: throw AssertionError("This is not the right type!")
        onBindViewHolder(typedHolder, adapterIndex)
    }

    override fun setItemList(items: List<Conversation>) {
        // Don't show conversations with invalid recipients.
        super.setItemList(items.filter { !it.isRecipientInvalid })
    }

    // MAIN CLASS STUFF

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item__recent, parent, false)
        return ThreadViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val conversation = safelyAt(position)
                ?: throw AssertionError("No conversation at $position")
        holder.setThread(conversation)

        val formattedLatestMessage = messageFormatter.formatMessage(conversation.latestMessage)

        holder.setLatestMessage(formattedLatestMessage)
        holder.setOnItemClickListener(conversation, onItemClickListener)
        holder.setOnItemLongClickListener(conversation, onItemLongClickListener)
    }
}