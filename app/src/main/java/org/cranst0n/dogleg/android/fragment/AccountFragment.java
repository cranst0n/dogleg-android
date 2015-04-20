package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Users;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Dialogs;
import org.cranst0n.dogleg.android.utils.Intents;
import org.cranst0n.dogleg.android.utils.SnackBars;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class AccountFragment extends BaseFragment {

  private Bus bus;
  private Users users;
  private User currentUser;

  private ImageView avatarView;

  private EditText oldPasswordField;
  private EditText newPasswordField;
  private EditText newPasswordConfirmField;
  private Button changePasswordButton;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    users = new Users(context);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    View accountView = inflater.inflate(R.layout.fragment_account, container, false);

    avatarView = (ImageView) accountView.findViewById(R.id.user_avatar);
    Button saveAvatarButton = (Button) accountView.findViewById(R.id.change_avatar_button);

    avatarView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        startActivityForResult(Intents.pickImage(), Intents.PICK_IMAGE);
      }
    });

    saveAvatarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {

        final MaterialDialog busyDialog = Dialogs.showBusyDialog(activity, "Changing Avatar...");

        Bitmap bitmap = ((BitmapDrawable) avatarView.getDrawable()).getBitmap();

        users.changeAvatar(currentUser, bitmap)
            .onSuccess(new BackendResponse.BackendSuccessListener<User>() {
              @Override
              public void onSuccess(@NonNull final User value) {
                SnackBars.showSimple(activity, "Updated avatar.");
                bus.post(value);
              }
            })
            .onError(SnackBars.showBackendError(activity))
            .onException(SnackBars.showBackendException(activity))
            .onFinally(new BackendResponse.BackendFinallyListener() {
              @Override
              public void onFinally() {
                busyDialog.dismiss();
              }
            });
      }
    });

    oldPasswordField = (EditText) accountView.findViewById(R.id.old_password_field);
    newPasswordField = (EditText) accountView.findViewById(R.id.new_password_field);
    newPasswordConfirmField = (EditText) accountView.findViewById(R.id.new_password_confirm_field);
    changePasswordButton = (Button) accountView.findViewById(R.id.change_password_button);

    changePasswordButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {

        final MaterialDialog busyDialog = Dialogs.showBusyDialog(activity, "Changing Password...");

        users.changePassword(currentUser, oldPasswordField.getText().toString(), newPasswordField
            .getText().toString(), newPasswordConfirmField.getText().toString())
            .onSuccess(new BackendResponse.BackendSuccessListener<User>() {
              @Override
              public void onSuccess(@NonNull final User value) {
                SnackBars.showSimple(activity, "Password changed.");
                oldPasswordField.setText("");
                newPasswordField.setText("");
                newPasswordConfirmField.setText("");
              }
            })
            .onError(SnackBars.showBackendError(activity, "Change Failed:"))
            .onException(SnackBars.showBackendException(activity, "Change Failed:"))
            .onFinally(new BackendResponse.BackendFinallyListener() {
              @Override
              public void onFinally() {
                busyDialog.dismiss();
              }
            });
      }
    });

    loadUserAvatar();

    return accountView;
  }

  @Subscribe
  public void newUser(final User user) {
    currentUser = user;

    loadUserAvatar();

    if (changePasswordButton != null) {
      changePasswordButton.setEnabled(currentUser.isValid());
    }
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

    switch (requestCode) {
      case Intents.PICK_IMAGE: {
        if (resultCode == Activity.RESULT_OK) {

          try {

            Uri imageUri = data.getData();

            InputStream imageStream = activity.getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            avatarView.setImageBitmap(selectedImage);

          } catch (FileNotFoundException e) {
            SnackBars.showSimple(activity, "File not found: " + e.getMessage());
          }
        }

        break;
      }
      default: {
        super.onActivityResult(requestCode, resultCode, data);
      }
    }
  }

  private void loadUserAvatar() {
    if (avatarView != null && currentUser.isValid()) {
      Ion.with(this)
          .load(users.avatarUrl(currentUser))
          .noCache()
          .asBitmap()
          .setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(final Exception e, final Bitmap result) {
              if (e == null) {
                avatarView.setImageBitmap(result);
              }
            }
          });
    }
  }
}
