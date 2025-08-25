package app.domain.store.model.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUserInfoResponse {

	private Long userId;
	private String username;
	private String email;
	private String nickname;
	private String realName;
	private String phoneNumber;
	private String userRole;

	public GetUserInfoResponse() {
	}

	public GetUserInfoResponse(Long userId, String username, String email, String nickname, String realName, String phoneNumber, String userRole) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.nickname = nickname;
		this.realName = realName;
		this.phoneNumber = phoneNumber;
		this.userRole = userRole;
	}

}
