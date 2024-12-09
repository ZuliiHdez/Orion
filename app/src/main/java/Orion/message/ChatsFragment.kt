package Orion.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewChats)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Configura tu adaptador aquí
        return view
    }
}