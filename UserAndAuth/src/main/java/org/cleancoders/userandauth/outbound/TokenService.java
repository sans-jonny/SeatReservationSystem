package org.cleancoders.userandauth.outbound;

public interface TokenService {
    String generate(String userId, String username, String role);
}
