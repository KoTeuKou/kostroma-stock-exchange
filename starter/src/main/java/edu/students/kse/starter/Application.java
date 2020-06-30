package edu.students.kse.starter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import edu.students.kse.fixoe.FixServerActor;
import edu.students.kse.me.METimer;
import edu.students.kse.me.MatchingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws TimeoutException, InterruptedException {
        ActorSystem system = ActorSystem.create("kse");
        ActorRef meRef = system.actorOf(Props.create(MatchingEngine.class));
        ActorRef fixRef = system.actorOf(Props.create(FixServerActor.class, meRef));
        ActorRef meTimerRef = system.actorOf(Props.create(METimer.class, meRef));
        system.scheduler().schedule(java.time.Duration.ofMillis(0),
                java.time.Duration.ofMillis(250), meTimerRef, "getTime", system.dispatcher(), ActorRef.noSender());
        logger.info("Application is running");

        Await.ready(system.whenTerminated(), Duration.apply(1, TimeUnit.DAYS));
    }
}
