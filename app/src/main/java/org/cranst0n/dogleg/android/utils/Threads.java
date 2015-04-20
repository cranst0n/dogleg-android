package org.cranst0n.dogleg.android.utils;

import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Threads {

  private static final ExecutorService executor = Executors.newFixedThreadPool(4);

  public static boolean isUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

  public static Future<?> background(@NonNull final FutureTask<?> task) {
    return executor.submit(task);
  }

  public static <T> Future<T> background(@NonNull final Callable<T> callable) {
    return executor.submit(callable);
  }

}
