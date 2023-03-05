package com.canvas.service.helperServices;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Filter class for header processing.
 */
@Service
public class HeaderFilter implements Filter {

    /**
     * Constructor for dependency injection of AESCryptoService.
     * @param aesCryptoService instance of AESCryptoService
     */
    @Autowired
    private final AESCryptoService aesCryptoService;

    @Autowired
    private final Environment env;

    public static final String Auth = "Authorization";

    private static final Logger logger = LoggerFactory.getLogger(HeaderFilter.class);

    @Autowired
    public HeaderFilter(AESCryptoService aesCryptoService, Environment env) {
        this.aesCryptoService = aesCryptoService;
        this.env = env;
    }

    /**
     * Implements the doFilter method to process the header by implementing the filter class,
     * all requests that come in go through this method.
     *
     * @param request servlet request
     * @param response servlet response
     * @param chain filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!Boolean.parseBoolean(env.getProperty("oauth.enabled"))) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // logic to filter URLs to implement Authz
        if (httpRequest.getRequestURI().toLowerCase().startsWith("/oauth2response") ||
            httpRequest.getRequestURI().startsWith("/login")) {
            chain.doFilter(request, response);
            return;
        }

        String headerValue = httpRequest.getHeader(Auth);

        // process request based on header value
        if (StringUtils.isBlank(headerValue)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                String decryptedToken = "Bearer " + this.aesCryptoService.decrypt(headerValue,"This is a secret key");

                // Wrap the request with a custom implementation that returns the modified header value
                HttpServletRequest modifiedRequest = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getHeader(String name) {
                        if ("Authorization".equals(name)) {
                            return decryptedToken;
                        }
                        return super.getHeader(name);
                    }
                };
                chain.doFilter(modifiedRequest, httpResponse);
            } catch (Exception e) {
                logger.error(e.getMessage());
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }
}
