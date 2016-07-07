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
import rx.Observable.Transformer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.plugins.DebugHook;
import rx.plugins.DebugNotification;
import rx.plugins.DebugNotificationListener;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

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

    @Test
    public void testReduceWithoutError() {
        Observable<String> observable = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);

        observable.reduce("start-", (a, b) -> a + b + "-").toBlocking().subscribe(logger::info);
    }

    @Test
    public void testMergeAndReduceWithoutError() {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable);
        mergedObservable.reduce("start-", (a, b) -> a + b + "-").toBlocking().subscribe(logger::info);
    }

    @Test
    public void testMergeAndReduceWithErrorReturned() {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10).map(i -> throwException(i));
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable)
                .doOnError((a) -> logger.error("Error: " + a.toString()));
        mergedObservable.reduce("start-", (a, b) -> a + b + "-").toBlocking().subscribe(logger::info, e -> logger.error("" + e));
    }

    @Test
    public void testMergeAndReduceWithErrorIgnored() {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            logger.info(notification.toString());
        }).take(10).map(i -> throwException(i));
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable)
                .doOnError((a) -> logger.error("Error: " + a.toString()))
                .onErrorResumeNext(Observable.empty());
        mergedObservable.reduce("start-", (a, b) -> a + b + "-").toBlocking().subscribe(logger::info);
    }

    private String throwException(String i) {
        if (i.equals("b4")) {
            throw new RuntimeException("Error");
        }
        return i;
    }

    @Test
    public void testUncheckedExceptionHandling() {
        Observable.just("Hello!")
                .map(input -> { throw new RuntimeException(); })
                .subscribe(
                        System.out::println,
                        error -> System.out.println("Error!")
                );
    }

    @Test
    public void testUncheckedFatalExceptionHandling() {
        Observable.just("Hello!")
                .map(input -> { throw new StackOverflowError(); })
                .subscribe(
                        System.out::println,
                        error -> System.out.println("Error!")
                );
    }

    private String transform(String input) throws IOException {
        throw new IOException();
    }

    @Test
    public void testCheckedExceptionHandling() {
        Observable.just("Hello!")
                .map(input -> {
                    try {
                        return transform(input);
                    } catch (Throwable t) {
                        throw Exceptions.propagate(t);
                    }
                })
                .subscribe(
                        System.out::println,
                        error -> System.out.println("Error!")
                );
    }

    @Test
    public void testCheckedExceptionHandlingWithoutSubscription() {
        Observable.just("Hello!")
                .map(input -> {
                    try {
                        return transform(input);
                    } catch (Throwable t) {
                        throw Exceptions.propagate(t);
                    }
                }).subscribe();
    }

    @Test
    public void testCheckedExceptionHandlingWithObservableError() {
        Observable.just("Hello!")
                .flatMap(input -> {
                    try {
                        return Observable.just(transform(input));
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }).subscribe(
                        System.out::println,
                        error -> System.out.println("Error!")
                );
    }

    @Test
    public void testMergedExceptionHandling() {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).take(10);
        Observable<String> observable2 = Observable.just("Hello!")
                .flatMap(input -> {
                    try {
                        return Observable.just(transform(input));
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                });
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable);
        mergedObservable.toBlocking().subscribe(
                    s -> System.out.println(s),
                    error -> System.out.println("Error!")
                );
    }

    @Test
    public void testExceptionHandlingWithMultipleLevels1() {
        Observable<String> observable = Observable.interval(1L, SECONDS).map(el -> {
            if (el == 8L) throw new RuntimeException();
            return "a" + el;
        }).take(10);
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    @Test
    public void testExceptionHandlingWithMultipleLevels2() {
        Observable<String> observable = Observable.interval(1L, SECONDS).map(el -> {
            if (el == 8L) throw new RuntimeException("Error");
            return "a" + el;
        }).take(10)
                .doOnError(e -> logger.error("Error caught: " + e.getMessage()))
                .onErrorResumeNext(Observable.empty());
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    @Test
    public void testExceptionHandlingWithMultipleLevels3() {
        Observable<String> observable = Observable.interval(1L, SECONDS)
                .map(el -> {
                    if (el == 8L) throw new RuntimeException("Error1");
                    return "a" + el;
                })
                .take(10)
                .map(el -> {
                    if (el.equals("a9")) throw new RuntimeException("Error2");
                    return "b" + el;
                })
                .doOnError(e -> logger.error("Error caught: " + e.getMessage()))
                .onErrorResumeNext(Observable.empty());
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    @Test
    public void testExceptionHandlingWithMultipleLevels4() {
        Observable<String> observable = Observable.interval(1L, SECONDS)
                .map(el -> {
                    if (el == 5L) throw new RuntimeException("Error1");
                    return "a" + el;
                })
                .take(10);
        observable.onExceptionResumeNext(observable)
                .doOnError(e -> logger.error("Error caught: " + e.getMessage()))
                .onErrorResumeNext(Observable.empty());
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    @Test
    public void testSuppressExceptionAndContinueWithCustomOperator() {
        Observable<String> observable = Observable.interval(1L, SECONDS)
                .map(el -> {
                    if (el == 5L) throw new RuntimeException("Error1");
                    return "a" + el;
                })
                .take(10)
                .lift(new OperatorSuppressError(el -> System.out.println("Error lifted!")));
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    final class OperatorSuppressError<T> implements Observable.Operator<T, T> {
        final Action1<Throwable> onError;

        public OperatorSuppressError(Action1<Throwable> onError) {
            this.onError = onError;
        }

        @Override
        public Subscriber<? super T> call(final Subscriber<? super T> t1) {
            return new Subscriber<T>(t1) {

                @Override
                public void onNext(T t) {
                    t1.onNext(t);
                }

                @Override
                public void onError(Throwable e) {
                    onError.call(e);
                }

                @Override
                public void onCompleted() {
                    t1.onCompleted();
                }

            };
        }
    }

    @Test
    public void testSuppressExceptionAndContinue() {
        Observable<String> observable = Observable.interval(1L, SECONDS)
                .map(el -> {
                    try {
                        if (el == 5L) throw new RuntimeException("Error1");
                    } catch (RuntimeException e) {
                        System.out.println(e);
                        return null;
                    }
                    return "a" + el;
                })
                .filter(Objects::nonNull)
                .take(10)
                .lift(new OperatorSuppressError(el -> System.out.println("Error lifted!")));
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    @Test
    public void testSuppressExceptionAndContinue2() {
        Observable<String> observable = Observable.interval(1L, SECONDS)
                .concatMap(el -> {
                    try {
                        if (el == 5L) throw new RuntimeException("Error1");
                    } catch (RuntimeException e) {
                        System.out.println(e);
                        return Observable.empty();
                    }
                    return Observable.just("a" + el);
                })
                .onErrorResumeNext(Observable.empty())
                .take(10);
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    @Test
    public void testSuppressExceptionAndContinue3() {
        Observable<String> observable = Observable.interval(1L, SECONDS)
                .concatMap(el -> {
                    return Observable.defer(() ->  {
                        if (el == 5L) throw new RuntimeException("Error1");
                        return Observable.just("a" + el);
                    }).onErrorResumeNext(Observable.empty());
                }).take(10);
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    /*@Test
    public void testSuppressExceptionAndContinue4() {
        Observable<String> observable = Observable.interval(1L, SECONDS)
                .compose(failSafeMap(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long t) {
                        return 1L;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }))
                .take(10);
        observable.toBlocking().subscribe(
                s -> System.out.println(s),
                error -> System.out.println("Error!")
        );
    }

    public static <T, R> Transformer<Observable<T>, Observable<R>> failSafeMap(Function<T, R> mapper, Consumer<Throwable> onError) {
        return new Transformer<Observable<T>, Observable<R>>() {
            @Override
            public Observable<Observable<R>> call(Observable<Observable<T>> observableObservable) {
                return obs.concatMap(new Func1<T, Observable<? extends R>>() {
                    @Override
                    public Observable<? extends R> call(T t) {
                        return Observable.defer(new Func0<Observable<R>>() {
                            @Override
                            public Observable<R> call() {
                                return (Observable<R>) mapper.apply(t);
                            }
                        }).onErrorResumeNext(Observable.empty());
                    }
                });
            }
        };
    }*/
}