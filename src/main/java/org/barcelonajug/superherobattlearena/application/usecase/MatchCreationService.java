package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.springframework.stereotype.Service;

@Service
public class MatchCreationService {

    private final SubmissionRepositoryPort submissionRepository;
    private final MatchRepositoryPort matchRepository;

    public MatchCreationService(SubmissionRepositoryPort submissionRepository, MatchRepositoryPort matchRepository) {
        this.submissionRepository = submissionRepository;
        this.matchRepository = matchRepository;
    }

    public List<UUID> autoMatch(UUID sessionId, Integer roundNo) {
        List<Submission> submissions = submissionRepository.findByRoundNo(roundNo);
        List<UUID> matchIds = new ArrayList<>();

        // Simple pairing logic: pair adjacent submissions
        for (int i = 0; i < submissions.size() - 1; i += 2) {
            Submission subA = submissions.get(i);
            Submission subB = submissions.get(i + 1);

            Match match = new Match();
            match.setMatchId(UUID.randomUUID());
            match.setSessionId(sessionId);
            match.setTeamA(subA.getTeamId());
            match.setTeamB(subB.getTeamId());
            match.setRoundNo(roundNo);
            match.setStatus(MatchStatus.PENDING);

            matchRepository.save(match);
            matchIds.add(match.getMatchId());
        }

        return matchIds;
    }
}
