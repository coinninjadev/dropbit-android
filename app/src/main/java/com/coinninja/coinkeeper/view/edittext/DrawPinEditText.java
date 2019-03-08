package com.coinninja.coinkeeper.view.edittext;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;

import java.util.Arrays;

public class DrawPinEditText {

    private float charSize;
    private float space = 18;
    private float lineSpacing = 10;

    private float lineStroke = 1;
    private float lineStrokeSelected = 2;

    private Paint linesPaint;

    final ColorStateList colorStates;

    public DrawPinEditText(int[][] states, int[] colors) {
        colorStates = new ColorStateList(states, colors);
    }

    public void initLines(float currentDensity, Paint paint) {
        lineStroke = currentDensity * lineStroke;
        lineStrokeSelected = currentDensity * lineStrokeSelected;
        linesPaint = new Paint(paint);
        linesPaint.setStrokeWidth(lineStroke);
    }

    public void initSpacing(float currentDensity) {
        space = currentDensity * space; //convert to pixels for our density
        lineSpacing = currentDensity * lineSpacing; //convert to pixels for our density
    }

    public void draw(Canvas canvas, PinEditText pinEditText, Editable inputText) {
        int currentWidth = pinEditText.getWidth();
        int currentHeight = pinEditText.getHeight();

        int paddingRight = pinEditText.getPaddingRight();
        int paddingLeft = pinEditText.getPaddingLeft();
        int paddingBottom = pinEditText.getPaddingBottom();

        int charLength = 6;
        int availableWidth = currentWidth - paddingRight - paddingLeft;

        if (space < 0) {
            charSize = (availableWidth / (charLength * 2 - 1));
        } else {
            charSize = (availableWidth - (space * (charLength - 1))) / charLength;
        }

        int startX = paddingLeft;
        int bottom = currentHeight - paddingBottom;

        int textLength = inputText.length();
        char[] charObfu = new char[textLength];
        Arrays.fill(charObfu, '*');

        String textObfuscate = String.copyValueOf(charObfu);
        //Text Width
        float[] textWidths = new float[textLength];
        pinEditText.getPaint().getTextWidths(textObfuscate, 0, textLength, textWidths);

        for (int i = 0; i < charLength; i++) {
            updateColorForLines(pinEditText, i == textLength);
            canvas.drawLine(startX, bottom, startX + charSize, bottom, linesPaint);

            if (textObfuscate.length() > i) {
                float middle = startX + charSize / 2;
                canvas.drawText(textObfuscate, i, i + 1, middle - textWidths[0] / 2, bottom - lineSpacing, pinEditText.getPaint());
            }

            if (space < 0) {
                startX += charSize * 2;
            } else {
                startX += charSize + space;
            }
        }
    }

    private void updateColorForLines(PinEditText pinEditText, boolean next) {
        if (pinEditText.isFocused()) {
            linesPaint.setStrokeWidth(lineStrokeSelected);
            linesPaint.setColor(getColorForState(android.R.attr.state_focused));
            if (next) {
                linesPaint.setColor(getColorForState(android.R.attr.state_selected));
            }
        } else {
            linesPaint.setStrokeWidth(lineStroke);
            linesPaint.setColor(getColorForState(-android.R.attr.state_focused));
        }
    }

    private int getColorForState(int... states) {
        return colorStates.getColorForState(states, Color.GRAY);
    }
}
