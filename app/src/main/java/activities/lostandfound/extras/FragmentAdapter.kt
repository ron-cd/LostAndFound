package activities.lostandfound.extras

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter responsible for managing and displaying fragments within a ViewPager2.
 */
class FragmentAdapter(
    private val fragments: List<Fragment>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    /**
     * Returns the fragment associated with the specified position.
     */
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    /**
     * Returns the total number of fragments in the list.
     */
    override fun getItemCount(): Int {
        return fragments.size
    }
}