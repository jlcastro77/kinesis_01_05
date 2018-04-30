package com.lmax.api;

public class DefaultCallback implements Callback
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