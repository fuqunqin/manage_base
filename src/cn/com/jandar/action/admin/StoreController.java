package cn.com.jandar.action.admin;

import java.util.List;

import cn.com.jandar.kit.DateUtil;
import cn.com.jandar.model.Store;
import cn.com.jandar.model.User;
import cn.com.jandar.plugin.DicPlugin;
import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;

@ControllerBind(controllerKey="/admin/store",resource="仓库维护")
public class StoreController extends AdminBaseController{
	@ButtonBind(buttonname="查询")
	public void index(){
		List<Store> storeList = Store.getStoreList(getPara("orderBy", "b_store.CKBH"), getPara("order", "asc"));
		setAttr("storeList", storeList);
		render("store_list.html");
	}
	@ButtonBind(buttonname="新增")
	public void add() {
		String CKBH=null;
		String maxCKBH = Store.getStoreMaxCHBH();
		int max = Integer.valueOf(maxCKBH);
		max++;
		if(max<10&&max>0){
			CKBH = "00"+String.valueOf(max);
		}else if(max>=10){
			CKBH = "0"+String.valueOf(max);
		}else{
			CKBH = String.valueOf(max);
		}
	    Store store = new Store();
	    store.set("CKBH", CKBH); 
		setAttr("store", store);
		render("store_input.html");
	}
	@ButtonBind(buttonname="新增")
	public void save() {
		Store store = getModel(Store.class);

		store.set("opdate", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		store.set("upipdate", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        store.set("FZRXM", DicPlugin.ts_userall.get(store.get("FZRID")));
        
		User loginUser = this.getSessionAttr(User.LOGIN_USER);
		if (loginUser != null) {
			int id = loginUser.getInt("id");
			store.set("operator", id);
			store.set("upoperator", id);
		}
		DicPlugin.loadb_storeDb();
		setAttr("msg", Store.save(store));
		setAttr("redirectionUrl", "/admin/store");
		render("../common/success.html");
	}
	@ButtonBind(buttonname="更新")
	public void edit() {
		Store store = Store.getStoreById(getPara("id"));
		setAttr("store", store);
		render("store_input.html");
	}
	@ButtonBind(buttonname="更新")
	public void update() {
		Store store = getModel(Store.class);
		store.set("upipdate", DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		User loginUser = this.getSessionAttr(User.LOGIN_USER);
		if (loginUser != null) {
			int id = loginUser.getInt("id");
			store.set("upoperator", id);
		}
		DicPlugin.loadb_storeDb();
		setAttr("msg", Store.update(store));
		setAttr("redirectionUrl", "/admin/store");
		render("../common/success.html");
	}

}
