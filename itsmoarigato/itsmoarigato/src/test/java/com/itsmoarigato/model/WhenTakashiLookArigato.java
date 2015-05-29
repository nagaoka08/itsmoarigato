package com.itsmoarigato.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.itsmoarigato.Message;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@Transactional
@Ignore //テストが失敗する暫定措置 com.itsmoarigato.config.HttpSessionConfigのアノテーション(@EnableEmbeddedRedis,@EnableRedisHttpSession)をコメントアウトして実行してください
public class WhenTakashiLookArigato {
	
	private static final String me = "takashi@hoge.co.jp";
	private static final String friend = "tae@hoge.co.jp";
	
	@Autowired
	Bucho bucho;
	
	@Autowired
	Arigato arigato;
	
	private Pagination p = new Pagination();
	
	@Before
	public void before(){
		link();
		clearArigato();
	}
	private void link(){
		jdbcTemplate.update("delete from friend_tbl");
		link(me,me);
		link(me,friend);
		link(me,Bucho.email);
	}
	private void clearArigato(){
		jdbcTemplate.update("delete from arigato_tbl");
	}
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	private void link(String me, String friend) {
		jdbcTemplate.update("insert into friend_tbl (me,friend,created) values (?,?,sysdate)",new Object[]{me,friend});
	}


	@Test
	public void メッセージがないとき自分あてのメッセージを見ると０件であるべき(){
		List<Message> messages = arigato.getMineMessages(me,p);
		assertThat(messages.size(),is(0));
	}

	@Test
	public void 自分あてのメッセージを登録してもらい自分あてのメッセージを見ると1件であるべき(){
		bucho.sayArigato(me);
		
		List<Message> messages = arigato.getMineMessages(me,p);
		assertThat(messages.size(),is(1));

		Message message = messages.get(0);
		assertThat(message.getId(),not(0));
		assertThat(message.getFromUser().getEmail(),is(Bucho.email));
		assertThat(message.getToUser().getEmail(),is(me));
		assertThat(message.getSubject(),is("いつもありがと"));
		assertThat(message.getContents(),is("今日もがんばってるね:)"));
	}

	@Test
	public void 他人あてのメッセージを登録してもらい自分あてのメッセージを見ると0件であるべき(){
		bucho.sayArigato(friend);
		
		List<Message> messages = arigato.getMineMessages(me,p);
		assertThat(messages.size(),is(0));
	}

	@Test
	public void 自分あてと他人あてのメッセージを登録してもらい周囲のメッセージを見ると2件であるべき(){
		bucho.sayArigato(me);
		bucho.sayArigato(friend);
		
		List<Message> messages = arigato.getAroundMessages(me,p);
		assertThat(messages.size(),is(2));
		assertThat(messages.get(0).getToUser().getEmail(),is(friend));//新しい順
		assertThat(messages.get(1).getToUser().getEmail(),is(me));
	}

	@Test
	public void 自分あてのメッセージを変更してもらい自分あてのメッセージを見ると1件であるべき(){
		bucho.sayArigatoAndUpdate(me);
		
		List<Message> messages = arigato.getMineMessages(me,p);
		assertThat(messages.size(),is(1));

		Message message = messages.get(0);
		assertThat(message.getId(),not(0));
		assertThat(message.getFromUser().getEmail(),is(Bucho.email));
		assertThat(message.getToUser().getEmail(),is(me));
		assertThat(message.getSubject(),is("今日もありがと"));
		assertThat(message.getContents(),is("ムリしないでね:)"));
	}

	@Test
	public void 自分あてのメッセージ3件を登録してもらいある時点以降の周囲のメッセージを見ると2件であるべき(){
		bucho.sayArigato(me);
		Timestamp atPoint = new Timestamp(System.currentTimeMillis());
		wait_();
		bucho.sayArigato(me);
		bucho.sayArigato(me);
		List<Message> messages = arigato.getAroundMessages(me,new Pagination(atPoint));
		assertThat(messages.size(),is(2));
	}

	private void wait_() {
		try {
			Thread.currentThread().sleep(1);
		} catch (InterruptedException e) {
			// N/A 
		}
	}
	
	@Test
	public void 自分あてのメッセージ3件を登録してもらいある時点以降の自分宛てのメッセージを見ると2件であるべき(){
		bucho.sayArigato(me);
		Timestamp atPoint = new Timestamp(System.currentTimeMillis());
		wait_();
		bucho.sayArigato(me);
		bucho.sayArigato(me);
		List<Message> messages = arigato.getMineMessages(me,new Pagination(atPoint));
		for (Message message : messages) {
			System.out.print(message.getId());
			System.out.print("\t");
			System.out.print(message.getContents());
			System.out.print("\t");
			System.out.println(message.getCreated());
		}
		assertThat(messages.size(),is(2));
	}
	@Test
	public void 存在しないメッセージを取得するとどうなるの(){
		//FIXME 実装してね
	}
	@Test
	public void 友達以外のメッセージを取得するとどうなるの(){
		//FIXME 実装してね
	}
	@Test
	public void 友達以外にメッセージを登録するとどうなるの(){
		//FIXME 実装してね
	}
	@Test
	public void 友達以外のメッセージを更新するとどうなるの(){
		//FIXME 実装してね
	}
	@Test
	public void 存在しないメッセージを更新するとどうなるの(){
		//FIXME 実装してね
	}
}