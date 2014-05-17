package com.dubaidrums.tickets.web;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@RequestMapping("/tix/**")
@Controller
public class TixController {

	@RequestMapping(produces = "text/html")
	public String create(){
		return "tix/form";
	}

}
