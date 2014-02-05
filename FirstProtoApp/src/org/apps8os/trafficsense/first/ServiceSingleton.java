package org.apps8os.trafficsense.first;

public class ServiceSingleton {
	   private static ServiceSingleton instance = null;
	   protected ServiceSingleton() {
	      // Exists only to defeat instantiation.
	   }
	   
	   private PebbleUiController pebbleUi;
	   private Route route;
	   
	   public static ServiceSingleton getInstance() {
	      if(instance == null) {
	         instance = new ServiceSingleton();
	      }
	      return instance;
	   }
	   
	   public void setPebbleUiController(PebbleUiController pebbleUi){
		   this.pebbleUi=pebbleUi;
	   }
	   
	   public void setRoute(Route route){
		   this.route=route;
	   }
	   
	   public PebbleUiController getPebbleUiController(){
		   return(pebbleUi);
	   }
	   
	   public Route getRoute(){
		   return route;
	   }
	}