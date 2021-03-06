package cn.com.jandar.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cn.com.jandar.kit.DbUtil;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.StringKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
/**
 * 字典信息加载
 * @author chenty
 *
 */
public class DicPlugin implements IPlugin{

	public final static Properties properties = new Properties();
	/**
	 * 字典维护信息
	 */
	//字典表ts_code
	public final static Map<String,Map<String,String>> jkzidian = new HashMap<String,Map<String,String>> ();
	public final static Map<String,List<Record>> jkzidianList = new HashMap<String,List<Record>> ();
	//仓库表字典B_STORE
	public final static Map<String,String> b_storeall = new LinkedHashMap<String,String> ();//所有仓库
	public final static Map<String,String> b_store = new LinkedHashMap<String,String> ();//有效的[生产库、备件库、坏件库]
	public final static Map<String,String> b_store001002 = new LinkedHashMap<String,String> ();//有效的[生产库、备件库]
	public final static Map<String,String> b_store001 = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_store002 = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_store003 = new LinkedHashMap<String,String> ();
	//单号表字典B_GOODS
	public final static Map<String,String> b_goodsall = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_goods = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_goodssimgle = new LinkedHashMap<String,String> ();
	//用户字典TS_USER
	public final static Map<String,String> ts_userall = new LinkedHashMap<String,String> ();
	//厂家字典B_FACTORY
	public final static Map<String,String> b_factoryall = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_factory = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_factorysimgle = new LinkedHashMap<String,String> ();
	//客户字典B_CUSTOMER
	public final static Map<String,String> b_customerall = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_customer = new LinkedHashMap<String,String> ();
	//设备字典B_DEVICE
	public final static Map<String,String> b_deviceall = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_device = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_device001 = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_device002 = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_device003 = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_devicesimgle = new LinkedHashMap<String,String> ();
	public final static Map<String,String> b_deviceofb_factory = new LinkedHashMap<String,String> ();
	public final static Map<String,Map<String,String>> b_deviceoflx = new HashMap<String,Map<String,String>> ();
	
	//模型字典P_MODEL
	public final static Map<String, String> p_model_all = new LinkedHashMap<String, String>();
	/**
	 * 有效模型
	 * */
	public final static Map<String, String> p_model = new LinkedHashMap<String, String>();
	
	//地区字典B_AREA
	public final static Map<String,String> ts_area = new LinkedHashMap<String,String> ();
	public final static Map<String,String> ts_areasimgle = new LinkedHashMap<String,String> ();
	
	/**
	 * 权限相关
	 */
	public final static List<Record> ts_codeList= new ArrayList<Record> ();
	public final static List<Record> catalogList = new ArrayList<Record> ();
	public final static Map<String,Record> catalogMap = new LinkedHashMap<String,Record> ();
	public final static List<Record> menuList = new ArrayList<Record> ();
	public final static Map<String,List<Record>> menuMap = new LinkedHashMap<String,List<Record>> ();
	/**
	 * 系统参数
	 */
	public final static Map<String,String> t_xtcs = new LinkedHashMap<String,String> ();

	public boolean start() {
		reset();
		return true;
	}

	public boolean stop() {
		return true;
	}

	public static void reset(){
		//加载系统参数
		loadt_xtcsDb();
		//加载字典
		loadPropertyFile("dic_comm.properties");
		//加载数据字典
		loadJkZidianDb();
		//加载菜单信息
		loadMenuDb();
		loadb_storeDb();
		loadb_goodsDb();
		loadts_userDb();
		loadb_factoryDb();
		loadb_customerDb();
		loadb_deviceDb();
		loadb_areaDb();
		loadp_modelDb();
	}
	//ts_xtcs系统参数加载
	public static void loadt_xtcsDb(){
		t_xtcs.clear();
		List<Record> list = Db.find("select * from t_xtcs ");
		for(Record record:list){
			String code = DbUtil.readDbString(record.get("code"));
			String value = DbUtil.readDbString(record.get("value"));
			t_xtcs.put(code, value);
		}
	}
	public static Properties loadPropertyFile(String file) {
		properties.clear();
		if (StringKit.isBlank(file))
			throw new IllegalArgumentException("Parameter of file can not be blank");
		if (file.contains(".."))
			throw new IllegalArgumentException("Parameter of file can not contains \"..\"");

		InputStream inputStream = null;
		String fullFile;	// String fullFile = PathUtil.getWebRootPath() + file;
		if (file.startsWith(File.separator))
			fullFile = PathKit.getWebRootPath() + File.separator + "WEB-INF" + file;
		else
			fullFile = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + file;

		try {
			inputStream = new FileInputStream(new File(fullFile));
			BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
			properties.load(bf);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Properties file not found: " + fullFile);
		} catch (IOException e) {
			throw new IllegalArgumentException("Properties file can not be loading: " + fullFile);
		}
		finally {
			try {if (inputStream != null) inputStream.close();} catch (IOException e) {e.printStackTrace();}
		}
		if (properties == null)
			throw new RuntimeException("Properties file loading failed: " + fullFile);
		return properties;
	}
	//监控字典表
	public static void loadJkZidianDb(){
		jkzidian.clear();
		jkzidianList.clear();
		List<Record> list = Db.find("select t.* from ts_code t ");

		for(Record record:list){
			String aaa100 = record.getStr("TYPE");//类别
			String aaa102 = record.getStr("CODE");//编号
			String aaa103 = record.getStr("VALUE");//值
			if(jkzidian.containsKey(aaa100)){
				Map<String,String> map = jkzidian.get(aaa100);
				map.put(aaa102, aaa103);
			}else{
				Map<String,String> map = new HashMap<String,String>();
				map.put(aaa102, aaa103);
				jkzidian.put(aaa100, map);
			}
			if(jkzidianList.containsKey(aaa100)){
				List<Record> listRecord = jkzidianList.get(aaa100);
				listRecord.add(record);
			}else{
				List<Record> listRecord = new ArrayList<Record>();
				listRecord.add(record);
				jkzidianList.put(aaa100, listRecord);
			}

		}
	}
	//加载菜单信息
	public static void loadMenuDb(){
		ts_codeList.clear();
		catalogList.clear();
		catalogMap.clear();
		menuList.clear();
		menuMap.clear();
		List<Record> list = Db.find("select * from ts_menu where qybz = '1' order by path ");
		for(Record e:list){
			ts_codeList.add(e);
			String code = DbUtil.readDbString(e.get("code"));
			String cdlx = DbUtil.readDbString(e.get("cdlx"));
			String parent_id = DbUtil.readDbString(e.get("parent_id"));
			if("001".equals(cdlx)){
				catalogList.add(e);
				if(!"".equals(code))
					catalogMap.put(code, e);
			}
			if("002".equals(cdlx)){
				menuList.add(e);
				if(menuMap.get(parent_id)==null){
					List<Record> mlist = new ArrayList<Record>(); 
					mlist.add(e);
					menuMap.put(parent_id, mlist);
				}else{
					List<Record> mlist = menuMap.get(parent_id);
					mlist.add(e);
				}
			}

		}

	}
	//b_store字典加载
	public static void loadb_storeDb(){
		b_store.clear();
		b_store001002.clear();
		b_storeall.clear();
		b_store001.clear();
		b_store002.clear();
		b_store003.clear();
		List<Record> list = Db.find("select * from b_store order by cklx");
		for(Record record:list){
			String cklx = DbUtil.readDbString(record.get("cklx"));
			String ckbh = DbUtil.readDbString(record.get("ckbh"));
			String ckmc = DbUtil.readDbString(record.get("ckmc"));
			String yxbz = DbUtil.readDbString(record.get("yxbz"));
			String info = "["+jkzidian.get("CKLX").get(cklx)+"]["+ckmc+"]";
			if(!"".equals(ckbh)){
				b_storeall.put(ckbh, info);
				if("001".equals(yxbz)){
					b_store.put(ckbh, info);
					if("001".equals(cklx)){
						b_store001.put(ckbh, info);
						b_store001002.put(ckbh, info);
					}
					if("002".equals(cklx)){
						b_store002.put(ckbh, info);
						b_store001002.put(ckbh, info);
					}
					if("003".equals(cklx)){
						b_store003.put(ckbh, info);
					}
				}
			}
		}
	}
	//b_goods字典加载
	public static void loadb_goodsDb(){
		b_goods.clear();
		b_goodsall.clear();
		b_goodssimgle.clear();
		List<Record> list = Db.find("select * from b_goods  ");
		for(Record record:list){
			String d_type = DbUtil.readDbString(record.get("d_type"));
			String d_name = DbUtil.readDbString(record.get("d_name"));
			String sm = DbUtil.readDbString(record.get("sm"));
			String yxbz = DbUtil.readDbString(record.get("yxbz"));
			String info = "["+d_name+"]["+sm+"]";
			if(!"".equals(d_type)){
				b_goodsall.put(d_type, info);
				b_goodssimgle.put(d_type, d_name);
				if("001".equals(yxbz))
					b_goods.put(d_type, info);
			}
		}
	}
	//用户字典TS_USER
	public static void loadts_userDb(){
		ts_userall.clear();
		List<Record> list = Db.find("select * from ts_user  ");
		for(Record record:list){
			String username = DbUtil.readDbString(record.get("username"));
			String name = DbUtil.readDbString(record.get("name"));
			ts_userall.put(username, name);
		}
	}
	//厂家字典B_FACTORY
	public static void loadb_factoryDb(){
		b_factory.clear();
		b_factoryall.clear();
		b_factorysimgle.clear();
		List<Record> list = Db.find("select * from b_factory ");
		for(Record record:list){
			String id = DbUtil.readDbString(record.get("id"));
			String fsname = DbUtil.readDbString(record.get("fsname"));
			String lxr = DbUtil.readDbString(record.get("lxr"));
			String lxdh = DbUtil.readDbString(record.get("lxdh"));
			String yxbz = DbUtil.readDbString(record.get("yxbz"));
			String info = "["+fsname+"]["+lxr+"]["+lxdh+"]";
			b_factoryall.put(id, info);
			b_factorysimgle.put(id, fsname);
			if("001".equals(yxbz))
				b_factory.put(id, info);
		}
	}
	//客户字典B_CUSTOMER
	public static void loadb_customerDb(){
		b_customer.clear();
		b_customerall.clear();
		List<Record> list = Db.find("select * from b_customer ");
		for(Record record:list){
			String id = DbUtil.readDbString(record.get("id"));
			String fsname = DbUtil.readDbString(record.get("csname"));
			String lxr = DbUtil.readDbString(record.get("lxr"));
			String lxdh = DbUtil.readDbString(record.get("lxdh"));
			String yxbz = DbUtil.readDbString(record.get("yxbz"));
			String info = "["+fsname+"]["+lxr+"]["+lxdh+"]";
			b_customerall.put(id, info);
			if("001".equals(yxbz))
				b_customer.put(id, info);
		}
	}
	//设备字典b_device
	public static void loadb_deviceDb(){
		b_deviceall.clear();
		b_device.clear();
		b_device001.clear();
		b_device002.clear();
		b_device003.clear();
		b_devicesimgle.clear();
		b_deviceofb_factory.clear();
		b_deviceoflx.clear();
		List<Record> list = Db.find("select * from b_device order by factoryid ");
		for(Record record:list){
			String id = DbUtil.readDbString(record.get("id"));
			String factoryid = DbUtil.readDbString(record.get("factoryid"));
			String factoryname = "["+DbUtil.readDbString(b_factorysimgle.get(factoryid))+"]";
			String dname = "["+DbUtil.readDbString(record.get("dname"))+"]";
			String sbxh = "["+DbUtil.readDbString(record.get("sbxh"))+"]";
			String sbsm = "["+DbUtil.readDbString(record.get("sbsm"))+"]";
			String sblx = DbUtil.readDbString(record.get("sblx"));
			String sbzt = DbUtil.readDbString(record.get("sbzt"));
			String cpdm = "["+DbUtil.readDbString(record.get("jldw"))+"]";
			String info1 = cpdm+factoryname+"["+jkzidian.get("SBLX").get(sblx)+"]"+dname+sbxh+sbsm;
			String info2 = cpdm+factoryid+dname+sbxh+sbsm;
			b_deviceall.put(id, info1);
			b_devicesimgle.put(id, dname);
			b_deviceofb_factory.put(id, factoryid);
			sbzt = DbUtil.readDbString(record.get("sbzt"));
			if(!"003".equals(sbzt))b_device.put(id, info1);
			if("001".equals(sbzt))b_device001.put(id, info1);
			if("002".equals(sbzt))b_device002.put(id, info1);
			if("003".equals(sbzt))b_device003.put(id, info1);
			if(b_deviceoflx.get(sblx)==null){
				Map<String,String> map = new LinkedHashMap<String,String>();
				map.put(id, info2);
				b_deviceoflx.put(sblx, map);
			}else{
				Map<String,String> map = b_deviceoflx.get(sblx);
				map.put(id, info2);
			}
			String lxzt = sblx+sbzt;
			if(b_deviceoflx.get(lxzt)==null){
				Map<String,String> map = new LinkedHashMap<String,String>();
				map.put(id, info2);
				b_deviceoflx.put(lxzt, map);
			}else{
				Map<String,String> map = b_deviceoflx.get(lxzt);
				map.put(id, info2);
			}

		}
	}
	
	//设备字典b_device
		public static void loadp_modelDb(){
			p_model_all.clear();
			p_model.clear();

			List<Record> list = Db.find("select * from p_model ");
			for(Record record:list){
				String id = DbUtil.readDbString(record.get("id"));
				String modlename = DbUtil.readDbString(record.get("modlename"));
				String bz = DbUtil.readDbString(record.get("bz"));
				String yxbz = DbUtil.readDbString(record.get("yxbz"));
				p_model_all.put(id, modlename);
				
				if("001".equals(yxbz)){
					p_model.put(id, modlename);
				}

			}
		}
	
	//客户字典B_AREA
	public static void loadb_areaDb(){
		ts_area.clear();
		ts_areasimgle.clear();
		
		List<Record> list = Db.find("select * from ts_area ");
		String a0000 = "";
		String a00 = "";
		for(Record record:list){
			String code = DbUtil.readDbString(record.get("code"));
			String value = DbUtil.readDbString(record.get("value"));
			String qybz = DbUtil.readDbString(record.get("qybz"));
			if("001".equals(qybz)){
				if(code.lastIndexOf("0000")!=-1){
					a0000 = value;
					ts_area.put(code, value);
				}else if(code.substring(4).lastIndexOf("00")!=-1){
					a00 = value;
					ts_area.put(code, a0000+value);
				}else{
					ts_area.put(code, a0000+a00+value);
				}
			}
			ts_areasimgle.put(code, value);
		}
	}



}
