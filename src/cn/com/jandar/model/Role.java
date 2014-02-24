package cn.com.jandar.model;

import java.sql.Timestamp;
import java.util.List;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.kit.StringKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

@TableBind(tableName="ts_role")
public class Role extends Model<Role> {
    private static final long serialVersionUID = 4881112057562529542L;
    public static final Role dao = new Role();
    
    /**
     * 获得用户列表
     * @return
     */
    public static List<Role> getRoleList() {
        List<Role> roleList = Role.dao.find("select * from ts_role order by id");
        return roleList;
    }
    
    public static Role getRoleById(String id){
        return Role.dao.findById(id);
    }
    
    public static List getRoleByIds(String ids){
    	List<Role> roleList = Role.dao.find("select * from ts_role where id in ("+ids+")");
        return roleList;
    }
    
    public static String delete(String[] ids) {
        for (String id : ids) {
            if (!Role.dao.deleteById(id)) {
                return "error";
            }
        }
        return "success";
    } 
    
    public static Page<Role> getRolePage(int sPageNum, int sPageSize, String orderBy, String order, String search) {
        StringBuffer sqlBuffer = new StringBuffer("FROM ts_role ");
        if (!StringKit.isBlank(search)) {
            sqlBuffer.append("where ts_role.name like '%" + search + "%' ");
        }
        sqlBuffer.append(" order by ").append(orderBy).append(" ").append(order);
        Page<Role> rolePage = (Page<Role>) Role.dao.paginate(sPageNum, sPageSize, "SELECT * ",
                                                                         sqlBuffer.toString());
        return rolePage;
    }
    
    public static String save(Role role) {
        role.set("createDate", new Timestamp(System.currentTimeMillis()));
        role.set("modifyDate", new Timestamp(System.currentTimeMillis()));
        role.save();
        return "保存成功";
    }

    public static String update(Role role) {
        role.set("modifyDate", new Timestamp(System.currentTimeMillis()));
        role.update();
        return "更新成功";
    }

    public static String isExitByUserName(String rolename) {
        Role role = Role.dao.findFirst("select * from ts_role where ts_role.name = ?",rolename);
        if(role == null){
            return "true";
        }
        return "false";
    }
    
}