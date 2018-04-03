package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.view.adapter.viewholder.ConversationDividerViewHolder

class ConversationsHeaderAdapter: BaseCompoundableAdapter<ConversationDividerViewHolder, Int>() {

    init {
        setVisibile(true)
    }

    fun setVisibile(visible: Boolean) {
        if (visible) {
            setItemList(listOf(1))
        } else {
            setItemList(listOf())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationDividerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item__conversation_divider, parent, false)
        return ConversationDividerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversationDividerViewHolder, position: Int) {
        // Do nothing, this should just configure itself from the layout
    }

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        // Do nothing, this should configure itself from the layout
    }
}