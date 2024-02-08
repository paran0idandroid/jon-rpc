package io.jon.rpc.common.exception;

public class SerializerException extends RuntimeException{

    private static final long serialVersionUID = -6783134254669118520L;
    public SerializerException(final Throwable e){
        super(e);
    }

    public SerializerException(final String message){
        super(message);
    }

    public SerializerException(final String message, final Throwable e){
        super(message, e);
    }
}
