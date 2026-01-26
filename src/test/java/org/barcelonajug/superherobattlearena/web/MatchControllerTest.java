package org.barcelonajug.superherobattlearena.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.SubmissionId;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.repository.HeroUsageRepository;
import org.barcelonajug.superherobattlearena.repository.MatchEventRepository;
import org.barcelonajug.superherobattlearena.repository.MatchRepository;
import org.barcelonajug.superherobattlearena.repository.RoundRepository;
import org.barcelonajug.superherobattlearena.repository.SubmissionRepository;
import org.barcelonajug.superherobattlearena.service.BattleEngine;
import org.barcelonajug.superherobattlearena.service.FatigueService;
import org.barcelonajug.superherobattlearena.service.RosterService;
import org.junit.jupiter.api.Test;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MatchControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private MatchRepository matchRepository;
    @MockitoBean
    private MatchEventRepository matchEventRepository;
    @MockitoBean
    private SubmissionRepository submissionRepository;
    @MockitoBean
    private RoundRepository roundRepository;
    @MockitoBean
    private BattleEngine battleEngine;
    @MockitoBean
    private RosterService rosterService;
    @MockitoBean
    private HeroUsageRepository heroUsageRepository;
    @MockitoBean // Can be mocked or real, likely mocked since it's used in controller logic to
                 // build heroes
    private FatigueService fatigueService;

    @Test
    void shouldRunMatch() {
        UUID matchId = UUID.randomUUID();
        Match match = new Match();
        match.setMatchId(matchId);
        match.setRoundNo(1);
        match.setTeamA(UUID.randomUUID());
        match.setTeamB(UUID.randomUUID());
        match.setStatus(MatchStatus.PENDING);

        given(matchRepository.findById(matchId)).willReturn(Optional.of(match));

        Round round = new Round();
        round.setRoundNo(1);
        round.setSpecJson(new RoundSpec("Test", 1, 100, null, null, null, null, "Arena"));
        given(roundRepository.findById(1)).willReturn(Optional.of(round));

        Submission subA = new Submission();
        subA.setTeamId(match.getTeamA());
        subA.setRoundNo(1);
        subA.setSubmissionJson(new DraftSubmission(List.of(1), "A"));
        given(submissionRepository.findById(new SubmissionId(match.getTeamA(), 1))).willReturn(Optional.of(subA));

        Submission subB = new Submission();
        subB.setTeamId(match.getTeamB());
        subB.setRoundNo(1);
        subB.setSubmissionJson(new DraftSubmission(List.of(2), "B"));
        given(submissionRepository.findById(new SubmissionId(match.getTeamB(), 1))).willReturn(Optional.of(subB));

        SimulationResult result = new SimulationResult(match.getTeamA(), 5,
                List.of(new MatchEvent("WIN", 1L, "Won", null, null, 1)));
        given(battleEngine.simulate(eq(matchId), anyList(), anyList(), any(Long.class), eq(match.getTeamA()),
                eq(match.getTeamB()), any()))
                .willReturn(result);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/matches/" + matchId + "/run", null,
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Match completed");
    }
}
