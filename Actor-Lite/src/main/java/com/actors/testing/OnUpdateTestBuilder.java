package com.actors.testing;

import android.support.annotation.NonNull;

import com.actors.Actor;
import com.actors.ActorSystemInstance;
import com.actors.Message;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * a class that handles building the unit test environment
 * <p>
 * Created by Ahmed Adel Ismail on 3/4/2018.
 */
public class OnUpdateTestBuilder<T extends Actor, R> extends ActorTestBuilder<T, T, R> {

    OnUpdateTestBuilder(Class<? extends T> callbackActor, Function<T, R> validationFunction) {
        super(callbackActor, validationActorFunction(validationFunction));
    }

    @NonNull
    private static <T extends Actor, R> Function<Object, R> validationActorFunction(
            final Function<T, R> validationFunction) {
        return new Function<Object, R>() {
            @SuppressWarnings("unchecked")
            @Override
            public R apply(Object o) throws Exception {
                return validationFunction.apply((T) o);
            }
        };
    }

    @Override
    public OnUpdateTestBuilder<T, R> mock(
            Class<?> actor, BiConsumer<ActorSystemInstance, Message> onMessageReceived) {
        super.mock(actor, onMessageReceived);
        return this;
    }

    @Override
    public OnUpdateTestBuilder<T, R> prepare(Consumer<T> preparation) {
        super.prepare(preparation);
        return this;
    }

    /**
     * prepare a {@link Message} to run the Unit test
     *
     * @param messageId the message ID
     * @return a {@link ActorsTestMessageBuilder} to handle creating a {@link Message}
     */
    public OnUpdateTestMessageBuilder<T, R> sendMessage(int messageId) {
        return new OnUpdateTestMessageBuilder<>(this, messageId);
    }


    @Override
    void registerActors(Class<? extends T> targetActor) throws Exception {
        new OnUpdateTestRegistration<T, R>().accept(targetActor, this);
    }


}
