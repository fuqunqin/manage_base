package cn.com.jandar.action.admin;

import java.util.ArrayList;
import java.util.List;

import cn.com.jandar.model.Area;
import cn.com.jandar.plugin.DicPlugin;
import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;


@ControllerBind(controllerKey="/admin/area",resource="地区维护")
public class AreaController extends AdminBaseController{
	@ButtonBind(buttonname="查询")
	public void index(){
		List<Area> areaList = Area.getAreaTreeList();
		List<Area> newAreaList = new ArrayList<Area>();
		for(Area area : areaList){
			if(area.getStr("CODE").endsWith("0000")){ 
			   area.put("level", 1);
		    }else if(area.getStr("CODE").endsWith("00")){
		       area.put("level", 2);
		    }else{
		       area.put("level", 3);
		    }
		   
		   newAreaList.add(area);
		}
		
		setAttr("areaTreeList", newAreaList);
		render("area_list.html");
		
	}
	@ButtonBind(buttonname="更新")
	public void update(){
		String code = this.getPara("code");
		String partCode = code.substring(0,2);
		if(Area.update(partCode)){
			DicPlugin.loadb_areaDb();
			redirect("/admin/area");
		}else{
			render("../common/error.html");
		}
		
	}
}
