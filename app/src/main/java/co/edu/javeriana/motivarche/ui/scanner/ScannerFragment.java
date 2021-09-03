
package co.edu.javeriana.motivarche.ui.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import co.edu.javeriana.motivarche.R;


public class ScannerFragment extends Fragment{
    public CustomARFragment arFragment;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View scannerView = inflater.inflate(R.layout.fragment_scanner, container, false);

        return scannerView;
    }




}
