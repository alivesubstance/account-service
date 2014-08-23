package proservice;

import proservice.cache.CacheServiceException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Mirian Dzhachvadze
 */
@Provider
public class AccountServiceExceptionHandler implements ExceptionMapper<AccountServiceException> {
    @Override
    public Response toResponse(AccountServiceException e) {
        //for simplicity send exception message only

        return Response.serverError()
                .entity(e.getMessage())
                .build();
    }
}
