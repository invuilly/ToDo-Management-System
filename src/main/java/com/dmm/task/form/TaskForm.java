package com.dmm.task.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
/**
 * 投稿新規登録および編集画面の入力フォームに対応する
 * Formクラス
 */
public class TaskForm {

	private String title;
	private String text;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;
	private boolean done;
}
