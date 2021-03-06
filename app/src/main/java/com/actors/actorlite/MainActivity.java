package com.actors.actorlite;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;

import com.actors.Actor;
import com.actors.ActorScheduler;
import com.actors.ActorSystem;
import com.actors.Message;
import com.actors.OnActorUnregistered;
import com.actors.annotations.Spawn;
import com.annotations.Command;
import com.annotations.CommandsMapFactory;
import com.mapper.CommandsMap;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

@CommandsMapFactory
public class MainActivity extends AppCompatActivity implements Actor, OnActorUnregistered {

    private CommandsMap map = CommandsMap.of(this);
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = ViewModelProviders.of(this).get(Model.class);
        ActorSystem.register(model);
        getSupportFragmentManager()
                .beginTransaction()
                .add(new MainFragment(), "MAIN FRAGMENT")
                .commit();

        getFragmentManager()
                .beginTransaction()
                .add(new NonSupportFragment(), "NON SUPPORT FRAGMENT")
                .commit();


    }

    @Override
    protected void onResume() {
        super.onResume();
        sendMessages("message from Activity");
    }

    private void sendMessages(String text) {

        ActorSystem.send(new Message(Model.MSG_PING, text), Model.class);

        Message message = new Message(R.id.message_id_print_fragment_log, text);
        ActorSystem.send(message, MainFragment.class);

        message = new Message(R.id.message_id_print_application_log, text);
        ActorSystem.send(message, MainApp.class);


        // send message after 3 seconds :
        message = new Message(R.id.message_id_print_service_log, text);
        ActorScheduler.after(3000)
                .send(message, MainService.class);
    }


    @Override
    public void onMessageReceived(Message message) {
        map.execute(message.getId(), message.getContent());
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Command(R.id.message_id_print_activity_log)
    void onPrintLogMessage(String text) {
        Log.e("MainActivity", "Thread : " + Thread.currentThread().getId());
        Log.e("MainActivity", text);
    }

    @Override
    public void onUnregister() {
        Log.w(getClass().getSimpleName(), "onCleared()");
    }

    @Override
    protected void onDestroy() {
        ActorSystem.unregister(model);
        if(isFinishing()){
            ActorScheduler.cancel(model.getClass());
        }
        super.onDestroy();
    }
}
