/*
 * Title: Distributed Database Query Engine Service (DDQES)
 * Description: The class serves as the class mapping for table book
 * Author: Ng Yi Ying
 * Data Created: 7 June, 2013
 * Data Modified: 7 June, 2013
 */
package track.hibernate.entity;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="book")
public class Book
{
	 @Id
	 private int bid;
	 private String btitle;
	 private String bauthors;
	 private int bpid;
	 private int bcopies;
 
	 public String toString()
	 {
		 return "Book: cid=" + this.bid + ", btitle=" + this.btitle + ", bauthors=" + this.bauthors + ", bpid=" + this.bpid
				 + ", bcopies=" + this.bcopies;
	 }
 
	// getters
	 public int getId() { return bid; }
	 public String getTitle() { return btitle; }
	 public String getAuthors() { return bauthors; }
	 public int getPublisher() {  return bpid; }
	 public int getCopies() { return bcopies; }
 
	 // setters
	 public void setId(int bid) { this.bid = bid; }
	 public void setTitle(String btitle) { this.btitle = btitle; }
	 public void setAuthors(String bauthors) { this.bauthors = bauthors; }
	 public void setPublisher(int bpid) { this.bpid = bpid; }
	 public void setCopies(int bcopies) { this.bcopies = bcopies; }

}