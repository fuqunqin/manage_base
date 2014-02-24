package cn.com.jandar.action.admin;

import org.apache.commons.lang3.StringUtils;

import cn.com.jandar.model.Role;
import cn.com.jandar.plugin.DicPlugin;

import com.jfinal.plugin.activerecord.Page;

import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;


@ControllerBind(controllerKey="/admin/role", resource="角色管理")
public class RoleController extends AdminBaseController{
	@ButtonBind(buttonname="查询")
	public void index(){
		Page<Role> page =  Role.getRolePage(getParaToInt("pageNumber",1),getParaToInt("pageSize",PAGESIZE),getPara("orderBy","ts_role.id"),getPara("order","desc"),getPara("filter_LIKES_name"));
		keepPara("filter_LIKES_name");
		setAttr("page", page);
		render("role_list.html");
	}
	@ButtonBind(buttonname="新增角色")
	public void add(){
		render("role_input.html");
	}
	@ButtonBind(buttonname="新增角色")
	public void save(){
		Role role = getModel(Role.class);
		role.set("resource", "");
		setAttr("msg", Role.save(role));
		setAttr("redirectionUrl", "/admin/role");
		render("../common/success.html");
	}
	@ButtonBind(buttonname="更新角色")
	public void edit(){
		Role role = Role.getRoleById(getPara("id"));
		String[] authorityList = StringUtils.split(role.getStr("resource"), ",");
		setAttr("role", role);
		setAttr("authorityList", authorityList);
		render("role_input.html");
	}
	@ButtonBind(buttonname="更新角色")
	public void update(){
		Role role=getModel(Role.class);
		setAttr("msg", Role.update(role));
		setAttr("redirectionUrl", "/admin/role");
		render("../common/success.html");
	}
	//添加权限
	@ButtonBind(buttonname="新增权限")
	public void addqx(){
		String id  = getPara("id");
		Role role = Role.getRoleById(id);
		setAttr("menuList",DicPlugin.ts_codeList);//全部开启菜单目录信息
		setAttr("role",role);
		render("role_qx_list.html");
	}
	@ButtonBind(buttonname="新增权限")
	public void saveqx(){
		Role role = getModel(Role.class);
		String[] urls = getParaValues("ids");
		String newurl="";
		if(urls!=null){
			for(String url:urls){
				if(url!=""){
					newurl+=url+";"; 
				}
			}
		}
		System.out.println(newurl);
		role.set("resource", newurl);
		setAttr("msg", Role.update(role));
		setAttr("redirectionUrl", "/admin/role");
		render("../common/success.html");
	}
}