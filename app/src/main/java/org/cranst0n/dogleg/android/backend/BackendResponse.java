package org.cranst0n.dogleg.android.backend;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.ion.Response;

import org.cranst0n.dogleg.android.utils.Json;
import org.cranst0n.dogleg.android.utils.Threads;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class BackendResponse<T extends JsonElement, U> {

  private final Future<Response<T>> ionCall;

  protected final Type type;
  protected final Class<U> clazz;

  protected final String Tag = getClass().getSimpleName();

  @Nullable
  protected Exception exception;
  @Nullable
  protected U value;
  @Nullable
  protected BackendMessage errorMessage;

  protected final List<BackendSuccessListener<U>> successListeners = new ArrayList<>();
  protected final List<BackendErrorListener> errorListeners = new ArrayList<>();
  protected final List<BackendExceptionListener> exceptionListeners = new ArrayList<>();
  protected final List<BackendFinallyListener> finallyListeners = new ArrayList<>();

  private BackendResponse() {
    ionCall = null;
    clazz = null;
    type = null;

    before();
  }

  public BackendResponse(@NonNull final Future<Response<T>> ionCall, @NonNull final Type type) {

    this.ionCall = ionCall;
    this.type = type;
    this.clazz = null;

    this.ionCall.setCallback(new ResultCallback());

    before();
  }

  public BackendResponse(@NonNull final Future<Response<T>> ionCall,
                         @NonNull final Class<U> clazz) {

    this.ionCall = ionCall;
    this.type = null;
    this.clazz = clazz;

    this.ionCall.setCallback(new ResultCallback());

    before();
  }

  protected void before() {

  }

  public boolean isDone() {
    if (ionCall != null) {
      return ionCall.isDone();
    } else {
      return value != null || errorMessage != null || exception != null;
    }
  }

  public void cancel() {
    if (ionCall instanceof SimpleFuture) {
      ((SimpleFuture) ionCall).cancelSilently();
    } else if (ionCall != null) {
      ionCall.cancel();
    }
  }

  @NonNull
  public BackendResponse<T, U> onSuccess(@NonNull final BackendSuccessListener<U> listener) {
    successListeners.add(listener);
    return this;
  }

  @NonNull
  public BackendResponse<T, U> onError(@NonNull final BackendErrorListener listener) {
    errorListeners.add(listener);
    return this;
  }

  @NonNull
  public BackendResponse<T, U> onException(@NonNull final BackendExceptionListener listener) {
    exceptionListeners.add(listener);
    return this;
  }

  @NonNull
  public BackendResponse<T, U> onFinally(@NonNull final BackendFinallyListener listener) {
    finallyListeners.add(listener);
    return this;
  }

  protected void notifySuccess(@NonNull final U value) {
    for (BackendSuccessListener l : successListeners) {
      l.onSuccess(value);
    }
  }

  protected void notifyError(@NonNull final BackendMessage message) {
    for (BackendErrorListener l : errorListeners) {
      l.onError(message);
    }
  }

  protected void notifyException(@NonNull final Exception ex) {
    for (BackendExceptionListener l : exceptionListeners) {
      l.onException(ex);
    }
  }

  protected void notifyFinally() {
    for (BackendFinallyListener l : finallyListeners) {
      l.onFinally();
    }
  }

  public interface BackendSuccessListener<T> {
    void onSuccess(@NonNull final T value);
  }

  public interface BackendErrorListener {
    void onError(@NonNull final BackendMessage message);
  }

  public interface BackendExceptionListener {
    void onException(@NonNull final Exception exception);
  }

  public interface BackendFinallyListener {
    void onFinally();
  }

  private class ResultCallback implements FutureCallback<Response<T>> {

    @Override
    public void onCompleted(@Nullable final Exception exception, final Response<T> result) {

      try {

        if (exception != null) {
          Log.e(Tag, "Backend exception: " + exception.getMessage(), exception);
          notifyException(exception);
        } else if (result.getException() != null) {
          Log.e(Tag, "Result exception: " + result.getException().getMessage(), result.getException());
          notifyException(result.getException());
        } else if (result.getHeaders() != null && result.getHeaders().code() == HttpURLConnection
            .HTTP_OK) {

          final Gson gson = Json.pimpedGson();

          if (type != null) {
            value = gson.fromJson(result.getResult(), type);
          } else {
            value = gson.fromJson(result.getResult(), clazz);
          }

          errorMessage = null;

          notifySuccess(value);
        } else {

          value = null;
          errorMessage = Json.pimpedGson().fromJson(result.getResult(), BackendMessage.class);

          if (errorMessage == null || errorMessage.isIncomplete()) {
            errorMessage = new BackendMessage(
                HttpURLConnection.HTTP_BAD_REQUEST, "Unknown Error", "Failed to handle server " +
                "response.");
          }

          Log.e(Tag, "Backend errorMessage: " + errorMessage);
          notifyError(errorMessage);
        }

      } finally {
        notifyFinally();
      }
    }
  }

  @NonNull
  public static <T extends JsonElement, U> BackendResponse<T, U> fromCallable(
      @NonNull final Callable<U> callable) {

    return new BackendResponse<T, U>() {

      FutureTask<U> wrappedFuture;

      @Override
      public void cancel() {
        if (wrappedFuture != null) {
          wrappedFuture.cancel(true);
        }
      }

      @Override
      protected void before() {
        wrappedFuture = new FutureTask<U>(callable) {

          @Override
          protected void done() {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                if (isCancelled()) {
                  notifyException(new CancellationException("Task was cancelled."));
                } else {

                  try {

                    value = get(2, TimeUnit.SECONDS);

                    if (value != null) {
                      notifySuccess(value);
                    } else {
                      notifyException(new NullPointerException("Value was null"));
                    }
                  } catch (Exception e) {
                    Log.e(Tag, "Problems getting future value.", e);
                    notifyException(e);
                  }
                }

                notifyFinally();
              }
            });
          }
        };

        Threads.background(wrappedFuture);
      }
    };
  }

  @NonNull
  public static <T extends JsonElement, U> BackendResponse<T, U> pure(@NonNull final U pureValue) {
    return new BackendResponse<T, U>() {

      @Override
      public void cancel() {

      }

      @NonNull
      public BackendResponse<T, U> onSuccess(@NonNull final BackendSuccessListener<U> listener) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            listener.onSuccess(pureValue);
          }
        });
        return this;
      }

      @NonNull
      public BackendResponse<T, U> onError(@NonNull final BackendErrorListener listener) {
        return this;
      }

      @NonNull
      public BackendResponse<T, U> onException(@NonNull final BackendExceptionListener listener) {
        return this;
      }

      @NonNull
      public BackendResponse<T, U> onFinally(@NonNull final BackendFinallyListener listener) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            listener.onFinally();
          }
        });
        return this;
      }
    };
  }

}
