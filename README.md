[![](https://jitpack.io/v/Ahmed-Adel-Ismail/ActorLite.svg)](https://jitpack.io/#Ahmed-Adel-Ismail/ActorLite)

# ActorLite

A Light weight Actor Model library that helps communication between Android Components in a Message Driven manner

# Why ActorLite

ActorLite is based on ErLang's Actor-Model, which depends on communicating between components through messages instead of method calls, this guarantees a very decoupled application

Also being an Actor means that this Actor component (Class) will receive messages on it's own thread, which makes this architecture thread safe

all you need to do is to implement the Actor interface, and you can use <b>ActorSystem</b> to send messages to classes that implement the Actor interface

and as an implementer to the actor interface, you should provide the Thread that your Actor will receive it's messages on, and implement <i>onMessageReceived()</i> to handle incoming messages

also as an actor you do not need to create any other objects, all you need to do is to tell the Actor-System to spawn (create) other Actors for you ... you do not need a dependency injection framework since it is already running under the hood of this framework

you can jump to see the sample code for fully functional module <a href="https://github.com/Ahmed-Adel-Ismail/ActorLite/blob/master/README.md#dependency-injection-with-spawn">here</a>

# How It Works
For every class that implements the <b>Actor</b> interface, it registers itself to the <b>ActorSystem</b>, which is responsible for delivering messages between the registered Actors through there address, the address of any Actor is the <b>Class</b> of it, for example the address of the <b>MainActivity</b> is <b>MainActivity.class</b>, and so on
	
You do not have to hold reference to Any Object any more, just send by the Object/Actor address and it will be received and executed on that Object's favorite thread ... you don't have to worry about multi-threading or references any more
	
To register an Actor to the Actor system, you either extend one of the available classes, or do it manually ... this will be explained in the coming section

# Getting Started - Setup Actors

# Integrate ActorLite to your Application's onCreate() method

In this step, you will cause any <b>Actvity</b> and any <b>android.support.v4.app.Fragment</b> that implements the <b>Actor</b> interface to automatically register and unregister itself to the <b>ActorSystem</b>

```java
@Override
public void onCreate() {
    super.onCreate();
    ActorLite.with(this);
}
```

you can override the the default configuration for the Actor-System through ActorSystemConfiguration :

```java
@Override
public void onCreate() {
    super.onCreate();
    ActorLite.with(this, actorSystemConfiguration());
    ...
}

// these are the default configurations :
private ActorSystemConfiguration actorSystemConfiguration() {
    return new ActorSystemConfiguration.Builder()
            .registerActors(RegistrationStage.ON_START)
            .unregisterActors(UnregistrationStage.ON_DESTROY)
            .postponeMailboxOnStop(true)
            .build();
}
```

# Register Activities or Support Fragments as Actors

For Activities and Support Fragments, all you have to do is implement the Actor interface, and they will be registered / un-registered for you based on the configurations, like the following :

```java
public class MainActivity extends AppCompatActivity implements Actor {

    public static final int MESSAGE_ID_DO_SOMETHING = 1938;

    ...

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        // specify the Thread that onMessageReceived()
        // will be executed on, in Activities
        // and Fragments it should be the
        // Main Thread
        return AndroidSchedulers.mainThread();
    }


    @Override
    public void onMessageReceived(Message message) {
        if(message.getId() == MESSAGE_ID_DO_SOMETHING){
            // handle message on the Thread
            // specified in observeOnScheduler()
        }
    }

}
```

# Register Services as Actors

For Services, you either need to extend the ActorService, or you will register it manually, notice that the Actor-System configuration will not affect Services ... let us take the easy way here and extend <b>ActorService</b> :

```java
public class MainService extends ActorService {

    public static final int MESSAGE_ID_DO_SOMETHING = 1456;

    ...

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        // specify the Thread that onMessageReceived()
        // will be executed on, in Services
        // it is more likely to be a background
        // thread since we need to do stuff
        // that wont update the UI
        return Schedulers.computation();
    }


    @Override
    public void onMessageReceived(Message message) {
        if(message.getId() == MESSAGE_ID_DO_SOMETHING){
            // handle message on the Thread
            // specified in observeOnScheduler()
        }
    }
}
```
	
# Register Application class as an Actor

The Application class itself can be an Actor if it implemented the <b>Actor</b> interface, and you can send to it Messages as well as any other Actor, all you need to do is implement the <b>Actor</b> interface

```java
public class MainApp extends Application implements Actor {

    public static final int MESSAGE_ID_DO_SOMETHING = 1626;

    @Override
    public void onCreate() {
        super.onCreate();
        ActorLite.with(this);
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        // specify the Thread that onMessageReceived()
        // will be executed on, in Application
        // class it is safer to make it in background
        // thread since we need to do stuff
        // that wont update the UI
        return Schedulers.computation();
    }


    @Override
    public void onMessageReceived(Message message) {
        if(message.getId() == MESSAGE_ID_DO_SOMETHING){
            // handle message on the Thread
            // specified in observeOnScheduler()
        }
    }
}
```

# Register any Object as an Actor

For Any Object it should register and unregister itself manually from the <b>ActorSystem</b> and cancel all the pending Messages in the <b>ActorScheduler</b>, so for any Object that will be an Actor, it should have an initialization point, and a destruction point (similar to onCreate() and onDestroy() in the life-Cycle methods), so an example of an Actor Object will be as follows :

```java
public class ActorObject implements Actor {

    public ActorObject() {
        ActorSystem.register(this);
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return Schedulers.computation();
    }


    @Override
    public void onMessageReceived(Message message) {
        // ...
    }

    public void onDestroy() {
        ActorSystem.unregister(this);
        ActorScheduler.cancel(getClass());
    }
}
```

# Sending a message to an actor

```java
MyCustomObject myCustomObject = ...;
Message message = new Message(MainFragment.MESSAGE_ID_DO_SOMETHING, myCustomObject);
ActorSystem.send(message, MainFragment.class);
```

If the <b>MainFragment</b> unregistered itself from <b>ActorSystem</b> before the message is sent, nothing will happen

# Sending a delayed message to an actor

```java
MyCustomObject myCustomObject = ...;
Message message = new Message(MainFragment.MESSAGE_ID_DO_SOMETHING, myCustomObject);
ActorScheduler.after(5000) // 5000 milliseconds
            .send(message, MainFragment.class);
```

# Sending a message to an actor and receiving response

In the Message object, you can set the <b>replyToActor</b> parameter so the message receiver can reply back to the sender, suppose this sample code is from a class named MyActor :

```java
MyCustomObject myCustomObject = ...;
Message message = new Message(MainFragment.MESSAGE_ID_DO_SOMETHING, myCustomObject, MyActor.class);
ActorSystem.send(message, MainFragment.class);
```

now the receiver (MainFragment.class) can send a message back to <b>MyActor.class</b> when it is done

# Using Message Builder

instead of passing too many parameters, you can use ActorSystem.createMessage() as follows :

```java
ActorSystem.createMessage(MSG_ID)
        .withContent("message content")
        .withReplyToActor(MyActor.class)
        .withReceiverActors(ReceiverOne.class, ReceiverTwo.class)
        .send();
```

# Setup android components as actors manually

Remember that you do not need to setup Activities Manually in all cases, so If you choose to Register and Unregister The remaining Android components manually, here is what to be done in every type :

# Setup Fragments (non-support Fragments) Manually

```java
public class MyActorFragment extends Fragment implements Actor {

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();
        ActorSystem.register(this);
    }

    @CallSuper
    @Override
    public void onStop() {
        ActorSystem.postpone(this);
        super.onStop();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        ActorSystem.unregister(this);
        if (getActivity() == null || getActivity().isFinishing()) {
            ActorScheduler.cancel(getClass());
        }
        super.onDestroy();
    }
}
```

# Setup Service Manually

```java
public abstract class MyActorService extends Service implements Actor {

    @CallSuper
    @Override
    public void onCreate() {
        super.onCreate();
        ActorSystem.register(this);
    }

    @CallSuper
    @Override
    public void onDestroy() {
        ActorSystem.unregister(this);
        ActorScheduler.cancel(getClass());
        super.onDestroy();
    }

    ...
}
```

# Listen to ActorSystem.unregister(Actor) through implementing OnActorUnregistered

your Actor can implement <b>OnActorUnregistered</b> to get notified when it is un-registered from the Actor-System, this is Ideal for Actors that are registered and un-registered from out-side there classes, in the next section, you will find heavy use of this interface

# Dependency Injection with @Spawn

Starting from version 1.0.0, you can <b>Spawn</b> Actors through annotations, in other words, you can tell the Actor-System to create another Actor for your current Actor, and when your current Actor is un-registered from the system, the spawned Actors are un-registered as well ... notice that Actors are meant to be singletons in there scope, so if you request to Spawn an Actor multiple times in the same scope, only one Actor will be available in this scope.

# Sample Module using ActorLite

This is an example for a full MVC example from Activity to Model to repository to data sources :

Our Activity will create it's Model (which extends the new architecture components ViewModel), and it will register it to the Actor-System, as follows :

```Java
public class MainActivity extends AppCompatActivity implements Actor {

    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(Model.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Message message = new Message(Model.MSG_PING, "message from MainActivity");
        ActorSystem.send(message, Model.class);
    }


    @Override
    public void onMessageReceived(Message message) {
        // handle messages from others
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return AndroidSchedulers.mainThread();
    }

}
```

And Our Model will request from the ActorSystem to spawn a Repository Actor for it, in other words, it requests from ActorSystem to create a Repository instance (if not created), so as soon as this Model is registered to ActorSystem, the Repository Actor will be registered as well :

```java
@Spawn(Repository.class)
public class Model extends ViewModel implements Actor {

    public static final int MSG_PING = 1;

    public Model(){
        ActorSystem.register(this);
    }

    @Override
    public void onMessageReceived(Message message) {
        if(message.getId() == MSG_PING) {
            Message newMessage = new Message(Repository.MSG_PING,message.getContent());
            ActorSystem.send(newMessage,Repository.class);
        }
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return Schedulers.computation();
    }

    @Override
    public void onCleared() {
        ActorSystem.unregister(this);
        ActorScheduler.cancel(getClass());
    }
}
```

you can pass to the @Spawn annotation a fully qualified class name (which implements Actor) instead of passing the Class, like for example :

```java
@Spawn(actorClasses = "com.actors.actorlite.Repository")
public class Model extends ViewModel implements Actor {
...
}
```

and by the way you can send to this actor by it's fully qualified name as well, for example :

```java
ActorSystem.send(message, "com.actors.actorlite.Repository");
```

if the class is not available or not an Actor, the ActorSystem will print an Exception and wont spawn the wrong elements


Our Model requested from the Actor-System to Spawn Repository.java, so the System will create this Actor as long as the Model is registered, and it will unregister this Actor when the Model is unregistered ... notice that the Spawned Actor will be registered as long as the first one that requested it to be spawned is still registered as well, and it will unregister on it's unregistration from the Actor-System  :

```java
@Spawn({ServerDataSource.class, DatabaseDataSource.class})
public class Repository implements Actor {

    public static final int MSG_PING = 1;

    public Repository(){
        // spawned Actors should have a default constructor
        // or no constructors at all
    }

    @Override
    public void onMessageReceived(Message message) {
        ActorSystem.send(new Message(ServerDataSource.MSG_PING,"message from repository"), ServerDataSource.class);
        ActorSystem.send(new Message(DatabaseDataSource.MSG_PING,"message from repository"), DatabaseDataSource.class);
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return Schedulers.computation();
    }
}
```

And Our Repository requested from the Actor-System to Spawn two Actors for it, which are ServerDataSource.java and DatabaseDataSource.java ... so they will be created as long as the Repository is Registered, and they are :

```java
public class ServerDataSource implements Actor, OnActorUnregistered {

    public static final int MSG_PING = 1;
    private final ServerApi serverApi = ...;

    @Override
    public void onMessageReceived(Message message) {
        // handle messages and retrieve data from server
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return Schedulers.io();
    }

    @Override
    public void onUnregister() {
        serverApi.close();
    }
}
```

```java
public class DatabaseDataSource implements Actor, OnActorUnregistered {

    public static final int MSG_PING = 1;
    private final Database database = ...;

    @Override
    public void onMessageReceived(Message message) {
        // handle messages and retrieve data from database
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return Schedulers.io();
    }

    @Override
    public void onUnregister() {
        database.close();
    }
}
```

 Notice that if you Spawn an actor multiple times in the same scope, only one instance will be created and running, and it will stay registered until all the actors depending on it are unregistered

 You can Spawn all the desired Actors when you start your Model as follows :

 ```Java
 @Spawn({Repository.class, ServerDataSource.class, DatabaseDataSource.class})
 public class Model extends ViewModel implements Actor {
    ...
 }
 ```

So all those Actors will be available as long as the Model is registered (remember that the architecture components <b>ViewModel</b> class will survive the Activity's rotation, so the <b>Model</b> will be registered as long as the Activity is not totally destroyed)


# Tips

To Avoid the big if/else blocks in onMessageReceived(), you can use <b>CommandsMap</b> instead ( https://github.com/Ahmed-Adel-Ismail/CommandsMap ), it is also used in the sample application in this repository, sample code for using <b>CommandsMap</b> is as follows :

```java
@CommandsMapFactory
public class MainActivity extends AppCompatActivity implements Actor {

    public static final int MSG_ONE_ID = 1;
    public static final int MSG_TWO_ID = 1;

    private CommandsMap map = CommandsMap.of(this);

    ...

    @Override
    public void onMessageReceived(Message message) {
        map.execute(message.getId(), message.getContent());
    }

    @Command(MSG_ONE_ID)
    void onMessageOneReceived(String text) {
        // handle Message with ID as 1 and it's Message.getContent()
        // returns String
    }

    @Command(MSG_TWO_ID)
    void onMessageTwoReceived(Integer value) {
        // handle Message with ID as 2 and it's Message.getContent()
        // returns Integer
    }
}
```

# Unit Testing

This library provides it's own Testing DSL to help you test your Actors without the need for Mocking or handling multithreading, for example suppose we have those Actors that are communicating with each other :

```java
@Spawn(DependencyActor.class)
class TargetActor implements Actor {

    private Message lastMessage;

    TargetActor() {
    }

    @Override
    public void onMessageReceived(Message message) {
        this.lastMessage = message;
        if (message.getId() == 1) {
            // do some logic
            ActorSystem.send(1, DependencyActor.class);
        } else if (message.getId() == 2) {
            // do some logic
            ActorSystem.send(3, CallbackActor.class);
        }

    }

    Message getLastMessage(){
        return lastMessage;
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return Schedulers.io();
    }
}


class DependencyActor implements Actor {

    DependencyActor() {
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message.getId() == 1) {
            // do some logic
            ActorSystem.send(2, TargetActor.class);
        }
    }

    @NonNull
    @Override
    public Scheduler observeOnScheduler() {
        return Schedulers.computation();
    }
}
```

Suppose that the <b>CallbackActor</b> is sending messages to <b>TargetActor</b>, which communicates with <b>DependencyActor</b>, and retrieves results from it then replies back to the <b>CallbackActor</b>

so we can make unit-tests for the <b>TargetActor</b> in isolation from the external world with just few lines

<b>1- Let us check if the lastMessage is updated when we send a message to this Actor: </b>

```java
@Test
public void sendMessageToTargetThenUpdateLastMessageValue() throws Exception {
    Message lastMessage = ActorsTestRunner.testActor(TargetActor.class)
            .captureUpdate(TargetActor::getLastMessage)
            .sendMessage(1)
            .withContent("one")
            .getUpdate();

    assertEquals("one",lastMessage.getContent());
}
```

with those few lines, the <b>ActorsTestRunner</b> handled mocking all the dependencies for us, also it handled multithreading as well (remember this Actor is supposed to run on a background thread)

<b>2- Let us check if the TargetActor will send the message with the id "1" to the DependencyActor and handle it's response message as expected: </b>

```java
@Test
public void sendMessageWithIdOneToDependencyActorThenHandleItsResponse()
        throws Exception {

    Message lastMessage = ActorsTestRunner.testActor(TargetActor.class)
            .captureUpdate(TargetActor::getLastMessage)
            .mock(DependencyActor.class, this::handleMessageWithIdOne)
            .sendMessage(1)
            .getUpdate();

    assertEquals(100, lastMessage.getId());
}

private void handleMessageWithIdOne(ActorSystemInstance actorSystem, Message message) {
    if (message.getId() == 1) {
        actorSystem.send(new Message(100, "fake-message"), TargetActor.class);
    }
}
```

in the previous test, we made sure that the <b>TargetActor</b> behaves as expected and it delivers the message to the <b>DependencyActor</b>, we mocked the behavior of the <b>DependencyActor</b> in those lines :

```java
.mock(DependencyActor.class, this::handleMessageWithIdOne)

...

private void handleMessageWithIdOne(ActorSystemInstance actorSystem,Message message) {
        if (message.getId() == 1) {
            actorSystem.send(new Message(100, "fake-message"), TargetActor.class);
        }
    }

```

This is how <b>ActorsTestRunner</b> guarantees isolation of the Actor under testing, it does not create any dependencies for your Actor under test, so if you want to create a dependency, you need to mock it, and it will replaces the Mocks with the real dependencies

<b>3- After we made sure that the TargetActor sends a message to it's dependency properly, in the next example, we will test that our TargetActor replies to the CallbackActor properly:</b>

```java
@Test
public void sendMessageWithIdTwoThenReceiveMessageWithIdThreeOnCallbackActor()
        throws Exception {

    int messageId = ActorsTestRunner.testActor(TargetActor.class)
            .whenReplyToAddress(CallbackActor.class)
            .captureReply(Message::getId)
            .sendMessage(2)
            .getReply();

    assertEquals(3,messageId);

}
```

In this last test, we made sure that when our <b>TargetActor</b> receives a Message with the id "2", it will send another Message to the <b>CallbackActor</b> with the id "3"

Notice that we did not need to mock the <b>CallbackActor</b> to be able to know it received the message, <b>ActorsTestRunner</b> handles the mocking process for us

With <b>ActorsTestRunner</b>'s DSL, you can hit 90% + test coverage with just few lines, by the way, the Unit test for this <b>TargetActor</b> covers 100% with only 20 lines, no Traditional Mocking or Test-doubles, no Multithreading handling, just 5 or 6 lines per Unit-Test and that's all

Notice also that you did not need to design your original code for test-ability, as the <b>ActorsTestRunner</b> handles this for you, it switches the production environment with a Testing Environment, and makes a special environment for every Unit-Test, which makes it easy to run the Unit-Tests in Parallel as well

There are many other features provided by <b>ActorsTestRunner</b> to make Unit-Testing a piece of cake, I did not mention every thing here

# Gradle Dependencies

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```gradle
dependencies {
        compile 'com.github.Ahmed-Adel-Ismail:ActorLite:1.1.5'
}
```

# Pro-Guard

```proguard
# Keep default constructors inside classes
-keepclassmembers class * {
   public protected <init>(...);
   <init>(...);
}
```

* Any feedback regarding Proguard, please open an issue with it