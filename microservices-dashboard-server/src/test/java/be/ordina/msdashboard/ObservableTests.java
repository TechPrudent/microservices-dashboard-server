/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ordina.msdashboard;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.assertj.core.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.plugins.DebugHook;
import rx.plugins.DebugNotification;
import rx.plugins.DebugNotificationListener;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Tests for observable merging
 * Ignored due to emission delays
 * @author Andreas Evers
 */
@Ignore
public class ObservableTests {

    private static final Logger logger = LoggerFactory.getLogger(ObservableTests.class);

    @Test
    public void testingCombiningNestedObservables() {
        Observable<String> observable1 = Observable.from(Arrays.array(1, 2, 3, 4, 5, 6, 7, 8, 9)).map(el -> "a" + el);
        Observable<String> observable2 = Observable.from(Arrays.array(10, 20, 30, 40, 50, 60, 70, 80, 90)).map(el -> "a" + el);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[]{observable1, observable2});
        Observable<String> mergedObservable = Observable.merge(observableObservable);
        mergedObservable.subscribe(System.out::println);
    }

    @Test
    public void testingObservablesWithLatency() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(10);
        Observable<Long> observable1 = Observable.interval(1L, SECONDS).take(10);
        observable1.subscribe((x) -> {
            latch.countDown();
            System.out.println(x);
        });
        latch.await();
    }

    @Test
    public void testingCombiningNestedObservablesWithLatency() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(19);
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).take(10);
        Observable<String> observable2 = Observable.from(Arrays.array(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L)).map(el -> "b" + el);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.merge(observableObservable);
        mergedObservable.subscribe((x) -> {
            latch.countDown();
            System.out.println(x);
        });
        latch.await();
    }

    @Test
    public void testingCombiningNestedObservablesWithDoubleLatency() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(20);
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).take(10);
        Observable<String> observable2 = Observable.interval(2L, 1L, SECONDS).map(el -> "b" + el).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.merge(observableObservable);
        mergedObservable.subscribe((x) -> {
            latch.countDown();
            System.out.println(x);
        });
        latch.await();
    }

    @Test
    public void testingCombiningNestedObservablesWithBlocking() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).take(10);
        Observable<String> observable2 = Observable.interval(2L, 1L, SECONDS).map(el -> "b" + el).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.merge(observableObservable);
        mergedObservable.toBlocking().subscribe(System.out::println);
    }

    @Test
    public void testingCombiningNestedObservablesWithExplicitSleep() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(aLong -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.merge(observableObservable);
        mergedObservable.toBlocking().subscribe(logger::info);
    }

    @Test
    public void testingCombiningNestedObservablesWithBlockingAndLogging() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).doOnEach(aLong -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable);
        mergedObservable.toBlocking().subscribe(System.out::println);
    }

    @Test
    public void testingCombiningNestedObservablesWithLatchAndLogging() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).doOnEach(aLong -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable);
        mergedObservable.subscribeOn(Schedulers.io()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
        });
        latch.await();
    }

    @Test
    public void testingCombiningNestedObservablesWithSchedulers() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable).subscribeOn(Schedulers.io());
        mergedObservable.toBlocking().subscribe(logger::info);
    }

    @Test
    public void testingCombiningNestedObservablesWithoutSchedulers() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable);
        mergedObservable.toBlocking().subscribe(logger::info);
    }

    @Test
    public void testingObserveOn() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        observable1.observeOn(Schedulers.computation());
        observable1.toBlocking().subscribe(s -> {
            logger.info("Got {}", s);
        }, e -> logger.error(e.getMessage(), e), () -> logger.info("Completed"));
    }

    @Test
    public void testingSubscribeOn() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        observable1.subscribeOn(Schedulers.computation());
        observable1.toBlocking().subscribe(s -> {
            logger.info("Got {}", s);
        }, e -> logger.error(e.getMessage(), e), () -> logger.info("Completed"));
    }

    @Test
    public void testWithoutObserveOnOrSubscribeOn() throws InterruptedException {
        Observable<String> observable = Observable.<String>create(s -> {
            logger.info("Start: Executing a Service");
            for (int i = 1; i <= 3; i++) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Emitting {}", "root " + i);
                s.onNext("root " + i);
            }
            logger.info("End: Executing a Service");
            s.onCompleted();
        });

        CountDownLatch latch = new CountDownLatch(1);

        observable.subscribe(s -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Got {}", s);
        }, e -> logger.error(e.getMessage(), e), () -> latch.countDown());

        latch.await();
    }

    @Test
    public void testWithSubscribeOn() throws InterruptedException {
        ExecutorService executor1 = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("SubscribeOn-%d").build());
        Observable<String> observable = Observable.<String>create(s -> {
            logger.info("Start: Executing a Service");
            for (int i = 1; i <= 3; i++) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Emitting {}", "root " + i);
                s.onNext("root " + i);
            }
            logger.info("End: Executing a Service");
            s.onCompleted();
        });

        CountDownLatch latch = new CountDownLatch(1);

        observable.subscribeOn(Schedulers.from(executor1)).subscribe(s -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Got {}", s);
        }, e -> logger.error(e.getMessage(), e), () -> latch.countDown());

        latch.await();
    }

    @Test
    public void testWithObserveOn() throws InterruptedException {
        ExecutorService executor1 = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("SubscribeOn-%d").build());
        Observable<String> observable = Observable.<String>create(s -> {
            logger.info("Start: Executing a Service");
            for (int i = 1; i <= 3; i++) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Emitting {}", "root " + i);
                s.onNext("root " + i);
            }
            logger.info("End: Executing a Service");
            s.onCompleted();
        });

        CountDownLatch latch = new CountDownLatch(1);

        observable.observeOn(Schedulers.from(executor1)).subscribe(s -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Got {}", s);
        }, e -> logger.error(e.getMessage(), e), () -> latch.countDown());

        latch.await();
    }

    @Test
    public void testRxJavaDebug() {
        RxJavaPlugins.getInstance().registerObservableExecutionHook(new DebugHook(new DebugNotificationListener() {
            public Object onNext(DebugNotification n) {
                logger.info("onNext on " + n);
                return super.onNext(n);
            }

            public Object start(DebugNotification n) {
                logger.info("start on " + n);
                return super.start(n);
            }

            public void complete(Object context) {
                logger.info("complete on " + context);
            }

            public void error(Object context, Throwable e) {
                logger.error("error on " + context);
            }
        }));
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        observable1.toBlocking().subscribe(System.out::println);
    }

}