package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.view.adapter.viewholder.SearchHeaderViewHolder

class SearchHeaderAdapter(
        private val headerClickListener: () -> Unit
) : BaseCompoundableAdapter<SearchHeaderViewHolder, Int>() {

    init {
        setItemList(listOf(1))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item__search_header, parent, false)
        return SearchHeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHeaderViewHolder, position: Int) {
        holder.itemView.setOnClickListener { headerClickListener() }
    }

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        val typedHolder = viewHolder as? SearchHeaderViewHolder ?: return
        onBindViewHolder(typedHolder, adapterIndex)
    }
}
