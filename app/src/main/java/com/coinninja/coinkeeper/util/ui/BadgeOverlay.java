package com.coinninja.coinkeeper.util.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;

import com.coinninja.coinkeeper.R;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class BadgeOverlay extends BitmapDrawable {
    private Resources resources;
    private Bitmap baseBitmap;
    Paint paint;

    public static Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static BadgeOverlay newInstance(Resources resource, Drawable baseDrawable) {
        Bitmap bitmap;

        if (baseDrawable instanceof VectorDrawableCompat || baseDrawable instanceof VectorDrawable)
            bitmap = getBitmapFromVectorDrawable(baseDrawable);
        else
            bitmap = ((BitmapDrawable) baseDrawable).getBitmap();

        return new BadgeOverlay(resource, bitmap);
    }

    BadgeOverlay(Resources resources, Bitmap baseBitmap) {
        super(resources, baseBitmap);
        this.resources = resources;
        this.baseBitmap = baseBitmap;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public Resources getResources() {
        return resources;
    }

    public Bitmap getBaseBitmap() {
        return baseBitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        paint.setColor(resources.getColor(R.color.color_error));
        float minSize = Math.min(getIntrinsicWidth(), getIntrinsicHeight());
        float radius = resources.getDimension(R.dimen.badge_radius);
        float x = getIntrinsicWidth() - radius;
        float y = minSize / 4.0F;
        canvas.drawCircle(x, y, radius, paint);
    }
}
