package Orion.message

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2 // Dos pestañas: Chats y Groups

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatsFragment() // Pestaña de Chats
            1 -> GroupsFragment() // Pestaña de Grupos
            else -> ChatsFragment()
        }
    }
}
