package com.dmm.task;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
 * パスワードハッシュ化クラス
 * パッケージエクスプローラ上でこのクラスを選択し、javaとして実行する
 */
public class GeneratePassword {

	public static void main(String[] args) {
		
		// ↓にハッシュ化したいパスワードの文字列を代入する
		String rawPassword = "admin";
		
		String password = getEncodePassword(rawPassword);
		System.out.println(password);
	}
	
	private static String getEncodePassword(String rawPassword) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.encode(rawPassword);
	}
}
