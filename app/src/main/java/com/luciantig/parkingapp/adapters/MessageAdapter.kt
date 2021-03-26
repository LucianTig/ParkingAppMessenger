package com.luciantig.parkingapp.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.models.ChatMessage
import com.luciantig.parkingapp.models.User
import kotlinx.android.synthetic.main.item_text_message.view.*

class MessageAdapter(private val currentUser: User) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>(){

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val diffCallback = object : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<ChatMessage>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_text_message,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chatMessage = differ.currentList[position]
        holder.itemView.apply {
            textView_message_text.text = chatMessage.text
            textView_message_time.text = chatMessage.timestamp.toString()

            if(chatMessage.senderId == currentUser.uid) {
                message_root.apply {
                    this.setBackgroundResource(R.drawable.rect_round_white)
                    val lParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END)
                    this.layoutParams = lParams
                }
            }else{
                message_root.apply {
                    this.setBackgroundResource(R.drawable.rect_round_primary_color)
                    val lParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.START
                    )
                    this.layoutParams = lParams
                }
            }
        }
    }
}