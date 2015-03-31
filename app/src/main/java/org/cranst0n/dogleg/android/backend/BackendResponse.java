package org.cranst0n.dogleg.android.backend;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.ion.Response;

import org.apache.http.HttpStatus;
import org.cranst0n.dogleg.android.utils.Threads;

import java.lang.reflect.Type;
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

  protected Exception exception;
  protected U value;
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

  public BackendResponse(final Future<Response<T>> ionCall, final Type type) {

    this.ionCall = ionCall;
    this.type = type;
    this.clazz = null;

    this.ionCall.setCallback(new ResultCallback());

    before();
  }

  public BackendResponse(final Future<Response<T>> ionCall, final Class<U> clazz) {

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

  public BackendResponse<T, U> onSuccess(final BackendSuccessListener<U> listener) {
    successListeners.add(listener);
    return this;
  }

  public BackendResponse<T, U> onError(final BackendErrorListener listener) {
    errorListeners.add(listener);
    return this;
  }

  public BackendResponse<T, U> onException(final BackendExceptionListener listener) {
    exceptionListeners.add(listener);
    return this;
  }

  public BackendResponse<T, U> onFinally(final BackendFinallyListener listener) {
    finallyListeners.add(listener);
    return this;
  }

  protected void notifySuccess(final U value) {
    for (BackendSuccessListener l : successListeners) {
      l.onSuccess(value);
    }
  }

  protected void notifyError(final BackendMessage message) {
    for (BackendErrorListener l : errorListeners) {
      l.onError(message);
    }
  }

  protected void notifyException(final Exception ex) {
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
    void onSuccess(final T value);
  }

  public interface BackendErrorListener {
    void onError(final BackendMessage message);
  }

  public interface BackendExceptionListener {
    void onException(final Exception exception);
  }

  public interface BackendFinallyListener {
    void onFinally();
  }

  private class ResultCallback implements FutureCallback<Response<T>> {

    @Override
    public void onCompleted(final Exception exception, final Response<T> result) {

      if (exception != null) {
        Log.e(Tag, "Backend exception: " + exception.getMessage(), exception);
        notifyException(exception);
      } else if (result.getException() != null) {
        Log.e(Tag, "Result exception: " + result.getException().getMessage(), result.getException());
        notifyException(result.getException());
      } else if (result.getHeaders() != null && result.getHeaders().code() == HttpStatus.SC_OK) {

        if (type != null) {
          value = new GsonBuilder().create().fromJson(result.getResult(), type);
        } else {
          value = new GsonBuilder().create().fromJson(result.getResult(), clazz);
        }

        errorMessage = null;

        notifySuccess(value);
      } else {

        value = null;
        errorMessage =
            new GsonBuilder().create().fromJson(result.getResult(), BackendMessage.class);

        if (errorMessage == null || errorMessage.isIncomplete()) {
          errorMessage = new BackendMessage(
              HttpStatus.SC_BAD_REQUEST, "Unknown Error", "Failed to handle server response.");
        }

        Log.e(Tag, "Backend errorMessage: " + errorMessage);
        notifyError(errorMessage);
      }

      notifyFinally();
    }
  }

  public static <T extends JsonElement, U> BackendResponse<T, U> fromCallable(final Callable<U> callable) {

    BackendResponse response = new BackendResponse<T, U>() {

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

    return response;
  }

  public static <T extends JsonElement, U> BackendResponse<T, U> pure(final U pureValue) {
    return new BackendResponse<T, U>() {

      @Override
      public void cancel() {

      }

      public BackendResponse<T, U> onSuccess(final BackendSuccessListener<U> listener) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            listener.onSuccess(pureValue);
          }
        });
        return this;
      }

      public BackendResponse<T, U> onError(final BackendErrorListener listener) {
        return this;
      }

      public BackendResponse<T, U> onException(final BackendExceptionListener listener) {
        return this;
      }

      public BackendResponse<T, U> onFinally(final BackendFinallyListener listener) {
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
