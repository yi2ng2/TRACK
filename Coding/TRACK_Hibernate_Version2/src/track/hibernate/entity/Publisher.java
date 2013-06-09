/*
 * Title: Distributed Database Query Engine Service (DDQES)
 * Description: The class serves as the class mapping for table publisher
 * Author: Ng Yi Ying
 * Data Created: 7 June, 2013
 * Data Modified: 7 June, 2013
 */
package track.hibernate.entity;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="publisher")
public class Publisher
{
	 @Id
	 private int pid;
	 private String pname;
	 private String pnation;
 
	 public String toString()
	 {
		 return "Publisher: pid=" + this.pid + ", pname=" + this.pname + ", pnation=" + this.pnation;
	 }
 
	// getters
	 public int getId() { return pid; }
	 public String getName() { return pname; }
	 public String getNation() { return pnation; }
 
	 // setters
	 public void setId(int pid) { this.pid = pid; }
	 public void setName(String pname) { this.pname = pname; }
	 public void setNation(String pnation) { this.pnation = pnation; }
 
}