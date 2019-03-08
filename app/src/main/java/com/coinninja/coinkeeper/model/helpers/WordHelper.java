package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.Word;

import javax.inject.Inject;

public class WordHelper {

    private DaoSessionManager daoSessionManager;

    @Inject
    public WordHelper(DaoSessionManager daoSessionManager) {
        this.daoSessionManager = daoSessionManager;
    }

    public Word saveWord(long walletId, String recoveryWord, int sortOrder) {
        Word word = new Word();
        word.setWalletId(walletId);
        word.setWord(recoveryWord);
        word.setSortOrder(sortOrder);
        daoSessionManager.getWordDao().insert(word);
        return word;
    }
}
