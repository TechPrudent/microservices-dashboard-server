package be.ordina.msdashboard;

import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Andreas Evers
 */
public class ObservableTests {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableTests.class);

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
        mergedObservable.toBlocking().subscribe(LOG::info);
    }

    @Test
    public void testingCombiningNestedObservablesWithBlockingAndLogging() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            LOG.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            LOG.info(notification.toString());
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
            LOG.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            LOG.info(notification.toString());
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
            LOG.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            LOG.info(notification.toString());
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable).subscribeOn(Schedulers.io());
        mergedObservable.toBlocking().subscribe(LOG::info);
    }

    @Test
    public void testingCombiningNestedObservablesWithoutSchedulers() throws InterruptedException {
        Observable<String> observable1 = Observable.interval(1L, SECONDS).map(el -> "a" + el).doOnEach(notification -> {
            LOG.info(notification.toString());
        }).take(10);
        Observable<String> observable2 = Observable.interval(1L, SECONDS).map(el -> "b" + el).doOnEach(notification -> {
            LOG.info(notification.toString());
        }).take(10);
        Observable<Observable<String>> observableObservable =
                Observable.from(new Observable[] { observable1, observable2 });
        Observable<String> mergedObservable = Observable.mergeDelayError(observableObservable);
        mergedObservable.toBlocking().subscribe(LOG::info);
    }
}