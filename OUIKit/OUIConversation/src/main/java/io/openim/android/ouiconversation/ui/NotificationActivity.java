package io.openim.android.ouiconversation.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.adapter.MessageViewHolder;
import io.openim.android.ouiconversation.databinding.ActivityNotificationBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.sdk.models.Message;

public class NotificationActivity extends BaseActivity<ChatVM, ActivityNotificationBinding> implements ChatVM.ViewAction {

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityNotificationBinding.inflate(getLayoutInflater()));
        sink();

        initView();
        init();
        listener();
        vm.markReadedByConID(vm.conversationID, null);
    }

    private void listener() {
        vm.messages.observe(this, v -> {
            if (null == v) return;
            messageAdapter.setMessages(v);
            messageAdapter.notifyDataSetChanged();
        });
    }

    private void initView() {
        ChatActivity.LinearLayoutMg linearLayoutManager = new ChatActivity.LinearLayoutMg(this);

        view.recyclerView.setLayoutManager(linearLayoutManager);
        view.recyclerView.addItemDecoration(new DefaultItemDecoration(this.getResources().getColor(android.R.color.transparent), 1, Common.dp2px(16)));
        messageAdapter = new MessageAdapter();
        messageAdapter.bindRecyclerView(view.recyclerView);
        vm.setMessageAdapter(messageAdapter);
        view.recyclerView.setAdapter(messageAdapter);
    }

    void init() {
        String name = getIntent().getStringExtra(Constant.K_NAME);
        String id = getIntent().getStringExtra(Constant.K_ID);
        view.title.setText(name);
        vm.conversationID = id;
        vm.loadHistoryMessage();
    }


    @Override
    public void scrollToPosition(int position) {

    }

    @Override
    public void closePage() {

    }
}
