package com.coinninja.coinkeeper.presenter.activity;

import javax.inject.Inject;

public class RecoveryWordsPresenter {
    static final int NUM_WORDS = 12;
    static final int MAX_PAGER_POSITION = NUM_WORDS - 1;

    public enum ButtonState {
        START, DEFAULT, END
    }

    View view;

    @Inject
    public RecoveryWordsPresenter() {
    }

    public void onPageChange(int position) {
        ButtonState buttonState = calculateButtonState(position);

        StringBuilder positionCountTXT = new StringBuilder();
        positionCountTXT.append("word ");
        positionCountTXT.append(Integer.toString(position + 1));
        positionCountTXT.append(" of ");
        positionCountTXT.append(Integer.toString(NUM_WORDS));

        view.setPageCounterText(positionCountTXT.toString());
        view.setPagePosition(position);

        switch (buttonState) {
            case START:
                view.hideFirst();
                view.showNext();
                break;
            case DEFAULT:
                view.showFirst();
                view.showNext();
                break;
            case END:
                view.showLast();
                break;
        }
    }

    public void onNextClicked() {
        int currentPosition = view.getPagePosition();
        ButtonState buttonState = calculateButtonState(currentPosition);

        if (buttonState == ButtonState.END) {
            view.showNextActivity();
        } else {
            view.scrollToPage(view.getPagePosition() + 1);
        }
    }

    public void onBackClicked() {
        view.scrollToPage(view.getPagePosition() - 1);
    }


    public void attach(View view) {
        this.view = view;
    }


    private ButtonState calculateButtonState(int position) {
        if (position == 0) {
            return ButtonState.START;
        }
        return position == MAX_PAGER_POSITION ? ButtonState.END : ButtonState.DEFAULT;
    }

    public interface View {
        int getPagePosition();

        void setPagePosition(int pagePosition);

        void scrollToPage(int pagePosition);

        void setPageCounterText(String msg);

        void showNextActivity();

        void hideFirst();

        void showNext();

        void showFirst();

        void showLast();
    }
}
