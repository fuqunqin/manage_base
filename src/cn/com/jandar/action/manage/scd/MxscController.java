package cn.com.jandar.action.manage.scd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.jandar.action.admin.AdminBaseController;
import cn.com.jandar.interceptor.StartXtInterceptor;
import cn.com.jandar.kit.DbUtil;
import cn.com.jandar.model.Device;
import cn.com.jandar.model.User;
import cn.com.jandar.plugin.DicPlugin;

import com.jfinal.aop.Before;
import com.jfinal.kit.StringKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import cty.kit.route.ButtonBind;
import cty.kit.route.ControllerBind;


@Before(StartXtInterceptor.class)
@ControllerBind(controllerKey="/manage/scd/mxsc",resource="模型生成")
public class MxscController extends AdminBaseController{
	@ButtonBind(buttonname="查询")
	public void index(){
		
		String modelname=getPara("filter_LIKES_name");
	    
		StringBuffer sqlBuffer = new StringBuffer("FROM p_model ");
		String sql = "where (1=1 ";
		if (!StringKit.isBlank(modelname)) {
            sql+= " and p_model.MODLENAME like  '%" + modelname + "%'";
        }
		sql += ")";
		sqlBuffer.append(sql);
		sqlBuffer.append(" order by ").append(getPara("orderBy", "p_model.MODLENAME")).append(" ")
				.append(getPara("order", "desc"));
		
		Page<Record> page = Db.paginate(getParaToInt("pageNumber", 1), getParaToInt("pageSize", PAGESIZE), "SELECT * ", sqlBuffer.toString());
		
		for(Record record : page.getList()){
            if(checkModelInUse(record.getInt("id"))){ 
            	record.set("modelInUse", true);
            }else{
            	record.set("modelInUse", false);
            }			
		}
		setAttr("page", page);
		keepPara("filter_LIKES_name");
		render("model_list.html");
	}
	
	public boolean checkModelInUse(int modelId){
		boolean result = true;
		if(Db.query("select * from p_scd where ddzt != '010' and MODELID=?", modelId).size() >0){
			result = false;
		}
		return result;
	}
	
	@ButtonBind(buttonname="新增")
	public void add(){
		render("model_input.html");
	}
	
	@ButtonBind(buttonname="删除")
	public void delete(){
		String ids[] = getParaValues("ids");
		for(String id:ids){
			Db.deleteById("p_model", "ID", id);
			}
		//setAttr("msg","删除成功" );
		//setAttr("redirectionUrl", "/manage/scd/mxsc");
		//render("/admin/common/success.html");
		renderJson("status","删除成功");
	}
	@Before(Tx.class)
	@ButtonBind(buttonname="新增")
	public void save(){
		String username = ((User)getSessionAttr(User.LOGIN_USER)).get("username");

		Record record = new Record();
		record.set("MODLENAME", DbUtil.readDbString(getPara("name")));
		record.set("BZ", DbUtil.readDbString(getPara("bz")));
		record.set("YXBZ", DbUtil.readDbString(getPara("yxbz")));
		record.set("OPERATOR", username);
		record.set("OPDATE", new Date());

		Boolean boo = Db.save("p_model", record);
		if(boo){//插入模型成功后，添加模型细节
			    //获取新插入model_id
				Long modelid = record.getLong("ID");
			
				String[] model_DEVICEIDs = getParaValues("DEVICEID");
				String[] model_DNUMs = getParaValues("DNUM");
				if(null != getParaValues("DEVICEID")){

					for(int i = 0;i< model_DEVICEIDs.length; i++){
						
					List<Record> records = Db.find("select * from p_model_detail where MODELID=? and DEVICEID=?", modelid,model_DEVICEIDs[i]);
					if(records.size()==1){
						Record r = records.get(0);
						r.set("DNUM", records.get(0).getInt("DNUM")+Integer.parseInt(model_DEVICEIDs[i]));
						Db.update("p_model_detail", r);
						}else{
							//使用设备id获得厂商id
							Record device = Db.findById("b_device", model_DEVICEIDs[i]);
							int factoryid = device.get("factoryid");
							Record recordd = new Record();	
							recordd.set("MODELID", modelid);
							recordd.set("FACTORYID", factoryid);
							recordd.set("DEVICEID", model_DEVICEIDs[i]);
							recordd.set("DNUM", model_DNUMs[i]);
						/*	recordd.set("SM", model_SMs[i]);*/
							//recordd.set("OPERATOR", username);
							//recordd.set("OPDATE", date);
							
							Db.save("p_model_detail", recordd);
							}
					}
				}
			}
		DicPlugin.loadp_modelDb();
		setAttr("msg","保存成功" );
		setAttr("redirectionUrl", "/manage/scd/mxsc");
		render("/admin/common/success.html");
	}
	
	@ButtonBind(buttonname="更新")
	public void details(){
		int id = getParaToInt("id");
		Record model = Db.findById("p_model", id);
		List<Record> detaillist = Db.find("select * from p_model_detail where  p_model_detail.MODELID=?", id);
		setAttr("model", model);
		setAttr("detaillist", detaillist);
		render("model_detail.html");
	}
	
	@ButtonBind(buttonname="查看")
	public void query(){
		int id = getParaToInt("id");
		Record model = Db.findById("p_model", id);
		List<Record> detaillist = Db.find("select * from p_model_detail where  p_model_detail.MODELID=?", id);
		setAttr("model", model);
		setAttr("detaillist", detaillist);
		render("model_detail_seek.html");
	}
	@Before(Tx.class)
	@ButtonBind(buttonname="更新")
	public void update(){

		Long modelid = getParaToLong("modelid");
		String yxbz = getPara("yxbz").toString();
		String bz = getPara("bz","备注");
		String username = ((User)getSessionAttr(User.LOGIN_USER)).get("username");//更新操作员
		Date date = new Date();
		
		Record record = Db.findById("p_model", modelid);
		record.set("YXBZ", yxbz);
		record.set("BZ", bz);
		record.set("UPOPERATOR", username);
		record.set("UPIPDATE", date);
		Db.update("p_model", record);
		
		Db.update("delete from p_model_detail where p_model_detail.MODELID=?", modelid);
		if(null != getParaValues("DEVICEID")){
			String[] model_DEVICEIDs = getParaValues("DEVICEID");
			String[] model_DNUMs = getParaValues("DNUM");
				for(int i = 0;i< model_DEVICEIDs.length; i++){
					List<Record> records = Db.find("select * from p_model_detail where MODELID=? and DEVICEID=?", modelid,model_DEVICEIDs[i]);
					if(records.size()==1){
							Record r = records.get(0);
							r.set("DNUM", records.get(0).getInt("DNUM")+Integer.parseInt(model_DEVICEIDs[i]));
							Db.update("p_model_detail", r);
						}else{
							Record device = Db.findById("b_device", model_DEVICEIDs[i]);
							int factoryid = device.get("factoryid");
							Record recordd = new Record();
							recordd.set("MODELID", modelid);
							recordd.set("FACTORYID", factoryid);
							recordd.set("DEVICEID", model_DEVICEIDs[i]);
							recordd.set("DNUM", model_DNUMs[i]);
							Db.save("p_model_detail", recordd);
						}
					}				
			}
		DicPlugin.loadp_modelDb();
		setAttr("msg","更新成功" );
		setAttr("redirectionUrl", "/manage/scd/mxsc");
		render("/admin/common/success.html");
	}
}
