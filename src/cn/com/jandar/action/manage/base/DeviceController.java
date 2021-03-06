package cn.com.jandar.action.manage.base;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;

import cn.com.jandar.action.admin.AdminBaseController;
import cn.com.jandar.model.Device;
import cn.com.jandar.model.User;
import cn.com.jandar.plugin.DicPlugin;
import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;

@ControllerBind(controllerKey = "/manage/base/device", resource = "设备维护")
public class DeviceController extends AdminBaseController {
	@ButtonBind(buttonname="查询")
	public void index() {
		Page<Device> page = Device.getDevicePage(getParaToInt("pageNumber", 1),
				getParaToInt("pageSize", PAGESIZE),
				getPara("orderBy", "b_device.id"), getPara("order", "desc"),
				getPara("filter_LIKES_sblx"), getPara("filter_LIKES_factoryid"), getPara("filter_LIKES_sbzt"));
		keepPara("filter_LIKES_sblx");
		keepPara("filter_LIKES_factoryid");
		keepPara("filter_LIKES_sbzt");
		setAttr("page", page);
//		System.out.println(getPara("filter_LIKES_sblx"));
//		System.out.println(getPara("filter_LIKES_factoryid"));
//		System.out.println(getPara("filter_LIKES_sbzt"));
		render("device_list.html");
	}
	@ButtonBind(buttonname="删除")
	public void delete() {
		String[] ids = getParaValues("ids");
		renderJson("status", Device.delete(ids));
	}
	@ButtonBind(buttonname="新增")
	public void add() {
		render("device_input.html");
	}
	@ButtonBind(buttonname="新增")
	public void save() {
		Device device = getModel(Device.class);
		device.set("OPERATOR",((User)getSessionAttr(User.LOGIN_USER)).get("username"));
		setAttr("msg", Device.save(device));
		DicPlugin.loadb_deviceDb();      //重置设备下拉信息
		setAttr("redirectionUrl", "/manage/base/device");
		render("/admin/common/success.html");
	}
	@ButtonBind(buttonname="更新")
	public void update() {
		Device device = getModel(Device.class);
		device.set("UPOPERATOR",((User)getSessionAttr(User.LOGIN_USER)).get("username"));
		setAttr("msg", Device.update(device));
		DicPlugin.loadb_deviceDb();      //重置设备下拉信息
		setAttr("redirectionUrl", "/manage/base/device");
		render("/admin/common/success.html");
	}
	@ButtonBind(buttonname="更新")
	public void edit() {
		Device device = Device.getDeviceById(getPara("id"));
		setAttr("device", device);
		render("device_input.html");
	}
	@ButtonBind(buttonname="查看")
	public void seek() {
		Device device = Device.getDeviceById(getPara("id"));
		setAttr("device", device);
		render("device_seek.html");
	}

//	@ButtonBind(buttonname="更改设备状态")
//	public void changeState() {
//		Device device = Device.getDeviceById(getPara("id"));
//		device.set("SBZT", getPara("SBZT"));
//		setAttr("msg", Device.update(device));
//		setAttr("redirectionUrl", "/manage/base/device");
//		render("/admin/common/success.html");
//
//	}
}
