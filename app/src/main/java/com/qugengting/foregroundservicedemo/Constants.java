package com.qugengting.foregroundservicedemo;

public class Constants {

    public interface ACTION {

         String STARTFOREGROUND_ACTION = "com.action.startforeground";
         String STOPFOREGROUND_ACTION = "com.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
         int FOREGROUND_SERVICE = 101;
    }
}