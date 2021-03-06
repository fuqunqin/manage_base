package cn.com.jandar.action.manage.base;

import cn.com.jandar.action.admin.AdminBaseController;
import cn.com.jandar.model.Factory;
import cn.com.jandar.model.User;
import cn.com.jandar.plugin.DicPlugin;

import com.jfinal.plugin.activerecord.Page;

import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;

/**
 * @author
 */
@ControllerBind(controllerKey="/manage/base/factory",resource="厂商登记")
public class FactoryController extends AdminBaseController{
	@ButtonBind(buttonname="查询")
	public void index(){
		
		Page<Factory> page =  Factory.getFactoryPage(getParaToInt("pageNumber",1),getParaToInt("pageSize",PAGESIZE),getPara("orderBy","b_factory.ID"),getPara("order","desc"),getPara("filter_LIKES_name"));
		keepPara("filter_LIKES_name");
		setAttr("page", page);
		render("factory_list.html");
	}
//    public void delete(){
//		String[] ids=getParaValues("ids");
//		renderJson("status", Factory.delete(ids));
//	}
	@ButtonBind(buttonname="新增")
	public void add(){
		render("factory_input.html");
	}
	@ButtonBind(buttonname="新增")
	public void save(){
		Factory factory = getModel(Factory.class);
		
		factory.set("OPERATOR",((User)getSessionAttr(User.LOGIN_USER)).get("username"));
		setAttr("msg", Factory.save(factory));
		DicPlugin.loadb_factoryDb();      //重置厂商下拉信息
		setAttr("redirectionUrl", "/manage/base/factory");
		render("/admin/common/success.html");
	}
	@ButtonBind(buttonname="更新")
	public void edit(){
		Factory factory = Factory.getFactoryById(getPara("ID"));
		setAttr("factory", factory);
		render("factory_input.html");
	}

	@ButtonBind(buttonname="更新")
	public void update(){
		Factory factory=getModel(Factory.class);
		
		factory.set("UPOPERATOR",((User)getSessionAttr(User.LOGIN_USER)).get("username"));
		setAttr("msg", Factory.update(factory));
		DicPlugin.loadb_factoryDb();      //重置厂商下拉信息
		setAttr("redirectionUrl", "/manage/base/factory");
		render("/admin/common/success.html");
	}
	
	@ButtonBind(buttonname="查看")
	public void seek(){
		Factory factory = Factory.getFactoryById(getPara("ID"));
		setAttr("factory", factory);
		render("factory_seek.html");
	}
	
	@ButtonBind(buttonname="检查厂商名字")
	public void checkFactoryName(){
		Factory factory=getModel(Factory.class);
		renderText(Factory.isExitByFactoryName(factory.getStr("FNAME")));
	}
	/**
	 *  更改有效标志
	 */

//	@ButtonBind(buttonname="更改厂商状态")
//	public void changeYXBZ() {
//	    Factory.changeYXBZ(getPara("ID"));
//	    setAttr("msg", "变更厂商状态成功！");
//	    setAttr("redirectionUrl", "/manage/base/factory");
//		render("/admin/common/success.html");
//	}
//	
}
