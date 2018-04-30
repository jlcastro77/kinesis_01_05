package com.lmax.api;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.lmax.api.account.AccountStateEvent;
import com.lmax.api.account.AccountStateEventListener;
import com.lmax.api.account.AccountStateRequest;
import com.lmax.api.account.AccountSubscriptionRequest;
import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.account.LoginRequest.ProductType;

public class SimpleAccountEventsClient implements LoginCallback, AccountStateEventListener
{
    @Override
    public void onLoginSuccess(final Session session)
    {
        System.out.println("My accountId is: " + session.getAccountDetails().getAccountId());

        session.registerAccountStateEventListener(this);

        session.subscribe(new AccountSubscriptionRequest(), new Callback()
        {
            @Override
            public void onSuccess()
            {
            }

            @Override
            public void onFailure(final FailureResponse failureResponse)
            {
                throw new RuntimeException("Failed");
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    while (true)
                    {
                        Thread.sleep(5000);
                        session.requestAccountState(new AccountStateRequest(), new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {
                            }

                            @Override
                            public void onFailure(final FailureResponse failureResponse)
                            {
                                System.err.println("Failed to request account state: " + failureResponse);
                            }
                        });
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }, 5L, 5L, TimeUnit.SECONDS);

        session.start();
    }

    @Override
    public void notify(final AccountStateEvent accountStateEvent)
    {
        System.out.println("Unrealsied PnL: " + accountStateEvent.getUnrealisedProfitAndLoss());
    }

    @Override
    public void onLoginFailure(FailureResponse failureResponse)
    {
        System.out.println("Login Failed: " + failureResponse);
    }

    public static void main(String[] args)
    {
        if (args.length != 4)
        {
            System.out.println("Usage " + SimpleAccountEventsClient.class.getName() + " <url> <username> <password> [CFD_DEMO|CFD_LIVE]");
            System.exit(-1);
        }

        String url = args[0];
        String username = args[1];
        String password = args[2];
        ProductType productType = ProductType.valueOf(args[3].toUpperCase());

        LmaxApi lmaxApi = new LmaxApi(url);
        SimpleAccountEventsClient accountEventsClient = new SimpleAccountEventsClient();

        lmaxApi.login(new LoginRequest(username, password, productType), accountEventsClient);
    }
}
