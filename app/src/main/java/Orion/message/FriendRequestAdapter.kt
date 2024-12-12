package Orion.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendRequestAdapter(
    private val friendRequests: List<FriendRequestManager.FriendRequest>,
    private val onAcceptAction: (FriendRequestManager.FriendRequest) -> Unit,
    private val onRejectAction: (FriendRequestManager.FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = friendRequests[position]
        holder.bind(request, onAcceptAction, onRejectAction)
    }

    override fun getItemCount(): Int = friendRequests.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: Button = itemView.findViewById(R.id.rejectButton)

        fun bind(
            request: FriendRequestManager.FriendRequest,
            onAcceptAction: (FriendRequestManager.FriendRequest) -> Unit,
            onRejectAction: (FriendRequestManager.FriendRequest) -> Unit
        ) {
            senderNameTextView.text = request.senderName

            acceptButton.setOnClickListener { onAcceptAction(request) }
            rejectButton.setOnClickListener { onRejectAction(request) }
        }
    }
}
