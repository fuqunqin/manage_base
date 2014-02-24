package cty.kit.route;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.jandar.kit.DbUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.jfinal.kit.StringKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;

import cty.kit.ClassSearcher;

public class AutoBindRoutes extends Routes {

	protected final Logger logger = Logger.getLogger(getClass());

	private List<Class<? extends Controller>> excludeClasses = Lists.newArrayList();

	private List<Class<? extends Controller>> excludeResource = Lists.newArrayList();

	private static List<Record> resources = Lists.newArrayList();

	private List<String> includeJars = Lists.newArrayList();

	private boolean includeAllJarsInLib;

	private boolean autoScan = true;

	private String suffix = "Controller";

	private static Map<String,Map<String,String>> buttoncheck = new HashMap<String,Map<String,String>>();

	public AutoBindRoutes() {

	}

	public AutoBindRoutes(boolean autoScan) {
		this.autoScan = autoScan;
	}

	public boolean setIncludeAllJarsInLib() {
		return includeAllJarsInLib;
	}

	public AutoBindRoutes addJar(String jarName) {
		if (StringKit.notBlank(jarName)) {
			includeJars.add(jarName);
		}
		return this;
	}

	public AutoBindRoutes addJars(String jarNames) {
		if (StringKit.notBlank(jarNames)) {
			addJars(jarNames.split(","));
		}
		return this;
	}

	public AutoBindRoutes addJars(String[] jarsName) {
		includeJars.addAll(Arrays.asList(jarsName));
		return this;
	}

	public AutoBindRoutes addJars(List<String> jarsName) {
		includeJars.addAll(jarsName);
		return this;
	}

	public AutoBindRoutes addExcludeClass(Class<? extends Controller> clazz) {
		if (clazz != null) {
			excludeClasses.add(clazz);
		}
		return this;
	}

	public AutoBindRoutes addExcludeClasses(Class<? extends Controller>[] clazzes) {
		excludeClasses.addAll(Arrays.asList(clazzes));
		return this;
	}

	public AutoBindRoutes addExcludeResource(Class<? extends Controller> clazz) {
		if (clazz != null) {
			excludeResource.add(clazz);
		}
		return this;
	}

	public AutoBindRoutes addExcludeResources(Class<? extends Controller>[] clazzes) {
		excludeResource.addAll(Arrays.asList(clazzes));
		return this;
	}

	public AutoBindRoutes addExcludeClasses(List<Class<? extends Controller>> clazzes) {
		excludeClasses.addAll(clazzes);
		return this;
	}
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void config() {
		List<Class<? extends Controller>> controllerClasses = ClassSearcher.findInClasspathAndJars(Controller.class, includeJars);
		ControllerBind controllerBind = null;
		for (Class controller : controllerClasses) {
			if (excludeClasses.contains(controller)) {
				continue;
			}
			controllerBind = (ControllerBind) controller.getAnnotation(ControllerBind.class);
			Record record = controllerResource(controller);
			if(record != null){
				if (!excludeResource.contains(controller)) {
					resources.add(record);
				}
			}
			if (controllerBind == null) {
				if (!autoScan) {
					continue;
				}
				this.add(controllerKey(controller), controller);
				logger.debug("routes.add(" + controllerKey(controller) + ", " + controller.getName() + ")");
			} else {
				if (StringKit.isBlank(controllerBind.viewPath())) {
					this.add(controllerBind.controllerKey(), controller);
					logger.debug("routes.add(" + controllerBind.controllerKey() + ", " + controller.getName() + ")");
				} else {
					this.add(controllerBind.controllerKey(), controller, controllerBind.viewPath());
					logger.debug("routes.add(" + controllerBind.controllerKey() + ", " + controller + "," + controllerBind.viewPath() + ")");
				}
				Map<String,String> mapbutton = new LinkedHashMap<String,String>();
				ButtonBind buttonBind = null;
				Method[] methods = controller.getMethods();
				for (Method me : methods) {
					buttonBind = me.getAnnotation(ButtonBind.class);
					if(buttonBind!=null){
						String buttonname = buttonBind.buttonname();
						String value = DbUtil.readDbString(mapbutton.get(buttonname));
						if(!"".equals(value)){
							value = value + controllerBind.controllerKey()+"/"+me.getName()+";";
							mapbutton.put(buttonname, value);
						}else
							mapbutton.put(buttonname, controllerBind.controllerKey()+"/"+me.getName()+";");
					}
				}
//				System.out.println(mapbutton.toString());
				//查询目录菜单角色信息
				buttoncheck.put(controllerBind.controllerKey(), mapbutton);
			}

		}
	}

	private String controllerKey(Class<Controller> clazz) {
		Preconditions.checkArgument(clazz.getSimpleName().endsWith(suffix),
				clazz.getSimpleName()+" does not has a ControllerBind annotation and it,s name is not end with " + suffix);
		String controllerKey = "/" + StringKit.firstCharToLowerCase(clazz.getSimpleName());
		controllerKey = controllerKey.substring(0, controllerKey.indexOf("Controller"));
		return controllerKey;
	}

	private Record controllerResource(Class<Controller> clazz){
		Record record = new Record();
		ControllerBind controllerBind  = (ControllerBind) clazz.getAnnotation(ControllerBind.class);
		if (controllerBind != null) {
			if(!StringKit.isBlank(controllerBind.resource())){
				record.set("resource", controllerBind.resource());
			}else{
				record.set("resource", clazz.getSimpleName());
			}
			if(!StringKit.isBlank(controllerBind.controllerKey())){
				record.set("keypath", controllerBind.controllerKey());
			}else{
				record.set("keypath", controllerKey(clazz));
			}
		} else{
			if (!autoScan) {
				return null;
			}
			record.set("resource", clazz.getSimpleName());
			record.set("keypath", controllerKey(clazz));
		}
		return record;
	}

	public static void synResource(){
//		Db.tx(new IAtom() {
//			public boolean run() throws SQLException {
//				if(Db.update("delete from ts_resource") >= 1){
//					if(!resources.isEmpty()){
//						Db.batch(" insert into ts_resource(keypath, resource) values(?, ?)", "keypath, resource", resources, resources.size());
//						return true;
//					}
//				}
//				return false;
//			}
//		});
		Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				//1、删除所有按钮信息
				Db.update("delete from ts_menu where cdlx = '003' ");
				//2、查询所有菜单信息
				List<Record> records = Db.find("select * from ts_menu where cdlx = '002'");
				for(int i =0;i<records.size();i++){
					Record record = records.get(i);
					String url = DbUtil.readDbString(record.get("url"));
					String code = DbUtil.readDbString(record.get("code"));
					String path = DbUtil.readDbString(record.get("path"));
					Map<String,String> mapbutton = buttoncheck.get(url);
					if(mapbutton!=null){
						Set<String> key = mapbutton.keySet();
						for (Iterator it = key.iterator(); it.hasNext();) {
							String s = (String) it.next();
							//System.out.println(s+"="+mapbutton.get(s));
							Record button = new Record();
							button.set("cdlx", "003");
							button.set("qybz", "1");
							button.set("parent_id", code);
							button.set("name", s);
							button.set("url", mapbutton.get(s));
							Db.save("ts_menu", button);
							button.set("code", code+button.get("id"));
							button.set("path", path+","+code+button.get("id")+"-"+button.get("id"));
							Db.update("ts_menu", button);
							//System.out.println("-----------");
						}
					}
				}
				return true;
			}
		});

	}

	public List<Class<? extends Controller>> getExcludeClasses() {
		return excludeClasses;
	}

	public void setExcludeClasses(List<Class<? extends Controller>> excludeClasses) {
		this.excludeClasses = excludeClasses;
	}

	public List<String> getIncludeJars() {
		return includeJars;
	}

	public void setIncludeJars(List<String> includeJars) {
		this.includeJars = includeJars;
	}

	public boolean isIncludeAllJarsInLib() {
		return includeAllJarsInLib;
	}

	public void setIncludeAllJarsInLib(boolean includeAllJarsInLib) {
		this.includeAllJarsInLib = includeAllJarsInLib;
	}


	public void setAutoScan(boolean autoScan) {
		this.autoScan = autoScan;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}




}
