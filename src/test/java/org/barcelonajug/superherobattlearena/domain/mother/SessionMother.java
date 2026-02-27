package org.barcelonajug.superherobattlearena.domain.mother;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Session;

public final class SessionMother {

  private SessionMother() {
    // Prevent instantiation
  }

  public static Session anActiveSession() {
    return new Session(UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC), true);
  }

  public static Session anActiveSession(UUID sessionId) {
    return new Session(sessionId, OffsetDateTime.now(ZoneOffset.UTC), true);
  }

  public static Session anInactiveSession() {
    return new Session(UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC), false);
  }

  public static Session anInactiveSession(UUID sessionId) {
    return new Session(sessionId, OffsetDateTime.now(ZoneOffset.UTC), false);
  }
}
