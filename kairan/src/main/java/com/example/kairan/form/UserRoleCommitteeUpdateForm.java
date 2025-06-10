package com.example.kairan.form;

import java.util.List;

import lombok.Data;

@Data
public class UserRoleCommitteeUpdateForm {
	private List<UserFormRow> users;
	
	@Data
	public static class UserFormRow {
		private Integer userId;
		private Integer roleId;
		private Integer committeeId;
	}
}
