package proservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import proservice.stats.StatsTracker;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Mirian Dzhachvadze
 */
@Path("/account")
public class AccountResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);

    public static final String ADD_PATH = "/add";
    public static final String GET_PATH = "/{id}";
    public static final String CLEAR_STATS_PATH = "/clearstats";

    private AccountService delegate;

    public AccountResource() {
    }

    public AccountResource(AccountService accountService) {
        this.delegate = accountService;
    }

    @Path(GET_PATH)
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Long getAmount(@PathParam("id") Integer id) {
        return delegate.getAmount(id);
    }

    @Path(ADD_PATH)
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public void addAmount(@FormParam("id") Integer id, @FormParam("value") Long value) {
        delegate.addAmount(id, value);
    }

    @Path(CLEAR_STATS_PATH)
    @GET
    public void clearStats() {
        StatsTracker.getInstance().clear();
    }


}
