/*
 * Title: Distributed Database Query Engine Service (DDQES)
 * Description: The class serves to handle query processing Hibernate connection
 * Author: Ng Yi Ying
 * Data Created: 7 June, 2013
 * Data Modified: 7 June, 2013
 */
package track.hibernate.util;
import java.io.File;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;


public class HibernateUtil {
	private static SessionFactory sessionFactory;
	private static ServiceRegistry serviceRegistry;
	
	static{
		try{
			//sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
			Configuration configuration = new Configuration();
		    configuration.configure("hibernate.cfg.xml");
		    serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();        
		    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		}catch (Exception e) {
			System.err.println("SessionFactory initialisation failed:" + e + " " + new File (".").getAbsolutePath());
			throw new RuntimeException(e);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void shutdown(){
		getSessionFactory().close();
	}
}
