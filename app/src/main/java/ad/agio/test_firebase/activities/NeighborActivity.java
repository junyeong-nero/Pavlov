package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ad.agio.test_firebase.Fragments.NeighborAuthFragment;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.databinding.ActivityNeighborBinding;

public class NeighborActivity extends AppCompatActivity {

    private ActivityNeighborBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNeighborBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> finish());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container,
                        new NeighborAuthFragment(),
                         "NeighborAuthFragment")
                .commit();
    }
}