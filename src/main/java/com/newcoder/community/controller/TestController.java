package com.newcoder.community.controller;

import com.newcoder.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class TestController {

    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response, HttpServletRequest request) {
        Cookie cookie = new Cookie("code", "123");
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        return "set cookie";
    }

    @GetMapping("/session/set")
    @ResponseBody
    public String setSession(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("name", "wlz");
        return "set seeeion";
    }

    @PostMapping("/ajax")
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0, "操作成功");
    }
}
