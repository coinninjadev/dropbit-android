package com.coinninja.coinkeeper.view.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;

public class InternalNotificationView {

    private final ViewGroup baseLayout;
    private DismissListener dismissListener;
    private View notificationView;

    public InternalNotificationView(ViewGroup baseLayout) {
        this.baseLayout = baseLayout;

    }

    public View show(InternalNotification notification) {
        LayoutInflater inflater = LayoutInflater.from(baseLayout.getContext());
        View root = inflater.inflate(R.layout.internal_notifications, null, false);

        MessageLevel messageLevel = notification.getMessageLevel();
        int rID = getColorRid(messageLevel != null ? messageLevel : MessageLevel.INFO);
        root.setBackgroundColor(baseLayout.getResources().getColor(rID));
        root.setTag(rID);

        root.findViewById(R.id.exit_button)
                .setOnClickListener(view -> onExitBtnClicked(root, notification));

        TextView messageView = root.findViewById(R.id.internal_message);
        messageView.setOnClickListener(view -> onMessageBodyClicked(root, notification));
        messageView.setText(notification.getMessage());

        baseLayout.addView(root);
        notificationView = root;

        //Load animation
        Animation slide_down = AnimationUtils.loadAnimation(baseLayout.getContext(),
                R.anim.slide_down);
        root.startAnimation(slide_down);

        return root;
    }

    public void unNaturallyDismiss() {
        baseLayout.removeView(notificationView);
    }

    public void onMessageBodyClicked(View notificationView, InternalNotification notification) {
        Uri clickAction = notification.getClickAction();
        if (clickAction != null && !clickAction.toString().isEmpty()) {
            openURL(clickAction);
        }

        onExitBtnClicked(notificationView, notification);
    }

    private void openURL(Uri uri) {
        Context application = baseLayout.getContext().getApplicationContext();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        application.startActivity(intent);
    }

    public void onExitBtnClicked(View notificationView, InternalNotification notification) {
        removeView(notificationView);
        dismissListener.onDismiss(notification);
    }

    public void removeView(View notificationView) {
        Animation slide_up = AnimationUtils.loadAnimation(baseLayout.getContext(),
                R.anim.slide_up);
        notificationView.startAnimation(slide_up);
        baseLayout.removeView(notificationView);
    }

    protected int getColorRid(MessageLevel messageLevel) {
        if (messageLevel == null) messageLevel = MessageLevel.INFO;

        switch (messageLevel) {
            case INFO:
                return R.color.info_background;
            case SUCCESS:
                return R.color.success_background;
            case WARN:
                return R.color.warn_background;
            case ERROR:
                return R.color.error_background;
            default:
                return R.color.info_background;
        }

    }

    public View getCurrentNotificationView() {
        return notificationView;
    }

    public void setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(InternalNotification notificationDbObject);
    }
}
