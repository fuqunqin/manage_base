package cn.com.jandar.action.manage.scd;

import java.util.ArrayList;
import java.util.List;

import cn.com.jandar.action.admin.AdminBaseController;
import cn.com.jandar.interceptor.StartXtInterceptor;
import cn.com.jandar.kit.Constant;
import cn.com.jandar.kit.DateUtil;
import cn.com.jandar.kit.DbUtil;
import cn.com.jandar.model.Produce;
import cn.com.jandar.model.Scd;
import cn.com.jandar.model.Scdsb;
import cn.com.jandar.model.Scdsbmx;
import cn.com.jandar.model.User;

import com.google.common.collect.Lists;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;


@Before(StartXtInterceptor.class)
@ControllerBind(controllerKey = "/manage/scd/sbscd", resource = "设备生产单")
public class SbscdController extends AdminBaseController {
	private static String CKDH = null;

	@ButtonBind(buttonname = "查询")
	public void index() {
		String ddzt; // 单据状态
		if (this.getPara("ddzt") != null) {
			ddzt = this.getPara("ddzt");
		}else{
			ddzt = null;
		}

		Page<Scd> page = Scd.getScds(getParaToInt("pageNumber", 1),getParaToInt("pageSize", PAGESIZE),
				getPara("orderBy", "p_scd.dh"), getPara("order", "desc"), ddzt,
				getPara("SEARCH_LIKES_CUSTOMER"),
				getPara("SEARCH_LIKES_DATETIME_BEGIN"),
				getPara("SEARCH_LIKES_DATETIME_END"));

		//筛选条件
		keepPara("SEARCH_LIKES_CUSTOMER");
		keepPara("SEARCH_LIKES_DATETIME_BEGIN");
		keepPara("SEARCH_LIKES_DATETIME_END");
		
		setAttr("page", page);
		setAttr("ddzt", ddzt);
        boolean result = true;
        String dh = null;
        
        for(Scd scd : page.getList()){
        	if((!scd.get("DDZT").equals("010"))&&scd.get("DHZT")==null){
        		result = false;
        		dh = scd.getStr("ckdh");
        		break;
        	}
        }
        if(result){
        	render("sbscd_list.html");
        }else{
        	this.setAttr("msg", "数据出错，找不到单号：'"+dh+"' 对应的出库数据，请联系管理员！");
			this.setAttr("redirectionUrl", "/manage/scd/sbscd");
			this.render("../../../admin/common/error.html");
        }
		
	}

	@ButtonBind(buttonname = "新增")
	public void add() {
		setAttr("scd", new Scd());
		setAttr("dhzt", true);
		setAttr("sbscd_form_name", "生产单->新增");
		render("sbscd_input.html");
	}

	@Before(Tx.class)
	@ButtonBind(buttonname = "新增")
	public void save() throws Exception {
		boolean result = true;
		String saveResult = null;
		Scd scd = this.getModel(Scd.class);
		String ddzt = DbUtil.readDbString(scd.getStr("DDZT"));

		User user = getSessionAttr(User.LOGIN_USER);
		if ("".equals(ddzt)) {
			return;
		}

		String dh = DbUtil.readDbString(scd.getStr("dh"));
		List<Scdsb> newScdsbList = new ArrayList<Scdsb>();
		if ("010".equals(ddzt)) {
			newScdsbList = this.saveInScd(dh, user, scd);
		}
		if ("001".equals(ddzt)) { // 保存待出\入库状态
			List<Record> detailList = Db
					.find("select * from p_model_detail where p_model_detail.MODELID=?",
							scd.getInt("MODELID"));
			int num = scd.get("SBNUM");
			
			Produce produce = getProduce(scd);

			List<Record> records = Lists.newArrayList();

			// 产品以及对应数量封装
			for (Record record : detailList) {
				Record e = new Record();
				e.set("deviceid", record.get("DEVICEID"));
				int dnum = record.get("DNUM");
				int total = 0;
				total = dnum * num;
				e.set("num", total);
				records.add(e);
			}

			saveResult = Produce.saveAll(user, produce, records);
			if (saveResult.equals(Produce.SUCCESS)) {
				scd.set("CKDH", CKDH);
				scd.set("DDZT", "001");
				this.saveInScd(dh, user, scd);
			}

		} 

		if ((saveResult == null) || saveResult.equals(Produce.SUCCESS)) {
			this.setAttr("msg", Produce.SUCCESS);
			this.setAttr("redirectionUrl", "/manage/scd/sbscd");
			this.render("../../../admin/common/success.html");
		} else {
			this.setAttr("msg", saveResult);
			this.setAttr("redirectionUrl", "/manage/scd/sbscd");
			this.render("../../../admin/common/error.html");
		}

	}

	public List<Scdsb> saveInScd(String dh, User user, Scd scd) {
		List<Scdsb> newScdsbList = new ArrayList<Scdsb>();

		if ("".equals(dh)) {
			String str = Constant.findb_goods_dh("030");
			scd.set("DH", str);
			// 产品生产信息表
			Scd.save(user, scd);

			// 产品信息设备表
			Scdsb.save(user, scd);

			scd = Scd.getscdByDh(str);
			newScdsbList = Scdsb.query(scd);
			// 根据所选模型保存产品信息明细表
			Scdsbmx.save(user, newScdsbList, scd.getInt("MODELID"));

		} else {
			Scd.update(user, scd);
			List<Scdsb> oldScdsbList = Scdsb.query(scd);
			/**
			 * 先删除
			 * */
			Scdsbmx.delete(oldScdsbList);
			Scdsb.delete(scd);

			/**
			 * 再保存，在原来操作人，新建时间基础上加更新员和更新时间
			 * */
			Scdsb.save(user, scd);
			newScdsbList = Scdsb.query(scd);

			Scdsbmx.save(user, newScdsbList, scd.getInt("MODELID"));
		}

		return newScdsbList;
	}

	// 已复核单据查看
	@ButtonBind(buttonname = "查看明细")
	public void queryDetail() {
		// 得到单号
		String dh = this.getPara("dh");
		Scd scd = Scd.getscdByDh(dh);
		List<Record> detaillist = Db.find(
				"select * from p_model_detail where  p_model_detail.MODELID=?",
				scd.get("MODELID"));
		setAttr("detaillist", detaillist);
		setAttr("scd", scd);
		setAttr("sbscd_form_name", "单据");
		render("order_detail.html");
	}

	// 查询草稿
	@ButtonBind(buttonname = "修改")
	public void edit() {
		// 得到单号
		String dh = this.getPara("dh");
		Scd scd = Scd.getscdByDh(dh);

		setAttr("scd", scd);
		setAttr("dhzt", true);
		setAttr("sbscd_form_name", "草稿修改");
		render("sbscd_input.html");
	}

	// 订单编辑
	public void editOrder() {
		// 得到单号
		String dh = this.getPara(0);
		int type = this.getParaToInt(1);
		Scd scd = Scd.getscdByDh(dh);

		setAttr("scd", scd);
		setAttr("type", type);
		switch (type) {
		case 1:
			setAttr("sbscd_form_name", "发货编辑");
			break;
		case 2:
			setAttr("sbscd_form_name", "确认收货编辑");
			break;
		case 3:
			setAttr("sbscd_form_name", "开始生产编辑");
			break;
		case 4:
			setAttr("sbscd_form_name", "发往客户编辑");
			break;
		case 5:
			setAttr("sbscd_form_name", "确认接收编辑");
			break;
		default:
			break;
		}

		render("order_input.html");
	}
	
	@ButtonBind(buttonname = "发货")
	public void edit_order_send() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "发货(编辑)")
	public void edit_order_send_edit() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "确认收货")
	public void edit_order_factory_receive() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "确认收货(编辑)")
	public void edit_order_factory_receive_edit() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "开始生产")
	public void edit_order_product() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "开始生产(编辑)")
	public void edit_order_product_edit() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "发往客户")
	public void edit_order_send_custom() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "发往客户(编辑)")
	public void edit_order_send_custom_edit() {
		editOrder();
	}
	
	@ButtonBind(buttonname = "确认收货(客户)")
	public void edit_order_custom_receive() {
		editOrder();
	}
	
	// 订单保存
	@ButtonBind(buttonname = "保存")
	public void saveOrder() {
		Scd scd = this.getModel(Scd.class);
		int type = this.getParaToInt("type");
		User user = this.getSessionAttr(User.LOGIN_USER);
		switch (type) {
		case 1:
			scd.set("FH007", user.get("id"));
			scd.set("FH008", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
			scd.set("DDZT", "002");
			break;
		case 2:
			scd.set("QRSH003", user.get("id"));
			scd.set("QRSH001", DateUtil.getStdDateTime(scd.get("QRSH001")
					.toString(), "yyyy-MM-dd HH:mm:ss"));
			scd.set("QRSH004", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
			scd.set("DDZT", "003");
			break;
		case 3:
			scd.set("KSSC004", user.get("id"));
			scd.set("KSSC005", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
			scd.set("DDZT", "004");
			break;
		case 4:
			scd.set("FWKH008", user.get("id"));
			scd.set("FWKH009", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
			scd.set("DDZT", "005");
			break;
		case 5:
			scd.set("QRJS003", user.get("id"));
			scd.set("QRJS004", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
			scd.set("DDZT", "006");
			break;
		default:
			break;
		}

		if (Scd.update(user, scd)) {
			this.setAttr("msg", "保存成功！");
			this.setAttr("redirectionUrl", "/manage/scd/sbscd");
			this.render("../../../admin/common/success.html");
		} else {
			this.setAttr("msg", "保存失败，请检查数据是否正确...多次保存失败请联系管理员!");
			this.setAttr("redirectionUrl", "/manage/scd/sbscd");
			this.render("../../../admin/common/error.html");
		}
	}
  
	// 订单作废跳转
	@ButtonBind(buttonname = "作废")
	public void cancelOrder(){
		// 得到单号
		String dh = this.getPara("dh");
		Scd scd = Scd.getscdByDh(dh);
		setAttr("scd", scd);
		
		render("order_cancel.html");
	}
	
	// 订单作废保存
	@Before(Tx.class)
	@ButtonBind(buttonname = "作废")
	public void saveCancelOrder(){
		Scd scd = this.getModel(Scd.class);
		String ZFRKCK = scd.get("ZFRKCK");
		String ZFSM = scd.get("ZFSM");
		
		scd = scd.findById(scd.getInt("id"));
		
		User user = this.getSessionAttr(User.LOGIN_USER);
		
		//作废处理
		scd.set("ZFRKCK", ZFRKCK);
		scd.set("ZFSM", ZFSM);
		scd.set("ZFCZY", user.get("username"));
		scd.set("ZFSJ", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		String zfrkdh = Constant.findb_goods_dh("001"); //设备入库单号
		scd.set("ZFRKDH", zfrkdh);
		
		//数据格式组织
		String ckdh = scd.getStr("CKDH");
	    Produce produce = Produce.getProduceByDh(ckdh);
		
		List<Record> detailList = Db
				.find("select * from c_produce_detail where c_produce_detail.DH=?",
						produce.getStr("dh"));
		
	    produce.set("id", null); 
        produce.set("dh", zfrkdh);
        produce.set("DHLX", "001");
        produce.set("CKCKBH", null);
        produce.set("RKCKBH", ZFRKCK);
        produce.set("DHZT", "002");//待出/入库
        produce.set("BZ", "订单作废入库：订单号"+zfrkdh);
        
		String result = Produce.saveAll(user, produce, detailList);
		
		if ((result == null) || result.equals(Produce.SUCCESS)) {
			scd.set("ddzt", "011");
			scd.update();
			this.setAttr("msg", Produce.SUCCESS);
			this.setAttr("redirectionUrl", "/manage/scd/sbscd");
			this.render("../../../admin/common/success.html");
		} else {
			this.setAttr("msg", result);
			this.setAttr("redirectionUrl", "/manage/scd/sbscd");
			this.render("../../../admin/common/error.html");
		}

	}
	
	public Produce getProduce(Scd scd) {
		Produce produce = new Produce();
		CKDH = Constant.findb_goods_dh("003");

		produce.set("dh", CKDH);
		produce.set("dhlx", "003"); // 默认出库单
		
		produce.set("dhzt", "002"); //
		produce.set("CUSTOMERID", scd.get("CUSTOMERID"));  
		
		produce.set("CKCKBH", scd.getStr("CKCKBH"));
		produce.set("BZ", "订单出库");

		return produce;
	}
}
