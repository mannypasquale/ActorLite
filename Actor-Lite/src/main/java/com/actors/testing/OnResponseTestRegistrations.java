package com.actors.testing;

import android.support.annotation.NonNull;

import com.actors.Actor;
import com.actors.Message;
import com.actors.R;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

class OnResponseTestRegistrations<T extends Actor, V extends Actor, R>
        implements BiConsumer<Class<? extends T>, ActorTestBuilder<T, V, R>> {

    @Override
    public void accept(Class<? extends T> targetActor, ActorTestBuilder<T, V, R> builder) throws Exception {
        new MocksRegistration().accept(builder);
        registerValidateOnActor(builder);
        registerTargetActor(builder, targetActor);
    }

    private void registerValidateOnActor(ActorTestBuilder<T, V, R> builder) throws Exception {
        builder.system.register(builder.validateOnActor,
                builder.system.testScheduler, updateResultOnMessageReceived(builder));
    }

    private void registerTargetActor(ActorTestBuilder<T, V, R> builder, final Class<? extends T> targetActor)
            throws Exception {
        final T actor = new ActorInitializer<>(builder.preparations).apply(targetActor);
        builder.system.register(actor, builder.system.testScheduler, new Consumer<Message>() {
            @Override
            public void accept(Message message) throws Exception {
                actor.onMessageReceived(message);
            }
        });
    }

    @NonNull
    private Consumer<Message> updateResultOnMessageReceived(final ActorTestBuilder<T, V, R> testBuilder) {
        return new Consumer<Message>() {
            @Override
            public void accept(Message message) throws Exception {
                testBuilder.result.set(0, testBuilder.validationFunction.apply(message));
            }
        };
    }


}
