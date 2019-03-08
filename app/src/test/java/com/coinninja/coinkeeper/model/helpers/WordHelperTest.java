package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.Word;
import com.coinninja.coinkeeper.model.db.WordDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WordHelperTest {


    @Mock
    DaoSessionManager sessionManager;

    @Mock
    WordDao wordDao;

    @Before
    public void setUp() {
        when(sessionManager.getWordDao()).thenReturn(wordDao);
    }


    @Test
    public void saveWordWillStoreWordInWalletWithSortOrder() {
        WordHelper wordHelper = new WordHelper(sessionManager);

        Word savedWord = wordHelper.saveWord(1L, "word", 0);

        Word word = new Word();
        word.setWalletId(1L);
        word.setWord("word");
        word.setSortOrder(0);

        verify(wordDao, times(1)).insert(any());
        assertThat(savedWord, equalTo(word));
    }
}