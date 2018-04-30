package com.lmax.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.marketdata.HistoricMarketDataRequest;
import com.lmax.api.marketdata.HistoricMarketDataSubscriptionRequest;
import com.lmax.api.marketdata.TopOfBookHistoricMarketDataRequest;
import com.lmax.api.orderbook.HistoricMarketDataEvent;
import com.lmax.api.orderbook.HistoricMarketDataEventListener;

/**
 * Demonstrates how to request historic market data and read data contained in the returned URLs.
 */
public class HistoricMarketDataRequester implements LoginCallback, HistoricMarketDataEventListener
{
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Session session;

    @Override
    public void onLoginSuccess(final Session session)
    {
        final String instructionId = "1";
        final long instrumentId = 4001;

        this.session = session;
        session.registerHistoricMarketDataEventListener(this);
        session.subscribe(new HistoricMarketDataSubscriptionRequest(), new Callback()
        {
            public void onSuccess()
            {
                // Request historic top of book price data
                final HistoricMarketDataRequest request = new TopOfBookHistoricMarketDataRequest(instructionId,
                                                                                                 instrumentId,
                                                                                                 toDate("2011-09-01"),
                                                                                                 toDate("2011-10-01"),
                                                                                                 HistoricMarketDataRequest.Format.CSV);
                // Request historic aggregate price data
                //final HistoricMarketDataRequest request = new AggregateHistoricMarketDataRequest(instructionId,
                //                                                                                 instrumentId,
                //                                                                                 toDate("2011-09-30"),
                //                                                                                 toDate("2011-10-01"),
                //                                                                                 HistoricMarketDataRequest.Format.CSV,
                //                                                                                 HistoricMarketDataRequest.Resolution.MINUTE,
                //                                                                                 AggregateHistoricMarketDataRequest.Option.ASK);

                session.requestHistoricMarketData(request, new Callback()
                {
                    public void onSuccess()
                    {
                        // Successful request - will be asynchronously notified when files are ready for download.
                    }

                    public void onFailure(final FailureResponse failureResponse)
                    {
                        System.err.printf("Failed to request historic market data: %s%n", failureResponse);
                    }
                });
            }

            public void onFailure(final FailureResponse failureResponse)
            {
                System.err.printf("Failed to subscribe: %s%n", failureResponse);
            }
        });

        session.start();
    }

    @Override
    public void notify(final HistoricMarketDataEvent historicMarketDataEvent)
    {
        // Open and process urls
        for (final URL url : historicMarketDataEvent.getUrls())
        {
            System.out.println("Opening url: " + url);
            session.openUrl(url, new UrlCallback()
            {
                public void onSuccess(final URL url, final InputStream inputStream)
                {
                    printCompressedFileContents(inputStream);
                }

                public void onFailure(final FailureResponse failureResponse)
                {
                    System.err.printf("Failed to open url: %s%n", failureResponse);
                }
            });
        }

        // The sample is done.  Stop the session.
        session.stop();
    }

    private void printCompressedFileContents(final InputStream compressedInputStream)
    {
        try
        {
            byte[] buffer = new byte[1024];
            final GZIPInputStream decompressedInputStream = new GZIPInputStream(compressedInputStream);
            int numBytes = decompressedInputStream.read(buffer);
            while (numBytes != -1)
            {
                System.out.print(new String(buffer, 0, numBytes));
                numBytes = decompressedInputStream.read(buffer);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to print compressed file contents", e);
        }
    }

    private Date toDate(final String string)
    {
        try
        {
            return dateFormat.parse(string);
        }
        catch (final ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void onLoginFailure(final FailureResponse failureResponse)
    {
        throw new RuntimeException("Unable to login: " + failureResponse.getDescription(), failureResponse.getException());
    }

    public static void main(String[] args)
    {
        if (args.length != 4)
        {
            System.out.println("Usage " + HistoricMarketDataRequester.class.getName() + " <url> <username> <password> [CFD_DEMO|CFD_LIVE]");
            System.exit(-1);
        }

        String url = args[0];
        String username = args[1];
        String password = args[2];
        LoginRequest.ProductType productType = LoginRequest.ProductType.valueOf(args[3].toUpperCase());

        LmaxApi lmaxApi = new LmaxApi(url);
        HistoricMarketDataRequester marketDataRequester = new HistoricMarketDataRequester();

        lmaxApi.login(new LoginRequest(username, password, productType), marketDataRequester);
    }
}