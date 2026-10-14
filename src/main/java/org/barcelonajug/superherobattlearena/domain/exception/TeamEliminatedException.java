package org.barcelonajug.superherobattlearena.domain.exception;

import java.util.UUID;

public class TeamEliminatedException extends IllegalStateException {

  /** Creates an exception identifying the eliminated team and the round it attempted to enter. */
  public TeamEliminatedException(UUID teamId, int roundNo) {
    super("Team " + teamId + " has been eliminated and cannot submit for round " + roundNo);
  }
}
