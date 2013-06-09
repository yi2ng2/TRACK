/*
 * Title: Distributed Database Query Engine Service (DDQES)
 * Description: The class serves as the class mapping for table customer
 * Author: Ng Yi Ying
 * Data Created: 7 June, 2013
 * Data Modified: 7 June, 2013
 */
package track.hibernate.entity;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="customer")
public class Customer
{
	 @Id
	 private int cid;
	 private String cname;
	 private int crank;
 
	 public String toString()
	 {
		 return "Customer: cid=" + this.cid + ", cname=" + this.cname + ", crank=" + this.crank;
	 }
 
	// getters
	 public int getId() { return cid; }
	 public String getName() { return cname; }
	 public int getRank() { return crank; }
 
	 // setters
	 public void setId(int cid) { this.cid = cid; }
	 public void setName(String cname) { this.cname = cname; }
	 public void setRank(int crank) { this.crank = crank; }
 
}