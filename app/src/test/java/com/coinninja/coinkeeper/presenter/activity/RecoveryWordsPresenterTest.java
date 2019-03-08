package com.coinninja.coinkeeper.presenter.activity;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecoveryWordsPresenterTest {
    private RecoveryWordsPresenter recoveryWordsPresenter;
    private RecoveryWordsPresenter.View view;

    @Before
    public void setUp() throws Exception {
        view = mock(RecoveryWordsPresenter.View.class);
        recoveryWordsPresenter = new RecoveryWordsPresenter();
        recoveryWordsPresenter.attach(view);
    }

    @Test
    public void onPageChange_StartPage() {
        int currentPagePosition = 0;
        int desiredPosition = 0;
        when(view.getPagePosition()).thenReturn(currentPagePosition);

        recoveryWordsPresenter.onPageChange(desiredPosition);

        verify(view).setPagePosition(eq(desiredPosition));
        verify(view).showNext();
        verify(view).hideFirst();
    }

    @Test
    public void onPageChange_SomewhereInTheMiddle() {
        int currentPagePosition = 0;
        int desiredPosition = RecoveryWordsPresenter.NUM_WORDS - 4;
        when(view.getPagePosition()).thenReturn(currentPagePosition);

        recoveryWordsPresenter.onPageChange(desiredPosition);

        verify(view).setPagePosition(eq(desiredPosition));
        verify(view).showNext();
        verify(view).showFirst();
    }

    @Test
    public void onPageChange_End() {
        int currentPagePosition = 0;
        int desiredPosition = RecoveryWordsPresenter.NUM_WORDS - 1;
        when(view.getPagePosition()).thenReturn(currentPagePosition);

        recoveryWordsPresenter.onPageChange(desiredPosition);

        verify(view).setPagePosition(eq(desiredPosition));
        verify(view).showLast();
    }

    @Test
    public void onNextClicked_StartPage() {
        int currentPagePosition = 0;
        when(view.getPagePosition()).thenReturn(currentPagePosition);

        recoveryWordsPresenter.onNextClicked();

        verify(view).scrollToPage(eq(currentPagePosition + 1));
        verify(view, never()).showNextActivity();
    }

    @Test
    public void onNextClicked_SomewhereInTheMiddle() {
        int currentPagePosition = RecoveryWordsPresenter.NUM_WORDS - 4;
        when(view.getPagePosition()).thenReturn(currentPagePosition);

        recoveryWordsPresenter.onNextClicked();

        verify(view).scrollToPage(eq(currentPagePosition + 1));
        verify(view, never()).showNextActivity();
    }

    @Test
    public void onNextClicked_End() {
        int currentPagePosition = RecoveryWordsPresenter.NUM_WORDS - 1;
        when(view.getPagePosition()).thenReturn(currentPagePosition);

        recoveryWordsPresenter.onNextClicked();

        verify(view, never()).scrollToPage(eq(currentPagePosition + 1));
        verify(view).showNextActivity();
    }

    @Test
    public void onBackClicked() {
        int currentPagePosition = RecoveryWordsPresenter.NUM_WORDS - 4;
        when(view.getPagePosition()).thenReturn(currentPagePosition);

        recoveryWordsPresenter.onBackClicked();

        verify(view).scrollToPage(eq(currentPagePosition - 1));
    }

    @Test
    public void attach() {
        assertEquals(view, recoveryWordsPresenter.view);
    }

}