package org.apps8os.trafficsense.first;

public class TrafficsenseContainer {
	   private static TrafficsenseContainer instance = null;
	   protected TrafficsenseContainer() {
	      // Exists only to defeat instantiation.
	   }
	   
	   private PebbleUiController pebbleUi;
	   private Route route;
	   
	   public static TrafficsenseContainer getInstance() {
	      if(instance == null) {
	         instance = new TrafficsenseContainer();
	      }
	      return instance;
	   }
	   
	   public void setPebbleUiController(PebbleUiController pebbleUi){
		   System.out.println("DBG setting the pebbleui to " + pebbleUi);
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