/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.rest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.service.UserService;
import org.bfabric.util.TokenUtils;

@Path("/token")
public class TokenRest {

    private static final Logger logger = Logger.getLogger(TokenRest.class.getName());

    @GET
    @Path("/validate")
    public Response validate(@Nonnull @QueryParam("token") String token) {
        String response = Messages.get("errorTokenInvalid");
        String decryptedToken = TokenUtils.decrypt(token);
        if (decryptedToken != null) {
            Map<String, String> requestParameters = Arrays.stream(decryptedToken.split(",")).map(s -> s.split("=", 2)).collect(Collectors.toMap(s -> s[0], s -> s.length > 1 ? s[1] : ""));
            if (requestParameters.containsKey("expiryDateTime")) {
                try {
                    final LocalDateTime expiryDateTime = LocalDateTime.parse(requestParameters.get("expiryDateTime"), Constants.DATETIME_FORMATTER);
                    if (LocalDateTime.now().isAfter(expiryDateTime)) {
                        response = Messages.get("errorTokenExpired");
                    } else {
                        if (CDI.current().select(UserService.class).get().useToken(token)) {
                            response = new Gson().toJson(requestParameters);
                        } else {
                            response = Messages.get("errorTokenUsed");
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Token validation failed: " + e.getMessage());
                    response = Messages.get("errorTokenInvalid");
                }
            }
        }
        return Response.ok(response).build();
    }
}