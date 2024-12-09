package Orion.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewGroups)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Configura tu adaptador aquí
        return view
    }
}