package org.cranst0n.dogleg.android.backend;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.ion.Response;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class BackendResponse<T extends JsonElement, U> {

  public enum ResponseType {Pending, Exception, Successful, Error}

  private final Future<Response<T>> ionCall;
  private final Class<U> clazz;

  private final String Tag = getClass().getSimpleName();

  private Exception exception;
  private U value;
  private BackendMessage errorMessage;

  private final List<BackendSuccessListener<U>> successListeners = new ArrayList<>();
  private final List<BackendErrorListener> errorListeners = new ArrayList<>();
  private final List<BackendExceptionListener> exceptionListeners = new ArrayList<>();
  private final List<BackendFinallyListener> finallyListeners = new ArrayList<>();

  private BackendResponse() {
    ionCall = null;
    clazz = null;
  }

  public BackendResponse(final Future<Response<T>> ionCall, final Class<U> clazz) {

    this.ionCall = ionCall;
    this.clazz = clazz;

    this.ionCall.setCallback(new ResultCallback());
  }

  public ResponseType type() {
    if (exception == null && errorMessage == null && value == null) {
      return ResponseType.Pending;
    } else if (exception != null) {
      return ResponseType.Exception;
    } else if (errorMessage != null) {
      return ResponseType.Error;
    } else {
      return ResponseType.Successful;
    }
  }

  public void cancel() {
    if(ionCall instanceof SimpleFuture) {
      ((SimpleFuture)ionCall).cancelSilently();
    } else {
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

  public static interface BackendSuccessListener<T> {
    void onSuccess(final T value);
  }

  public static interface BackendErrorListener {
    void onError(final BackendMessage message);
  }

  public static interface BackendExceptionListener {
    void onException(final Exception exception);
  }

  public static interface BackendFinallyListener {
    void onFinally();
  }

  private class ResultCallback implements FutureCallback<Response<T>> {

    @Override
    public void onCompleted(Exception ex, Response<T> result) {
      exception = ex;

      if (exception != null) {
        for (BackendExceptionListener l : exceptionListeners) {
          l.onException(exception);
        }

        Log.e(Tag, "Backend exception: " + exception.getMessage(), exception);

      } else if (result.getHeaders() != null && result.getHeaders().code() == HttpStatus.SC_OK) {
        value = new GsonBuilder().create().fromJson(result.getResult(), clazz);
        errorMessage = null;

        for (BackendSuccessListener l : successListeners) {
          l.onSuccess(value);
        }
      } else {
        value = null;
        errorMessage =
            new GsonBuilder().create().fromJson(result.getResult(), BackendMessage.class);

        if(errorMessage == null || errorMessage.isIncomplete()) {
          errorMessage = new BackendMessage(
              HttpStatus.SC_BAD_REQUEST, "Unknown Error", "Failed to handle server response.");
        }

        Log.e(Tag, "Backend errorMessage: " + errorMessage);

        for (BackendErrorListener l : errorListeners) {
          l.onError(errorMessage);
        }
      }

      for (BackendFinallyListener l : finallyListeners) {
        l.onFinally();
      }
    }
  }

  public static <T extends JsonElement,U> BackendResponse<T,U> pure(final U value) {
    return new BackendResponse<T, U>() {

      @Override
      public ResponseType type() {
        return ResponseType.Successful;
      }

      @Override
      public void cancel() {

      }

      public BackendResponse<T, U> onSuccess(final BackendSuccessListener<U> listener) {
        listener.onSuccess(value);
        return this;
      }

      public BackendResponse<T, U> onError(final BackendErrorListener listener) {
        return this;
      }

      public BackendResponse<T, U> onException(final BackendExceptionListener listener) {
        return this;
      }

      public BackendResponse<T, U> onFinally(final BackendFinallyListener listener) {
        listener.onFinally();
        return this;
      }
    };
  }
}
