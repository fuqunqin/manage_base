package cn.com.jandar.action.admin;

import java.util.List;

import cn.com.jandar.model.Code;
import cn.com.jandar.plugin.DicPlugin;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;


@ControllerBind(controllerKey="/admin/code",resource="字典维护")
public class CodeController extends AdminBaseController{
	@ButtonBind(buttonname="查询")
	public void index(){
		List<Record> list = Code.getCodeList(getPara("filter_LIKES_name"));
		setAttr("codeList", list);
		render("code_list.html");
	}
	
	
	@ButtonBind(buttonname="新增字典类型")
	public void add(){
		render("code_input.html");
	}
	
	@ButtonBind(buttonname="更新字典类型 ")
	public void edit(){
		List<Code> codes = Code.getCodeByType(getPara("type"));
		String typename = codes.get(0).getStr("typename");
		keepPara("type");
		setAttr("typename", typename);
		System.out.println(typename);
		setAttr("codes", codes);
		render("code_input.html");
		
	}
	
	@ButtonBind(buttonname="新增字典类型")
	public void save(){
		String type = getPara("type");
		String typename = getPara("typename");
		//检查重复,并保存
		boolean boo = Code.addtype(type, typename);
		if(boo){
			setAttr("msg", "添加成功");
		}else{
			setAttr("msg", "添加失败");
		}
		//DicPlugin.loadJkZidianDb();//重新加载用户字典
		setAttr("redirectionUrl", "/admin/code");
		render("../common/success.html");
	}
	
	@ButtonBind(buttonname="新增字典")
	public void zidianadd(){
		Code code = new Code();
		List<Code> codes = Code.getCodeByType(getPara("type"));
		String typename = codes.get(0).getStr("typename");
		
		code.set("type", getPara("type"));
		code.set("typename", typename);
		
		setAttr("code", code);
		render("codezidian_input.html");
	}

	@ButtonBind(buttonname="新增字典")
	public void zidiansave(){
		Code code = getModel(Code.class);
		String type = code.getStr("TYPE");
		boolean boo = Code.savezidian(code);
		if(boo){
			setAttr("msg", "添加成功");
		}else{
			setAttr("msg", "添加失败<br>不允许重复编码");
		}
		
		//DicPlugin.loadJkZidianDb();//重新加载用户字典
		setAttr("redirectionUrl", "/admin/code/edit?type="+type);//跳转到
		render("../common/success.html");
	}
	
	
	
	@ButtonBind(buttonname="更新字典")
	public void zidianedit(){
		Code code = Code.getCodeById(getPara("id"));
		setAttr("code", code);
		render("codezidian_input.html");
	}
	
	
	@ButtonBind(buttonname="更新字典")
	public void zidianupdate(){
		Code code = getModel(Code.class);
		String type = code.getStr("TYPE");
		
		setAttr("msg", Code.update(code));
		DicPlugin.loadJkZidianDb();//重新加载用户字典
		setAttr("redirectionUrl",  "/admin/code/edit?type="+type);
		render("../common/success.html");
	}
	
	
	
	@ButtonBind(buttonname="更新字典类型")
	public void update(){
		Code code = getModel(Code.class);
		setAttr("msg", Code.update(code));
		DicPlugin.loadJkZidianDb();//重新加载用户字典
		setAttr("redirectionUrl", "/admin/code");
		render("../common/success.html");
	}
	
	
	/**
	 * 
	@ButtonBind(buttonname="删除")
	public void delete(){
		String ids[] = getParaValues("ids");
		DicPlugin.loadJkZidianDb();//重新加载用户字典
		renderJson("status", Code.delete(ids));
	}
	
	@ButtonBind(buttonname="停/启用")
	public void editzt(){
		String id = getPara("id");
		String qybz = getPara("zt");
		Record cd = Db.findById("ts_code", id);
		
		if("001".equals(qybz)){
			cd.set("QYBZ", "002");//设置停用
		}else{
			cd.set("QYBZ", "001");//设置启用
		}
		Boolean boo = Db.update("ts_code", cd);
		if(boo){
			DicPlugin.loadJkZidianDb();//重新加载用户字典
			setAttr("msg", "设置成功");
		}
		setAttr("redirectionUrl", "/admin/code");
		render("../common/success.html");
	}
	 */
	
}
