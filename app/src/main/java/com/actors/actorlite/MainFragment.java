package com.actors.actorlite;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actors.Actor;
import com.actors.ActorSystem;
import com.actors.Message;
import com.actors.OnActorUnregistered;
import com.annotations.Command;
import com.annotations.CommandsMapFactory;
import com.mapper.CommandsMap;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Ahmed Adel Ismail on 12/26/2017.
 */
@CommandsMapFactory
public class MainFragment extends Fragment implements Actor, OnActorUnregistered {

    private CommandsMap map = CommandsMap.of(this);

    @Override
    public void onMessageReceived(Message message) {
        map.execute(message.getId(), message.getContent());
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Command(R.id.message_id_print_fragment_log)
    void onPrintLogMessage(String text) {
        Log.e("MainFragment", "Thread : " + Thread.currentThread().getId());
        Log.e("MainFragment", text);

        String content = "message from Fragment";

        Message message = new Message(R.id.message_id_print_non_support_fragment_log, content);
        ActorSystem.send(message, NonSupportFragment.class);

        message = new Message(R.id.message_id_print_activity_log, content);
        ActorSystem.send(message, MainActivity.class);

    }

    @Override
    public void onUnregister() {
        Log.w(getClass().getSimpleName(), "onCleared()");
    }
}
