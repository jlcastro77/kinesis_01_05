package com.lmax.api;

import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.account.LoginRequest.ProductType;
import com.lmax.api.order.Execution;
import com.lmax.api.order.ExecutionEventListener;
import com.lmax.api.order.Order;
import com.lmax.api.order.OrderEventListener;
import com.lmax.api.order.OrderSubscriptionRequest;
import com.lmax.api.position.PositionEvent;
import com.lmax.api.position.PositionEventListener;
import com.lmax.api.position.PositionSubscriptionRequest;

public class PositionTrackingClient implements LoginCallback, PositionEventListener, OrderEventListener, ExecutionEventListener
{
    private Session session;

    public PositionTrackingClient()
    {
    }
    
    @Override
    public void notify(PositionEvent positionEvent)
    {
        System.out.printf("Customer: %d, net position is: %s, for Instrument: %d%n", positionEvent.getAccountId(), positionEvent.getOpenQuantity(), positionEvent.getInstrumentId());
    }
    
    @Override
    public void notify(Execution execution)
    {
        System.out.println(execution);
    }
    
    @Override
    public void notify(Order order)
    {
        System.out.println(order);
    }

    @Override
    public void onLoginSuccess(Session session)
    {
        System.out.println("My accountId is: " + session.getAccountDetails().getAccountId());

        this.session = session;
        session.registerPositionEventListener(this);
        session.registerExecutionEventListener(this);
        session.registerOrderEventListener(this);
        session.subscribe(new PositionSubscriptionRequest(), new DefaultCallback());
        session.subscribe(new OrderSubscriptionRequest(), new DefaultCallback());

        session.start();
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
            System.out.println("Usage " + PositionTrackingClient.class.getName() + " <url> <username> <password> [CFD_DEMO|CFD_LIVE]");
            System.exit(-1);
        }
        
        String url = args[0];
        String username = args[1];
        String password = args[2];
        ProductType productType = ProductType.valueOf(args[3].toUpperCase());

        LmaxApi lmaxApi = new LmaxApi(url);
        PositionTrackingClient loginClient = new PositionTrackingClient();

        lmaxApi.login(new LoginRequest(username, password, productType), loginClient);
    }
    
    private static class DefaultCallback implements Callback
    {
        public void onSuccess()
        {
        }

        @Override
        public void onFailure(final FailureResponse failureResponse)
        {
            throw new RuntimeException(failureResponse.toString());
        }
    }
}
