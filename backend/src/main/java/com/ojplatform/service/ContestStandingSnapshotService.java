package com.ojplatform.service;

import com.ojplatform.dto.StandingDTO;
import com.ojplatform.entity.ContestSubmission;

/**
 * 比赛榜单快照相关业务接口。
 */
public interface ContestStandingSnapshotService {

    StandingDTO getStanding(Long contestId);

    void refreshStandingForSubmission(ContestSubmission submission);

    void rebuildContestSnapshot(Long contestId, boolean frozenView);
}
