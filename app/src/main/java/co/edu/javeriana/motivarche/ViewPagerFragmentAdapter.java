package co.edu.javeriana.motivarche;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    private String [] titles = new String[]{"Chats","Usuarios"};

    public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new UsersFragment();
        }
        return new ChatsFragment();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
