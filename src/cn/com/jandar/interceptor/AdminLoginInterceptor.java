package cn.com.jandar.interceptor;


import cn.com.jandar.action.admin.AdminBaseController;
import cn.com.jandar.model.User;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;


/**
 * 判断用户是否已经登入
 * 
 * @author 
 *
 */
public class AdminLoginInterceptor implements Interceptor{

	public void intercept(ActionInvocation ai) {
//		String viewPath = ai.getViewPath();
//		AdminBaseController c = (AdminBaseController) ai.getController();
//		if(ai.getActionKey().startsWith("/admin")||ai.getActionKey().startsWith("/manage")){
//		if(User.isLogin(c)){//判断是否登录
//		User user= c.getSessionAttr(User.LOGIN_USER);
//		viewPath = viewPath.substring(0, viewPath.length()-1);
//		if(user.getStr("roleStr").indexOf(viewPath)==-1&&!"/admin".equals(viewPath)&&!"/admin/user/edit_only".equals(ai.getActionKey())&&!"/admin/user/update_only".equals(ai.getActionKey())){
//		c.setAttr("msg", "没有权限！");
//		c.setAttr("redirectionUrl", "/admin/main");
//		c.render("/admin/common/error.html");
//		}else{
//		ai.invoke();
//		return;
//		}
//		}else{
//		c.render("/admin/user/login.html");
//		}
//		}else{
//		ai.invoke();
//		}

		//1、判断是否已经完成期初入库 

		//未完成，则 可做模块有admin下所有模块 包括系统启用	    manage/base 下所有模块   和 期初入库模块

		//已完成，则 可做模块为除了 期初入库模块 和系统启用以外所有模块

		//System.out.println("拦截："+ai.getViewPath());
		String viewPath = ai.getViewPath();
		
		System.out.println(viewPath+ai.getMethodName());
		
		AdminBaseController c = (AdminBaseController) ai.getController();
		if(User.isLogin(c)){//判断是否登录
			User user= c.getSessionAttr(User.LOGIN_USER);
			String roleStr = user.getStr("roleStr");
			if(user.get("username").equals("admin")){				
				ai.invoke();
			}else{
				if(user.getStr("roleStr").indexOf(viewPath+ai.getMethodName())==-1&&!"/admin/".equals(viewPath)){
					c.setAttr("msg", "没有权限！");
					c.setAttr("redirectionUrl", "/admin/main");
					c.render("/admin/common/error.html");
				}else{
					ai.invoke();

				}
			}
		}else{
			c.render("/admin/user/login.html");
		}
	}

}
