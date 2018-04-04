package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.view.adapter.viewholder.InviteFriendViewHolder

class InviteFriendAdapter(
        private val inviteFriendAction: () -> Unit
): BaseCompoundableAdapter<InviteFriendViewHolder, Int>() {

    init {
        setItemList(listOf(1))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteFriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item__invite_friend, parent, false)
        return InviteFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteFriendViewHolder, position: Int) {
        holder.itemView.setOnClickListener { inviteFriendAction() }
    }

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        val typedHolder = viewHolder as? InviteFriendViewHolder ?: return
        onBindViewHolder(typedHolder, adapterIndex)
    }
}