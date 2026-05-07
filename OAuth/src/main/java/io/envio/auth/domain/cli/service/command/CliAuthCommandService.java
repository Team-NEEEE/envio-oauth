package io.envio.auth.domain.cli.service.command;

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto; // (추가됨) 002번 응답 DTO

public interface CliAuthCommandService {

	// 1. [001 API] 로그인 세션 생성
	CliLoginStartResDto createLoginSession();

	// 2. [GitHub Redirect] 콜백 처리 (토큰 발급 및 Redis 업데이트) -> 새로 추가!
	void processGithubCallback(String code, String sessionId);

	// 3. [002 API] 터미널 폴링 상태 확인 -> 새로 추가!
	CliLoginStatusResDto getLoginStatus(String loginSessionId);

	// 4. [003 API] 기존에 짜두신 유저 및 기기 DB 최종 등록
	CliLoginSaveResDto registerUserAndDevice(final CliLoginSaveReqDto reqDto);

	// 5. 사용 끝난 세션 삭제
	void deleteSession(final String loginSessionId);
}