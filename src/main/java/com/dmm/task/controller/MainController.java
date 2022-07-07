package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.dmm.task.AccountUserDetails;
import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;

@Controller
public class MainController {

	@Autowired
	private TasksRepository repo;
	
	@GetMapping("/loginForm")
	/**
	 * ログイン画面を表示する処理
	 * @return login.html
	 */
	public String loginForm() {
		return "login";
	}
	
	@GetMapping("/accessDeniedPage")
	public String accessDeniedPage() {
		return "accessDeniedPage";
	}
	
	@GetMapping("/main")
	/**
	 * カレンダー画面を表示する処理
	 * ※ログイン直後にこの処理が呼ばれる
	 * @param model
	 * @param user ログインユーザ
	 * @param date 現在の日付またはカレンダーの左右ボタンで遷移した年月
	 * @return main.html
	 */
	public String _main(Model model, @AuthenticationPrincipal AccountUserDetails user,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		
		// 日付ごとのタスクを保持するマップ変数
	    MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();

	    // カレンダーを表示するため2次元リスト
	    List<List<LocalDate>> matrix = new ArrayList<>();
	
	    // 1週間ずつ日付を保持するリスト
	    List<LocalDate> week = new ArrayList<>();
	    
	    // -------------------初週の処理 START
	    matrix.add(week);
	    LocalDate d;
	    
	    // ログイン直後の場合はdate変数がnullのため画面操作時の日付を取得する
	    if(date == null) {
	    	// 画面操作時の年月日を取得
	    	d = LocalDate.now();
	    	// 変数dに画面操作時の年月日の初日をセットし直す
	    	// 例：画面操作時が2022/7/7であれば、2022/7/1をセットする
	    	d = LocalDate.of(d.getYear(), d.getMonthValue(), 1);
	    } else {
	    	// カレンダーページで過去または未来年月を選択した場合はその年月を変数dに保持する
	    	d = date;
	    }
	    
	    // カレンダーページに埋め込む前月と翌月をセットする
	    model.addAttribute("prev", d.minusMonths(1));
	    model.addAttribute("next", d.plusMonths(1));
	    model.addAttribute("month", d.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
	    DayOfWeek w = d.getDayOfWeek();
	    LocalDate start = d = d.minusDays(w.getValue());
	    for(int i = 1; i <= 7; i++) {
	      week.add(d);
	      tasks.put(d, null);
	      d = d.plusDays(1);
	    }
	    
	    // -------------------初週の処理 END
	    
	    // ------------------2週目の処理 START
	    week = new ArrayList<>();
	    matrix.add(week);
	    for(int i = 7; i <= d.lengthOfMonth(); i++) {
	    	w = d.getDayOfWeek();
	    	week.add(d);
	    	tasks.put(d, null);
	    	// 処理中の日付が土曜日であればweek変数を初期化する
		    if(w == DayOfWeek.SATURDAY) {
		    	week = new ArrayList<>();
		    	matrix.add(week);
		    }

		     d = d.plusDays(1);
	    }
	    // ------------------2週目の処理 END
	    
	    // 最終週
	    w = d.getDayOfWeek();
	    for(int i = 1; i <= 7-w.getValue(); i++) {
	    	week.add(d);
	    	tasks.put(d, null);
	    	d = d.plusDays(1);
	    }
	    LocalDate end = d;
	    model.addAttribute("matrix", matrix);
	    
		// ログインユーザの権限に応じてデータベースのtasksテーブルから登録済のタスクを取得する処理
	    List<Tasks> list;
	    
	 // ADMINの場合は全ユーザのタスクを取得する
	    if(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ADMIN"))) {
	    	list = repo.findAllByDateBetween(start.atTime(0,0), end.atTime(0,0));
	    } else {
	    	// ADMINではない場合はログインユーザのみのタスクを取得する
	    	list = repo.findByDateBetween(start.atTime(0, 0),end.atTime(0, 0), user.getName());
	    }
	    
	    for(Tasks t : list) {
	    	// データベースから取得したタスクをリスト変数にセットする
	    	tasks.add(t.getDate().toLocalDate(), t);
	    }
	    
	    model.addAttribute("tasks", tasks);

	    return "main";
	}
	
	@GetMapping("/main/create/{date}")
	/**
	 * タスク投稿(create.html)画面を表示する処理
	 * カレンダーページの登録リンクをクリックしたときに呼び出される処理
	 * @param model
	 * @param date
	 * @return 投稿(create.html)画面
	 */
	public String create(Model model, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		return "create";
	}
	
	@PostMapping("/main/create")
	/**
	 * タスクを新規登録する処理
	 * 投稿(create.html)画面の投稿ボタンをクリックしたときに呼び出される
	 * @param model
	 * @param form
	 * @param user ログインユーザ
	 * @return　カレンダーページにリダイレクトする
	 */
	public String createPost(Model model, TaskForm form, @AuthenticationPrincipal AccountUserDetails user) {
		Tasks task = new Tasks();
		task.setName(user.getName());
		task.setTitle(form.getTitle());
		task.setText(form.getText());
		task.setDate(form.getDate().atTime(0, 0));
		
		repo.save(task);
		
		return "redirect:/main";
	}
	
	@GetMapping("/main/edit/{id}")
	/**
	 * 投稿編集(edit.html)画面を表示する処理
	 * カレンダーページのタスクリンクをクリックしたときに呼び出される
	 * @param model
	 * @param id tasksテーブルに登録済のID
	 * @return edit.thml
	 */
	public String edit(Model model, @PathVariable Integer id) {
		Tasks task = repo.getById(id);
		model.addAttribute("task", task);
		return "edit";
	}
	
	@PostMapping("/main/edit/{id}")
	/**
	 * 登録済のタスクを更新する処理
	 * 投稿編集(edit.html)画面の更新ボタン押下時に呼び出される処理
	 * @param model
	 * @param form
	 * @param id tasksテーブルに登録済のID
	 * @param user ログインユーザ
	 * @return カレンダーページにリダイレクトする
	 */
	public String editPost(Model model, TaskForm form, @PathVariable Integer id, @AuthenticationPrincipal AccountUserDetails user) {
		Tasks task = new Tasks();
		task.setId(id);
		
		task.setName(user.getName());
		task.setTitle(form.getTitle());
		task.setText(form.getText());
		task.setDate(form.getDate().atTime(0,0));
		task.setDone(form.isDone());
		
		repo.save(task);
		
		return "redirect:/main";
	}
	
	@PostMapping("/main/delete/{id}")
	/**
	 * 登録済のタスクを削除する処理
	 * 投稿編集(edit.html)画面の削除ボタン押下時に呼び出される処理
	 * @param model
	 * @param form
	 * @param id tasksテーブルに登録済のID
	 * @return カレンダーページにリダイレクトする
	 */
	public String deletePost(Model model, TaskForm form, @PathVariable Integer id) {
		Tasks task = new Tasks();
		task.setId(id);
		
		repo.delete(task);
		
		return "redirect:/main";
	}
}