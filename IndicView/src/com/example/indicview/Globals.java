package com.example.indicview;

public class Globals{
	   private static Globals instance=null;
	 
	   // Global variable
	   private boolean tocnt;
	 
	   // Restrict the constructor from being instantiated
	   private Globals(){}
	 
	   public void setData(boolean d){
	     this.tocnt=d;
	   }
	   public boolean getData(){
	     return this.tocnt;
	   }
	 
	   public static synchronized Globals getInstance(){
	     if(instance==null){
	       instance=new Globals();
	     }
	     return instance;
	   }
	};
