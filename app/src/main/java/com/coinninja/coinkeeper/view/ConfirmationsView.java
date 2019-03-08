package com.coinninja.coinkeeper.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;

public class ConfirmationsView extends View {
    public static final int STAGE_DROPBIT_SENT = 0x0000;
    public static final int STAGE_ADDRESS_RECEIVED = 0x0001;
    public static final int STAGE_BROADCASTING = 0x0002;
    public static final int STAGE_PENDING = 0x0003;
    public static final int STAGE_COMPLETE = 0x0004;

    public static final int CONFIGURATION_DROPBIT = 0x0010;
    public static final int CONFIGURATION_TRANSACTION = 0x0020;

    public static final int DEFAULT_PADDING = 5;
    public static final int TEXT_PADDING = 6;
    public static final float DEFAULT_TEXT_SIZE = 18F;
    public static final float STROKE_WIDTH = 4F;

    private Paint paint;
    private TextPaint textPaint;
    private Rect rect;
    private int activeColor;
    private int inactiveColor;
    private Rect bounds = new Rect();

    private float connectorLength = 33F;
    private float beadDiameter = 20F;
    private int currentStep = 0;
    private int numStages = 3;
    private float radius;
    private float scaledTextPadding;
    private float scaledDefaultPadding;
    private Drawable lastStepDrawable;

    public ConfirmationsView(Context context) {
        this(context, null);
    }

    public ConfirmationsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ConfirmationsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ConfirmationsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        Resources res = getContext().getResources();
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.density = res.getDisplayMetrics().density;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        rect = new Rect();
        radius = calcBeadRadius();

        if (attrs == null) {
            return;
        }

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ConfirmationsWidgetStyle);
        activeColor = ta.getColor(R.styleable.ConfirmationsWidgetStyle_beadActiveColor, getContext().getResources().getColor(R.color.background_bead_active));
        inactiveColor = ta.getColor(R.styleable.ConfirmationsWidgetStyle_beadInactiveColor, getContext().getResources().getColor(R.color.background_bead_inactive));
        beadDiameter = ta.getDimension(R.styleable.ConfirmationsWidgetStyle_beadDiameter, beadDiameter);
        connectorLength = ta.getDimension(R.styleable.ConfirmationsWidgetStyle_beadConnectorLength, connectorLength);
        int lastStepDrawableResourceId = ta.getResourceId(R.styleable.ConfirmationsWidgetStyle_lastStepVector, R.drawable.ic_confirmation_check);
        setConfiguration(ta.getInt(R.styleable.ConfirmationsWidgetStyle_configuration, CONFIGURATION_TRANSACTION));
        setStage(ta.getInt(R.styleable.ConfirmationsWidgetStyle_stage, STAGE_DROPBIT_SENT));
        if (lastStepDrawableResourceId > 0) {
            lastStepDrawable = res.getDrawable(lastStepDrawableResourceId).mutate();
        }
        ta.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(activeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        textPaint.setColor(activeColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        float cx = radius + getPaddingStart() + scaledDefaultPadding;
        float cy = getHeight() / 2;

        for (int i = 1; i <= numStages; i++) {
            drawBead(canvas, cx, cy, i);
            cx += radius;

            configurePaint(i);

            if (i < numStages) {
                cx = drawConnector(canvas, cx, cy);
                cx += radius;
            }
        }
    }

    private float drawConnector(Canvas canvas, float cx, float cy) {
        float ex = cx + connectorLength;
        canvas.drawLine(cx + STROKE_WIDTH / 2, cy, ex, cy, paint);
        return ex;
    }

    private void configurePaint(int whichBead) {
        if (whichBead >= getCurrentStepWithOffset()) {
            paint.setColor(inactiveColor);
            textPaint.setColor(inactiveColor);
        }
    }

    private void drawBead(Canvas canvas, float cx, float cy, int which) {
        canvas.drawCircle(cx, cy, radius, paint);

        if (which == numStages && null != lastStepDrawable) {
            drawIcon(canvas, cx, cy, which);
        } else {
            drawLabel(canvas, cx, cy, which);
        }
    }

    private void drawIcon(Canvas canvas, float cx, float cy, int which) {
        int maxHeight = calculateIconMaxHeight(getHeight());
        int maxWidth = calculateIconMaxWidth();
        float ratio;

        if (lastStepDrawable.getIntrinsicHeight() > lastStepDrawable.getIntrinsicWidth()) {
            ratio = (float) maxHeight / (float) lastStepDrawable.getIntrinsicHeight();
        } else {
            ratio = (float) maxWidth / (float) lastStepDrawable.getIntrinsicWidth();
        }

        lastStepDrawable.setBounds(
                (int) (cx - (lastStepDrawable.getIntrinsicWidth() * ratio / 2)),
                (int) (cy - (lastStepDrawable.getIntrinsicHeight() * ratio / 2)),
                (int) (cx + (lastStepDrawable.getIntrinsicWidth() * ratio / 2)),
                (int) (cy + (lastStepDrawable.getIntrinsicHeight() * ratio / 2))
        );


        if (which == getCurrentStepWithOffset()) {
            lastStepDrawable.setTint(activeColor);
        } else {
            lastStepDrawable.setTint(inactiveColor);
        }

        lastStepDrawable.draw(canvas);
    }

    private void drawLabel(Canvas canvas, float cx, float cy, int which) {
        textPaint.getTextBounds(Integer.toString(which), 0, 1, bounds);
        float textX = cx - bounds.width() / 2 + scaledTextPadding / 2;
        if (which == 1) {
            textX = textX - STROKE_WIDTH;
        }
        canvas.drawText(Integer.toString(which),
                textX,
                cy + bounds.height() / 2,
                textPaint
        );
    }

    public float calcBeadRadius() {
        return beadDiameter / 2F;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        scaledDefaultPadding = scaleValue(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PADDING);
        scaledTextPadding = scaleValue(TypedValue.COMPLEX_UNIT_DIP, TEXT_PADDING);

        int minWidth = (int) (getPaddingEnd() + getPaddingStart() + scaledDefaultPadding * 2 +
                numStages * beadDiameter + (numStages - 1) * connectorLength);
        int width = resolveSizeAndState(minWidth, widthMeasureSpec, 1);

        int minHeight = (int) ((int) beadDiameter + getPaddingBottom() + getPaddingTop() + scaledDefaultPadding * 2);
        int height = resolveSizeAndState(minHeight, heightMeasureSpec, 1);

        setMeasuredDimension(width, height);
        radius = calcBeadRadius();
        configureTextPaint(height);
    }

    private float scaleValue(int unit, float value) {
        Resources resources;
        if (getContext() == null) {
            resources = Resources.getSystem();
        } else {
            resources = getContext().getResources();
        }
        return TypedValue.applyDimension(unit, value, resources.getDisplayMetrics());
    }

    private void configureTextPaint(int height) {
        int textMaxHeight = calculateLabelMaxHeight(height);
        int textMaxWidth = calculateLabelMaxWidth();
        float textSizeScaled = calculateTextSizeScaled(scaleTextSize(DEFAULT_TEXT_SIZE), textMaxWidth, textMaxHeight);
        textPaint.setTextSize(textSizeScaled);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private float scaleTextSize(float textSize) {
        return scaleValue(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    private float calculateTextSizeScaled(float scaledTextSize, int textMaxWidth, int textMaxHeight) {
        Rect bounds = new Rect();
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(scaledTextSize);
        paint.getTextBounds("m", 0, 1, bounds);
        if (bounds.height() > textMaxHeight || bounds.width() > textMaxWidth) {
            scaledTextSize = calculateTextSizeScaled(scaledTextSize - 1F, textMaxWidth, textMaxHeight);
        }
        return scaledTextSize;
    }

    private int calculateLabelMaxWidth() {
        return (int) ((int) (beadDiameter - (STROKE_WIDTH * 2)) - (scaledTextPadding * 2));
    }

    private int calculateIconMaxWidth() {
        return (int) (beadDiameter - (scaledTextPadding * 2));
    }

    private int calculateLabelMaxHeight(int height) {
        return (int) ((int) (height - getPaddingBottom() - getPaddingTop() - (scaledDefaultPadding * 2) -
                (STROKE_WIDTH * 2)) - (scaledTextPadding * 2));
    }

    private int calculateIconMaxHeight(int height) {
        return (int) ((int) (height - getPaddingBottom() - getPaddingTop() - (scaledDefaultPadding * 2) -
                (STROKE_WIDTH * 2)) - (scaledTextPadding * 4));
    }

    public void setDiameter(float diameter) {
        beadDiameter = diameter;
        invalidate();
    }

    public int getNumStages() {
        return numStages;
    }

    public int calcStageOffset() {
        return getNumStages() == 5 ? 0 : -2;
    }

    public int getCurrentStepWithOffset() {
        return getCurrentStep() + calcStageOffset();
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getConfiguration() {
        switch (numStages) {
            case 5:
                return CONFIGURATION_DROPBIT;
            case 3:
                return CONFIGURATION_TRANSACTION;
        }
        return -1;
    }

    public void setConfiguration(int configuration) {
        switch (configuration) {
            case CONFIGURATION_DROPBIT:
                numStages = 5;
                break;
            case CONFIGURATION_TRANSACTION:
            default:
                numStages = 3;
        }

        invalidate();
    }

    public int getStage() {
        switch (getCurrentStep()) {
            case 1:
                return STAGE_DROPBIT_SENT;
            case 2:
                return STAGE_ADDRESS_RECEIVED;
            case 3:
                return STAGE_BROADCASTING;
            case 4:
                return STAGE_PENDING;
            case 5:
                return STAGE_COMPLETE;
            default:
                return STAGE_DROPBIT_SENT;
        }
    }

    public void setStage(int stage) {
        switch (stage) {
            case STAGE_DROPBIT_SENT:
                currentStep = 1;
                break;
            case STAGE_ADDRESS_RECEIVED:
                currentStep = 2;
                break;
            case STAGE_BROADCASTING:
                currentStep = 3;
                break;
            case STAGE_PENDING:
                currentStep = 4;
                break;
            case STAGE_COMPLETE:
                currentStep = 5;
                break;
            default:
                currentStep = 1;
        }

        invalidate();
    }
}
