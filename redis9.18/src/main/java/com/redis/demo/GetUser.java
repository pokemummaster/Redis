package com.redis.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class GetUser {

    @Autowired
    UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;





    @RequestMapping("/getUsers")
    @ResponseBody
    String getUsers(){
        long startTime;
        long endTime;
        startTime = System.currentTimeMillis();
        //获取redis中key="userJSON"的value
        String userJSON = (String)redisTemplate.opsForValue().get("userJSON");
        if(userJSON!=null){
            endTime = System.currentTimeMillis();
            System.out.println("取缓存成功，耗时" + (endTime-startTime) + "ms");
            return userJSON;
        }
        //在redis中获取不到则执行以下代码
        startTime = System.currentTimeMillis();
        List<User> userList = userMapper.getUserList();
        //将userList转为json字符串
        userJSON = JSONArray.fromObject(userList).toString();
        endTime = System.currentTimeMillis();
        //将查询结果存入redis中作缓存
        //四个参数从左至右分别为：key，value，有效时间，时间单位
        //这里即：创建一个key="userJSON",value=#{userJSON}的键值对，其有效时间是10秒
        redisTemplate.opsForValue().set("userJSON",userJSON,10, TimeUnit.SECONDS);
        System.out.println("取缓存失败，耗时" + (endTime-startTime) + "ms");
        return userJSON;
    }

    @RequestMapping("/getUsers2")
    @ResponseBody
    public ModelAndView getUsers2(){
        ModelAndView modelAndView =new ModelAndView();
        long listLength = redisTemplate.opsForList().size("userJSON");
        List<User> list =  redisTemplate.opsForList().range("userJSON",0,listLength);
        if(list.size()!=0){
        List<User> userList = userMapper.getUserList();
            System.out.println("取缓存成功，耗时");
            modelAndView.setViewName("wujifu");
            modelAndView.addObject("list",userList);
            return modelAndView;
        }
        //在redis中获取不到则执行以下代码
        List<User> userList = userMapper.getUserList();
        for(User user : userList){
            redisTemplate.opsForList().leftPush("userJSON",user);
        }
        modelAndView.setViewName("wujifu");
        modelAndView.addObject("list",userList);
        return modelAndView;
    }




}
