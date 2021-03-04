package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import ad.agio.test_firebase.ExpandableListAdapter;
import ad.agio.test_firebase.controller.NoticeController;
import ad.agio.test_firebase.databinding.ActivityNoticeBinding;
import ad.agio.test_firebase.domain.Notice;

public class NoticeActivity extends AppCompatActivity {

    private ActivityNoticeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        NoticeController noticeController = new NoticeController();
        noticeController.readNotice(list -> {
            List<ExpandableListAdapter.Item> data = new ArrayList<>();
            for (Notice notice : list) {
                ExpandableListAdapter.Item places = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, notice.title);
                places.invisibleChildren = new ArrayList<>();
                places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, notice.content));
                data.add(places);
//                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, notification.title));
//                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, notification.content));
            }
            binding.recyclerview.setAdapter(new ExpandableListAdapter(data));
        });

//        ExpandableListAdapter.Item places = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Places");
//        places.invisibleChildren = new ArrayList<>();
//        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Kerala"));
//        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Tamil Nadu"));
//        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Karnataka"));
//        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Maharashtra"));

//        data.add(places);
    }
}