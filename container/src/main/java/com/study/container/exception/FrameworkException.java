// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FrameworkException.java

package com.study.container.exception;


public class FrameworkException extends RuntimeException
{

    public FrameworkException()
    {
    }

    public FrameworkException(String msg)
    {
        super(msg);
    }

    public FrameworkException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FrameworkException(Throwable cause)
    {
        super(cause);
    }

    private static final long serialVersionUID = 0xcd4622c8L;
}
