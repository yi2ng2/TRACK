package track.hibernate.entity;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="customer")
public class Customer
{
	 @Id
	 private int id;
	 private String name;
	 private int rank;
 
	 public String toString()
	 {
		 return "Customer: id=" + this.id + ", name=" + this.name + ", rank=" + this.rank;
	 }
 
	// getters
	 public int getId() { return id; }
	 public String getName() { return name; }
	 public int getRank() { return rank; }
 
	 // setters
	 public void setId(int id) { this.id = id; }
	 public void setName(String name) { this.name = name; }
	 public void setRank(int rank) { this.rank = rank; }
 
}