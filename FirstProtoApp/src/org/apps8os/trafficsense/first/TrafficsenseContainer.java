package org.apps8os.trafficsense.first;

public class TrafficsenseContainer {
	   private static TrafficsenseContainer instance = null;
	   
	   private PebbleUiController mPebbleUi;
	   private Route mRoute;
	   
	   /*
	    * This is a singleton, only one instance allowed.
	    */
	   protected TrafficsenseContainer() { }
	   
	   public static TrafficsenseContainer getInstance() {
	      if(instance == null) {
	         instance = new TrafficsenseContainer();
	      }
	      return instance;
	   }
	   
	   public void setPebbleUiController(PebbleUiController pebbleUi) {
		   mPebbleUi = pebbleUi;
	   }
	   
	   public void setRoute(Route route) {
		   mRoute = route;
	   }
	   
	   public PebbleUiController getPebbleUiController() {
		   return mPebbleUi;
	   }
	   
	   public Route getRoute() {
		   return mRoute;
	   }
	}
