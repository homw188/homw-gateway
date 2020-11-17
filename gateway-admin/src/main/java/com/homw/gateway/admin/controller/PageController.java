package com.homw.gateway.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class PageController extends BaseController {

	@RequestMapping("")
	public String index() {
		return "/index";
	}
	
	@RequestMapping("/index")
	public String indexPage() {
		return "/index";
	}
	
	@RequestMapping("/home")
	public String homePage() {
		return "/home";
	}
}
