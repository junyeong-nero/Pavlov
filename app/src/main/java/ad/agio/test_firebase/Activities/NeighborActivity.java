package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ad.agio.test_firebase.databinding.ActivityNeighborBinding;

public class NeighborActivity extends AppCompatActivity {

    private ActivityNeighborBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNeighborBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}