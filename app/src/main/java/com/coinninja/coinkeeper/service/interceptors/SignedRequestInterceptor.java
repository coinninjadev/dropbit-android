package com.coinninja.coinkeeper.service.interceptors;

import android.annotation.SuppressLint;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.di.interfaces.UUID;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.util.DateUtil;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class SignedRequestInterceptor implements Interceptor {

    static final String CN_AUTH_SIG = "CN-Auth-Signature";
    static final String CN_AUTH_TIMESTAMP = "CN-Auth-Timestamp";
    static final String CN_AUTH_WALLET_ID = "CN-Auth-Wallet-ID";
    static final String CN_AUTH_USER_ID = "CN-Auth-User-ID";
    static final String CN_AUTH_DEVICE_UUID = "CN-Auth-Device-UUID";

    private final DateUtil dateUtil;
    private final DataSigner signer;
    private final String uuid;
    private final CNWalletManager cnWalletManager;

    @Inject
    public SignedRequestInterceptor(DateUtil dateUtil, DataSigner signer, @UUID String uuid, CNWalletManager cnWalletManager) {
        this.dateUtil = dateUtil;
        this.signer = signer;
        this.uuid = uuid;
        this.cnWalletManager = cnWalletManager;
    }

    public static byte[] bodyToString(RequestBody request) {
        try {
            RequestBody copy = request;
            Buffer buffer = new Buffer();
            if (copy != null) {
                copy.writeTo(buffer);
            } else {
                return "".getBytes();
            }

            byte[] bytes = new byte[(int) buffer.size()];
            buffer.read(bytes);
            return bytes;
        } catch (IOException e) {
            return "did not work".getBytes();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(signRequest(chain.request()));
    }

    Request signRequest(Request origRequest) {
        String currentTimeFormatted = dateUtil.getCurrentTimeFormatted();
        Request.Builder builder = origRequest.newBuilder();

        builder.header(CN_AUTH_TIMESTAMP, currentTimeFormatted);
        builder.header(CN_AUTH_DEVICE_UUID, uuid);

        String body = new String(bodyToString(origRequest.body()));
        if (body.isEmpty()) {
            builder.header(CN_AUTH_SIG, signer.sign(currentTimeFormatted));
        } else {
            builder.header(CN_AUTH_SIG, signer.sign(body));
        }

        addAccountToHeaders(builder);

        return builder.build();
    }

    private void addAccountToHeaders(Request.Builder builder) {
        Account account = cnWalletManager.getAccount();
        if (null != account.getCnWalletId()) {
            builder.header(CN_AUTH_WALLET_ID, account.getCnWalletId());
        }

        if (null != account.getCnUserId() && !account.getCnUserId().isEmpty()) {
            builder.header(CN_AUTH_USER_ID, account.getCnUserId());
        }
    }
}
