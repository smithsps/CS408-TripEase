package com.cs408.tripease;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class LoginHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);

    private AuthProvider authProvider;

    private final String usernameParam = "username";
    private final String passwordParam = "password";
    private String directLoggedInOKURL = "";

    public LoginHandler setDirectLoggedInOKURL(String directLoggedInOKURL) {
        this.directLoggedInOKURL = directLoggedInOKURL;
        return this;
    }

    public static LoginHandler create(AuthProvider authProvider, String directLoggedInOKURL) {
        LoginHandler lh = new LoginHandler();
        lh.authProvider = authProvider;
        lh.directLoggedInOKURL = directLoggedInOKURL;
        return lh;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        if (req.method() != HttpMethod.POST) {
            context.fail(405); // Must be a POST
        } else {
            if (!req.isExpectMultipart()) {
                throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
            }
            MultiMap params = req.formAttributes();
            String username = params.get(usernameParam);
            String password = params.get(passwordParam);
            if (username == null || password == null) {
                log.warn("No username or password provided in form - did you forget to include a BodyHandler?");
                context.session().put("errorLogin", "Invalid Username or Password");
                doRedirect(req.response(), "login");
                return;
            } else {
                if(username.equals("") || password.equals("")) {
                    log.warn("Username or Password is empty");
                    context.session().put("errorLogin", "Username or Password is empty.");
                    doRedirect(req.response(), "login");
                    return;
                }

                Session session = context.session();
                JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
                authProvider.authenticate(authInfo, res -> {
                    if (res.succeeded()) {
                        User user = res.result();
                        context.setUser(user);
                        if (session != null) {
                            doRedirect(req.response(), directLoggedInOKURL);
                            return;
                        }
                        // Either no session or no return url
                        doRedirect(req.response(), directLoggedInOKURL);
                    } else {
                        //context.session().put("errorLogin", "Invalid Username or Password.");
                        //doRedirect(req.response(), "login");
			context.fail(400);
                    }
                });
            }
        }
    }

    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader("location", url).setStatusCode(302).end();
    }
}
