package com.dmm.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dmm.task.service.AccountUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig  extends WebSecurityConfigurerAdapter{

	@Autowired
	private AccountUserDetailsService userDetailsService;
	
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.debug(false).ignoring().antMatchers("/images/**", "/js/**", "/css/**", "/h2-console/**");	
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService)
				.passwordEncoder(passwordEncoder());
		super.configure(auth);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.exceptionHandling().accessDeniedPage("/accessDeniedPage")
		.and().authorizeRequests().antMatchers("/loginForm").permitAll()
		.anyRequest().authenticated();
		
		http.formLogin()
		.loginPage("/loginForm")
		.loginProcessingUrl("/authenticate")
		.usernameParameter("userName")
		.passwordParameter("password")
		.defaultSuccessUrl("/main")
		.failureUrl("/loginForm?error=true");

		http.logout().logoutSuccessUrl("/loginForm")
		.permitAll();
	}
	
	
}
