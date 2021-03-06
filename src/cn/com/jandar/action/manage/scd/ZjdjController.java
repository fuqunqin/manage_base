package cn.com.jandar.action.manage.scd;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.com.jandar.action.admin.AdminBaseController;
import cn.com.jandar.interceptor.StartXtInterceptor;
import cn.com.jandar.model.Area;
import cn.com.jandar.model.Scd;
import cn.com.jandar.model.Scdsb;
import cn.com.jandar.model.Scdsbmx;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;

@Before(StartXtInterceptor.class)
@ControllerBind(controllerKey="/manage/scd/zjdj",resource="整机信息登记")
public class ZjdjController extends AdminBaseController{
	
	@ButtonBind(buttonname = "查询")
	public void index(){
		//获取有客户的地区的列表
	    Map<String,String> customer_area = new LinkedHashMap<String,String> ();
		customer_area = Area.getCustomerAreaMap();
		setAttr("area", customer_area);
		render("zjdj_list.html");
	}
	
	@ButtonBind(buttonname = "查询")
	public void search(){
		
		//获取有客户的地区的列表
	    Map<String,String> customer_area = new LinkedHashMap<String,String> ();
		customer_area = Area.getCustomerAreaMap();
		
		String dhzt = "003"; // 单据状态
		if (this.getPara("dhzt") != null) {
			dhzt = this.getPara("dhzt");
		}
		
		Page<Scdsb> page = Scdsb.getScdsbList(getParaToInt("pageNumber", 1),
				getParaToInt("pageSize", PAGESIZE),
				getPara("orderBy", "p2.id"), getPara("order", "desc"), "006",getPara("filter_CUSTOMERID"));

		setAttr("page", page);
		setAttr("dhzt", dhzt);
		setAttr("area", customer_area);

		setAttr("filter_CUSTOMER_Area",getPara("filter_CUSTOMER_Area"));
		setAttr("filter_CUSTOMERID",getPara("filter_CUSTOMERID"));
		render("zjdj_list.html");
	}
	
	@ButtonBind(buttonname = "维修")
	public void edit() {
		
		Scdsb scdsb = Scdsb.getScdsbByScdId(getPara("id"));
		Scd scd =  Scd.getscdById(scdsb.getInt("SCDID").toString());
		setAttr("scdsb", scdsb);
		setAttr("filter_CUSTOMER_Area",getPara("filter_CUSTOMER_Area"));
		setAttr("scd", scd);
		render("zjdj_input.html");
	}
	
	@ButtonBind(buttonname = "维修")
	public void update(){
		
		Scdsb scdsb = getModel(Scdsb.class);
		setAttr("msg", Scdsb.update(scdsb));
		
		setAttr("redirectionUrl", "/manage/scd/zjdj/search?filter_CUSTOMERID="+getPara("filter_CUSTOMERID")+"&filter_CUSTOMER_Area="+getPara("filter_CUSTOMER_Area"));
		render("/admin/common/success.html");
	}
	
	@ButtonBind(buttonname = "查看明细")
	public void query(){
		
		 List<Record> record  = Scdsbmx.dao.query(getPara("id"));
		 setAttr("record",record);
		 render("zjdj_detail.html");
	}
	
}
