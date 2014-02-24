package cn.com.jandar.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author
 */
@TableBind(tableName = "p_scdsbmx")
public class Scdsbmx extends Model<Scdsbmx> {
	private static final long serialVersionUID = -2063970373324095312L;
	public static final Scdsbmx dao = new Scdsbmx();

	public static List<Record> query(List<Scdsb> newScdsbList) {
        List<Record> allRecord = new ArrayList<Record>();
        for(Scdsb scdsb : newScdsbList){
        	 List<Record> partRecord = Db.find("select * from p_scdsbmx where 1=1 and SCDSB =?",scdsb.getInt("id")); 
        	 allRecord.addAll(partRecord);
        }
		return allRecord;
	}
	
	public static void delete(List<Scdsb> scdsbList) {
		for(Scdsb scdsb : scdsbList){
			Db.update("delete from p_scdsbmx where SCDSB = '"
					+ scdsb.getInt("ID") + "'");
		}
	}

	public static void save(User user, List<Scdsb> scdsbList, int MODELID) {
          //得到组成模型的设备
  		  List<Record> detailList = Db.find("select * from p_model_detail where p_model_detail.MODELID=?", MODELID);
          for(Scdsb scdsb :scdsbList){
        	  for(Record record : detailList){
        		  Scdsbmx scdsbmx = new Scdsbmx();
        		  scdsbmx.set("DEVICEID", record.getInt("ID"));
        		  scdsbmx.set("SCDSB", scdsb.getInt("id")); 
        		  scdsbmx.set("DNUM", record.getInt("DNUM"));
        		  scdsbmx.set("SM", record.getStr("SM"));
        		  scdsbmx.set("FACTORYID", record.getInt("FACTORYID"));
        		  scdsbmx.set("SCSBZT", "001"); //默认正常
        		  
        		  scdsbmx.set("OPERATOR", user.getStr("username"));
        		  scdsbmx.set("OPDATE", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        		  
        		  scdsbmx.set("UPOPERATOR", user.getStr("username"));
        		  scdsbmx.set("UPIPDATE", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        		  
        		  scdsbmx.save();
        	  }
          }
	}
	
	public static List<Record> query(String id) {
        List<Record> partRecord = Db.find("SELECT a.MODELID,c.*,d.MODLENAME,d.BZ dbz FROM p_scd a,p_scdsb b, p_scdsbmx c ,p_model d  WHERE c.SCDSB = b.id AND b.SCDID = a.id AND a.MODELID = d.id  AND SCDSB = ?",id); 
		return partRecord;
	}

}
