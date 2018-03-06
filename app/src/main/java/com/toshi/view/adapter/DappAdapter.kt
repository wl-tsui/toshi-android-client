/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.model.local.dapp.DappCategory
import com.toshi.model.local.dapp.DappFooter
import com.toshi.model.local.dapp.DappListItem
import com.toshi.model.network.TempDapp
import com.toshi.util.LogUtil
import com.toshi.view.adapter.viewholder.DappCategoryViewHolder
import com.toshi.view.adapter.viewholder.DappFooterViewHolder
import com.toshi.view.adapter.viewholder.DappViewHolder

class DappAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ITEM = 1
        private const val CATEGORY = 2
        private const val FOOTER = 3
    }

    private val dappsList = mutableListOf<DappListItem>()
    var onFooterClickedListener: (() -> Unit)? = null

    fun setDapps(dapps: List<TempDapp>) {
        if (dapps.isEmpty()) return
        val dappsWithCategories = addCategoryItems(dapps)
        this.dappsList.addAll(dappsWithCategories)
        this.dappsList.add(DappFooter())
        notifyDataSetChanged()
    }

    private fun addCategoryItems(dapps: List<TempDapp>): List<DappListItem> {
        val dappsListWithCategories = mutableListOf<DappListItem>()
        for ((i) in dapps.withIndex()) {
            when {
                i == 0 -> addNewCategory(dappsListWithCategories, dapps[i])
                dapps[i].category != dapps[i - 1].category -> addNewCategory(dappsListWithCategories, dapps[i])
                else -> dappsListWithCategories.add(dapps[i])
            }
        }
        return dappsListWithCategories
    }

    private fun addNewCategory(dappsListWithCategories: MutableList<DappListItem>, currentDapp: TempDapp) {
        dappsListWithCategories.add(DappCategory(currentDapp.category))
        dappsListWithCategories.add(currentDapp)
    }

    override fun getItemViewType(position: Int): Int {
        val item = dappsList[position]
        return when (item) {
            is DappCategory -> CATEGORY
            is DappFooter -> FOOTER
            else -> ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        return when (viewType) {
            CATEGORY -> {
                val view = layoutInflater.inflate(R.layout.list_item__dapp_category, parent, false)
                DappCategoryViewHolder(view)
            }
            FOOTER -> {
                val view = layoutInflater.inflate(R.layout.list_item__dapp_footer, parent, false)
                DappFooterViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.list_item__dapp, parent, false)
                DappViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val dapp = dappsList[position]
        when {
            holder is DappCategoryViewHolder && dapp is DappCategory -> holder.setCategory(dapp)
            holder is DappFooterViewHolder && dapp is DappFooter -> {
                holder.setOnClickListener { onFooterClickedListener?.invoke() }
            }
            holder is DappViewHolder && dapp is TempDapp -> holder.setDapp(dapp)
            else -> LogUtil.exception(javaClass, "Invalid dapp item in this context")
        }
    }

    override fun getItemCount() = dappsList.size
}