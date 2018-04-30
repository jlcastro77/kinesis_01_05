package com.lmax.api;

import java.io.FileNotFoundException;

import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.account.LoginRequest.ProductType;
import com.lmax.api.order.Execution;
import com.lmax.api.order.ExecutionEventListener;
import com.lmax.api.order.MarketOrderSpecification;
import com.lmax.api.order.Order;
import com.lmax.api.order.OrderCallback;
import com.lmax.api.order.OrderEventListener;
import com.lmax.api.order.OrderSubscriptionRequest;
import com.lmax.api.orderbook.OrderBookEvent;
import com.lmax.api.orderbook.OrderBookEventListener;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;
import com.lmax.api.reject.InstructionRejectedEvent;
import com.lmax.api.reject.InstructionRejectedEventListener;

public class BasicOrderClient implements LoginCallback, OrderBookEventListener, StreamFailureListener, OrderEventListener, InstructionRejectedEventListener, ExecutionEventListener
{
    private final long instrumentId;
    
    private int failureCount = 5;
    private Session session;
    private long t0 = -1;
    private long orderCount;

    public BasicOrderClient(long instrumentId)
    {
        this.instrumentId = instrumentId;
    }
    
    @Override
    public void notifyStreamFailure(Exception e)
    {
        System.out.println("Error occured on the stream");
        e.printStackTrace(System.out);
        
        if ("UNAUTHENTICATED".equals(e.getMessage()) || e instanceof FileNotFoundException)
        {
            session.stop();
        }
        
        if (--failureCount == -1)
        {
            session.stop();
        }
    }

    String instructionId = null;
    FixedPointNumber quantity = FixedPointNumber.ONE;
    
    @Override
    public void notify(OrderBookEvent orderBookEvent)
    {
        t0 = (-1 == t0) ? System.currentTimeMillis() : t0;
        
        quantity = quantity.negate();
        
        session.placeMarketOrder(new MarketOrderSpecification(instrumentId, instructionId, quantity, TimeInForce.IMMEDIATE_OR_CANCEL), new OrderCallback()
        {
            
            @Override
            public void onSuccess(String instructionId)
            {
                try
                {
                    Thread.sleep(6);
                }
                catch (InterruptedException e)
                {
                }
                
                orderCount++;
                
                if (0 == orderCount % 100)
                {
                    double timeTaken = (System.currentTimeMillis() - t0);
                    double opsPerSec = (orderCount * 1000) / timeTaken;
                    System.out.printf("Orders/sec: %.2f%n", opsPerSec);
                }
            }
            
            @Override
            public void onFailure(FailureResponse failureResponse)
            {
            }
        });
    }
    
    @Override
    public void notify(Execution execution)
    {
    }
    
    @Override
    public void notify(InstructionRejectedEvent instructionRejected)
    {
        System.out.println("Rejection Received: " + instructionRejected);
    }
    
    @Override
    public void notify(Order order)
    {
    }

    @Override
    public void onLoginSuccess(Session session)
    {
        System.out.println("My accountId is: " + session.getAccountDetails().getAccountId());

        this.session = session;
        this.session.registerOrderBookEventListener(this);
        this.session.registerStreamFailureListener(this);
        this.session.registerOrderEventListener(this);
        this.session.registerInstructionRejectedEventListener(this);
        this.session.registerExecutionEventListener(this);
        
        this.session.subscribe(new OrderSubscriptionRequest(), new DefaultCallback());
        
        subscribeToInstrument(instrumentId);

        session.start();
    }

    private void subscribeToInstrument(long instrumentId)
    {
        System.out.printf("Subscribing to: %d%n", instrumentId);
        
        this.session.subscribe(new OrderBookSubscriptionRequest(instrumentId), new Callback()
        {
            public void onSuccess()
            {
            }

            @Override
            public void onFailure(final FailureResponse failureResponse)
            {
                throw new RuntimeException("Failed: " + failureResponse);
            }
        });
    }

    @Override
    public void onLoginFailure(FailureResponse failureResponse)
    {
        System.out.println("Login Failed: " + failureResponse);
    }
    
    public static void main(String[] args) throws InterruptedException
    {
        if (args.length < 4)
        {
            System.out.println("Usage " + BasicOrderClient.class.getName() + " <url> <username> <password> [CFD_DEMO|CFD_LIVE] instrumentId");
            System.exit(-1);
        }
        
        String url = args[0];
        String username = args[1];
        String password = args[2];
        ProductType productType = ProductType.valueOf(args[3].toUpperCase());
        long instrumentId = Long.parseLong(args[4]);

        do 
        {
            System.out.printf("Attempting to login to: %s as %s%n", url, username);
            
            LmaxApi lmaxApi = new LmaxApi(url);
            BasicOrderClient loginClient = new BasicOrderClient(instrumentId);
            
            lmaxApi.login(new LoginRequest(username, password, productType), loginClient);
            
            System.out.println("Logged out, pausing for 10s before retrying");
            Thread.sleep(10000);
        } 
        while (true);
    }
}
