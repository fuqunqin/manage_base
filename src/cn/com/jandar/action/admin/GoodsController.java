package cn.com.jandar.action.admin;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import cn.com.jandar.kit.DateUtil;
import cn.com.jandar.model.Goods;
import cn.com.jandar.model.User;
import cn.com.jandar.plugin.DicPlugin;
import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;

@ControllerBind(controllerKey = "/admin/goods", resource = "单号类型维护")
public class GoodsController extends AdminBaseController {
	@ButtonBind(buttonname="查询")
	public void index() {
		List<Goods> goodsList = Goods.getGoodsList(getPara("orderBy", "b_goods.D_TYPE"), getPara("order", "asc"));
		setAttr("goodsList", goodsList);
		render("goods_list.html");
	}
	@ButtonBind(buttonname="新增")
	public void add() {
		render("goods_input.html");
	}
	@ButtonBind(buttonname="新增")
	public void save() {
		Goods goods = getModel(Goods.class);
		goods.set("dhscrq",DateUtil.getFormatDateTime(new Date(), "yyyy-MM-dd"));
		goods.set("opdate", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		goods.set("upipdate", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		User loginUser = this.getSessionAttr(User.LOGIN_USER);
		if (loginUser != null) {
			int id = loginUser.getInt("id");
			goods.set("operator", id);
			goods.set("upoperator", id);
		}
		
		DicPlugin.loadb_goodsDb();
		setAttr("msg", Goods.save(goods));
		setAttr("redirectionUrl", "/admin/goods");
		render("../common/success.html");
	}
	@ButtonBind(buttonname="更新")
	public void edit() {
		Goods goods = Goods.getGoodsById(getPara("id"));
		setAttr("goods", goods);
		render("goods_input.html");
	}
	@ButtonBind(buttonname="更新")
	public void update() {
		Goods goods = getModel(Goods.class);

		 goods.set("upipdate",new Timestamp(System.currentTimeMillis()));
		 
		User loginUser = this.getSessionAttr(User.LOGIN_USER);

		if (loginUser != null) {
			int id = loginUser.getInt("id");
			goods.set("upoperator", id);
		}
		
		DicPlugin.loadb_goodsDb();
		setAttr("msg", Goods.update(goods));
		setAttr("redirectionUrl", "/admin/goods");
		render("../common/success.html");
	}
	@ButtonBind(buttonname="停/启用")
	public void stop() {
		Goods goods = Goods.getGoodsById(getPara("id"));
		if (goods.getStr("yxbz").equals("001")) {
			goods.set("yxbz", "002");
		} else {
			goods.set("yxbz", "001");
		}
		setAttr("msg", Goods.update(goods));
		setAttr("redirectionUrl", "/admin/goods");
		render("../common/success.html");
	}
}