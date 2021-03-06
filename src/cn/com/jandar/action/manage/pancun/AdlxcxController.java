package cn.com.jandar.action.manage.pancun;

import java.util.ArrayList;
import java.util.List;

import cn.com.jandar.action.admin.AdminBaseController;
import cn.com.jandar.interceptor.StartXtInterceptor;
import cn.com.jandar.model.Produce;
import cn.com.jandar.model.ProduceDetail;

import com.jfinal.aop.Before;
import com.jfinal.kit.StringKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;
@Before(StartXtInterceptor.class)
@ControllerBind(controllerKey = "/manage/pancun/adlxcx", resource = "按单类型查询")
public class AdlxcxController extends AdminBaseController {

	@ButtonBind(buttonname = "查询")
	public void index() {
		// 条件：出入库单类型 起始日期 截至日期
		String filter_LIKES_DHLX = getPara("filter_LIKES_DHLX");
		String filter_LIKES_startTime = getPara("filter_LIKES_startTime");
		String filter_LIKES_endTime = getPara("filter_LIKES_endTime");
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer(
				"FROM c_produce where (1=1 and DHZT='003')");
		if (!StringKit.isBlank(filter_LIKES_DHLX)) {
			sql.append(" and  c_produce.DHLX = ? ");
			param.add(filter_LIKES_DHLX);
		}
		if (!StringKit.isBlank(filter_LIKES_startTime)) {
			sql.append(" and  c_produce.CHECKDATE >= ? ");
			param.add(filter_LIKES_startTime);
		}
		if (!StringKit.isBlank(filter_LIKES_endTime)) {
			sql.append(" and  c_produce.CHECKDATE <= ? ");
			param.add(filter_LIKES_endTime + " 23:59:59");
		}
		sql.append(" order by ").append("c_produce.id").append(" ")
				.append("asc");
		Page<Record> page = Db.paginate(getParaToInt("pageNumber", 1),
				getParaToInt("pageSize", PAGESIZE), "SELECT * ",
				sql.toString(), param.toArray());
		List<Record> datas = page.getList();
		keepPara("filter_LIKES_DHLX");
		keepPara("filter_LIKES_startTime");
		keepPara("filter_LIKES_endTime");
		setAttr("page", page);
		render("kc_list.html");
	}

	@ButtonBind(buttonname = "更新")
	public void edit() {
		Produce produce = Produce.getProduceById(getPara("id"));
		List<Record> produceDetail = ProduceDetail.getProduceDetailByDH(produce
				.getStr("DH"));
		setAttr("produce", produce);
		setAttr("produceDetail", produceDetail);
		render("kc_input.html");
	}
}
