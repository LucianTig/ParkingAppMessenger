package com.luciantig.parkingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.luciantig.parkingapp.R

import com.luciantig.parkingapp.models.ChatMessage
import com.luciantig.parkingapp.models.User
import kotlinx.android.synthetic.main.item_person.view.*
import kotlinx.android.synthetic.main.item_text_message.view.*

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val diffCallback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<User>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_person,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.itemView.apply {
            textView_name.text = user.username
            textView_bio.text = user.carNumber

            setOnClickListener{
                onItemClickListener?.let { it(user) }
            }
        }
    }

    private var onItemClickListener: ((User) -> Unit)? = null

    fun setOnItemClickListener( listener: (User) -> Unit) {
        onItemClickListener = listener
    }
}