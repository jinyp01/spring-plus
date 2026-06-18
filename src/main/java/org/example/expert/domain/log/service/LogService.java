package org.example.expert.domain.log.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {

	private final LogRepository logRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveManagerRequestLog(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest) {
		Log log = new Log(
			authUser.getId(),
			todoId,
			managerSaveRequest.getManagerUserId(),
			authUser.getEmail()
		);
		logRepository.save(log);
	}
}
